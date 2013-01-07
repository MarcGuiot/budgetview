package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;

import java.io.IOException;

public class WebButton extends WebComponent<HtmlButton> {

  public WebButton(WebBrowser browser, HtmlButton button) {
    super(browser, button);
  }

  public WebPage click() throws WebCommandFailed {
    return browser.doClick(node);
  }
}
