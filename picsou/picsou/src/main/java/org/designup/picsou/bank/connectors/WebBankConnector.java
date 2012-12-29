package org.designup.picsou.bank.connectors;

import com.gargoylesoftware.htmlunit.AlertHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.designup.picsou.bank.connectors.webcomponents.WebBrowser;
import org.designup.picsou.bank.connectors.webcomponents.utils.Download;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public abstract class WebBankConnector extends AbstractBankConnector {
  protected boolean hasError = false;
  protected WebBrowser browser;

  /**
   * @deprecated Replace with WebBrowser & co
   */
  protected HtmlPage page;

  public WebBankConnector(Integer bankId, GlobRepository repository, Directory directory) {
    super(directory, repository, bankId);
    this.bankId = bankId;
    this.browser = new WebBrowser(new ErrorAlertHandler());
  }

  /**
   * @deprecated Replace with WebBrowser & co
   */
  protected WebClient getClient() {
    return browser.getClient();
  }

  public static BufferedImage getFirstImage(HtmlImage img) {
    try {
      final ImageReader imageReader = img.getImageReader();
      return imageReader.read(0);
    }
    catch (IOException e) {
      throw new RuntimeException("Can not load image " + img.getId());
    }
  }

  protected void loadPage(final String url) throws IOException {
    browser.load(url);
    page = browser.getCurrentHtmlPage();
  }

  protected <T extends HtmlElement> T getElementById(final String id) {
    return browser.getElementById(id);
  }

  protected HtmlAnchor findLink(List<HtmlAnchor> anchors, String ref) {
    for (HtmlAnchor anchor : anchors) {
      if (anchor.getHrefAttribute().contains(ref)) {
        return anchor;
      }
    }
    throw new RuntimeException("Can not find ref '" + ref + "' in :\n" + page.asXml());
  }

  protected File downloadFile(Glob realAccount, HtmlElement anchor) {
    Download download = new Download(browser, anchor);
    return download.saveAsQif(realAccount);
  }

  public void stop() {
    browser.stop();
  }

  private class ErrorAlertHandler implements AlertHandler {
    public void handleAlert(Page page, String errorMessage) {
      hasError = true;
      notifyErrorFound(errorMessage);
    }
  }
}
