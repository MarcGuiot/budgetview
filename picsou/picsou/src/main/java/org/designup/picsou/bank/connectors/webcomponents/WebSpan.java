package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlSpan;

public class WebSpan extends WebComponent<HtmlSpan> {
  public WebSpan(WebBrowser browser, HtmlSpan node) {
    super(browser, node);
  }

  public String getText() {
    return node.asXml();
  }
}
