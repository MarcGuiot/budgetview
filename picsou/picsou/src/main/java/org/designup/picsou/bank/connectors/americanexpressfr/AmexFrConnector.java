package org.designup.picsou.bank.connectors.americanexpressfr;

import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.connectors.WebBankConnector;
import org.designup.picsou.bank.connectors.utils.FilteringConnection;
import org.designup.picsou.bank.connectors.webcomponents.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.*;
import org.designup.picsou.exporter.ofx.OfxExporter;
import org.designup.picsou.model.*;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static org.globsframework.model.FieldValue.value;

public class AmexFrConnector extends WebBankConnector {
  public static final Integer BANK_ID = 16;

  private static final String LOGIN_URL = "https://www.americanexpress.com/france/";

  private UserAndPasswordPanel userAndPasswordPanel;

  public static void main(String[] args) throws IOException {
    WebConnectorLauncher.show(BANK_ID, new Factory());
  }

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory, boolean syncExistingAccount, Glob synchro) {
      return new AmexFrConnector(syncExistingAccount, repository, directory, synchro);
    }
  }

  private AmexFrConnector(boolean syncExistingAccount, GlobRepository repository, Directory directory, Glob synchro) {
    super(BANK_ID, syncExistingAccount, repository, directory, synchro);
    browser.setJavascriptEnabled(false);
    browser.setHttpConnectionProvider(new HttpConnectionProvider() {
      public HttpWebConnection getHttpConnection(WebClient client) {
        FilteringConnection connection = new FilteringConnection(client);
        connection.exclude("doubleclick.net", "bootstrap.js", "s_code.js", "compresscacheservlet?filetype=js", "backbutton.js");
        connection.setDebugEnabled(false);
        return connection;
      }
    });
  }

  protected JPanel createPanel() {
    userAndPasswordPanel = new UserAndPasswordPanel(new ConnectAction(), directory);
    JPanel panel = userAndPasswordPanel.getPanel();
    userAndPasswordPanel.setUserCode(getSyncCode());
    loadHomePage();
    return panel;
  }

  private void loadHomePage() {
    userAndPasswordPanel.setFieldsEnabled(true);
    userAndPasswordPanel.setEnabled(false);
    directory.get(ExecutorService.class).submit(new Runnable() {
      public void run() {
        try {
          notifyInitialConnection();
          loadPage(LOGIN_URL);
          userAndPasswordPanel.setEnabled(true);
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
  }

  public String getCode() {
    return userAndPasswordPanel.getUser();
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
      directory.get(ExecutorService.class)
        .submit(new Callable<Object>() {
          public Object call() throws Exception {
            try {
              WebPage homePage = browser.getCurrentPage();
              WebForm idForm = homePage.getFormByName("ssoform");
              idForm.getTextInputById("UserID").setText(userAndPasswordPanel.getUser());
              idForm.getPasswordInputById("Password").setText(userAndPasswordPanel.getPassword());
              idForm.getSelectById("manage").selectByValue("option1");
              WebPage cardsPage = idForm.submit();
              if (!cardsPage.getUrl().contains("accountSummary")) {
                userAndPasswordPanel.requestFocus();
                userAndPasswordPanel.setFieldsEnabled(true);
                userAndPasswordPanel.setEnabled(true);
                notifyIdentificationFailed();
                loadHomePage();
                return null;
              }

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
    notifyDownloadInProgress();

    WebPage cardsPage = browser.getCurrentPage();
    WebPage cardPage = cardsPage.getAnchorById("fr_myca_view_transactions").click();
    List<String> cardNames = cardPage.getSelectById("cardAccount").getEntryNames();
    if (cardNames.size() == 0) {
      throw new WebParsingError(cardPage.getUrl(), "Found no accounts in: " + cardPage.getSelectById("cardAccount").asXml());
    }
    else if (cardNames.size() > 1) {
      throw new WebParsingError(cardPage.getUrl(), "Found too many accounts in: " + cardPage.getTableById("summaryTable").asXml());
    }
    parseCardPage(cardNames.get(0), cardPage);
  }

  private WebPage parseCardPage(String cardName, WebPage cardPage) throws WebParsingError, WebCommandFailed, IOException {

    notifyDownloadForAccount(cardName);

    String cardNumber = extractCardNumber(cardName);
    String accountPosition = extractCurrentPosition(cardPage);
    Glob realAccount = createOrUpdateRealAccount(cardName, cardNumber, accountPosition,
                                                 getYesterdaysDate(), BANK_ID);

    GlobRepository tempRepository = GlobRepositoryBuilder.createEmpty();

    Glob account =
      tempRepository.create(Account.TYPE,
                            value(Account.BANK, BANK_ID),
                            value(Account.NUMBER, cardNumber),
                            value(Account.NAME, cardName),
                            value(Account.NAME, cardName),
                            value(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()));

    for (String periodValue : cardPage.getSelectById("viewPeriod").getValues()) {
      cardPage = browser.load(getCardPeriodUrl(periodValue));
      WebPanel txnsSection = cardPage.getPanelById("txnsSection");
      String transactionTableId = "table-txnsCard0";
      if (txnsSection.containsTagWithId("table", transactionTableId)) {
        parseTransactions(account, tempRepository,
                          txnsSection.getTableById(transactionTableId));

      }
      else if (txnsSection.asText().contains("Il n'y a aucune opération enregistrée au cours de cette Période de Facturation")) {
        // skip
      }
      else {
        throw new WebParsingError(txnsSection, "Cannot find table with id '" + transactionTableId + "'");
      }
    }

    StringWriter writer = new StringWriter();
    OfxExporter.write(tempRepository, writer, false);
    repository.update(realAccount.getKey(), RealAccount.FILE_CONTENT, writer.toString());
    return browser.getCurrentPage();
  }

  private String getCardPeriodUrl(String periodValue) {
    return "https://global.americanexpress.com/myca/intl/estatement/emea/statement.do?request_type=&Face=fr_FR&BPIndex=" + periodValue + "&sorted_index=0&inav=fr_myca_view_transactions";
  }

  private void parseTransactions(Glob account, GlobRepository tempRepository, WebTable table) throws WebParsingError, IOException {
    for (WebTableRow row : table.getRowsWithoutHeaderAndFooters()) {
      FieldValuesBuilder valuesBuilder = new FieldValuesBuilder();
      valuesBuilder.set(value(Transaction.ACCOUNT, account.get(Account.ID)));
      extractDate(row.getCell(0), valuesBuilder);
      extractLabel(row.getCell(1), valuesBuilder);
      extractAmount(row.getCell(2), row.getCell(3), valuesBuilder);
      tempRepository.create(Transaction.TYPE, valuesBuilder.toArray());
    }
  }

  private void extractLabel(WebTableCell cell, FieldValuesBuilder valuesBuilder) {
    String label = cleanupLabel(cell.asXml());
    valuesBuilder.set(Transaction.LABEL, label);
    valuesBuilder.set(Transaction.ORIGINAL_LABEL, label);
  }

  public static String cleanupLabel(String nodeXml) {
    return nodeXml
      .replaceAll("[\n\t ]+", " ")
      .replace("</td>", "")
      .replaceAll("<a.*>.*</a>", "")
      .replaceAll("<div.*>.*</div>", "")
      .replaceAll("<td.*>", "")
      .trim();
  }

  private void extractDate(WebTableCell cell, FieldValuesBuilder valuesBuilder) throws WebParsingError {
    String[] dateElements = cell.asText().replace(".", "").split(" ");
    if (dateElements.length != 2) {
      throw new WebParsingError(cell, "Cannot parse date: " + cell.asText());
    }
    try {
      int day = Integer.parseInt(dateElements[0]);
      valuesBuilder.set(Transaction.DAY, day);
      valuesBuilder.set(Transaction.BANK_DAY, day);
    }
    catch (NumberFormatException e) {
      throw new WebParsingError(cell, e);
    }

    Integer currentMonthId = Month.getMonthId(new Date());
    int currentYear = Month.toYear(currentMonthId);
    int parsedMonth = getMonthNumber(cell, dateElements[1].trim().toLowerCase());
    Integer parsedMonthId = Month.toMonthId(currentYear, parsedMonth);
    if (parsedMonthId > currentMonthId) {
      parsedMonthId = Month.toMonthId(currentYear - 1, parsedMonth);
    }
    valuesBuilder.set(Transaction.MONTH, parsedMonthId);
    valuesBuilder.set(Transaction.BANK_MONTH, parsedMonthId);
  }

  private void extractAmount(WebTableCell creditCell, WebTableCell debitCell, FieldValuesBuilder valuesBuilder) throws WebParsingError {
    Double credit = extractAmount(creditCell.asText());
    Double debit = extractAmount(debitCell.asText());
    if (credit != null) {
      if (debit == null) {
        valuesBuilder.set(Transaction.AMOUNT, credit);
      }
      else {
        throw new WebParsingError(browser.getUrl(), "Found both credit and debit amounts in: " + creditCell.getEnclosingRow().asXml());
      }
    }
    else { // credit == null
      if (debit != null) {
        valuesBuilder.set(Transaction.AMOUNT, -debit);
      }
      else {
        throw new WebParsingError(browser.getUrl(), "Found no credit and debit amounts in: " + creditCell.getEnclosingRow().asXml());

      }
    }
  }

  public Double extractAmount(String text) throws WebParsingError {
    if (text.trim().length() == 0) {
      return null;
    }
    String amountText = text.replace(" EUR", "").replace(".", "").replace(",", ".");
    try {
      return Double.parseDouble(amountText);
    }
    catch (NumberFormatException e) {
      throw new WebParsingError(browser.getUrl(), "Cannot parse amount for: " + text);
    }
  }

  private String extractCurrentPosition(WebPage cardPage) throws WebParsingError {
    WebTableCell cell = cardPage.getTableById("summaryTable").getCellById("colOSBalance");
    if (!cell.getPanelByAttribute("div", "class", "summaryTitles").asText().contains("Solde actuel")) {
      throw new WebParsingError(browser.getUrl(), "Unexpected column title in: " + cell.asXml());
    }
    return cell.asText()
      .replace("Solde actuel", "")
      .replace(" EUR", "")
      .replace(".", "")
      .replace(",", "")
      .trim();
  }

  private String extractCardNumber(String cardName) {
    return cardName.replaceAll("[A-z -]+", "").trim();
  }

  private int getMonthNumber(WebTableCell cell, String month) throws WebParsingError {
    if (month.startsWith("jan")) {
      return 1;
    }
    if (month.startsWith("fév") || month.startsWith("fev")) {
      return 2;
    }
    if (month.startsWith("mar")) {
      return 3;
    }
    if (month.startsWith("avr")) {
      return 4;
    }
    if (month.startsWith("mai")) {
      return 5;
    }
    if (month.startsWith("juin")) {
      return 6;
    }
    if (month.startsWith("juil")) {
      return 7;
    }
    if (month.startsWith("ao")) {
      return 8;
    }
    if (month.startsWith("sep")) {
      return 9;
    }
    if (month.startsWith("oct")) {
      return 10;
    }
    if (month.startsWith("nov")) {
      return 11;
    }
    if (month.startsWith("dec") || month.startsWith("déc")) {
      return 12;
    }

    throw new WebParsingError(cell, "Unexpected month: " + month);
  }
}
