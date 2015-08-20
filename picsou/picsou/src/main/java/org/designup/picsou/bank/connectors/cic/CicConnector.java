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
import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class CicConnector extends WebBankConnector {
  public static final Integer BANK_ID = 2;

  private static final String INDEX = "https://www.cic.fr/";
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
    JPanel panel = userAndPasswordPanel.createPanel(this);
    userAndPasswordPanel.setUserCode(getSyncCode());
    loadHomePage();
    return panel;
  }

  private void loadHomePage() {
    userAndPasswordPanel.setFieldsEnabled(true);
    userAndPasswordPanel.setEnabled(false);
    ExecutorService executorService = directory.get(ExecutorService.class);
    executorService.submit(new Runnable() {
      public void run() {
        try {
          notifyInitialConnection();
          loadPage(INDEX);
          userAndPasswordPanel.setEnabled(true);
          notifyWaitingForUser();
          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              userAndPasswordPanel.requestFocus();
            }
          });
        }
        catch (Exception e) {
          notifyErrorFound(e);
        }
      }
    });
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
      directory.get(ExecutorService.class).submit(new Runnable() {
        public void run() {

          WebPage homePage = browser.getCurrentPage();
          if (isLoggedIn(homePage)) {
            try {
              homePage = browser.load(INDEX + "/cic/fr/identification/deconnexion/deconnexion.cgi");
            }
            catch (Exception e) {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  notifyErrorFound("Impossible de se déconnecter pour initialiser l'identification. " +
                                   "Nous vous recommandons de vous rendre sur le site du CIC avec votre navigateur " +
                                   "internet pour vérifier que vous arrivez bien à vous y connecter, " +
                                   "puis d'essayer à nouveau à partir de BudgetView.");
                }
              });
              return;
            }
          }

          WebForm idForm = null;
          try {
            idForm = browser.retry(homePage, new WebBrowser.Function1Arg<WebForm, WebPage>() {
              public WebForm call(WebPage webPage) throws Exception {
                return webPage.getFormByName("ident");
              }
            });
          }
          catch (final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                notifyErrorFound("L'identification n'est pas possible pour le moment. " +
                                 "Nous vous recommandons de vous rendre sur le site du CIC avec votre navigateur " +
                                 "internet pour vérifier que vous arrivez bien à vous y connecter, " +
                                 "puis d'essayer à nouveau à partir de BudgetView.");
              }
            });
          }

          if (idForm == null) {
            return;
          }

          try {
            idForm.getTextInputById("e_identifiant").setText(userAndPasswordPanel.getUser());
            idForm.getPasswordInputById("e_mdp").setText(userAndPasswordPanel.getPassword());
            WebPage loggedInPage = idForm.submit();
            if (!isLoggedIn(loggedInPage)) {
              notifyIdentificationFailed(browser);
              loadHomePage();
              return;
            }

            WebPage downloadPage = browser.load(DOWNLOAD_PAGE_ADDRESS);
            WebForm downloadForm = downloadPage.getFormById("P:F");
            WebTable accountTable = downloadForm.getTableById("account-table");
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
    }
  }

  private boolean isLoggedIn(WebPage loggedInPage) {
    return loggedInPage.containsAnchorWithHRef("/cic/fr/identification/deconnexion/deconnexion.cgi");
  }

  public void downloadFile() throws Exception {
    WebPage downloadPage = browser.getCurrentPage();
    WebForm downloadForm = downloadPage.getFormById("P:F");

    downloadForm.getRadioButtonById("ofx:DataEntry").select();

    WebTable accountTable = downloadForm.getTableById("account-table");
    for (WebTableRow row : accountTable.getAllRows()) {
      String siteAccountName = row.getCell(1).asText();
      boolean toImport = isToImport(siteAccountName);
      row.getCell(0).getCheckBox().setChecked(toImport);
    }

    notifyDownloadInProgress();
    Download download = downloadForm.submitByNameAndDownload("_FID_DoDownload");
    String fileContent = download.readAsOfx();
    for (WebTableCell cell : accountTable.getColumn(1)) {
      for (Glob realAccount : accounts) {
        if (cell.asText().trim().contains(realAccount.get(RealAccount.NAME))) {
          repository.update(realAccount.getKey(), RealAccount.FILE_CONTENT, fileContent);
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
