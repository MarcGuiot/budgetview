package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;

public class WebButtonInput extends WebComponent<HtmlButtonInput> {

  public WebButtonInput(WebBrowser browser, HtmlButtonInput button) {
    super(browser, button);
  }

  public WebPage click() throws WebCommandFailed {
    return browser.doClick(node);
  }
}
