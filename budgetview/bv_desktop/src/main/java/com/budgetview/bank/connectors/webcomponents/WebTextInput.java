package com.budgetview.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class WebTextInput extends WebComponent<HtmlTextInput> {

  public WebTextInput(WebBrowser browser, HtmlTextInput input) {
    super(browser, input);
  }

  public void setText(String content) {
    node.setText(content);
  }

  public String getValue(){
    return node.getAttribute("value");
  }
}
