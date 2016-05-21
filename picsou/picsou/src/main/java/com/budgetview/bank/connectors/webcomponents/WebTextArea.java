package com.budgetview.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlTextArea;

public class WebTextArea extends WebComponent<HtmlTextArea> {

  public WebTextArea(WebBrowser browser, HtmlTextArea textArea) {
    super(browser, textArea);
  }

  public void setText(String text) {
    node.setText(text);
  }
}
