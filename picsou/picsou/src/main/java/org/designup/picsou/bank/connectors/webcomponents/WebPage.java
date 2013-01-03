package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class WebPage extends WebContainer<HtmlElement> {
  private final HtmlPage page;

  public WebPage(WebBrowser browser, HtmlPage page) {
    super(browser, page.getDocumentElement());
    this.page = page;
  }

  public String getTitleText() {
    return page.getTitleText();
  }
}
