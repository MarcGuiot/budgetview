package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlButton;
import org.designup.picsou.bank.connectors.webcomponents.utils.Download;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;

import java.io.IOException;

public class WebButton extends WebComponent {
  private HtmlButton button;

  public WebButton(WebBrowser browser, HtmlButton button) {
    super(browser, button);
    this.button = button;
  }

  public WebPage click() throws WebCommandFailed {
    try {
      return browser.setCurrentPage(button.click());
    }
    catch (IOException e) {
      throw new WebCommandFailed(e);
    }
  }
}
