package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlInput;

public class WebTextField extends WebComponent<HtmlInput> {

  public WebTextField(WebBrowser browser, HtmlInput input) {
    super(browser, input);
  }

  public void setText(String content) {
    node.setAttribute("value", content);
  }

  public String getValue(){
    return node.getAttribute("value");
  }
}
