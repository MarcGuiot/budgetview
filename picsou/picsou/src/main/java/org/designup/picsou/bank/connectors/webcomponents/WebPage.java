package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class WebPage extends WebContainer<HtmlElement> {
  public WebPage(WebBrowser browser, HtmlPage page) {
    super(browser, page.getDocumentElement());
  }
}
