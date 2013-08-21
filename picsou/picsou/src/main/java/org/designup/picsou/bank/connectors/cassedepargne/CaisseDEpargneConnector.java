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
import org.designup.picsou.bank.connectors.webcomponents.filters.WebFilters;
import org.designup.picsou.bank.connectors.webcomponents.utils.*;
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

public class CaisseDEpargneConnector extends WebBankConnector implements HttpConnectionProvider {
  public static final int BANK_ID = 6;
  public static String INDEX = "https://www.caisse-epargne.fr/particuliers/ind_pauthpopup.aspx?srcurl=accueil";
  private UserAndPasswordPanel userAndPasswordPanel;
  private int tempo = 0;

  public CaisseDEpargneConnector(boolean syncExistingAccount, GlobRepository repository, Directory directory, Glob synchro) {
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
      return new CaisseDEpargneConnector(syncExistingAccount, repository, directory, synchro);
    }
  }

  protected JPanel createPanel() {
    userAndPasswordPanel = new UserAndPasswordPanel(new ConnectAction(), directory);
    userAndPasswordPanel.createPanel(this);
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
    WebSelect comptes = browser.retry(currentPage, new WebBrowser.Function1Arg<WebSelect, WebPage>() {
      public WebSelect call(WebPage webPage) throws Exception {
        return webPage.getSelectById("MM_TELECHARGE_OPERATIONS_m_ExDDLListeComptes");
      }
    });
    List<String> names = comptes.getEntryNames();

    int count = 0;
    for (Glob account : accounts) {
      int retry = 0;
      count++;
      String element = getName(names, account);
      if (element != null) {
        while (true) {
          try {
            currentPage = tryDownload(currentPage, count, account, element);
            break;
          }
          catch (Exception e) {
            tempo += 1000;
            if (++retry < 3) {
              Log.write("Caisse d'epargne : retry ");
              WebPanel cptdmte0 = browser.getCurrentPage().getPanelById("NavExt");
              cptdmte0.findFirst(WebFilters.and(WebFilters.tagEquals(HtmlAnchor.TAG_NAME), WebFilters.textContentContains("Mes comptes")))
                .asAnchor()
                .click();
              currentPage.getAnchor(WebFilters.textContentContains("Télécharger des opérations"))
                .click();
              waitJavaScript(3000);
            }
            else {
              throw e;
            }
          }
        }
      }
    }
  }

  private void waitJavaScript(int timeout) {
    browser.waitForBackgroundJavaScript(timeout + tempo);
  }

  private WebPage tryDownload(WebPage currentPage, int count, Glob account, String element) throws WebParsingError, WebCommandFailed {
    notifyDownloadForAccount((count) + " : " + element);
    WebSelect comptes = browser.retry(currentPage, new WebBrowser.Function1Arg<WebSelect, WebPage>() {
      public WebSelect call(WebPage webPage) throws Exception {
        return webPage.getSelectById("MM_TELECHARGE_OPERATIONS_m_ExDDLListeComptes");
      }
    });
    comptes.select(element);
    waitJavaScript(1500);
    browser.retry(currentPage, new WebBrowser.Function1Arg<WebRadioButton, WebPage>() {
      public WebRadioButton call(WebPage webPage) throws Exception {
        return webPage.getRadioButtonById("MM_TELECHARGE_OPERATIONS_chkDate");
      }
    }).select();
    currentPage.executeJavascript("javascript:setTimeout('__doPostBack(\\'MM$TELECHARGE_OPERATIONS$chkDate\\',\\'\\')', 0)");
    waitJavaScript(3000);
    Date from = getDate(0, -120);
    WebTextInput debutTxtDate = browser.retry(currentPage, new WebBrowser.Function1Arg<WebTextInput, WebPage>() {
      public WebTextInput call(WebPage currentPage) throws Exception {
        return currentPage.getTextInputById("MM_TELECHARGE_OPERATIONS_m_DateDebut_txtDate");
      }
    });
    debutTxtDate
      .setText(extractDay(from));
    Date to = getDate(0, -1);
    currentPage.getTextInputById("MM_TELECHARGE_OPERATIONS_m_DateFin_txtDate")
      .setText(extractDay(to));

    waitJavaScript(500);

    currentPage.getSelectById("MM_TELECHARGE_OPERATIONS_ddlChoixLogiciel")
      .selectContain("ofx");
    waitJavaScript(1500);
    currentPage = browser.retry(currentPage, new WebBrowser.Function1Arg<WebAnchor, WebPage>() {
      public WebAnchor call(WebPage webPage) throws Exception {
        return webPage.getAnchorById("MM_TELECHARGE_OPERATIONS_m_ChoiceBar_lnkRight");
      }
    }).click();

    waitJavaScript(1500);

    Download download = null;
    try {
      download = browser.retry(currentPage, new WebBrowser.Function1Arg<WebAnchor,WebPage>() {
        public WebAnchor call(WebPage webPage) throws Exception {
          return webPage.getAnchorById("MM_TELECHARGE_OPERATIONS_AR_lnkTelechargement_lnk");
        }
      }).clickAndDownload();
    }
    catch (RuntimeException e) {
      try {
        WebSpan error = currentPage.getSpanById("MM_LblMessagePopinErro");
        notifyInfo(account.get(RealAccount.NAME) + " : " + error.asText());
        return currentPage;
      }
      catch (WebParsingError error1) {
        throw e;
      }
    }
    String s = download.readAsOfx();
    repository.update(account.getKey(), RealAccount.FILE_CONTENT, s);

    browser.retry(currentPage, new WebBrowser.Function1Arg<WebAnchor, WebPage>() {
      public WebAnchor call(WebPage webPage) throws Exception {
        return webPage.getAnchor(WebFilters.textContentContains("Télécharger des opérations"));
      }
    }).click();
    waitJavaScript(1500);
    return currentPage;
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
            WebPage autentificationPage = browser.getCurrentPage();
            WebPanel auth_content = autentificationPage.getPanelById("auth-content");
            WebTextInput webInput = auth_content.findFirst(WebFilters.tagEquals(HtmlInput.TAG_NAME)).asTextInput();
            webInput.setText(userAndPasswordPanel.getUser());
            WebAnchor valider = auth_content.findFirst(WebFilters.and(WebFilters.tagEquals(HtmlImage.TAG_NAME),
                                                                      WebFilters.attributeEquals("title", "Valider")))
              .parent().asAnchor();
            valider.click();
            auth_content = browser.getCurrentPage().getPanelById("auth-content");
            WebPanel clavierparticulier = browser.getCurrentPage().getPanelById("clavierparticulier");
            WebComponent.HtmlNavigates all = clavierparticulier.findAll(WebFilters.tagEquals(HtmlAnchor.TAG_NAME));
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

            auth_content.findFirst(WebFilters.and(WebFilters.tagEquals(HtmlImage.TAG_NAME),
                                                  WebFilters.attributeEquals("title", "Valider")))
              .parent().asAnchor()
              .click();

            waitJavaScript(2000);
            WebPanel cptdmte0 = browser.getCurrentPage().getPanelById("NavExt");
            cptdmte0.findFirst(WebFilters.and(WebFilters.tagEquals(HtmlAnchor.TAG_NAME), WebFilters.textContentContains("Mes comptes")))
              .asAnchor()
              .click();
            WebPage currentPage = browser.getCurrentPage();
//            browser.setToTopLevelWindow();
            currentPage.getAnchor(WebFilters.textContentContains("Télécharger des opérations")).click();
//            currentPage = browser.setToTopLevelWindow();
            WebSelect comptes = currentPage.getSelectById("MM_TELECHARGE_OPERATIONS_m_ExDDLListeComptes");
            List<String> accounts = comptes.getEntryNames();
            for (String account : accounts) {
              if (account.toLowerCase().contains(("Encours CB").toLowerCase())){
                continue;
              }
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
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
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
