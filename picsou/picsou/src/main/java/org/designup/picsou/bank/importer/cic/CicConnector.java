package org.designup.picsou.bank.importer.cic;

import org.designup.picsou.bank.BankConnectorDisplay;
import org.designup.picsou.bank.importer.WebBankPage;
import org.designup.picsou.bank.importer.webcomponents.*;
import org.designup.picsou.bank.importer.webcomponents.utils.Download;
import org.designup.picsou.bank.importer.webcomponents.utils.UserAndPasswordPanel;
import org.designup.picsou.bank.importer.webcomponents.utils.WebCommandFailed;
import org.designup.picsou.bank.importer.webcomponents.utils.WebConnectorLauncher;
import org.designup.picsou.model.RealAccount;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class CicConnector extends WebBankPage {
  public static final Integer BANK_ID = 2;

  private static final String INDEX = "https://www.cic.fr/cic/fr/banques/particuliers/index.html";
  private static final String DOWNLOAD_PAGE_ADDRESS = "https://www.cic.fr/cic/fr/banque/telechargement.cgi";
  private UserAndPasswordPanel userAndPasswordPanel;

  public static void main(String[] args) throws IOException {
    WebConnectorLauncher.show(new Factory());
  }

  public static class Factory implements BankConnectorDisplay {

    public GlobList show(Window parent, Directory directory, GlobRepository repository) {
      CicConnector connector = new CicConnector(parent, directory, repository);
      connector.init();
      return connector.show();
    }
  }

  public CicConnector(Window parent, Directory directory, GlobRepository repository) {
    super(parent, directory, repository, BANK_ID);
  }

  public JPanel getPanel() {

    userAndPasswordPanel = new UserAndPasswordPanel(new ConnectAction(), directory);

    Thread thread = new Thread() {
      public void run() {
        try {
          loadPage(INDEX);
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              userAndPasswordPanel.setEnabled();
            }
          });
        }
        catch (IOException e) {
          e.printStackTrace();
        }
      }
    };
    thread.start();

    return userAndPasswordPanel.getPanel();
  }

  private class ConnectAction implements ActionListener {

    public void actionPerformed(ActionEvent event) {
      try {
        startProgress();
        WebPage homePage = browser.getCurrentPage();
        WebForm idForm = homePage.getFormByName("ident");
        idForm.getTextFieldById("e_identifiant").setText(userAndPasswordPanel.getUser());
        idForm.getTextFieldById("e_mdp").setText(userAndPasswordPanel.getPassword());
        idForm.submit();

        WebPage downloadPage = browser.load(DOWNLOAD_PAGE_ADDRESS);
        WebForm downloadForm = downloadPage.getFormByName("CMFormTelechargement");
        WebTable accountTable = downloadForm.getTableWithNamedInput("compte");
        WebTableColumn column = accountTable.getColumn(1);
        for (WebTableCell cell : column) {
          createOrUpdateRealAccount(cell.asText(), "", null, null, BANK_ID);
        }
        doImport();
      }
      catch (Exception e) {
        throw new WebCommandFailed(e);
      }
      finally {
        endProgress();
      }
    }
  }

  public void loadFile() {
    WebPage downloadPage = browser.getCurrentPage();
    WebForm downloadForm = downloadPage.getFormByName("CMFormTelechargement");

    downloadForm.getRadioButtonByValue("OFX").select();

    WebTable accountTable = downloadForm.getTableWithNamedInput("compte");
    WebTableColumn column = accountTable.getColumn(0).removeLastCells(1);
    for (WebTableCell cell : column) {
      cell.getCheckBox().setChecked(true);
    }

//    for (Glob glob : this.accounts) {
//      int count = accountsTable.getRowCount();
//      for (int i = 1; i < count; i++) {
//        if (accountsTable.getCellAt(i, 1).getTextContent().contains(glob.get(RealAccount.NAME))) {
//          try {
//            java.util.List<HtmlElement> elementList = accountsTable.getCellAt(i, 0).getHtmlElementsByTagName(HtmlInput.TAG_NAME);
//            if (elementList.size() == 1) {
//              elementList.get(0).click();
//            }
//          }
//          catch (IOException e) {
//            throw new RuntimeException(e);
//          }
//        }
//      }
//    }

    Download download = downloadForm.submitByNameAndDownload("submit");
    File file = download.saveAsOfx();
    for (WebTableCell cell : accountTable.getColumn(1)) {
      for (Glob glob : accounts) {
        if (cell.asText().trim().contains(glob.get(RealAccount.NAME))) {
          repository.update(glob.getKey(), RealAccount.FILE_NAME, file.getAbsolutePath());
        }
      }
    }

    System.out.println("CicConnector.loadFile: ");
    GlobPrinter.print(repository, RealAccount.TYPE);
  }
}
