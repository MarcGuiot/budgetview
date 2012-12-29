package org.designup.picsou.bank.importer.webcomponents;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.designup.picsou.bank.importer.webcomponents.utils.HttpConnectionProvider;
import org.designup.picsou.bank.importer.webcomponents.utils.WebCommandFailed;

import java.io.IOException;

public class WebBrowser {

  public WebClient webClient;

  private HtmlPage currentPage;
  private AlertHandler errorAlertHandler;
  private HttpConnectionProvider httpConnectionProvider;

  public WebBrowser(AlertHandler errorAlertHandler) {
    this.errorAlertHandler = errorAlertHandler;
    this.httpConnectionProvider = new HttpConnectionProvider() {
      public HttpWebConnection getHttpConnection(WebClient client) {
        return new HttpWebConnection(client);
      }
    };
  }

  public void setHttpConnectionProvider(HttpConnectionProvider httpConnectionProvider) {
    this.httpConnectionProvider = httpConnectionProvider;
  }

  private void createWebClient() {
    webClient = new WebClient(BrowserVersion.FIREFOX_10);
    webClient.setThrowExceptionOnScriptError(false);
    webClient.setCssEnabled(false);
    webClient.setJavaScriptEnabled(true);
    webClient.setCache(new Cache());
    webClient.setWebConnection(httpConnectionProvider.getHttpConnection(webClient));
    webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    webClient.setAlertHandler(errorAlertHandler);
  }

  public WebPage load(String url) {
    try {
      currentPage = getClient().getPage(url);
      return new WebPage(this, currentPage);
    }
    catch (IOException e) {
      throw new WebCommandFailed(e);
    }
  }

  public WebPage setCurrentPage(Page page) {
    this.currentPage = (HtmlPage)page;
    return getCurrentPage();
  }

  /**
   * @deprecated @see #getCurrentWebPage
   */
  public HtmlPage getCurrentHtmlPage() {
    return currentPage;
  }

  public WebPage getCurrentPage() {
    return new WebPage(this, currentPage);
  }

  public String getUrl() {
    return currentPage.getUrl().toString();
  }

  /**
   * @deprecated To be made private
   */
  public WebClient getClient() {
    if (webClient == null) {
      createWebClient();
    }
    return webClient;
  }

  public <T extends HtmlElement> T getElementById(final String id) {
    T select = (T)currentPage.getElementById(id);
    if (select == null) {
      throw new RuntimeException("Can not find tag '" + id + "' in :\n" + currentPage.asXml());
    }
    return select;
  }

  public String dumpCurrentPage() {
    return currentPage.asXml();
  }
}
