package org.designup.picsou.bank.connectors.cassedepargne;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.designup.picsou.bank.BankConnector;
import org.designup.picsou.bank.BankConnectorFactory;
import org.designup.picsou.bank.connectors.WebBankConnector;
import org.designup.picsou.bank.connectors.webcomponents.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.HttpConnectionProvider;
import org.designup.picsou.bank.connectors.webcomponents.utils.UserAndPasswordPanel;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebConnectorLauncher;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;
import org.designup.picsou.model.RealAccount;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class CaisseDEpargne extends WebBankConnector implements HttpConnectionProvider {
  public static final int BANK_ID = 6;
  public static String INDEX = "https://www.caisse-epargne.fr/particuliers/ind_pauthpopup.aspx?srcurl=accueil";
  private UserAndPasswordPanel userAndPasswordPanel;


  public CaisseDEpargne(boolean syncExistingAccount, GlobRepository repository, Directory directory, Glob synchro) {
    super(BANK_ID, syncExistingAccount, repository, directory, synchro);
    setBrowserVersion(BrowserVersion.FIREFOX_10);
  }

  public static void main(String[] args) throws IOException {
    BasicConfigurator.configure(new NullAppender());
    Logger.getRootLogger().setLevel(Level.ERROR);
    WebConnectorLauncher.show(BANK_ID, new Factory());
  }

  public static class Factory implements BankConnectorFactory {
    public BankConnector create(GlobRepository repository, Directory directory, boolean syncExistingAccount, Glob synchro) {
      return new CaisseDEpargne(syncExistingAccount, repository, directory, synchro);
    }
  }


  protected JPanel createPanel() {
    userAndPasswordPanel = new UserAndPasswordPanel(new ConnectAction(), directory);
    directory.get(ExecutorService.class)
      .submit(new Runnable() {
        public void run() {
          try {
            notifyInitialConnection();
            loadPage(INDEX);
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                userAndPasswordPanel.setEnabled(true);
              }
            });
          }
          catch (Exception e) {
            notifyErrorFound(e);
          }
          finally {
            notifyWaitingForUser();
          }
        }
      });
    return userAndPasswordPanel.getPanel();
  }

  public void downloadFile() throws Exception {
    notifyDownloadInProgress();
    WebPage currentPage = browser.getCurrentPage();
    WebSelect comptes = currentPage.getSelectById("MM_TELECHARGE_OPERATIONS_m_ExDDLListeComptes");
    List<String> names = comptes.getEntryNames();

    int retry = 0;
    int count = 0;
    for (Glob account : accounts) {
      String element = getName(names, account);
      if (element != null) {

        try {
          notifyDownloadForAccount((++count) + " : " + element);
          comptes = currentPage.getSelectById("MM_TELECHARGE_OPERATIONS_m_ExDDLListeComptes");
          comptes.select(element);
          browser.waitForBackgroundJavaScript(1000);
          Date from = getDate(0, -60);
          currentPage.getTextInputById("MM_TELECHARGE_OPERATIONS_m_DateDebut_JJ")
            .setText(extractDay(from));
          currentPage.getTextInputById("MM_TELECHARGE_OPERATIONS_m_DateDebut_MM")
            .setText(extractMonth(from));
          currentPage.getTextInputById("MM_TELECHARGE_OPERATIONS_m_DateDebut_AA")
            .setText(extractYear(from));

          Date to = getDate(0, -1);
          currentPage.getTextInputById("MM_TELECHARGE_OPERATIONS_m_DateFin_JJ")
            .setText(extractDay(to));
          currentPage.getTextInputById("MM_TELECHARGE_OPERATIONS_m_DateFin_MM")
            .setText(extractMonth(to));
          currentPage.getTextInputById("MM_TELECHARGE_OPERATIONS_m_DateFin_AA")
            .setText(extractYear(to));
          currentPage.getAnchorById("MM_TELECHARGE_OPERATIONS_m_ChoiceBar_lnkRight")
            .click();
          browser.waitForBackgroundJavaScript(1000);
          currentPage = browser.setToTopLevelWindow();
          Download download = currentPage.getAnchor(WebContainer.and(
            WebContainer.filterAttribute("class", "btnTelecharger"),
            WebContainer.filterAttribute("title", "Télécharger")))
            .clickAndDownload();
          String s = download.readAsOfx();
          repository.update(account.getKey(), RealAccount.FILE_CONTENT, s);

          browser.waitForBackgroundJavaScript(1000);
          currentPage.getAnchorById("MM_TELECHARGE_OPERATIONS_AR_m_ChoiceBar_lnkRight").click();
          browser.waitForBackgroundJavaScript(1000);
          currentPage = browser.setToTopLevelWindow();
        }
        catch (WebParsingError error) {
          if (++retry < 3) {
            Log.write("Caisse d'epargne : retry ");
            WebPanel cptdmte0 = browser.getCurrentPage().getPanelById("CPTDMTE0");
            cptdmte0.getAnchor(null).click();
            browser.waitForBackgroundJavaScript(3000);
            currentPage = browser.setToTopLevelWindow();
          }
          else {
            throw error;
          }
        }
      }
    }
  }

  private String getName(List<String> names, Glob account) {
    for (Iterator<String> iterator = names.iterator(); iterator.hasNext(); ) {
      String name = iterator.next();
      if (name.contains(" - ")) {
        if (name.contains(account.get(RealAccount.NUMBER)) && name.contains(account.get(RealAccount.NAME))) {
          iterator.remove();
          return name;
        }
      }
      else {
        if (name.contains(account.get(RealAccount.NAME))) {
          iterator.remove();
          return name;
        }
      }
    }
    return null;
  }

  public String getCode() {
    return userAndPasswordPanel.getUser();
  }

  public void panelShown() {
    userAndPasswordPanel.requestFocus();
  }

  public void reset() {
  }


  class ConnectAction extends AbstractAction {

    public void actionPerformed(ActionEvent e) {
      notifyIdentificationInProgress();
      userAndPasswordPanel.setEnabled(false);
      userAndPasswordPanel.setFieldsEnabled(false);
      directory.get(ExecutorService.class).submit(new Runnable() {
        public void run() {
          try {
//            System.out.println("CaisseDEpargne$ConnectAction.run debut");
//            WebPanel accesComptes = browser.getCurrentPage().getPanelById("btn_acces_comptes");
//            WebAnchor cptsLink = accesComptes.getSingleAnchor();
//            cptsLink.click()
//            String onclick = cptsLink.getOnclick();
//            System.out.println("CaisseDEpargne$ConnectAction.run " + onclick);
//            WebPage autentificationPage = cptsLink.click();
//            browser.waitForBackgroundJavaScript(5000);
//            browser.getCurrentPage().executeJavascript(onclick);
            WebPage autentificationPage = browser.getCurrentPage();
            WebPanel auth_content = autentificationPage.getPanelById("auth-content");
            WebTextInput webInput = auth_content.findFirst(WebContainer.filterTag(HtmlInput.TAG_NAME)).asTextInput();
            webInput.setText(userAndPasswordPanel.getUser());
            WebAnchor valider = auth_content.findFirst(WebContainer.and(WebContainer.filterTag(HtmlImage.TAG_NAME),
                                                                        WebContainer.filterAttribute("title", "Valider")))
              .parent().asAnchor();
//            System.out.println("CaisseDEpargne$ConnectAction.run valider");
            valider.click();
            auth_content = browser.getCurrentPage().getPanelById("auth-content");
            WebPanel clavierparticulier = browser.getCurrentPage().getPanelById("clavierparticulier");
            WebComponent.HtmlNavigates all = clavierparticulier.findAll(WebContainer.filterTag(HtmlAnchor.TAG_NAME));
            List<WebAnchor> anchors = all.asAnchor();
            for (int i = 0; i < userAndPasswordPanel.getPassword().length(); i++) {
              char value = userAndPasswordPanel.getPassword().charAt(i);
              for (WebAnchor anchor : anchors) {
                String tabindex = anchor.getNode().getAttribute("tabindex");
                if (Strings.isNotEmpty(tabindex) && tabindex.charAt(0) == value) {
                  anchor.click();
                  break;
                }
              }
            }

            auth_content.findFirst(WebContainer.and(WebContainer.filterTag(HtmlImage.TAG_NAME),
                                                    WebContainer.filterAttribute("title", "Valider")))
              .parent().asAnchor()
              .click();

            browser.waitForBackgroundJavaScript(2000);
            WebPanel cptdmte0 = browser.getCurrentPage().getPanelById("CPTDMTE0");
            WebPage currentPage = cptdmte0.findFirst(WebContainer.filterTag(HtmlAnchor.TAG_NAME))
              .asAnchor()
              .click();
            currentPage = browser.setToTopLevelWindow();
//            currentPage = browser.getCurrentPage();
            WebSelect comptes = currentPage.getSelectById("MM_TELECHARGE_OPERATIONS_m_ExDDLListeComptes");
            List<String> accounts = comptes.getEntryNames();
            for (String account : accounts) {
              int i = account.indexOf(" - ");
              String number = account;
              String name = account;
              if (i > 0) {
                number = account.substring(0, i);
                name = account.substring(i + 3, account.length());
              }
              createOrUpdateRealAccount(name, number, null, null, BANK_ID);
            }
            doImport();
          }
          catch (Exception e) {
            System.out.println(browser.getCurrentPage().asXml());
            notifyErrorFound(e);
          }
        }
      });
    }
  }


  public HttpWebConnection getHttpConnection(WebClient client) {
    return new HttpWebConnection(client) {
      public WebResponse getResponse(WebRequest request) throws IOException {
        String s = request.getUrl().toString();
        if (s.startsWith("https://logs2.xiti.com")) {
          return new StringWebResponse("", request.getUrl());
        }
//        System.out.println("Request : " + s);
        WebResponse response = super.getResponse(request);
        return response;
      }
    };
  }

  static String extractDay(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
    return dateFormat.format(date);
  }

  static String extractMonth(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("MM");
    return dateFormat.format(date);
  }

  static String extractYear(Date date) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yy");
    return dateFormat.format(date);
  }

  private Date getDate(final int monthBack, final int dayback) {
    Date today = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(today);
    calendar.add(Calendar.MONTH, monthBack);
    calendar.add(Calendar.DAY_OF_MONTH, dayback);
    return calendar.getTime();
  }

}
