package org.designup.picsou.bank.connectors.creditagricole;

import com.gargoylesoftware.htmlunit.HttpWebConnection;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.BankSynchroService;
import org.designup.picsou.bank.connectors.WebBankConnector;
import org.designup.picsou.bank.connectors.webcomponents.*;
import org.designup.picsou.bank.connectors.webcomponents.filters.WebFilter;
import org.designup.picsou.bank.connectors.webcomponents.filters.WebFilters;
import org.designup.picsou.bank.connectors.webcomponents.utils.*;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.RealAccount;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class CreditAgricoleConnector extends WebBankConnector implements HttpConnectionProvider {
  private JTextField codeField;
  private JButton validerCode;
  private JPasswordField passwordTextField;
  private String urlGrid;
  private GotoAutentifcation autentification;


  public static void main(String[] args) throws IOException {
    WebConnectorLauncher.show(61, new CreditAgricoleFactory(61));
//    WebConnectorLauncher.show(67, new CreditAgricoleFactory(67));
  }

  public CreditAgricoleConnector(int bankId, String url, GotoAutentifcation autentification,
                                 boolean syncExistingAccount, GlobRepository repository, Directory directory, Glob synchro) {
    super(bankId, syncExistingAccount, repository, directory, synchro);
    this.autentification = autentification;
    urlGrid = url;
  }

  public HttpWebConnection getHttpConnection(WebClient client) {
    return new HttpWebConnection(client) {
      public WebResponse getResponse(WebRequest request) throws IOException {
//        String s = request.getUrl().toString();
//        System.out.println("webRequest " + s);
//        Map<String, String> headers = request.getAdditionalHeaders();
//        for (Map.Entry<String, String> entry : headers.entrySet()) {
//          System.out.println("webRequest header : " + entry.getKey() + "<=>" + entry.getValue());
//        }
//        List<NameValuePair> parameters = request.getRequestParameters();
//        for (NameValuePair parameter : parameters) {
//          System.out.println("webRequest param : " + parameter.getName() + " ==> " + parameter.getValue());
//        }
//        String body = request.getRequestBody();
//        System.out.println("webRequest body : " + body);
        WebResponse response = super.getResponse(request);
//        System.out.println("webRequest response " + response.getLoadTime() + " ms.");
        return response;
      }
    };
  }

  public static void register(BankSynchroService bankSynchroService) {
    for (int i = 51; i <= 88; i++) {
      bankSynchroService.register(i, new CreditAgricoleFactory(i));
    }
  }

  interface GotoAutentifcation {
    public boolean go(WebPage webPage, String code) throws WebCommandFailed, WebParsingError;
  }


  public static class CreditAgricoleFactory implements BankConnectorFactory {
    private int id;

    CreditAgricoleFactory(int id) {
      this.id = id;
    }

    public BankConnector create(GlobRepository repository, Directory directory, boolean syncExistingAccount, Glob synchro) {
      Glob glob = repository.get(Key.create(Bank.TYPE, id));
      return new CreditAgricoleConnector(id,
                                         glob.get(Bank.URL),
                                         new GotoAutentifcation() {

                                           public boolean go(WebPage webPage, String code) throws WebCommandFailed, WebParsingError {
                                             WebPanel comptes = webPage.getPanelById("btnComptes");
                                             if (comptes.hasId("inputcomptes")) {
                                               comptes.getTextInputById("inputcomptes").setText(code);
                                               comptes.getAnchorWithRef("#").click();
                                               return true;
                                             }
                                             else {
                                               WebAnchor ref = webPage.getAnchorWithRef("javascript:bamv3_validation();");
                                               ref.click();
                                               return false;
                                             }
                                           }
                                         },
                                         syncExistingAccount, repository, directory, synchro);
    }
  }

  protected JPanel createPanel() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/userAndPasswordPanel.splits");

    codeField = new JTextField();
    builder.add("userCode", codeField);
    codeField.setText(getSyncCode());

    validerCode = new JButton("valider");
    builder.add("connectButton", validerCode);
    validerCode.addActionListener(new ValidateAction());
    validerCode.setEnabled(false);

    passwordTextField = new JPasswordField();
    builder.add("password", passwordTextField);

    directory.get(ExecutorService.class)
      .submit(new Runnable() {
        public void run() {
          try {
            notifyInitialConnection();
            loadPage(urlGrid);
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                validerCode.setEnabled(true);
              }
            });
            notifyWaitingForUser();
          }
          catch (Exception e) {
            notifyErrorFound(e);
          }
        }
      });

    return builder.load();
  }

  public void downloadFile() throws Exception {
    notifyDownloadInProgress();
    WebAnchor confirmer = browser.getCurrentPage().getAnchorWithRef("javascript:verifForm('Confirmer')");
    WebPage webPage = confirmer.click();
    WebAnchor open = webPage.getAnchor(WebFilters.refStartsWith("javascript:ouvreTelechargement"));
    Download download = open.clickAndDownload();
    String fileContent = download.readAsOfx();
    for (Glob realAccount : accounts) {
      repository.update(realAccount.getKey(), RealAccount.FILE_CONTENT, fileContent);
    }
  }

  public String getCode() {
    return codeField.getText();
  }

  public void panelShown() {
  }

  public void reset() {
  }

  private class ValidateAction extends AbstractAction {
    public void actionPerformed(ActionEvent e) {
      setEnabled(false);
      notifyIdentificationInProgress();
      directory.get(ExecutorService.class).submit(new Runnable() {
        public void run() {
          try {
            WebPage homePage = browser.getCurrentPage();
            boolean codeInMain = autentification.go(homePage, codeField.getText());
            final WebPanel indent;
            if (codeInMain) {
              indent = browser.retry(new Callable<WebPanel>() {
                public WebPanel call() throws Exception {
                  WebPage ident = browser.getCurrentPage().getGlobalFrameByName("frameIdent");
                  return ident.getPanelById("bloc-pave-saisis-code");
                }
              });
            }
            else {
              indent = browser.getCurrentPage().getPanelById("contenu");
              WebTextInput ccpte = indent.getTextInputByName("CCPTE");
              ccpte.setText(codeField.getText());
            }
            WebTable table = browser.retry(new Callable<WebTable>() {
              public WebTable call() throws Exception {
                return indent.getTableById("pave-saisie-code");
              }
            });
            String[][] items = table.getContentAsTextItems();
            for (char c : passwordTextField.getPassword()) {
              findAndClickInCell(table, items, "" + c);
            }
            if (codeInMain) {
              homePage = indent.getAnchorWithRef("javascript:Valid()").click();
              browser.waitForBackgroundJavaScript(5000);
              WebPanel liberror = homePage.findPanelById("liberror");
              if (liberror != null) {
                notifyInfo(liberror.asText());
                browser.waitForBackgroundJavaScript(10000);
                liberror = homePage.findPanelById("liberror");
                if (liberror != null && Strings.isNotEmpty(liberror.asText())){
                  notifyErrorFound(liberror.asText());
                  return;
                }
              }
            }
            else {
              indent.getAnchorWithRef("javascript:ValidCertif();").click();
            }

            homePage = browser.getCurrentPage();

            final WebPage webPage = homePage.executeJavascript("javascript:doAction('Telechargement','bnt')");
            WebForm downloadForm = browser.retry(new Callable<WebForm>() {
              public WebForm call() throws Exception {
                return webPage.getFormByName("frm_fwk");
              }
            });
            WebFilter checkboxFilter = WebFilters.and(WebFilters.tagEquals(HtmlInput.TAG_NAME),
                                                      WebFilters.typeEquals("CHECKBOX"));
            WebTable accountList = downloadForm.getTableContaining(checkboxFilter);
            List<WebTableRow> rows = accountList.getAllRows();
            for (WebTableRow row : rows) {
              WebCheckBox checkBox = null;
              for (WebTableCell cell : row.getCells()) {
                checkBox = cell.findFirst(checkboxFilter).asCheckBox();
                if (checkBox != null) {
                  checkBox.setChecked(true);
                  break;
                }
              }
              if (checkBox != null) {
                int i = 0;
                String accountName = null;
                String accountNumber = null;
                for (WebTableCell cell : row.getCells()) {
                  if (i == 1) {
                    accountName = cell.asText();
                  }
                  if (i == 3) {
                    accountNumber = cell.asText();
                  }
                  ++i;
                }
                notifyPreparingAccount(accountName);
                createOrUpdateRealAccount(accountName, accountNumber, null, null, bankId);
              }
            }

            WebPage currentPage = browser.getCurrentPage();
            currentPage.getSelectById("TEL1_TYPE").select("OFX-Quicken");
            currentPage.getInputByValue("INTERVAL").select();

            doImport();
          }
          catch (WebParsingError error) {
            notifyErrorFound(error);
          }
          catch (WebCommandFailed failed) {
            notifyErrorFound(failed);
          }
          catch (Exception e) {
            notifyErrorFound(e);
          }
        }
      });
    }

    private void findAndClickInCell(WebTable table, String[][] items, String c) throws WebCommandFailed {
      for (int i1 = 0; i1 < items.length; i1++) {
        String[] item = items[i1];
        for (int i2 = 0; i2 < item.length; i2++) {
          String s = item[i2];
          if (Strings.isNotEmpty(s) && s.trim().contains(c)) {
            table.getRow(i1).getCell(i2).click();
            return;
          }
        }
      }
      StringBuilder builder = new StringBuilder();
      for (String[] item : items) {
        builder.append(Arrays.toString(item));
      }
      notifyErrorFound("Cannot find password got " + builder.toString());
    }
  }
}
