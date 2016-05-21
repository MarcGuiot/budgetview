package com.budgetview.bank.connectors.webcomponents;

import com.budgetview.bank.connectors.webcomponents.utils.WebCommandFailed;
import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.UrlUtils;
import com.budgetview.bank.connectors.webcomponents.utils.HttpConnectionProvider;
import com.budgetview.bank.connectors.webcomponents.utils.WebParsingError;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.Callable;

public class WebBrowser {
  public WebClient webClient;
  private HtmlPage currentPage;
  private AlertHandler errorAlertHandler;
  private HttpConnectionProvider httpConnectionProvider;
  private boolean javascriptEnabled = true;
  private boolean cssEnabled  =false;
  private BrowserVersion browserVersion = BrowserVersion.FIREFOX_24;

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
    if (webClient != null) {
      webClient.getOptions().setJavaScriptEnabled(javascriptEnabled);
    }
  }

  public void setCssEnabled(boolean cssEnabled) {
    this.cssEnabled = cssEnabled;
  }

  public void setHttpConnectionProvider(HttpConnectionProvider httpConnectionProvider) {
    this.httpConnectionProvider = httpConnectionProvider;
  }

  private void createWebClient() {
    webClient = new WebClient(browserVersion);
    WebClientOptions options = webClient.getOptions();
    options.setThrowExceptionOnScriptError(false);
    options.setCssEnabled(cssEnabled);
    options.setJavaScriptEnabled(javascriptEnabled);
    webClient.setCache(new Cache());
    webClient.setWebConnection(httpConnectionProvider.getHttpConnection(webClient));
    webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    webClient.setAlertHandler(errorAlertHandler);
  }

  public void setTimeout(int timeout) {
    getClient().getOptions().setTimeout(timeout);
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

  public WebPage setCurrentPage(Page page) {
    if (page instanceof UnexpectedPage) {
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
      throw new WebParsingError(currentPage.getDocumentElement(), "Can not find tag '" + id + "'");
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

  public WebPage setToTopLevelWindow() {
    webClient.setCurrentWindow(webClient.getTopLevelWindows().get(0));
    return updateCurrentPage();
  }

  public WebPage updateCurrentPage() {
    Page page = webClient.getCurrentWindow().getEnclosedPage();
    if (page instanceof HtmlPage) {
      if (page != currentPage) {
        currentPage = (HtmlPage)page;
        return new WebPage(this, (HtmlPage)page);
      }
    }
    return new WebPage(this, currentPage);
  }

  public BufferedImage loadImage(String imageUrl) throws Exception {
    final WebClient webclient = currentPage.getWebClient();

    final URL url = currentPage.getFullyQualifiedUrl(imageUrl);
    final WebRequest request = new WebRequest(url);
    request.setAdditionalHeader("Referer", currentPage.getWebResponse().getWebRequest().getUrl().toExternalForm());
    WebResponse response = webclient.loadWebResponse(request);
    final ImageInputStream iis = ImageIO.createImageInputStream(response.getContentAsStream());
    final Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
    if (!iter.hasNext()) {
      throw new WebParsingError(currentPage.getDocumentElement(), "Failed to download image '" + imageUrl + "'");
    }
    ImageReader imageReader = iter.next();
    imageReader.setInput(iis);
    BufferedImage image = imageReader.read(0);
    iis.close();
    imageReader.dispose();
    return image;
  }

  public interface Function1Arg<T, D> {
    T call(D d) throws Exception;
  }

  public <T, D> T retry(final D d, final Function1Arg<T, D> callable){
    return retry(new Callable<T>() {
      public T call() throws Exception {
        return callable.call(d);
      }
    });
  }

  public <T> T retry(Callable<T> callable) {
    long timeOut = System.currentTimeMillis() + 10000;
    while (true) {
      try {
        return callable.call();
      }
      catch (Exception e) {
        if (System.currentTimeMillis() > timeOut) {
          if (e instanceof RuntimeException) {
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
