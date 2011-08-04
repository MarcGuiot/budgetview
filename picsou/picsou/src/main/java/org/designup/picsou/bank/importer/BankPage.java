package org.designup.picsou.bank.importer;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.RealAccount;
import org.designup.picsou.model.util.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.editors.GlobCheckBoxView;
import org.globsframework.gui.editors.GlobLinkComboEditor;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.layout.CardHandler;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.model.utils.LocalGlobRepository;
import org.globsframework.model.utils.LocalGlobRepositoryBuilder;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.attachment.AttachmentHandler;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;

public abstract class BankPage {
  protected Directory directory;
  private Integer bankId;
  protected LocalGlobRepository repository;
  protected PicsouDialog dialog;
  private CardHandler cardHandler;
  protected Repeat<Glob> repeat;
  protected GlobList accountsInPage = new GlobList();
  private CancelAction cancelAction;
  protected WebClient client;
  protected HtmlPage page;
  protected ErrorAlertHandler errorAlertHandler;
  protected boolean hasError = false;

  public BankPage(Directory directory, GlobRepository repository, Integer bankId) {
    this.directory = directory;
    this.bankId = bankId;
    this.repository = LocalGlobRepositoryBuilder.init(repository)
      .copy(Account.TYPE, RealAccount.TYPE)
      .get();
  }

  public void init() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/connection/synchroDialog.splits");
    cardHandler = builder.addCardHandler("card");
    initCardImport(directory, builder);
    builder.add("bankSpecific", getPanel());
    dialog = PicsouDialog.create(directory.get(JFrame.class), directory);
    JPanel panel = builder.load();
    cancelAction = new CancelAction();

