package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ScriptResult;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

public class WebPage extends WebContainer<HtmlElement> {
  private final HtmlPage page;

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

  public WebPage getGlobalFrameByName(String name) throws WebParsingError {
    Page page1 = page.getFrameByName(name).getEnclosedPage();
    if (page1 instanceof HtmlPage){
      return new WebPage(browser, (HtmlPage)page1);
    }
    throw new WebParsingError(this, "Not an htmlPage : " + page1);
  }
}
