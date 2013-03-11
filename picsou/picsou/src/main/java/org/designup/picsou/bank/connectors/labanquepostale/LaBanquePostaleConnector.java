package org.designup.picsou.bank.connectors.labanquepostale;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.connectors.WebBankConnector;
import org.designup.picsou.bank.connectors.webcomponents.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebConnectorLauncher;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;
import org.designup.picsou.gui.browsing.BrowsingAction;
import org.designup.picsou.model.*;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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

  public static void main(String[] args) throws IOException {
    WebConnectorLauncher.show(BANK_ID, new Factory());
  }

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory, boolean syncExistingAccount, Glob synchro) {
      return new LaBanquePostaleConnector(syncExistingAccount, repository, directory, synchro);
    }
  }

  private LaBanquePostaleConnector(boolean syncExistingAccount, GlobRepository repository, Directory directory, Glob synchro) {
    super(BANK_ID, syncExistingAccount, repository, directory, synchro);
    browser.setJavascriptEnabled(true);
    browser.getClient().getCookieManager().setCookiesEnabled(true);
  }


  protected JPanel createPanel() {

    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/laBanquePostaleConnectorPanel.splits");

    userIdField = new JTextField();
    builder.add("userIdField", userIdField);

    userIdField.setText(getSyncCode());

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

    directory.get(ExecutorService.class)
      .submit(new Runnable() {
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
      });
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
              doImport();
            }
            catch (final Exception e) {
                  notifyErrorFound(e);
            }
            return null;
          }
        });
    }
  }

  public void downloadFile() throws Exception {
    WebPage accountsPage = browser.getCurrentPage();
    List<AccountEntry> entries = new ArrayList<AccountEntry>();
    parseAccounts(accountsPage, "comptes", false, entries);
    parseAccounts(accountsPage, "comptesEpargne", true, entries);

    if (entries.isEmpty()){
      return;
    }
    WebPage newPage = entries.get(0).anchor.click();

    for (AccountEntry entry : entries) {
      WebSelect listeComptes = newPage.getSelectById("listeComptes");
      listeComptes.selectByValue(entry.account.get(RealAccount.NUMBER));
      newPage = newPage.getInputById("idNum0").click();
      WebPanel main = newPage.getPanelById("main");
      WebComponent.HtmlNavigate liens = main.findFirst(WebContainer.filterAttribute("class", "liens"));
      WebAnchor telecharger = liens.in().next().in().asAnchor();
      newPage = telecharger.click();
      WebForm form = newPage.getFormById("formID");
      WebAnchor anchor = form.getAnchor(WebContainer.filterAttribute("title", "Modifier"));
      WebPage currentPage = anchor.click();
      WebSelect format = currentPage.getSelectById("format");
      currentPage = format.selectByValue("OFX");
      currentPage.getRadioButtonById("duree0").select();

      form = currentPage.getFormById("formulaire");
      Download download = form.submitByIdAndDownload("idNum0");
      String fileContent = download.readAsOfx();
      repository.update(entry.account.getKey(), RealAccount.FILE_CONTENT, fileContent);
      currentPage = new WebPage(browser, (HtmlPage)browser.getClient().getCurrentWindow().getParentWindow().getEnclosedPage());
      WebPanel window = currentPage.getPanelById("modalWindow");
      newPage = window.getAnchorWithRef("#").click();
    }
  }


  //  private String loadOperationsForAccount(WebPage accountPage, AccountEntry entry) throws Exception {
//    GlobRepository tempRepository = GlobRepositoryBuilder.createEmpty();
//    Glob account =
//      tempRepository.create(Account.TYPE,
//                            value(Account.BANK, BANK_ID),
//                            value(Account.NUMBER, entry.number),
//                            value(Account.NAME, entry.name),
//                            value(Account.ACCOUNT_TYPE, entry.isSavings ? AccountType.SAVINGS.getId() : AccountType.MAIN.getId()));
//
//    Integer accountId = account.get(Account.ID);
//
//    List<WebTableRow> rows = accountPage.getTableById("mouvements").getRowsWithoutHeaderAndFooters();
//    for (WebTableRow row : rows) {
//
//      Date date = DATE_FORMAT.parse(row.getCell(0).asText().trim());
//      int monthId = Month.getMonthId(date);
//      int day = Month.getDay(date);
//      String label = extractLabel(row.getCell(1).getSingleSpan().asXml());
//      Double amount = extractAmount(row.getCell(2).asText());
//
//      tempRepository.create(Transaction.TYPE,
//                            value(Transaction.ACCOUNT, accountId),
//                            value(Transaction.MONTH, monthId),
//                            value(Transaction.DAY, day),
//                            value(Transaction.BANK_MONTH, monthId),
//                            value(Transaction.BANK_DAY, day),
//                            value(Transaction.LABEL, label),
//                            value(Transaction.ORIGINAL_LABEL, label),
//                            value(Transaction.AMOUNT, amount));
//    }
//
//    File file = File.createTempFile("budgetview_download", "ofx");
//    FileWriter writer = new FileWriter(file);
//    OfxExporter.write(tempRepository, writer, false);
//    return file.getAbsolutePath();
//  }

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

  public String getCode() {
    return userIdField.getText();
  }

  private void parseAccounts(WebPage accountsPage, String tableId, boolean savings, List<AccountEntry> entries) throws WebParsingError {
    WebTable accountTable = accountsPage.getTableById(tableId);
    List<WebTableRow> rows = accountTable.getRowsWithoutHeaderAndFooters();
    for (WebTableRow row : rows) {
      String name = row.getCell(0).asText();
      String number = row.getCell(1).asText();
      String position = row.getCell(2).asText();
      WebAnchor anchor = row.getCell(0).getSingleAnchor();
      Glob account = createOrUpdateRealAccount(name, number, position, null, bankId);
      if (account != null){
        repository.update(account.getKey(), RealAccount.SAVINGS, savings);
        entries.add(new AccountEntry(account, anchor));
      }
    }
  }

  public class AccountEntry {
    private Glob account;
    public final WebAnchor anchor;

    public AccountEntry(Glob account, WebAnchor anchor) {
      this.account = account;
      this.anchor = anchor;
    }
  }
}
