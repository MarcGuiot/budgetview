package org.designup.picsou.bank.connectors.cic;

import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.connectors.WebBankConnector;
import org.designup.picsou.bank.connectors.webcomponents.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.Download;
import org.designup.picsou.bank.connectors.webcomponents.utils.UserAndPasswordPanel;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebConnectorLauncher;
import org.designup.picsou.model.RealAccount;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

public class CicConnector extends WebBankConnector {
  public static final Integer BANK_ID = 2;

  private static final String INDEX = "https://www.cic.fr/cic/fr/banques/particuliers/index.html";
  private static final String DOWNLOAD_PAGE_ADDRESS = "https://www.cic.fr/cic/fr/banque/telechargement.cgi";
  private UserAndPasswordPanel userAndPasswordPanel;

  public static void main(String[] args) throws IOException {
    WebConnectorLauncher.show(new Factory());
  }

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory) {
      return new CicConnector(repository, directory);
    }
  }

  private CicConnector(GlobRepository repository, Directory directory) {
    super(BANK_ID, repository, directory);
    browser.setJavascriptEnabled(false);
  }

  public JPanel getPanel() {

    userAndPasswordPanel = new UserAndPasswordPanel(new ConnectAction(), directory);

    Thread thread = new Thread() {
      public void run() {
        try {
          notifyInitialConnection();
          loadPage(INDEX);
          notifyWaitingForUser();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              userAndPasswordPanel.setEnabled(true);
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
        notifyIdentification();
        userAndPasswordPanel.setEnabled(false);
        userAndPasswordPanel.setFieldsEnabled(false);
        Thread thread = new Thread(new Runnable() {
          public void run() {
            try {
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
            catch (final Exception e) {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  notifyErrorFound(e.getMessage());
                }
              });
            }
          }
        });
        thread.start();
    }
  }

  public void downloadFile() {
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

    notifyDownloadInProgress();
    Download download = downloadForm.submitByNameAndDownload("submit");
    File file = download.saveAsOfx();
    for (WebTableCell cell : accountTable.getColumn(1)) {
      for (Glob glob : accounts) {
        if (cell.asText().trim().contains(glob.get(RealAccount.NAME))) {
          repository.update(glob.getKey(), RealAccount.FILE_NAME, file.getAbsolutePath());
        }
      }
    }
  }
}
