package org.designup.picsou.bank.importer;

import org.designup.picsou.gui.components.dialogs.PicsouDialog;
import org.designup.picsou.gui.startup.OpenRequestManager;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.designup.picsou.model.RealAccount;
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

public abstract class BankPage {
  protected Directory directory;
  private Integer bankId;
  protected LocalGlobRepository repository;
  protected PicsouDialog dialog;
  private CardHandler cardHandler;
  protected Repeat<Glob> repeat;
  protected GlobList accountsInPage = new GlobList();
  private CancelAction cancelAction;

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
                                   String name = realAccount.get(RealAccount.NAME);
                                   cellBuilder.add("accountName", new JLabel(name));
                                   cellBuilder.add("solde", new JLabel(realAccount.get(RealAccount.POSITION)));
                                   cellBuilder.add("typeName", new JLabel(realAccount.get(RealAccount.TYPE_NAME)));
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

  protected void createOrUpdateRealAccount(String type, String name, String position, final Integer bankId) {
    if (Strings.isNotEmpty(type) && Strings.isNotEmpty(name)) {
      Glob account = repository.getAll(RealAccount.TYPE,
                                       GlobMatchers.and(GlobMatchers.fieldEquals(RealAccount.TYPE_NAME, type),
                                                        GlobMatchers.fieldEquals(RealAccount.NAME, name),
                                                        GlobMatchers.fieldEquals(RealAccount.BANK, bankId)))
        .getFirst();
      if (account == null) {
        account = repository.create(RealAccount.TYPE,
                                    FieldValue.value(RealAccount.TYPE_NAME, type),
                                    FieldValue.value(RealAccount.NAME, name),
                                    FieldValue.value(RealAccount.BANK, bankId),
                                    FieldValue.value(RealAccount.POSITION, position));
      }
      else {
        repository.update(account.getKey(), RealAccount.POSITION, position);
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
                                 FieldValue.value(Account.NUMBER, values.get(RealAccount.NAME)),
                                 FieldValue.value(Account.NAME, values.get(RealAccount.TYPE_NAME)),
                                 FieldValue.value(Account.POSITION, read(values)),
                                 FieldValue.value(Account.DIRECT_SYNCHRO, Boolean.TRUE),
                                 FieldValue.value(Account.IS_VALIDATED, true));
      }
    }
  }

  protected abstract Double extractAmount(String position);

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

}
