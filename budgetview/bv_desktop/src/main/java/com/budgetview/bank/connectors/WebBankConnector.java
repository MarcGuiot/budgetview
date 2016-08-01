package com.budgetview.bank.connectors;

import com.budgetview.bank.connectors.webcomponents.WebBrowser;
import com.budgetview.bank.connectors.webcomponents.WebPage;
import com.budgetview.bank.connectors.webcomponents.utils.WebCommandFailed;
import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.budgetview.bank.connectors.webcomponents.utils.HttpConnectionProvider;
import com.budgetview.bank.connectors.webcomponents.utils.WebParsingError;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class WebBankConnector extends AbstractBankConnector {
  protected boolean hasError = false;
  protected WebBrowser browser;

  /**
   * @deprecated Replace with WebBrowser & co
   */
  protected HtmlPage page;

  public WebBankConnector(Integer bankId, boolean syncExistingAccount, GlobRepository repository, Directory directory, Glob synchro) {
    super(bankId, repository, directory, synchro);
    this.bankId = bankId;
    this.browser = new WebBrowser(new ErrorAlertHandler());
    if (this instanceof HttpConnectionProvider) {
      browser.setHttpConnectionProvider((HttpConnectionProvider)this);
    }
  }

  public void setBrowserVersion(BrowserVersion browserVersion) {
    browser.setBrowserVersion(browserVersion);
  }

  public String getCurrentLocation() {
    return browser.getUrl();
  }

  /**
   * @deprecated Replace with WebBrowser & co
   */
  protected WebClient getClient() {
    return browser.getClient();
  }

  protected WebPage loadPage(final String url) throws WebCommandFailed {
    WebPage webPage = browser.load(url);
    page = browser.getCurrentHtmlPage();
    return webPage;
  }

  protected <T extends HtmlElement> T getElementById(final String id) throws WebParsingError {
    return browser.<T>getElementById(id);
  }

  protected HtmlAnchor findLink(List<HtmlAnchor> anchors, String ref) {
    for (HtmlAnchor anchor : anchors) {
      if (anchor.getHrefAttribute().contains(ref)) {
        return anchor;
      }
    }
    throw new RuntimeException("Can not find ref '" + ref + "' in :\n" + page.asXml());
  }

  public void stop() {
    browser.stop();
  }

  public String shiftDateddMMyyy(int monthBack, int dayback) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    Date today = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(today);
    calendar.add(Calendar.MONTH, monthBack);
    calendar.add(Calendar.DAY_OF_MONTH, dayback);
    return dateFormat.format(calendar.getTime());
  }

  private class ErrorAlertHandler implements AlertHandler {
    public void handleAlert(Page page, String errorMessage) {
      hasError = true;
      notifyErrorFound(errorMessage);
    }
  }
}
