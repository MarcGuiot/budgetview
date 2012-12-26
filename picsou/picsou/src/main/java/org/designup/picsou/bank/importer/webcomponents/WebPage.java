package org.designup.picsou.bank.importer.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.designup.picsou.bank.importer.webcomponents.utils.HtmlUnit;

public class WebPage extends WebPanel {
  public WebPage(WebBrowser browser, HtmlPage page) {
    super(browser, page.getDocumentElement());
  }

  private HtmlElement getElement(String id) {
    return HtmlUnit.getElementById(browser.getCurrentHtmlPage(), id, HtmlElement.class);
  }
}
