package org.designup.picsou.bank.connectors.cic;

import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.connectors.WebBankConnector;
import org.designup.picsou.bank.connectors.webcomponents.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.UserAndPasswordPanel;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebConnectorLauncher;
import org.designup.picsou.model.RealAccount;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class CicConnector extends WebBankConnector {
  public static final Integer BANK_ID = 2;

  private static final String INDEX = "https://www.cic.fr/cic/fr/banques/particuliers/index.html";
  private static final String DOWNLOAD_PAGE_ADDRESS = "https://www.cic.fr/cic/fr/banque/telechargement.cgi";

  private UserAndPasswordPanel userAndPasswordPanel;

  public static void main(String[] args) throws IOException {
    WebConnectorLauncher.show(BANK_ID, new Factory());
  }

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory, boolean syncExistingAccount, Glob synchro) {
      return new CicConnector(syncExistingAccount, repository, directory, synchro);
    }
  }

  private CicConnector(boolean syncExistingAccount, GlobRepository repository, Directory directory, Glob synchro) {
    super(BANK_ID, syncExistingAccount, repository, directory, synchro);
    browser.setJavascriptEnabled(false);
  }

  protected JPanel createPanel() {
    userAndPasswordPanel = new UserAndPasswordPanel(new ConnectAction(), directory);
    JPanel panel = userAndPasswordPanel.getPanel();
    userAndPasswordPanel.setUserCode(getSyncCode());
    ExecutorService executorService = directory.get(ExecutorService.class);
    executorService.submit(new Runnable() {
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
        catch (Exception e) {
          notifyErrorFound(e);
        }
      }
    });
    return panel;
  }

  public void panelShown() {
    userAndPasswordPanel.requestFocus();
  }

  public void reset() {
  }

  private class ConnectAction extends AbstractAction {

    public void actionPerformed(ActionEvent event) {
      notifyIdentificationInProgress();
      userAndPasswordPanel.setEnabled(false);
      userAndPasswordPanel.setFieldsEnabled(false);
      Thread thread = new Thread(new Runnable() {
        public void run() {
          try {
            WebPage homePage = browser.getCurrentPage();
            WebForm idForm = homePage.getFormByName("ident");
            idForm.getTextInputById("e_identifiant").setText(userAndPasswordPanel.getUser());
            idForm.getPasswordInputById("e_mdp").setText(userAndPasswordPanel.getPassword());
            WebPage loggedInPage = idForm.submit();
            if (!loggedInPage.getUrl().contains("www.cic.fr/cic/fr/banque/espace_personnel")) {
              userAndPasswordPanel.requestFocus();
              userAndPasswordPanel.setFieldsEnabled(true);
              userAndPasswordPanel.setEnabled(true);
              notifyIdentificationFailed();
              return;
            }

            WebPage downloadPage = browser.load(DOWNLOAD_PAGE_ADDRESS);
            WebForm downloadForm = downloadPage.getFormByName("CMFormTelechargement");
            WebTable accountTable = downloadForm.getTableWithNamedInput("compte");
            WebTableColumn column = accountTable.getColumn(1);
            for (WebTableCell cell : column) {
              AccountLabel accountLabels = new AccountLabel(cell.asText());
              createOrUpdateRealAccount(accountLabels.accountName, accountLabels.accountNumber, null, null, BANK_ID);
            }
            doImport();
          }
          catch (final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                notifyErrorFound(e);
              }
            });
          }
        }
      });
      thread.start();
    }
  }

  public void downloadFile() throws Exception {
    WebPage downloadPage = browser.getCurrentPage();
    WebForm downloadForm = downloadPage.getFormByName("CMFormTelechargement");

    downloadForm.getRadioButtonByValue("OFX").select();

    WebTable accountTable = downloadForm.getTableWithNamedInput("compte");
    WebTableColumn column = accountTable.getColumn(0).removeLastCells(1);
    for (WebTableCell cell : column) {
      String siteAccountName = cell.getEnclosingRow().getCell(1).asText();
      boolean toImport = isToImport(siteAccountName);
      cell.getCheckBox().setChecked(toImport);
    }

    notifyDownloadInProgress();
    Download download = downloadForm.submitByNameAndDownload("submit");
    File file = download.saveAsOfx();
    for (WebTableCell cell : accountTable.getColumn(1)) {
      for (Glob realAccount : accounts) {
        if (cell.asText().trim().contains(realAccount.get(RealAccount.NAME))) {
          repository.update(realAccount.getKey(), RealAccount.FILE_NAME, file.getAbsolutePath());
        }
      }
    }
  }

  public String getCode() {
    return userAndPasswordPanel.getUser();
  }

  private boolean isToImport(String siteAccountName) {
    boolean toImport = false;
    for (Glob account : accounts) {
      if (siteAccountName.contains(account.get(RealAccount.NAME))) {
        toImport = true;
      }
    }
    return toImport;
  }

  private class AccountLabel {

    public String accountNumber;
    public String accountName;

    private AccountLabel(String label) {
      StringBuilder numberBuilder = new StringBuilder();
      for (int i = 0; i < label.length(); i++) {
        if (Character.isDigit(label.charAt(i))) {
          numberBuilder.append(label.charAt(i));
        }
        else if (!Character.isSpaceChar(label.charAt(i))) {
          accountName = label.substring(i, label.length());
          break;
        }
      }
      accountNumber = numberBuilder.toString();
    }

    public String toString() {
      return accountNumber + " / " + accountName;
    }
  }
}
