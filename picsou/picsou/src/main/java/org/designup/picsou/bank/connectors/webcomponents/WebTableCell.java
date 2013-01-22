package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebCommandFailed;

import java.io.IOException;

public class WebTableCell extends WebContainer<HtmlTableCell> {
  public WebTableCell(WebBrowser browser, HtmlTableCell cell) {
    super(browser, cell);
  }

  public WebTableRow getEnclosingRow() {
    return new WebTableRow(browser, node.getEnclosingRow());
  }

  public WebPage click() throws WebCommandFailed {
    try {
      return browser.setCurrentPage(node.click());
    }
    catch (IOException e) {
      throw new WebCommandFailed(e);
    }
  }
}
