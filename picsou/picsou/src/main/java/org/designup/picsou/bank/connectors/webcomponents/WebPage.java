package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class WebPage extends WebContainer<HtmlElement> {
  private HtmlPage page;

  public WebPage(WebBrowser browser, HtmlPage page) {
    super(browser, page.getDocumentElement());
    this.page = page;
  }

  public WebPage executeJavascript(String script) {
    HtmlPage page = (HtmlPage)node.getPage();
    ScriptResult scriptResult = page.executeJavaScript(script);
    browser.setCurrentPage(scriptResult.getNewPage());
    return browser.getCurrentPage();
  }

  public String getTitle() {
    return page.getTitleText();
  }
}
