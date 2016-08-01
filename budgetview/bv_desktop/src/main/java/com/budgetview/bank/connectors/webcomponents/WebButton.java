package com.budgetview.bank.connectors.webcomponents;

import com.budgetview.bank.connectors.webcomponents.utils.WebCommandFailed;
import com.gargoylesoftware.htmlunit.html.HtmlButton;

public class WebButton extends WebComponent<HtmlButton> {

  public WebButton(WebBrowser browser, HtmlButton button) {
    super(browser, button);
  }

  public WebPage click() throws WebCommandFailed {
    return browser.doClick(node);
  }

  public Download clickAndDownload() {
    return new Download(browser, node);
  }

}
