package com.budgetview.bank.connectors.webcomponents;

import com.budgetview.bank.connectors.webcomponents.utils.WebCommandFailed;
import com.gargoylesoftware.htmlunit.html.HtmlFrame;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.budgetview.bank.connectors.webcomponents.utils.WebParsingError;

import java.net.MalformedURLException;

public class WebFrame extends WebContainer<HtmlFrame> {
  protected WebFrame(WebBrowser browser, HtmlFrame node) {
    super(browser, node);
  }

  public WebPage loadTargetPage() throws WebCommandFailed, WebParsingError {
    try {
      String targetUrl = ((HtmlPage)node.getPage()).getFullyQualifiedUrl(node.getSrcAttribute()).toString();
      return browser.load(targetUrl);
    }
    catch (MalformedURLException e) {
      throw new WebParsingError(this, e);
    }
  }
}