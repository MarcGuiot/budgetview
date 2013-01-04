package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlInput;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;

import java.io.IOException;

public class WebInput extends WebComponent<HtmlInput> {

  public WebInput(WebBrowser browser, HtmlInput input) {
    super(browser, input);
  }

  public WebPage click() throws WebCommandFailed {
    try {
      return browser.setCurrentPage(node.click());
    }
    catch (IOException e) {
      throw new WebCommandFailed(e);
    }
  }

  public void select() {
    node.setChecked(true);
  }
}
