package com.budgetview.bank.connectors.banquepopulaire;

import com.budgetview.bank.BankConnector;
import com.budgetview.bank.BankConnectorFactory;
import com.budgetview.bank.connectors.WebBankConnector;
import com.budgetview.bank.connectors.webcomponents.*;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.budgetview.bank.connectors.webcomponents.filters.WebFilters;
import com.budgetview.bank.connectors.webcomponents.utils.HttpConnectionProvider;
import com.budgetview.bank.connectors.webcomponents.utils.WebConnectorLauncher;
import com.budgetview.bank.connectors.webcomponents.utils.WebParsingError;
import com.budgetview.model.RealAccount;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class BanquePopulaireConnector extends WebBankConnector implements HttpConnectionProvider {
  public static final int BANK_ID = 12;
  public static final String INDEX = "https://www.ibps.bpbfc.banquepopulaire.fr";
  private JTextField codeField;
  private JButton validerCode;
  private JPasswordField passwordTextField;
  private String url;
  private List<WebTable> tables;
  private WebButton downloadButton;

  public static void main(String[] args) throws IOException {
    WebConnectorLauncher.show(12, new BanquePopulaireConnectorFactory(12));
  }


  public BanquePopulaireConnector(boolean syncExistingAccount, GlobRepository repository,
                                  Directory directory, Glob synchro, String url) {
    super(BANK_ID, syncExistingAccount, repository, directory, synchro);
    this.url = url;
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
            loadPage(url);
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
    String date = shiftDateddMMyyy(-3, 0);
    for (WebTable table : tables) {
      setDateAndSelect(date, table);
    }
    Download download = downloadButton.clickAndDownload();
    String s = download.readAsOfx();
    for (Glob account : accounts) {
      updateAccount(s, account);
    }
  }

  private void updateAccount(String s, Glob account) {
    for (WebTable table : tables) {
      for (WebTableRow row : table.getRowsWithoutHeaderAndFooters()) {
        if (row.getCell(0).asText().equals(account.get(RealAccount.NUMBER))) {
          repository.update(account.getKey(), RealAccount.FILE_CONTENT, s);
          return;
        }
      }
    }
  }

  private void setDateAndSelect(String date, WebTable table) throws WebParsingError {
    List<WebTableRow> rows = table.getRowsWithoutHeaderAndFooters();
    for (WebTableRow row : rows) {
      row.getCell(4).getTextInput().setText(date);
      row.getCell(6).getCheckBox().setChecked(true);
    }
  }

  public String getCode() {
    return codeField.getText();
  }

  public void panelShown() {
    if (Strings.isNullOrEmpty(codeField.getText())) {
      codeField.requestFocus();
    }
    else {
      passwordTextField.requestFocus();
    }
  }

  public void reset() {
  }

  public HttpWebConnection getHttpConnection(WebClient client) {
    return new HttpWebConnection(client) {
      public WebResponse getResponse(WebRequest request) throws IOException {
        String s = request.getUrl().toString();
        System.out.println("SgConnector.getResponse " + s);
        WebResponse response = super.getResponse(request);
        System.out.println("SgConnector.getResponse " + response.getLoadTime() + " ms.");
//        System.out.println("BanquePopulaireConnector.getResponse : " + response.getContentAsString());
        return response;
      }
    };
  }

  private static class BanquePopulaireConnectorFactory implements BankConnectorFactory {
    public BanquePopulaireConnectorFactory(int id) {
    }

    public BankConnector create(GlobRepository repository, Directory directory, boolean syncExistingAccount, Glob synchro) {
      return new BanquePopulaireConnector(syncExistingAccount, repository, directory, synchro, INDEX);
    }
  }

  private class ValidateAction extends AbstractAction {
    public void actionPerformed(ActionEvent event) {
      notifyIdentificationInProgress();
      directory.get(ExecutorService.class).submit(new Runnable() {
        public void run() {
          try {
            WebPage homePage = browser.getCurrentPage();
            if (homePage.getTitle().contains("prévision indisponibilité")) {
              homePage = homePage.getAnchor(WebFilters.textContentContains("Continuer")).click();
            }
            WebTextInput identifiant = homePage.getTextInputById("IDToken1");
            identifiant.setText(codeField.getText());
            WebPasswordInput password = homePage.getPasswordInputById("IDToken2");
            password.setText(passwordTextField.getText());
            WebInput btn = homePage.getInputById("loginBtn");
            WebPage webPage = btn.click();
            webPage = webPage.getAnchor(WebFilters.textContentContains("Accéder à la liste des comptes")).click();

            // bug htmlunit 2.10 fixé en 2.12 ==> les sub frame ne sont pas loadée

            WebPage controlPanel = findPage("controlPanel");
            controlPanel.getListItemById("TtelechargementOp")
              .navigate().in().asAnchor().click();
            WebPage panel = findPage("applicationPanel");
            WebForm form = panel.getFormById("myForm");
            HtmlButton suivant = form
              .getFirst(WebFilters.and(WebFilters.tagEquals(HtmlButton.TAG_NAME),
                                       WebFilters.attributeEquals("title", "Accèder à la sélection des comptes à télécharger.")))
              .asButton().getNode();
            WebRequest request = form.getNode().getWebRequest(suivant);
            List<NameValuePair> parameters = request.getRequestParameters();
            for (Iterator<NameValuePair> iterator = parameters.iterator(); iterator.hasNext(); ) {
              NameValuePair parameter = iterator.next();
              if (parameter.getName().equalsIgnoreCase("dialogActionPerformed")) {
                iterator.remove();
                parameters.add(new NameValuePair("dialogActionPerformed", "SUIVANT"));
                break;
              }
            }
            browser.getClient().getPage(panel.getPage().getEnclosingWindow(), request);
            panel = findPage("applicationPanel");
            WebForm downloadPage = panel.getFormById("myForm");
            tables = downloadPage.findAll(WebFilters.tagEquals(HtmlTable.TAG_NAME)).asTables();
            for (WebTable table : tables) {
              List<WebTableRow> rows = table.getRowsWithoutHeaderAndFooters();
              for (WebTableRow row : rows) {
                BanquePopulaireConnector.this
                  .createOrUpdateRealAccount(row.getCell(1).asText() + row.getCell(2).asText(),
                                             row.getCell(0).asText(), row.getCell(3).asText(), new Date(), bankId);
              }
            }
            downloadButton = panel.getButtonById("btn1");
            doImport();
          }
          catch (Exception e) {
            notifyErrorFound(e);
          }
        }

        private WebPage findPage(String name) {
          List<WebWindow> windows = browser.getClient().getWebWindows();
          for (WebWindow window : windows) {
            if (window.getName().equalsIgnoreCase(name))
              return new WebPage(browser, (HtmlPage)window.getEnclosedPage());
          }
          return null;
        }
      });
    }
  }
}
