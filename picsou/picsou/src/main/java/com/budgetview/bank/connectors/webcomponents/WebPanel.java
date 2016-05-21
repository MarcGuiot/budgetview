package com.budgetview.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlElement;

public class WebPanel extends WebContainer<HtmlElement> {

  public WebPanel(WebBrowser browser, HtmlElement element) {
    super(browser, element);
  }
}
