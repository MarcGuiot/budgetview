package com.budgetview.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;

public class WebRadioButton extends WebComponent<HtmlRadioButtonInput> {

  protected WebRadioButton(WebBrowser browser, HtmlRadioButtonInput input) {
    super(browser, input);
  }

  public void select() {
    node.setChecked(true);
  }
}
