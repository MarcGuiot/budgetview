package com.budgetview.bank.connectors.webcomponents;

import com.budgetview.bank.connectors.webcomponents.utils.WebCommandFailed;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;

public class WebButtonInput extends WebComponent<HtmlButtonInput> {

  public WebButtonInput(WebBrowser browser, HtmlButtonInput button) {
    super(browser, button);
  }

  public WebPage click() throws WebCommandFailed {
    return browser.doClick(node);
  }
}