    dialog.setPanelAndButton(panel, cancelAction);
  }

  abstract public JPanel getPanel();

  protected void showAccounts() {
    repeat.set(accountsInPage);
    cardHandler.show("cardComptes");
  }

  protected void initCardImport(final Directory directory, SplitsBuilder builder) {
    repeat = builder.addRepeat("listCompte", Collections.<Glob>emptyList(),
                               new RepeatComponentFactory<Glob>() {
                                 public void registerComponents(RepeatCellBuilder cellBuilder, Glob realAccount) {
                                   String name = realAccount.get(RealAccount.NUMBER);
                                   cellBuilder.add("accountName", new JLabel(name));
                                   cellBuilder.add("solde", new JLabel(realAccount.get(RealAccount.POSITION)));
                                   cellBuilder.add("typeName", new JLabel(realAccount.get(RealAccount.NAME)));
                                   GlobCheckBoxView importedCheckBox =
                                     GlobCheckBoxView.init(RealAccount.IMPORTED, repository, directory)
                                       .setName("import:" + name)
                                       .forceSelection(realAccount.getKey());
                                   cellBuilder.add("imported", importedCheckBox.getComponent());

                                   GlobCheckBoxView savingsCheckBox =
                                     GlobCheckBoxView.init(RealAccount.SAVINGS, repository, directory)
                                       .setName("savings:" + name)
                                       .forceSelection(realAccount.getKey());
                                   JCheckBox jCheckBox = savingsCheckBox.getComponent();
                                   Glob account = repository.findLinkTarget(realAccount, RealAccount.ACCOUNT);
                                   if (account != null) {
                                     repository.update(realAccount.getKey(), RealAccount.SAVINGS,
                                                       account.get(Account.ACCOUNT_TYPE).equals(AccountType.SAVINGS.getId()));
                                     jCheckBox.setEnabled(false);
                                   }
                                   cellBuilder.add("isSavings", jCheckBox);

                                   GlobLinkComboEditor linkComboEditor =
                                     GlobLinkComboEditor.init(RealAccount.ACCOUNT, repository, directory);
                                   linkComboEditor.setShowEmptyOption(true)
                                     .setEmptyOptionLabel(Lang.get("bank.qif.account.new"))
//                                     .setFilter(new GlobFieldMatcher(Account.ID, ))
                                     .forceSelection(realAccount.getKey());
                                   cellBuilder.add("exisingAccount", linkComboEditor.getComponent());
                                 }
                               });
    builder.add("doImport", new DoImportAction());
  }

  protected void createOrUpdateRealAccount(String name, String number, String position, final Integer bankId) {
    if (Strings.isNotEmpty(name) || Strings.isNotEmpty(number)) {
      Glob account = repository.getAll(RealAccount.TYPE,
                                       GlobMatchers.and(GlobMatchers.fieldEquals(RealAccount.NAME, name.trim()),
                                                        GlobMatchers.fieldEquals(RealAccount.NUMBER, number.trim()),
                                                        GlobMatchers.fieldEquals(RealAccount.BANK, bankId)))
        .getFirst();
      if (account == null) {
        account = repository.create(RealAccount.TYPE,
                                    FieldValue.value(RealAccount.NAME, name.trim()),
                                    FieldValue.value(RealAccount.NUMBER, number.trim()),
                                    FieldValue.value(RealAccount.BANK, bankId),
                                    FieldValue.value(RealAccount.POSITION, position.trim()));
      }
      else {
        repository.update(account.getKey(), RealAccount.POSITION, position.trim());
      }
      accountsInPage.add(account);
    }
  }

  protected File createQifLocalFile(Glob realAccount, InputStream contentAsStream) {
    File file;
    try {
      file = File.createTempFile("download", ".qif");
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      fileOutputStream.write(("! accountId=" + realAccount.get(RealAccount.ACCOUNT) + "\n").getBytes());
      Files.copyStream(contentAsStream, fileOutputStream);
      file.deleteOnExit();
    }
    catch (IOException e) {
      Log.write("Can not create temporary file.");
      return null;
    }
    return file;
  }

  public abstract List<File> loadFile();

  protected void loadPage(final String index) throws IOException {
    client = new WebClient();
    client.setCssEnabled(false);
    client.setJavaScriptEnabled(true);
    client.setCache(new Cache());
    client.setAjaxController(new NicelyResynchronizingAjaxController());
    page = (HtmlPage)client.getPage(index);
    errorAlertHandler = new ErrorAlertHandler();
    client.setAlertHandler(errorAlertHandler);
  }

  protected <T extends HtmlElement> T getElementById(final String id) {
    T select = (T)page.getElementById(id);
    if (select == null) {
      throw new RuntimeException("Can not find tag '" + id + "' in :\n" + page.asXml());
    }
    return select;
  }

  protected HtmlAnchor findLink(List<HtmlAnchor> anchors, String ref) {
    for (HtmlAnchor anchor : anchors) {
      if (anchor.getHrefAttribute().contains(ref)) {
        return anchor;
      }
    }
    throw new RuntimeException("Can not find ref '" + ref + "' in :\n" + page.asXml());
  }

  protected File downloadFile(Glob realAccount, HtmlElement anchor) {
    DownloadAttachmentHandler downloadAttachmentHandler = new DownloadAttachmentHandler();
    client.setAttachmentHandler(downloadAttachmentHandler);
    try {
      Page page1 = anchor.click();
      TextPage page = (TextPage)page1;
      System.out.println("BankPage.downloadFile" + page.getContent());
    }
    catch (IOException e) {
      Log.write("In anchor click", e);
      return null;
    }
    synchronized (downloadAttachmentHandler) {
      if (downloadAttachmentHandler.page == null) {
        try {
          downloadAttachmentHandler.wait(3000);
        }
        catch (InterruptedException e1) {
        }
      }
    }
    if (downloadAttachmentHandler.page != null) {
      InputStream contentAsStream = downloadAttachmentHandler.page.getWebResponse().getContentAsStream();
      return createQifLocalFile(realAccount, contentAsStream);
    }
    else {
      Log.write("No download");
    }
    return null;
  }


  protected class DoImportAction extends AbstractAction {
    protected DoImportAction() {
      super("Import");
    }

    public void actionPerformed(ActionEvent e) {
      repository.getCurrentChanges()
        .safeVisit(RealAccount.TYPE, new RealAccountChangeSetVisitor());
      List<File> files = loadFile();
      dialog.setVisible(false);
      repository.commitChanges(true);
      directory.get(OpenRequestManager.class).openFiles(files);
    }

    private class RealAccountChangeSetVisitor implements ChangeSetVisitor {
      public void visitCreation(Key key, FieldValues values) throws Exception {
        if (values.get(RealAccount.IMPORTED)) {
          updateOrCreateAccount(key, values);
        }
      }

      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
        if (values.contains(RealAccount.IMPORTED) && values.get(RealAccount.IMPORTED)) {
          updateOrCreateAccount(key, repository.get(key));
        }
        else if (values.contains(RealAccount.POSITION)) {
          Glob realAccount = repository.get(key);
          if (realAccount.get(RealAccount.IMPORTED)) {
            updateOrCreateAccount(key, realAccount);
          }
        }
      }

      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
      }

      private void updateOrCreateAccount(Key key, FieldValues values) {
        Integer accountId = values.get(RealAccount.ACCOUNT);
        if (accountId == null) {
          Glob account = createAccount(values);
          repository.update(key, RealAccount.ACCOUNT, account.get(Account.ID));
        }
        else {
          Glob glob = repository.find(Key.create(Account.TYPE, accountId));
          if (glob == null) {
            createAccount(values);
          }
          else {
            repository.update(glob.getKey(), FieldValue.value(Account.POSITION, read(values)),
                              FieldValue.value(Account.DIRECT_SYNCHRO, Boolean.TRUE));
          }
        }
      }

      private Double read(FieldValues values) {
        return extractAmount(values.get(RealAccount.POSITION));
      }

      private Glob createAccount(FieldValues values) {
        return repository.create(Account.TYPE,
                                 FieldValue.value(Account.BANK, bankId),
                                 FieldValue.value(Account.ACCOUNT_TYPE,
                                                  values.get(RealAccount.SAVINGS)
                                                  ? AccountType.SAVINGS.getId() : AccountType.MAIN.getId()),
                                 FieldValue.value(Account.NUMBER, values.get(RealAccount.NUMBER)),
                                 FieldValue.value(Account.NAME, values.get(RealAccount.NAME)),
                                 FieldValue.value(Account.POSITION, read(values)),
                                 FieldValue.value(Account.DIRECT_SYNCHRO, Boolean.TRUE),
                                 FieldValue.value(Account.IS_VALIDATED, true));
      }
    }
  }

  protected Double extractAmount(String position){
    return Amounts.extractAmount(position);
  }

  protected class CancelAction extends AbstractAction {
    protected CancelAction() {
      super(Lang.get("cancel"));
    }

    public void actionPerformed(ActionEvent e) {
      dialog.setVisible(false);
    }
  }

  public void show() {
    dialog.pack();
    dialog.showCentered();
  }

  private class ErrorAlertHandler implements AlertHandler {
    public void handleAlert(Page page, String s) {
      hasError = true;
      MessageDialog.show("bank.error", dialog, directory, "bank.error.msg", s);
    }
  }

  private class DownloadAttachmentHandler implements AttachmentHandler {
    private Page page;

    public void handleAttachment(Page page) {
      synchronized (this) {
        this.page = page;
        notifyAll();
      }
    }
  }
}
