package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import org.designup.picsou.bank.connectors.webcomponents.utils.HttpConnectionProvider;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.Callable;

public class WebBrowser {
  public WebClient webClient;
  private HtmlPage currentPage;
  private AlertHandler errorAlertHandler;
  private HttpConnectionProvider httpConnectionProvider;
  private boolean javascriptEnabled = true;
  private BrowserVersion browserVersion = BrowserVersion.FIREFOX_10;

  public WebBrowser(AlertHandler errorAlertHandler) {
    this.errorAlertHandler = errorAlertHandler;
    this.httpConnectionProvider = new HttpConnectionProvider() {
      public HttpWebConnection getHttpConnection(WebClient client) {
        return new HttpWebConnection(client);
      }
    };
  }

  public void setBrowserVersion(BrowserVersion browserVersion) {
    this.browserVersion = browserVersion;
  }

  public void setJavascriptEnabled(boolean enabled) {
    this.javascriptEnabled = enabled;
  }

  public void setHttpConnectionProvider(HttpConnectionProvider httpConnectionProvider) {
    this.httpConnectionProvider = httpConnectionProvider;
  }

  private void createWebClient() {
    webClient = new WebClient(browserVersion);
    webClient.setThrowExceptionOnScriptError(false);
    webClient.setCssEnabled(false);
    webClient.setJavaScriptEnabled(javascriptEnabled);
    webClient.setCache(new Cache());
    webClient.setWebConnection(httpConnectionProvider.getHttpConnection(webClient));
    webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    webClient.setAlertHandler(errorAlertHandler);
  }

  public void setTimeout(int timeout) {
    getClient().setTimeout(timeout);
  }

  public WebPage load(String url) throws WebCommandFailed {
    try {
      currentPage = getClient().getPage(url);
      return new WebPage(this, currentPage);
    }
    catch (IOException e) {
      throw new WebCommandFailed(e);
    }
  }

  public WebPage loadPageInSameSite(String path) throws WebParsingError, WebCommandFailed {
    try {
      return load(currentPage.getFullyQualifiedUrl(path).toString());
    }
    catch (MalformedURLException e) {
      throw new WebParsingError(getUrl(), e);
    }
  }

  public WebPage setCurrentPage(Page page) {
    if (page instanceof UnexpectedPage){
      return getCurrentPage();
    }
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
    if (currentPage == null) {
      return "[none]";
    }
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

  public <T extends HtmlElement> T getElementById(final String id) throws WebParsingError {
    T select = (T)currentPage.getElementById(id);
    if (select == null) {
      throw new WebParsingError(getUrl(), "Can not find tag '" + id + "' in :\n" + currentPage.asXml());
    }
    return select;
  }

  public String dumpCurrentPage() {
    return currentPage.asXml();
  }

  public void stop() {
    webClient.closeAllWindows();
  }

  public String downloadToString(String url, String extension) throws WebCommandFailed {
    try {
      WebRequest request = new WebRequest(UrlUtils.toUrlUnsafe(url));
      WebResponse response = new HttpWebConnection(webClient).getResponse(request);
      return Download.readResponse(response, extension, null, "Unable to save " + url);
    }
    catch (IOException e) {
      throw new WebCommandFailed(e, "Cannot download file");
    }
  }

  public void waitForBackgroundJavaScript(int timeout) {
    webClient.waitForBackgroundJavaScript(timeout);
  }

  WebPage doClick(HtmlElement element) throws WebCommandFailed {
    try {
      return setCurrentPage(element.click());
    }
    catch (IOException e) {
      throw new WebCommandFailed(e);
    }
  }

  public WebPage setToTopLevelWindow(){
    webClient.setCurrentWindow(webClient.getTopLevelWindows().get(0));
    return updateCurrentPage();
  }

  public WebPage updateCurrentPage() {
    Page page = webClient.getCurrentWindow().getEnclosedPage();
    if (page instanceof HtmlPage){
      if (page != currentPage){
        currentPage = (HtmlPage)page;
        return new WebPage(this, (HtmlPage)page);
      }
    }
    return new WebPage(this, currentPage);
  }


  public <T> T retry(Callable<T> callable) {
    long timeOut = System.currentTimeMillis() + 10000;
    while (true){
      try {
        return callable.call();
      }
      catch (Exception e) {
        if (System.currentTimeMillis() > timeOut) {
          if (e instanceof RuntimeException){
            throw ((RuntimeException)e);
          }
          else {
            throw new RuntimeException("on ", e);
          }
        }
        waitForBackgroundJavaScript(500);
      }
    }
  }
}
