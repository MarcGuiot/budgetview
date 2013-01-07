package org.designup.picsou.bank.connectors.creditmutuel;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.*;
import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.connectors.WebBankConnector;
import org.designup.picsou.bank.connectors.webcomponents.WebForm;
import org.designup.picsou.bank.connectors.webcomponents.WebPage;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebConnectorLauncher;
import org.designup.picsou.model.RealAccount;
import org.globsframework.gui.splits.SplitsBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class CreditMutuelArkeaConnector extends WebBankConnector {

  public static final int BANK_ID = 15;

  private String INDEX = "https://www.cmso.com/creditmutuel/cmso/index.jsp?fede=cmso";
  private JTextField codeField;
  private JButton validerCode;
  private JPasswordField passwordTextField;
  private HtmlTable accountsTable;

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory) {
      return new CreditMutuelArkeaConnector(directory, repository);
    }
  }

  private CreditMutuelArkeaConnector(Directory directory, GlobRepository repository) {
    super(BANK_ID, repository, directory);
  }

  protected JPanel createPanel() {
    SplitsBuilder builder = SplitsBuilder.init(directory);
    builder.setSource(getClass(), "/layout/bank/connection/userAndPasswordPanel.splits");

    codeField = new JTextField();
    builder.add("code", codeField);

    validerCode = new JButton("valider");
    builder.add("validerCode", validerCode);
    validerCode.addActionListener(new ValiderActionListener());

    passwordTextField = new JPasswordField();
    builder.add("password", passwordTextField);

    Thread thread = new Thread() {
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
    };
    thread.start();

    return builder.load();
  }

  public void panelShown() {
    codeField.requestFocus();
  }

  public void reset() {
    // TODO: remettre en etat apres exception
  }

  public void downloadFile() throws Exception {
    WebPage web = new WebPage(browser, browser.getCurrentHtmlPage());
    WebForm webForm = web.getFormByName("choixCompte");
    for (Glob glob : this.accounts) {
      int count = accountsTable.getRowCount();
      for (int i = 1; i < count; i++) {
        if (accountsTable.getCellAt(i, 1).getTextContent().contains(glob.get(RealAccount.NAME))) {
          try {
            List<HtmlElement> elementList = accountsTable.getCellAt(i, 0).getHtmlElementsByTagName(HtmlInput.TAG_NAME);
            if (elementList.size() == 1) {
              elementList.get(0).click();
            }
          }
          catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
    webForm.getAnchorWithImage("valider.gif").click();
    WebForm patametersWeb = web.getFormByName("parametresForm");
    patametersWeb.getInputByValue("2").select();
    web.getAnchorWithImage("telecharger.gif").click();
    DomNodeList<DomElement> tables = (DomNodeList)page.getElementsByTagName(HtmlTable.TAG_NAME);
    HtmlTable table = (HtmlTable)tables.get(0);
    int count = table.getRowCount();
    for (int i = 1; i < count; i++) {
      HtmlTableCell at = table.getCellAt(i, 0);
      List<HtmlElement> htmlElements = at.getHtmlElementsByTagName(HtmlAnchor.TAG_NAME);
      if (!htmlElements.isEmpty()) {
        for (Glob glob : accounts) {
          HtmlElement link = htmlElements.get(0);
          if (link.getTextContent().contains(glob.get(RealAccount.NAME))) {
            File file = downloadQifFile(glob, link);
            repository.update(glob.getKey(), RealAccount.FILE_NAME, file.getAbsolutePath());
            break;
          }
        }
      }
    }
  }

  protected File downloadQifFile(Glob realAccount, HtmlElement anchor) {
    try {
      Page page1 = anchor.click();
      TextPage page = (TextPage)page1;
      WebResponse response = page.getWebResponse();
      InputStream contentAsStream = response.getContentAsStream();
      return createQifLocalFile(realAccount, contentAsStream, response.getContentCharset());
    }
    catch (IOException e) {
      Log.write("In anchor click", e);
      return null;
    }
  }

  private class ValiderActionListener implements ActionListener {

    public void actionPerformed(ActionEvent event) {
      try {
        notifyIdentificationInProgress();
        HtmlForm form = page.getFormByName("formIdentification");
        HtmlInput personne = form.getInputByName("noPersonne");
        HtmlInput password = form.getInputByName("motDePasse");
        personne.setValueAttribute(codeField.getText());
        password.setValueAttribute(new String(passwordTextField.getPassword()));
        HtmlElement element = getAnchor(form);
        page = element.click();
        getClient().waitForBackgroundJavaScript(10000);
        HtmlElement elementById = getElementById("quotidien");
        getAnchor(elementById).click();
        WebPage webPage = new WebPage(browser, page);
        webPage.getFirstAnchorWithText("telechargement").click();

        HtmlElement comptes = webPage.getElementByName("div", "choixCompte");
        accountsTable = (HtmlTable)comptes.getElementsByTagName(HtmlTable.TAG_NAME).get(1);
        int count = accountsTable.getRowCount();
        for (int i = 1; i < count; i++) {
          HtmlTableCell name = accountsTable.getCellAt(i, 1);
          HtmlTableCell position = accountsTable.getCellAt(i, 2);
          createOrUpdateRealAccount(name.getTextContent(), "", position.getTextContent(), null, BANK_ID);
        }
        doImport();
      }
      catch (Exception e) {
        throw new RuntimeException(page.asXml(), e);
      }
      finally {
        notifyWaitingForUser();
      }
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
    WebConnectorLauncher.show(new Factory());
  }
}
