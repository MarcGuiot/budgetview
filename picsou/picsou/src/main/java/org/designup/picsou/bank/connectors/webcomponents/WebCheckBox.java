package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlInput;

public class WebCheckBox extends WebComponent<HtmlInput> {

  public WebCheckBox(WebBrowser browser, HtmlInput input) {
    super(browser, input);
  }

  public void setChecked(boolean checked) {
    node.setChecked(checked);
  }
}
