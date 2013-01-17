package org.designup.picsou.bank.connectors.labanquepostale;

import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.connectors.WebBankConnector;
import org.designup.picsou.bank.connectors.webcomponents.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebConnectorLauncher;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;
import org.designup.picsou.exporter.ofx.OfxExporter;
import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.model.*;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.globsframework.model.FieldValue.value;

public class LaBanquePostaleConnector extends WebBankConnector {
  public static final Integer BANK_ID = 3;

  private static final String LOGIN_URL = "https://voscomptesenligne.labanquepostale.fr/voscomptes/canalXHTML/identif.ea?origin=particuliers";

  private JTextField userIdField;
  private JLabel[] keyboardLabels;
  private JLabel[] codeLabels;
  private String currentCode = "";
  private LoginAction loginAction;

  private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

  public static void main(String[] args) throws IOException {
    WebConnectorLauncher.show(new Factory());
  }

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory) {
      return new LaBanquePostaleConnector(repository, directory);
    }
  }

  private LaBanquePostaleConnector(GlobRepository repository, Directory directory) {
    super(BANK_ID, repository, directory);
    browser.setJavascriptEnabled(true);
  }

  protected JPanel createPanel() {

    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/laBanquePostaleConnectorPanel.splits");

    userIdField = new JTextField();
    builder.add("userIdField", userIdField);

    builder.add("userIdHelp", new BrowsingAction("Aide du site", directory) {
      protected String getUrl() {
        return "https://www.labanquepostale.fr/index/particuliers/outils/aide/aide_banque_en_ligne/Aide_identification.html";
      }
    });

    keyboardLabels = new JLabel[10];
    for (int i = 0; i < keyboardLabels.length; i++) {
      keyboardLabels[i] = new JLabel();
      builder.add("label" + i, keyboardLabels[i]);
    }

    codeLabels = new JLabel[6];
    for (int i = 0; i < codeLabels.length; i++) {
      codeLabels[i] = builder.add("codeLabel" + i, new JLabel()).getComponent();
    }

    builder.add("clearCode", new ClearCodeAction());

    loginAction = new LoginAction();
    loginAction.setEnabled(false);
    builder.add("login", loginAction);

    for (int i = 0; i < keyboardLabels.length; i++) {
      final int index = i;
      keyboardLabels[i].addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent mouseEvent) {
          if (keyboardLabels[index].getIcon() != null) {
            processKeyboardClick(index);
          }
        }
      });
    }

    initLogin();

    return builder.load();
  }

  private void initLogin() {
    loginAction.setEnabled(false);

    Thread thread = new Thread() {
      public void run() {
        try {
          notifyInitialConnection();
          WebPage homePage = loadPage(LOGIN_URL);
          notifyWaitingForUser();

          WebForm loginForm = homePage.getFormByName("formAccesCompte");

          for (int i = 0; i < keyboardLabels.length; i++) {
            WebPanel cell = loginForm.getPanelById("val_cel_" + i);
            keyboardLabels[i].setIcon(cell.getSingleImage().asIcon());
          }

          SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              loginAction.setEnabled(true);
            }
          });
        }
        catch (Exception e) {
          notifyErrorFound(e);
        }
      }
    };
    thread.start();
  }

  public void panelShown() {
    userIdField.requestFocus();
  }

  public void reset() {
    currentCode = "";
    updateCodeLabels();
    for (int i = 0; i < keyboardLabels.length; i++) {
      keyboardLabels[i].setIcon(null);
    }
    initLogin();
  }

  private void processKeyboardClick(int index) {
    if (currentCode.length() >= codeLabels.length) {
      return;
    }
    currentCode = currentCode + Integer.toString(index);
    updateCodeLabels();
  }

  private class ClearCodeAction extends AbstractAction {
    public void actionPerformed(ActionEvent event) {
      currentCode = "";
      updateCodeLabels();
    }
  }

  private void updateCodeLabels() {
    for (int i = 0; i < codeLabels.length; i++) {
      codeLabels[i].setText(i < currentCode.length() ? "*" : " ");
    }
  }

  private class LoginAction extends AbstractAction {
    public void actionPerformed(ActionEvent event) {
      notifyIdentificationInProgress();
      loginAction.setEnabled(false);
      directory.get(ExecutorService.class)
        .submit(new Callable<Object>() {
          public Object call() throws Exception {
            try {
              WebPage loginPage = browser.getCurrentPage();
              WebForm loginForm = loginPage.getFormByName("formAccesCompte");

              loginForm.getTextInputById("val_cel_dentifiant").setText(userIdField.getText());
              loginForm.setHiddenFieldById("cs", currentCode);

              WebPage accountsPage = loginPage.executeJavascript("window.document.forms[\"formAccesCompte\"].submit();");
              if (!accountsPage.getUrl().contains("voscomptesenligne.labanquepostale.fr/voscomptes/") ||
                  !accountsPage.containsTagWithId("table", "comptes")) {
                notifyIdentificationFailed();
                reset();
                return null;
              }

              notifyDownloadInProgress();

              List<AccountEntry> entries = new ArrayList<AccountEntry>();
              parseAccounts(accountsPage, "comptes", false, entries);
              parseAccounts(accountsPage, "comptesEpargne", true, entries);

              accounts.clear();
              if (!entries.isEmpty()) {

                for (AccountEntry entry : entries) {

                  WebPage accountPage = browser.loadPageInSameSite(entry.url);
                  String urlPrefix = "/voscomptes/canalXHTML/CCP/releves_ccp/init-releve_ccp.ea";
                  if (accountPage.containsText(urlPrefix)) {
                    accountPage = browser.loadPageInSameSite(urlPrefix + "?typeRecherche=10&compte.numero=" + entry.number);
                  }

                  String path = loadOperationsForAccount(accountPage, entry);

                  Glob account = createOrUpdateRealAccount(entry.name,
                                                           entry.number,
                                                           entry.position,
                                                           null,
                                                           BANK_ID);
                  repository.update(account.getKey(),
                                    value(RealAccount.FILE_NAME, path),
                                    value(RealAccount.SAVINGS, entry.isSavings));

                }
              }
              importCompleted();
            }
            catch (final Exception e) {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  notifyErrorFound(e);
                }
              });
            }
            return null;
          }
        });
    }
  }

  private String loadOperationsForAccount(WebPage accountPage, AccountEntry entry) throws Exception {
    GlobRepository tempRepository = GlobRepositoryBuilder.createEmpty();
    Glob account =
      tempRepository.create(Account.TYPE,
                            value(Account.BANK, BANK_ID),
                            value(Account.NUMBER, entry.number),
                            value(Account.NAME, entry.name),
                            value(Account.ACCOUNT_TYPE, entry.isSavings ? AccountType.SAVINGS.getId() : AccountType.MAIN.getId()));

    Integer accountId = account.get(Account.ID);

    List<WebTableRow> rows = accountPage.getTableById("mouvements").getRowsWithoutHeaderAndFooters();
    for (WebTableRow row : rows) {

      Date date = DATE_FORMAT.parse(row.getCell(0).asText().trim());
      int monthId = Month.getMonthId(date);
      int day = Month.getDay(date);
      String label = extractLabel(row.getCell(1).getSingleSpan().asXml());
      Double amount = extractAmount(row.getCell(2).asText());

      tempRepository.create(Transaction.TYPE,
                            value(Transaction.ACCOUNT, accountId),
                            value(Transaction.MONTH, monthId),
                            value(Transaction.DAY, day),
                            value(Transaction.BANK_MONTH, monthId),
                            value(Transaction.BANK_DAY, day),
                            value(Transaction.LABEL, label),
                            value(Transaction.ORIGINAL_LABEL, label),
                            value(Transaction.AMOUNT, amount));
    }

    File file = File.createTempFile("budgetview_download", "ofx");
    FileWriter writer = new FileWriter(file);
    OfxExporter.write(tempRepository, writer, false);
    return file.getAbsolutePath();
  }

  static String extractLabel(String xml) {
    return xml
      .replace("\n", "")
      .replace("<span>", "")
      .replace("</span>", "")
      .replaceAll("<br/>.*", "")
      .replaceAll("<[^<]*>", "")
      .replaceAll("</[^<]*>", "")
      .replaceAll("[ \t]+", " ")
      .trim();
  }

  protected Double extractAmount(String amount) throws WebParsingError {
    String cleanedUpAmount = amount.replace("[ ]+", "").replace(",", ".");
    try {
      return Double.parseDouble(cleanedUpAmount);
    }
    catch (NumberFormatException e) {
      throw new WebParsingError(browser.getUrl(), e);
    }
  }

  private void parseAccounts(WebPage accountsPage, String tableId, boolean savings, List<AccountEntry> entries) throws WebParsingError {
    WebTable accountTable = accountsPage.getTableById(tableId);
    List<WebTableRow> rows = accountTable.getRowsWithoutHeaderAndFooters();
    for (WebTableRow row : rows) {
      String name = row.getCell(0).asText();
      String number = row.getCell(1).asText();
      String position = row.getCell(2).asText();
      String url = row.getCell(0).getSingleAnchor().getTargetUrl();
      entries.add(new AccountEntry(name, number, position, url, savings));
    }
  }

  private class AccountEntry {
    public final String name;
    public final String number;
    public final String position;
    public final String url;
    public final boolean isSavings;

    private AccountEntry(String name, String number, String position, String url, boolean savings) {
      isSavings = savings;
      this.name = name;
      this.number = number;
      this.position = position;
      this.url = url;
    }
  }

  public void downloadFile() throws Exception {
  }
}
