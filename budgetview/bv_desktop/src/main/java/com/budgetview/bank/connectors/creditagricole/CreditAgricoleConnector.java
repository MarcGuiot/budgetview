package com.budgetview.bank.connectors.creditagricole;

import com.budgetview.bank.connectors.webcomponents.*;
import com.budgetview.bank.connectors.webcomponents.utils.HttpConnectionProvider;
import com.budgetview.bank.connectors.webcomponents.utils.WebCommandFailed;
import com.budgetview.bank.connectors.webcomponents.utils.WebConnectorLauncher;
import com.budgetview.bank.connectors.webcomponents.utils.WebParsingError;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.budgetview.bank.BankConnector;
import com.budgetview.bank.BankConnectorFactory;
import com.budgetview.bank.BankSynchroService;
import com.budgetview.bank.connectors.WebBankConnector;
import com.budgetview.bank.connectors.webcomponents.filters.WebFilter;
import com.budgetview.bank.connectors.webcomponents.filters.WebFilters;
import com.budgetview.model.Bank;
import com.budgetview.model.RealAccount;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
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
    WebConnectorLauncher.show(67, new CreditAgricoleFactory(67));
  }

  public CreditAgricoleConnector(int bankId, String url, GotoAutentifcation autentification,
                                 boolean syncExistingAccount, GlobRepository repository, Directory directory, Glob synchro) {
    super(bankId, syncExistingAccount, repository, directory, synchro);
    this.autentification = autentification;
    urlGrid = url;
  }

  boolean isFiltered(String path){
    if (path.endsWith("bloc_home_test/js/programmation.js")){
      return true;
    }
    return false;
  }

  public HttpWebConnection getHttpConnection(WebClient client) {
    return new HttpWebConnection(client) {
      public WebResponse getResponse(WebRequest request) throws IOException {
        URL url = request.getUrl();
        String path = url.getPath();
        WebResponse response = null;
        if (isFiltered(path)) {
          response = new StringWebResponse("", url);
        }
        else {
          response = super.getResponse(request);
        }

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
                                             if (webPage.hasId("btnComptes")) {
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
                                             else {
                                               webPage.getAnchorById("comptes").click();
                                               return false;
                                             }
                                           }
                                         },
                                         syncExistingAccount, repository, directory, synchro);
    }
  }

  protected JPanel createPanel() {
    final SplitsBuilder builder = SplitsBuilder.init(directory);
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
    addToBeDisposed(new Disposable() {
      public void dispose() {
        builder.dispose();
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
      localRepository.update(realAccount.getKey(), RealAccount.FILE_CONTENT, fileContent);
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
              browser.waitForBackgroundJavaScript(2000);
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
            WebInput interval = currentPage.findFirst(WebFilters.and(WebFilters.tagEquals(HtmlInput.TAG_NAME),
                                                                     WebFilters.typeEquals("radio"),
                                                                     WebFilters.attributeEquals("value", "INTERVAL"))).asInput();
            if (interval != null) {
              interval.select();
            }

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