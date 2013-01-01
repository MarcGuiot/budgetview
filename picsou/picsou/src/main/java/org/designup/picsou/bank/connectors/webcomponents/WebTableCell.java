package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlTableCell;

public class WebTableCell extends WebContainer<HtmlTableCell> {
  public WebTableCell(WebBrowser browser, HtmlTableCell cell) {
    super(browser, cell);
  }

  public WebTableRow getEnclosingRow() {
    return new WebTableRow(browser, node.getEnclosingRow());
  }
}
