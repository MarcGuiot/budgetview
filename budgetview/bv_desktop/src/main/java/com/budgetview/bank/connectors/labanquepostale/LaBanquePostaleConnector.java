package com.budgetview.bank.connectors.labanquepostale;

import com.budgetview.bank.BankConnector;
import com.budgetview.bank.BankConnectorFactory;
import com.budgetview.bank.connectors.WebBankConnector;
import com.budgetview.bank.connectors.utils.FilteringConnection;
import com.budgetview.bank.connectors.webcomponents.*;
import com.budgetview.bank.connectors.webcomponents.filters.WebFilters;
import com.budgetview.bank.connectors.webcomponents.utils.HttpConnectionProvider;
import com.budgetview.bank.connectors.webcomponents.utils.WebCommandFailed;
import com.budgetview.bank.connectors.webcomponents.utils.WebConnectorLauncher;
import com.budgetview.desktop.browsing.BrowsingAction;
import com.budgetview.desktop.components.tips.ErrorTip;
import com.budgetview.desktop.components.tips.TipPosition;
import com.budgetview.model.RealAccount;
import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.budgetview.bank.connectors.webcomponents.utils.WebParsingError;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class LaBanquePostaleConnector extends WebBankConnector implements HttpConnectionProvider {
  public static final Integer BANK_ID = 3;

  private static final String LOGIN_URL = "https://voscomptesenligne.labanquepostale.fr/voscomptes/canalXHTML/identif.ea?origin=particuliers";

  private JTextField userIdField;
  private JTextField passwordField;
  private JLabel keyboardLabel;
  private LoginAction loginAction;
  private Action clearCodeAction;

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
    browser.setCssEnabled(true);
    browser.getClient().getCookieManager().setCookiesEnabled(true);
  }

  public HttpWebConnection getHttpConnection(WebClient client) {
    FilteringConnection connection = new FilteringConnection(client);
    connection.exclude(".css", "swfobject.js", "messagerie.js", "xtroi.js", "ws_q4l", "appelsyndication-hub.ea", "onsubmit.js",
                       "google-analytics.com", "xiti.com", "bloc.html",
                       "jquery.datepicker.js");
    return connection;
  }

  protected JPanel createPanel() {

    final SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/laBanquePostaleConnectorPanel.splits");

    userIdField = new JTextField();
    builder.add("userIdField", userIdField);

    userIdField.setText(getSyncCode());

    builder.add("userIdHelp", new BrowsingAction("Aide du site", directory) {
      protected String getUrl() {
        return "https://www.labanquepostale.fr/particuliers/outils/aide/aide_banque_en_ligne/Aide_identification.Fonct_general.html";
      }
    });

    passwordField = new JTextField();
    passwordField.setEditable(false);
    builder.add("password", passwordField);

    keyboardLabel = new JLabel();
    builder.add("keyboardLabel", keyboardLabel);

    clearCodeAction = new ClearCodeAction();
    builder.add("clearCode", clearCodeAction);

    loginAction = new LoginAction();
    loginAction.setEnabled(false);
    builder.add("login", loginAction);

    initLogin();

    addToBeDisposed(new Disposable() {
      public void dispose() {
        builder.dispose();
      }
    });

    return builder.load();
  }

  private void initLogin() {
    loginAction.setEnabled(false);

    directory.get(ExecutorService.class)
      .submit(new Runnable() {
        private LBPMouseListener mouseListener;

        public void run() {
          try {
            notifyInitialConnection();
            WebPage homePage = loadPage(LOGIN_URL);
            notifyWaitingForUser();

            WebForm loginForm = homePage.getFormByName("formAccesCompte");
            WebPanel imageClavier = loginForm.getPanelById("imageclavier");
            String imagePath = imageClavier.getBackgroundImagePath();
            BufferedImage image = browser.loadImage(imagePath);
            keyboardLabel.setIcon(new ImageIcon(image));
            if (mouseListener != null) {
              keyboardLabel.removeMouseListener(mouseListener);
            }
            mouseListener = new LBPMouseListener(homePage);
            keyboardLabel.addMouseListener(mouseListener);
            notifyWaitingForUser();

            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                clearCodeAction.setEnabled(true);
                loginAction.setEnabled(true);
              }
            });

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

  private void updatePasswordField() throws WebParsingError {
    WebTextInput passwordInput = browser.getCurrentPage().getTextInputById("cvs-bloc-mdp-input");
    passwordField.setText(passwordInput.getValue());
  }

  public void panelShown() {
    if (Strings.isNullOrEmpty(userIdField.getText())) {
      userIdField.requestFocus();
    }
    else {
      passwordField.requestFocus();
    }
  }

  public void reset() {
    keyboardLabel.setIcon(null);
    passwordField.setText(null);
    initLogin();
  }

  private class ClearCodeAction extends AbstractAction {
    public void actionPerformed(ActionEvent event) {
      try {
        browser.getCurrentPage().getFormByName("formAccesCompte").getButtonInputByValue("Effacer").click();
        updatePasswordField();
      }
      catch (Throwable e) {
        notifyErrorFound(e);
      }
    }
  }

  private class LoginAction extends AbstractAction {
    public void actionPerformed(ActionEvent event) {

      if (Strings.isNullOrEmpty(userIdField.getText())) {
        ErrorTip.show(userIdField, "Vous devez saisir votre identifiant", directory, TipPosition.TOP_LEFT);
        return;
      }
      if (Strings.isNullOrEmpty(passwordField.getText())) {
        ErrorTip.show(passwordField, "Vous devez saisir votre mot de passe", directory, TipPosition.TOP_LEFT);
        return;
      }

      notifyIdentificationInProgress();
      loginAction.setEnabled(false);
      directory.get(ExecutorService.class)
        .submit(new Callable<Object>() {
          public Object call() throws Exception {
            try {
              WebPage loginPage = browser.getCurrentPage();
              WebForm loginForm = loginPage.getFormByName("formAccesCompte");

              loginForm.getTextInputById("val_cel_identifiant").setText(userIdField.getText());

              WebPage accountsPage = loginPage.executeJavascript("window.document.forms[\"formAccesCompte\"].submit();");
              if (!accountsPage.getUrl().contains("voscomptesenligne.labanquepostale.fr/voscomptes/") ||
                  !accountsPage.containsTagWithId("table", "comptes")) {
                notifyIdentificationFailed(browser);
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

    if (entries.isEmpty()) {
      return;
    }
    WebPage newPage = entries.get(0).anchor.click();

    for (AccountEntry entry : entries) {
      WebSelect listeComptes = newPage.getSelectById("listeComptes");
      listeComptes.selectByValue(entry.account.get(RealAccount.NUMBER));
      newPage = newPage.getInputById("idNum0").click();
      WebPanel main = newPage.getPanelById("main");
      WebComponent.HtmlNavigate liens = main.findFirst(WebFilters.attributeEquals("class", "liens"));
      WebAnchor telecharger = liens.in().next().in().asAnchor();
      newPage = telecharger.click();
      WebForm form = newPage.getFormById("formID");
      WebAnchor anchor = form.getAnchor(WebFilters.attributeEquals("title", "Modifier"));
      WebPage currentPage = anchor.click();
      WebSelect format = currentPage.getSelectById("format");
      currentPage = format.selectByValue("OFX");
      currentPage.getRadioButtonById("duree0").select();

      form = currentPage.getFormById("formulaire");
      Download download = form.submitByIdAndDownload("idNum0");
      String fileContent = download.readAsOfx();
      localRepository.update(entry.account.getKey(), RealAccount.FILE_CONTENT, fileContent);
      currentPage = new WebPage(browser, (HtmlPage)browser.getClient().getCurrentWindow().getParentWindow().getEnclosedPage());
      WebPanel window = currentPage.getPanelById("modalWindow");
      newPage = window.getAnchorWithRef("#").click();
    }
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

  protected Double extractAmount(WebTableCell cell) throws WebParsingError {
    String amount = cell.asText();
    String cleanedUpAmount = amount.replace("[ ]+", "").replace(",", ".");
    try {
      return Double.parseDouble(cleanedUpAmount);
    }
    catch (NumberFormatException e) {
      throw new WebParsingError(cell, e);
    }
  }

  public String getCode() {
    return userIdField.getText();
  }

  private void parseAccounts(WebPage accountsPage, String tableId, boolean savings, List<AccountEntry> entries) throws WebParsingError {
    WebTable accountTable = accountsPage.findTableById(tableId);
    if (accountTable == null) {
      return;
    }
    List<WebTableRow> rows = accountTable.getRowsWithoutHeaderAndFooters();
    for (WebTableRow row : rows) {
      String name = row.getCell(0).asText();
      String number = row.getCell(1).asText();
      String position = row.getCell(2).asText();
      WebAnchor anchor = row.getCell(0).getSingleAnchor();
      Glob account = createOrUpdateRealAccount(name, number, position, null, bankId);
      if (account != null) {
        localRepository.update(account.getKey(), RealAccount.SAVINGS, savings);
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

  private class LBPMouseListener extends MouseAdapter {

    private WebPage homePage;

    public LBPMouseListener(WebPage homePage) {
      this.homePage = homePage;
    }

    public void mouseClicked(MouseEvent event) {
      if (keyboardLabel.getIcon() == null) {
        return;
      }
      int index = getButtonId(event);
      try {
        homePage.getButtonById("val_cel_" + index).click();
        updatePasswordField();
      }
      catch (WebCommandFailed e) {
        throw new RuntimeException(e);
      }
      catch (WebParsingError e) {
        throw new RuntimeException(e);
      }
    }

    private int getButtonId(MouseEvent e) {
      double tileWidth = keyboardLabel.getIcon().getIconWidth() / 4;
      double tileHeight = keyboardLabel.getIcon().getIconHeight() / 4;
      int row = (int)Math.floor(((double)e.getY()) / tileHeight);
      int col = (int)Math.floor(((double)e.getX()) / tileWidth);
      return (row * 4) + col;
    }
  }
}
