package org.designup.picsou.bank.importer.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlInput;

public class WebInput extends WebComponent<HtmlInput> {

  public WebInput(WebBrowser browser, HtmlInput input) {
    super(browser, input);
  }

  public void select() {
    node.setChecked(true);
  }
}
