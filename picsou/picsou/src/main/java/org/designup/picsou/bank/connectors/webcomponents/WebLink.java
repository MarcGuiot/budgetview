package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

import java.io.IOException;
import java.net.MalformedURLException;

public class WebLink extends WebComponent<HtmlAnchor> {

  public WebLink(WebBrowser browser, HtmlAnchor anchor) {
    super(browser, anchor);
  }

  public String getTargetUrl() throws WebParsingError {
    try {
      return ((HtmlPage)node.getPage()).getFullyQualifiedUrl(node.getHrefAttribute()).toString();
    }
    catch (MalformedURLException e) {
      throw new WebParsingError(browser.getUrl(), e);
    }
  }

  public WebPage click() throws WebCommandFailed {
    try {
      return browser.setCurrentPage(node.click());
    }
    catch (IOException e) {
      throw new WebCommandFailed(e);
    }
  }

  public String toString() {
    return node.asText();
  }
}
