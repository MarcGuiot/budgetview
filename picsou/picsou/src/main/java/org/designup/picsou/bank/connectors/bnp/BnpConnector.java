package org.designup.picsou.bank.connectors.bnp;

import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.host.Event;
import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.connectors.WebBankConnector;
import org.designup.picsou.bank.connectors.webcomponents.*;
import org.designup.picsou.bank.connectors.webcomponents.filters.WebFilter;
import org.designup.picsou.bank.connectors.webcomponents.utils.*;
import org.designup.picsou.model.RealAccount;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.globsframework.model.FieldValue.value;

public class BnpConnector extends WebBankConnector implements HttpConnectionProvider {
  public static final int BANK_ID = 5;

  private static final String INDEX = "https://www.secure.bnpparibas.net/banque/portail/particulier/HomeConnexion?type=homeconnex";
  private static final String HOME_URL = "https://www.secure.bnpparibas.net/banque/portail/particulier/FicheA?pageId=unedescomptesnode";
  private static final String DOWNLOADS_URL = "https://www.secure.bnpparibas.net/NSFR?Action=ASK_TELE";
  private static final Pattern ACCOUNT_DATE_REGEXP = Pattern.compile(".*Solde[\\s]+au[\\s]+([0-9]+/[0-9]+/[0-9]+).*");
  private Action loginAction;
  private Action clearCodeAction;
  private JTextField codeField;
  private JTextField passwordField;
  private JLabel keyboardLabel;
  private BnpKeyboardPanel grill;
  private List<AccountEntry> accountEntries;

