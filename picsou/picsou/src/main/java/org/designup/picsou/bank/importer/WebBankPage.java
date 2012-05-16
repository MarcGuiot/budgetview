package org.designup.picsou.bank.importer;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.attachment.AttachmentHandler;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.designup.picsou.gui.components.dialogs.MessageDialog;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.stream.ReplacementInputStreamBuilder;

import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public abstract class WebBankPage extends BankPage {
  protected WebClient client;
  protected HtmlPage page;
  protected ErrorAlertHandler errorAlertHandler;
  protected boolean hasError = false;

  public WebBankPage(Window parent, Directory directory, GlobRepository repository, Integer bankId) {
    super(parent, directory, repository, bankId);
    this.bankId = bankId;
  }

  public static BufferedImage getFirstImage(HtmlImage img) {
    try {
      final ImageReader imageReader = img.getImageReader();
      return imageReader.read(0);
    }
    catch (IOException e) {
      throw new RuntimeException("Can not load image " + img.getId());
    }
  }

  protected void loadPage(final String index) throws IOException {
    client = new WebClient();
    client.setThrowExceptionOnScriptError(false);
    client.setCssEnabled(false);
    client.setJavaScriptEnabled(true);
    client.setCache(new Cache());
    client.setWebConnection(getHttpConnection());
    client.setAjaxController(new NicelyResynchronizingAjaxController());
    page = (HtmlPage)client.getPage(index);
    errorAlertHandler = new ErrorAlertHandler();
    client.setAlertHandler(errorAlertHandler);
  }

  protected HttpWebConnection getHttpConnection() {
    return new HttpWebConnection(client);
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
      anchor.click();
//      InputStream contentAsStream = page1.getWebResponse().getContentAsStream();
//      return BankPage.createQifLocalFile(realAccount, contentAsStream);
//      System.out.println("BankPage.downloadFile" + page1.getContent());
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
      WebResponse response = downloadAttachmentHandler.page.getWebResponse();
      InputStream contentAsStream = response.getContentAsStream();
      return BankPage.createQifLocalFile(realAccount, contentAsStream, response.getContentCharset());
    }
    else {
      Log.write("No download");
    }
    return null;
  }

//  protected class DoImportAction extends AbstractAction {
//    protected DoImportAction() {
//      super("Import");
//    }
//
//    public void actionPerformed(ActionEvent e) {
//      repository.getCurrentChanges()
//        .safeVisit(RealAccount.TYPE, new RealAccountChangeSetVisitor());
//      List<File> files = loadFile();
//      dialog.setVisible(false);
//      repository.commitChanges(true);
//      directory.get(OpenRequestManager.class).openFiles(files);
//    }
//
//    private class RealAccountChangeSetVisitor implements ChangeSetVisitor {
//      public void visitCreation(Key key, FieldValues values) throws Exception {
//        if (values.get(RealAccount.IMPORTED)) {
//          updateOrCreateAccount(key, values);
//        }
//      }
//
//      public void visitUpdate(Key key, FieldValuesWithPrevious values) throws Exception {
//        if (values.contains(RealAccount.IMPORTED) && values.get(RealAccount.IMPORTED)) {
//          updateOrCreateAccount(key, repository.get(key));
//        }
//        else if (values.contains(RealAccount.POSITION)) {
//          Glob realAccount = repository.get(key);
//          if (realAccount.get(RealAccount.IMPORTED)) {
//            updateOrCreateAccount(key, realAccount);
//          }
//        }
//      }
//
//      public void visitDeletion(Key key, FieldValues previousValues) throws Exception {
//      }
//
//      private void updateOrCreateAccount(Key key, FieldValues values) {
//        Integer accountId = values.get(RealAccount.ACCOUNT);
//        if (accountId == null) {
//          Glob account = createAccount(values);
//          repository.update(key, RealAccount.ACCOUNT, account.get(Account.ID));
//        }
//        else {
//          Glob glob = repository.find(Key.create(Account.TYPE, accountId));
//          if (glob == null) {
//            createAccount(values);
//          }
//          else {
//            repository.update(glob.getKey(), FieldValue.value(Account.POSITION, read(values)),
//                              FieldValue.value(Account.DIRECT_SYNCHRO, Boolean.TRUE));
//          }
//        }
//      }
//
//      private Double read(FieldValues values) {
//        return extractAmount(values.get(RealAccount.POSITION));
//      }
//
//      private Glob createAccount(FieldValues values) {
//        return repository.create(Account.TYPE,
//                                 FieldValue.value(Account.BANK, bankId),
//                                 FieldValue.value(Account.ACCOUNT_TYPE,
//                                                  values.get(RealAccount.SAVINGS)
//                                                  ? AccountType.SAVINGS.getId() : AccountType.MAIN.getId()),
//                                 FieldValue.value(Account.NUMBER, values.get(RealAccount.NUMBER)),
//                                 FieldValue.value(Account.NAME, values.get(RealAccount.NAME)),
//                                 FieldValue.value(Account.POSITION, read(values)),
//                                 FieldValue.value(Account.DIRECT_SYNCHRO, Boolean.TRUE),
//                                 FieldValue.value(Account.IS_VALIDATED, true));
//      }
//    }
//  }

//  protected Double extractAmount(String position){
//    return Amounts.extractAmount(position);
//  }

//  protected class CancelAction extends AbstractAction {
//    protected CancelAction() {
//      super(Lang.get("cancel"));
//    }
//
//    public void actionPerformed(ActionEvent e) {
//      dialog.setVisible(false);
//    }
//  }

//  public void show() {
//    dialog.pack();
//    dialog.showCentered();
//  }

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
