package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlTableHeader;

public class WebTableHeader extends WebContainer<HtmlTableHeader> {
  public WebTableHeader(WebBrowser browser, HtmlTableHeader node) {
    super(browser, node);
  }

  public String getText() {
    return node.asXml();
  }
}
