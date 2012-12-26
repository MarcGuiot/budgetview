package org.designup.picsou.bank.importer.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import org.designup.picsou.bank.importer.webcomponents.utils.WebCommandFailed;

import java.io.IOException;

public class WebLink extends WebComponent<HtmlAnchor> {

  public WebLink(WebBrowser browser, HtmlAnchor anchor) {
    super(browser, anchor);
  }

  public WebPage click() {
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
