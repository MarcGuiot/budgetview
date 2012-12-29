package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlInput;

public class WebRadioButton extends WebComponent<HtmlInput> {

  protected WebRadioButton(WebBrowser browser, HtmlInput input) {
    super(browser, input);
  }

  public void select() {
    node.setChecked(true);
  }
}
