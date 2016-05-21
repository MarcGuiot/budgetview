package com.budgetview.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;

public class WebPasswordInput extends WebComponent<HtmlPasswordInput> {

  public WebPasswordInput(WebBrowser browser, HtmlPasswordInput input) {
    super(browser, input);
  }

  public void setText(String content) {
    node.setText(content);
  }

  public String getValue() {
    return node.getAttribute("value");
  }
}
