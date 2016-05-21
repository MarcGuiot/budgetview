package com.budgetview.bank.connectors.creditmutuel;

import com.budgetview.bank.BankConnector;
import com.budgetview.bank.BankConnectorFactory;
import com.budgetview.bank.connectors.WebBankConnector;
import com.budgetview.bank.connectors.webcomponents.*;
import com.budgetview.bank.connectors.webcomponents.filters.WebFilters;
import com.budgetview.bank.connectors.webcomponents.utils.HttpConnectionProvider;
import com.budgetview.bank.connectors.webcomponents.utils.WebCommandFailed;
import com.budgetview.bank.connectors.webcomponents.utils.WebConnectorLauncher;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.budgetview.bank.connectors.webcomponents.utils.WebParsingError;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class CreditMutuelArkeaConnector extends WebBankConnector implements HttpConnectionProvider {

  public static final int BANK_ID = 15;

  private String INDEX = "https://www.cmso.com/banque/assurance/credit-mutuel/web/j_6/accueil";
  private JTextField codeField;
  private JButton validerCode;
  private JPasswordField passwordTextField;
  private HtmlTable accountsTable;
  private List<WebAnchor> anchor;

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory, boolean syncExistingAccount, Glob synchro) {
      return new CreditMutuelArkeaConnector(syncExistingAccount, directory, repository, synchro);
    }
  }

  private CreditMutuelArkeaConnector(boolean syncExistingAccount, Directory directory, GlobRepository repository,
                                     final Glob synchro) {
    super(BANK_ID, syncExistingAccount, repository, directory, synchro);
    this.setBrowserVersion(BrowserVersion.FIREFOX_24);
  }

  public HttpWebConnection getHttpConnection(WebClient client) {
    return new HttpWebConnection(client) {
      public WebResponse getResponse(WebRequest request) throws IOException {
        String s = request.getUrl().toString();
        System.out.println("webRequest " + s);
        Map<String, String> headers = request.getAdditionalHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
          System.out.println("webRequest header : " + entry.getKey() + "<=>" + entry.getValue());
        }
        List<NameValuePair> parameters = request.getRequestParameters();
        for (NameValuePair parameter : parameters) {
          System.out.println("webRequest param : " + parameter.getName() + " ==> " + parameter.getValue());
        }
        String body = request.getRequestBody();
        System.out.println("webRequest body : " + body);
        WebResponse response = super.getResponse(request);
        System.out.println("webRequest response " + response.getLoadTime() + " ms.");
        return response;
      }
    };
  }

  protected JPanel createPanel() {
    final SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/userAndPasswordPanel.splits");

    codeField = new JTextField();
    builder.add("userCode", codeField);
    codeField.setText(getSyncCode());

    validerCode = new JButton("valider");
    validerCode.setEnabled(false);
    builder.add("connectButton", validerCode);
    validerCode.addActionListener(new ValiderActionListener());

    passwordTextField = new JPasswordField();
    builder.add("password", passwordTextField);

    directory.get(ExecutorService.class)
      .submit(new Runnable() {
        public void run() {
          try {
            loadPage(INDEX);
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                validerCode.setEnabled(true);
              }
            });
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
    // TODO: remettre en etat apres exception
  }

  public void downloadFile() throws Exception {

    for (WebAnchor webAnchor : anchor) {
      Download download = webAnchor.clickAndDownload();
      String fileContent = download.readAsOfx();
      System.out.println("CreditMutuelArkeaConnector$ValiderActionListener.actionPerformed  : " + fileContent);
    }
  }

  private class ValiderActionListener implements ActionListener {

    public void actionPerformed(ActionEvent event) {
      try {
        notifyIdentificationInProgress();

        WebPage currentPage = browser.getCurrentPage();
        WebAnchor connectButton = currentPage.getPanelById("connexion-bouton")
          .navigate().in().asAnchor();
        currentPage = connectButton.click();
        browser.waitForBackgroundJavaScript(5000);

        WebTextInput identifiant = currentPage.getTextInputById("identifiant");
        WebPasswordInput password = currentPage.getPasswordInputById("password");

        identifiant.setText(codeField.getText());
        password.setText(new String(passwordTextField.getPassword()));

        // todo verifier que les champs sont bien remplies

        currentPage.findFirst(WebFilters.and(WebFilters.tagEquals(HtmlAnchor.TAG_NAME),
                                             WebFilters.textContentContains("CONNECTEZ-VOUS")))
          .asAnchor()
          .click();
        browser.waitForBackgroundJavaScript(10000);
        currentPage = browser.updateCurrentPage();

        tryClickDownload(currentPage);
        browser.waitForBackgroundJavaScript(5000);
        currentPage = browser.updateCurrentPage();

        WebPanel subPanel = null;
        WebSelect select = null;
        int count = 0;
        while (select == null && count < 10) {
          subPanel = currentPage.getPanelById("titrePageFonctionnelle");
          select = subPanel.findFirst(WebFilters.and(WebFilters.tagEquals(HtmlOption.TAG_NAME),
                                                     WebFilters.attributeEquals("value", "OFX")))
            .parent().asSelect();
          browser.waitForBackgroundJavaScript(1000);
          currentPage = browser.updateCurrentPage();
          count++;
        }
        if (select == null){
          throw new RuntimeException("download page not loaded ");
        }
        select.selectByValue("OFX");

        List<WebCheckBox> checkbox = subPanel.findAll(WebFilters.and(WebFilters.tagEquals(HtmlInput.TAG_NAME),
                                                                     WebFilters.typeEquals("checkbox")))
          .asCheckBox();
        boolean next = false;
        for (WebCheckBox box : checkbox) {
          if (next){
            box.setChecked(true);
          }
          next = true;
        }

        browser.waitForBackgroundJavaScript(1000);
        currentPage = browser.updateCurrentPage();
        subPanel = currentPage.getPanelById("titrePageFonctionnelle");


        WebAnchor rechercher = subPanel.findFirst(WebFilters.and(WebFilters.tagEquals(HtmlAnchor.TAG_NAME),
                                                                 WebFilters.textContentContains("RECHERCHER")))
          .asAnchor();
        rechercher.click();
        browser.waitForBackgroundJavaScript(1000);
        currentPage = browser.updateCurrentPage();

        System.out.println("CreditMutuelArkeaConnector$ValiderActionListener.actionPerformed RECHERCHER " + browser.getCurrentPage().asXml());

        WebComponent.HtmlNavigate first;
        WebPanel subPanel2;
        count = 0;
        do {
          browser.waitForBackgroundJavaScript(1000);
          currentPage = browser.updateCurrentPage();
          subPanel2 = currentPage.getPanelById("titrePageFonctionnelle");
          first = subPanel2.findFirst(WebFilters.and(WebFilters.tagEquals(HtmlDivision.TAG_NAME),
                                                     WebFilters.textContentContains("Chargement en cours ...")));
          count++;
        }
        while (first.asPanel() != null && count < 10);

        count = 0;
        while (anchor == null && count < 10){
          anchor = subPanel2.findAll(WebFilters.and(WebFilters.tagEquals(HtmlAnchor.TAG_NAME),
                                                    WebFilters.textContentContains("Télécharger")))
            .asAnchor();
          count++;
          if (anchor == null){
            browser.waitForBackgroundJavaScript(1000);
          }
        }
        if (anchor == null) {
          notifyErrorFound("Impossible de trouver le bouton télécharger.");
          return;
        }
        String text;
        for (WebAnchor webAnchor : anchor) {
          text = webAnchor.navigate().parent(6).in().in().asPanel().getNode().asText();
          createOrUpdateRealAccount(getCode(), text, null, (Date)null, null);
        }

        doImport();
      }
      catch (Exception e) {
        System.out.println("CreditMutuelArkeaConnector$ValiderActionListener.actionPerformed " + page.asXml());
        throw new RuntimeException(page.asXml(), e);
      }
      finally {
        notifyWaitingForUser();
      }
    }

    private void tryClickDownload(WebPage currentPage) throws WebParsingError, WebCommandFailed {
      WebPanel menu = currentPage.findPanelById("menuPART");

      WebComponent.HtmlNavigate menuItem = findMenu(menu);
      menuItem.asAnchor().mouseOver();

      WebComponent.HtmlNavigate subMenuItem = findSubMenu(menu);
      subMenuItem.asAnchor().mouseOver();
      subMenuItem.asAnchor().click();
    }

    private WebComponent.HtmlNavigate findSubMenu(WebPanel menu) throws WebParsingError {
      return menu.findFirst(WebFilters.and(WebFilters.tagEquals(HtmlAnchor.TAG_NAME),
                                           WebFilters.textContentContains("Télécharger mes opérations")));
    }

    private WebComponent.HtmlNavigate findMenu(WebPanel menu) throws WebParsingError {
      WebComponent.HtmlNavigate first = menu.findFirst(WebFilters.and(WebFilters.tagEquals(HtmlAnchor.TAG_NAME),
                                                                      WebFilters.textContentContains("MES OUTILS DE GESTION")));
      return first;
    }

    private HtmlElement getAnchor(HtmlElement form) {
      DomNodeList<HtmlElement> htmlElements = form.getElementsByTagName(HtmlAnchor.TAG_NAME);
      if (htmlElements.isEmpty()) {
        throw new RuntimeException("missing a link");
      }
      return htmlElements.get(0);
    }
  }

  public static void main(String[] args) throws IOException {
    WebConnectorLauncher.show(BANK_ID, new Factory());
  }
}