  public static void main(String[] args) throws IOException {
    WebConnectorLauncher.show(BANK_ID, new Factory());
  }

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory, boolean syncExistingAccount, Glob synchro) {
      return new BnpConnector(syncExistingAccount, repository, directory, synchro);
    }
  }

  private BnpConnector(boolean syncExistingAccount, GlobRepository repository, Directory directory, Glob synchro) {
    super(BANK_ID, syncExistingAccount, repository, directory, synchro);
    browser.setTimeout(15000);
  }

  protected JPanel createPanel() {
    final SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/bnpConnectorPanel.splits");

    codeField = new JTextField();
    builder.add("code", codeField);
    codeField.setText(getSyncCode());

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

    grill = new BnpKeyboardPanel(5);
    builder.add("grill", grill);

    initLogin();
    addToBeDisposed(new Disposable() {
      public void dispose() {
        builder.dispose();
      }
    });

    return builder.load();
  }

  private void initLogin() {
    directory.get(ExecutorService.class)
      .execute(new Runnable() {
        public void run() {
          try {
            notifyInitialConnection();
            WebPage loginPage = loadPage(INDEX);

            final WebPanel id = loginPage.getPanelById("secret-nbr-keyboard");
            String style = id.getAttributeValue("style"); //background-image: url("/NSImgGrille?timestamp=1381585729451");
            int i = style.indexOf("background-image: url(");
            if (i < 0) {
              notifyErrorFound(new WebParsingError(id, "background-image: url( not found in " + style));
              return;
            }

            String imageUrl = style.substring(i + "background-image: url(\"".length(), style.lastIndexOf(");") - 1);

            final HtmlPage page = browser.getCurrentHtmlPage();
            final WebClient webclient = page.getWebClient();

            final URL url = page.getFullyQualifiedUrl(imageUrl);
            final WebRequest request = new WebRequest(url);
            request.setAdditionalHeader("Referer", page.getWebResponse().getWebRequest().getUrl().toExternalForm());
            WebResponse response = webclient.loadWebResponse(request);
            final ImageInputStream iis = ImageIO.createImageInputStream(response.getContentAsStream());
            final Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
            if (!iter.hasNext()) {
              notifyErrorFound(new WebParsingError(id, "Fail to download grille " + style));
              return;
            }
            ImageReader imageReader = iter.next();
            imageReader.setInput(iis);
            BufferedImage image = imageReader.read(0);
            iis.close();
            imageReader.dispose();

            grill.setSize(image.getWidth(), image.getHeight());
            grill.setImage(image, new BnpKeyboardPanel.CoordinateListener() {
              public void enter(int x, int y) {
              }

              public void click(int x, int y) {
                int key = y * 5 + x + 1;
                final String k;
                if (key < 10){
                  k = "0" + key;
                }
                else {
                  k = "" + key;
                }
                try {
                  WebAnchor anchor = id.findFirst(new WebFilter() {
                    public boolean matches(HtmlElement element) {
                      String ondblclick = element.getAttribute("ondblclick");
                      return element.getTagName().equals(HtmlAnchor.TAG_NAME) &&
                             (ondblclick != null && ondblclick.contains(k));
                    }
                  }).asAnchor();
                  if (anchor != null) {
                    anchor.click();
                    browser.waitForBackgroundJavaScript(1000);
                    updatePasswordField();
                  }
                }
                catch (WebCommandFailed failed) {
                  notifyErrorFound(failed);
                }
                catch (WebParsingError error) {
                  notifyErrorFound(error);
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
    WebPasswordInput ch2 = browser.retry(new Callable<WebPasswordInput>() {
      public WebPasswordInput call() throws Exception {
        browser.setToTopLevelWindow();
        return browser.getCurrentPage().getPasswordInputByName("ch2");
      }
    });

    passwordField.setText(ch2.getValue());
  }

  public void panelShown() {
    if (Strings.isNullOrEmpty(codeField.getText())) {
      codeField.requestFocus();
    }
    else {
      passwordField.requestFocus();
    }
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
              accountEntries = new ArrayList<AccountEntry>();
              for (WebTableCell labelCell : labelCells) {
                if (!labelCell.containsAnchor()) {
                  continue;
                }
                String name = labelCell.asText();
                String onclick = labelCell.getSingleAnchor().getOnclick();
                AccountEntry entry = new AccountEntry(name, onclick);
                accountEntries.add(entry);

                String positionText = labelCell.getEnclosingRow().getCell(3).asText();
                entry.setPositionForDefaultDate(positionText, labelCell);
              }

              if (accountEntries.isEmpty()) {
                notifyIdentificationFailed();
                reset();
                return;
              }

              notifyDownloadInProgress();

              try {
                for (AccountEntry entry : accountEntries) {
                  WebPage accountPage = browser.load(HOME_URL).executeJavascript(entry.onclick);
                  parseAccountPage(entry, accountPage);
                }
              }
              catch (WebCommandFailed e) {
                // server took too long, ignore this part
                Log.write("-- BNPP response taking too long: skipped");
              }

              for (AccountEntry entry : accountEntries) {
                createOrUpdateRealAccount(entry.name, entry.number, entry.position, entry.updateDate, BANK_ID);
              }

              doImport();
            }
            catch (final Throwable e) {
              SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                  notifyErrorFound(e);
                }
              });
              return;
            }
          }
        });
    }
  }

  private void parseAccountPage(AccountEntry entry, WebPage accountPage) throws WebParsingError {
    notifyPreparingAccount(entry.name);
    WebTable table;
    try {
      table = accountPage.getTableWithClass("infoCompteDroite");
    }
    catch (WebParsingError error) {
      // si pour le compte cheque on ne trouve pas l'infoCompteDroite c'est qu'il y a un probleme.
      if (entry.name.startsWith("Compte de")) {
        throw error;
      }
      return ;
    }
    Matcher matcher = ACCOUNT_DATE_REGEXP.matcher(table.asText().replaceAll("\\s", " "));
    if (!matcher.matches()) {
      throw new WebParsingError(table, "Position date not found");
    }
    String date = matcher.group(1);

    WebTableCell cell = table.getRow(0).getCell(1);
    String position = cell.asText().replaceAll("[\\s€]+", "");
    entry.setPosition(position, date, cell);
    return;
  }

  private AccountEntry findEntry(List<AccountEntry> entries, String path, WebTableCell accountNameCell) throws WebParsingError {
    for (AccountEntry entry : entries) {
      if (path.contains(entry.name)) {
        return entry;
      }
    }
    throw new WebParsingError(accountNameCell, "Cannot find entry for " + path);
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
    notifyDownloadInProgress();

    WebFrame frame = browser.load(DOWNLOADS_URL).getFrameByName("main");
    WebForm downloadConfigForm = frame.loadTargetPage().getFormByAction("/SAF_TLC_CNF");
    try {
      downloadConfigForm.getInputByNameAndValue("ch_rop", "tous").select();
    }
    catch (WebParsingError error) {
      // ignore => il n'y a qu'un compte.
    }
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
      AccountEntry entry = findEntry(accountEntries, accountNameCell.asText(), accountNameCell);
      entry.setDownloadUrl(targetUrl);
      entry.setNumber(extractNumber(accountNameCell.asXml()));
    }

    for (AccountEntry entry : accountEntries) {
      notifyDownloadForAccount(entry.name);
      if (entry.downloadUrl != null) {
        String fileContent = browser.downloadToString(entry.downloadUrl, ".qif");
        // => on recoit un html avec :
        //Aucune op&eacute;ration n'est &agrave; t&eacute;l&eacute;charger sur ce compte pour la p&eacute;riode choisie
        if (!fileContent.startsWith("<html>")){
          for (Glob realAccount : accounts) {
            if (realAccount.get(RealAccount.NAME).trim().contains(entry.name)) {
              repository.update(realAccount.getKey(),
                                value(RealAccount.NUMBER, entry.number),
                                value(RealAccount.FILE_CONTENT, fileContent));
              break;
            }
          }
        }
      }
    }
  }

  public String getCode() {
    return codeField.getText();
  }

  public void reset() {
    try {
      grill.setImage(null, BnpKeyboardPanel.CoordinateListener.NULL);
      grill.repaint();
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

    public void setPositionForDefaultDate(String positionText, WebTableCell labelCell) throws WebParsingError {
      setPosition(positionText, DATE_FORMAT.format(getYesterdaysDate()), labelCell);
    }

    public void setPosition(String positionText, String updateDateText, WebTableCell cell) throws WebParsingError {
      position = cleanUpAmount(positionText);
      updateDate = extractDate(updateDateText, cell);
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

    private Date extractDate(String text, WebTableCell cell) throws WebParsingError {
      try {
        return DATE_FORMAT.parse(text);
      }
      catch (ParseException e) {
        throw new WebParsingError(cell, e);
      }
    }
  }
}
