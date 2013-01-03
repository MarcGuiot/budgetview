package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

public class WebTableRow extends WebContainer<HtmlTableRow> {
  public WebTableRow(WebBrowser browser, HtmlTableRow row) {
    super(browser, row);
  }

  public WebTableCell getCell(int columnIndex) {
    return new WebTableCell(browser, node.getCell(columnIndex));
  }
}
