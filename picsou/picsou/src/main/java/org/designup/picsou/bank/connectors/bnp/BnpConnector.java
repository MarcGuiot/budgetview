package org.designup.picsou.bank.connectors.bnp;

import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.connectors.WebBankConnector;
import org.designup.picsou.bank.connectors.webcomponents.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.*;
import org.designup.picsou.model.RealAccount;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.joda.time.DateTime;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BnpConnector extends WebBankConnector implements HttpConnectionProvider {
  public static final int BANK_ID = 5;

  private static final String INDEX = "https://www.secure.bnpparibas.net/banque/portail/particulier/HomeConnexion?type=homeconnex";
  private static final String HOME_URL = "https://www.secure.bnpparibas.net/banque/portail/particulier/FicheA?pageId=unedescomptesnode";
  private static final String DOWNLOADS_URL = "https://www.secure.bnpparibas.net/NSFR?Action=ASK_TELE";
  //  private static final String INDEX = "file:index.html";
  //  private static final String URL_TELECHARGEMENT = "file:tel_telechargement.html";
  private static final Pattern ACCOUNT_DATE_REGEXP = Pattern.compile("Solde[ \n]+au[ \n]+([0-9]+/[0-9]+/[0-9]+).*");
  private Action loginAction;
  private Action clearCodeAction;
  private JTextField codeField;
  private JTextField passwordField;
  private JLabel keyboardLabel;

  public static void main(String[] args) throws IOException {
    WebConnectorLauncher.show(new Factory());
  }

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory) {
      return new BnpConnector(repository, directory);
    }
  }

  private BnpConnector(GlobRepository repository, Directory directory) {
    super(BANK_ID, repository, directory);
    browser.setTimeout(15000);
  }

  protected JPanel createPanel() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/bnpConnectorPanel.splits");

    codeField = new JTextField();
    builder.add("code", codeField);

    passwordField = new JTextField();
    passwordField.setEditable(false);
    builder.add("password", passwordField);

    keyboardLabel = new JLabel();
    builder.add("keyboardLabel", keyboardLabel);

    clearCodeAction = new ClearCodeAction();
    builder.add("clearCode", clearCodeAction);

    loginAction = new LoginAction();
    builder.add("login", loginAction);

    clearCodeAction.setEnabled(false);
    loginAction.setEnabled(false);

    initLogin();

    return builder.load();
  }

  private void initLogin() {
    directory.get(ExecutorService.class)
      .execute(new Runnable() {
        public void run() {
          try {
            notifyInitialConnection();
            WebPage loginPage = loadPage(INDEX);

            ImageMapper.install(loginPage.getImageMapByName("MapGril"), keyboardLabel)
              .addListener(new ImageMapper.Listener() {
                public void imageClicked() {
                  try {
                    updatePasswordField();
                  }
                  catch (WebParsingError e) {
                    notifyErrorFound(e);
                  }
                }
              });

            notifyWaitingForUser();

            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                clearCodeAction.setEnabled(true);
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
    passwordField.setText(browser.getCurrentPage().getPasswordInputByName("ch2").getValue());
  }

  public void panelShown() {
    codeField.requestFocus();
  }

  private class LoginAction extends AbstractAction {
    public void actionPerformed(ActionEvent event) {
      directory.get(ExecutorService.class)
        .execute(new Runnable() {
          public void run() {
            try {
              notifyIdentificationInProgress();
              WebPage loginPage = browser.getCurrentPage();
              loginPage.getTextInputByName("ch1").setText(codeField.getText());

              WebPage accountsPage = loginPage.getAnchorWithRef("javascript:valider2();").click();

              List<WebTableCell> labelCells = accountsPage.getTableCellsWithClass("libelleCompte");
              List<AccountEntry> entries = new ArrayList<AccountEntry>();
              for (WebTableCell labelCell : labelCells) {
                if (!labelCell.containsAnchor()) {
                  continue;
                }
                String name = labelCell.asText();
                String onclick = labelCell.getSingleAnchor().getOnclick();
                AccountEntry entry = new AccountEntry(name, onclick);
                entries.add(entry);

                String positionText = labelCell.getEnclosingRow().getCell(3).asText();
                entry.setPositionForDefaultDate(positionText);
              }

              if (entries.isEmpty()) {
                notifyIdentificationFailed();
                reset();
                return;
              }

              notifyDownloadInProgress();

              try {
                for (AccountEntry entry : entries) {
                  System.out.println("loading entry: " + entry.onclick + " on " + HOME_URL);
                  WebPage accountPage = browser.load(HOME_URL).executeJavascript(entry.onclick);
                  parseAccountPage(entry, accountPage);
                }
              }
              catch (WebCommandFailed e) {
                // server took too long, ignore this part
                System.out.println("-- BNPP response taking too long: skipped");
              }

              notifyDownloadInProgress();

              WebFrame frame = browser.load(DOWNLOADS_URL).getFrameByName("main");
              WebForm downloadConfigForm = frame.loadTargetPage().getFormByAction("/SAF_TLC_CNF");
              downloadConfigForm.getInputByNameAndValue("ch_rop", "tous").select();
              downloadConfigForm.getSelectByName("ch_rop_fmt_fic").selectByValue("RQM2005TF");
              downloadConfigForm.getSelectByName("ch_rop_fmt_dat").selectByValue("MMJJAAAA");
              downloadConfigForm.getSelectByName("ch_rop_fmt_sep").selectByValue("PT");
              downloadConfigForm.getInputByNameAndValue("ch_rop_dat", "tous").select();
              downloadConfigForm.getInputByNameAndValue("ch_memo", "OUI").select();

              WebPage qifsPage = downloadConfigForm.getInputByAttribute("src", "/gif/bn_val.gif").click();
              List<WebTableCell> cells = qifsPage.getTableCellsWithClass("hdoc1");
              for (WebTableCell cell : cells) {
                String targetUrl = cell.getSingleAnchor().getTargetUrl();
                WebTableCell accountNameCell = cell.getEnclosingRow().getCell(1);
                AccountEntry entry = findEntry(entries, accountNameCell.asText());
                entry.setDownloadUrl(targetUrl);
                entry.setNumber(extractNumber(accountNameCell.asXml()));
              }

              accounts.clear();
              for (AccountEntry entry : entries) {
                System.out.println("==>  " + entry);
                notifyDownloadForAccount(entry.name);
                Glob account = createOrUpdateRealAccount(entry.name,
                                                         entry.number,
                                                         entry.position,
                                                         entry.updateDate,
                                                         BANK_ID);

                File file = browser.downloadToTempFile(entry.downloadUrl, ".qif");
                repository.update(account.getKey(), RealAccount.FILE_NAME, file.getAbsolutePath());
              }
            }
            catch (Throwable e) {
              e.printStackTrace();
              notifyErrorFound(e);
            }
            importCompleted();
          }
        });
    }
  }

  private void parseAccountPage(AccountEntry entry, WebPage accountPage) throws WebParsingError {
    notifyPreparingAccount(entry.name);
    WebTable table = accountPage.getTableWithClass("infoCompteDroite");
    System.out.println("BnpConnector.parseAccountPage: table=" + table.asText());
    Matcher matcher = ACCOUNT_DATE_REGEXP.matcher(table.asText().replaceAll("[ \n\t]+", " "));
    if (!matcher.matches()) {
      throw new WebParsingError(table, "Position date not found");
    }
    String date = matcher.group(1);

    String position = table.getRow(0).getCell(1).asText().replaceAll("[ €]+", "");
    entry.setPosition(position, date);
  }

  private AccountEntry findEntry(List<AccountEntry> entries, String path) throws WebParsingError {
    for (AccountEntry entry : entries) {
      if (path.contains(entry.name)) {
        return entry;
      }
    }
    throw new WebParsingError(browser.getUrl(), "Cannot find entry for " + path);
  }

  private class ClearCodeAction extends AbstractAction {
    public void actionPerformed(ActionEvent event) {
      try {
        browser.getCurrentPage().getAnchorWithRef("Javascript:ReInit();").click();
        updatePasswordField();
      }
      catch (Throwable e) {
        notifyErrorFound(e);
      }
    }
  }

  public void downloadFile() throws Exception {
  }

  public void reset() {
    try {
      updatePasswordField();
      initLogin();
    }
    catch (WebParsingError e) {
      notifyErrorFound(e);
    }
  }

  public HttpWebConnection getHttpConnection(WebClient client) {
    return new BnpWebConnection(client);
  }

  static String extractNumber(String xmlText) {
    return xmlText
      .replace("\n", " ")
      .replace("\t", " ")
      .replace("&nbsp;", " ")
      .replaceAll("[ ]+", " ")
      .replaceAll(".*<br>", "")
      .replaceAll(".*<br/>", "")
      .replace("</td>", "")
      .trim();
  }

  private class AccountEntry {
    public final String name;
    public final String onclick;
    public String position;
    public Date updateDate;
    private String downloadUrl;
    public String number;

    private AccountEntry(String name, String onclick) {
      this.name = name;
      this.onclick = onclick;
    }

    public void setPositionForDefaultDate(String positionText) throws WebParsingError {
      setPosition(positionText, DATE_FORMAT.format(getYesterdaysDate()));
    }

    public void setPosition(String positionText, String updateDateText) throws WebParsingError {
      position = cleanUpAmount(positionText);
      updateDate = extractDate(updateDateText);
    }

    public void setNumber(String number) {
      this.number = number;
    }

    public void setDownloadUrl(String downloadUrl) {
      this.downloadUrl = downloadUrl;
    }

    public String toString() {
      return "account - name: " + name + "\n" +
             "      position: " + position + "\n" +
             "    updateDate: " + updateDate + "\n" +
             "   downloadUrl: " + downloadUrl + "\n" +
             "        number: " + number;
    }

    protected String cleanUpAmount(String amount) throws WebParsingError {
      return amount.replace(".", "").replace(",", ".");
    }

    private SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    private Date extractDate(String text) throws WebParsingError {
      try {
        return DATE_FORMAT.parse(text);
      }
      catch (ParseException e) {
        throw new WebParsingError(browser.getUrl(), e);
      }
    }
  }
}