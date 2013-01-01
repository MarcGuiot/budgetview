package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;

import java.io.IOException;

public class WebLink extends WebComponent<HtmlAnchor> {

  public WebLink(WebBrowser browser, HtmlAnchor anchor) {
    super(browser, anchor);
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
