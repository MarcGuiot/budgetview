package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.globsframework.gui.utils.AbstractDocumentListener;

import javax.swing.event.DocumentEvent;
import javax.swing.text.JTextComponent;

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
