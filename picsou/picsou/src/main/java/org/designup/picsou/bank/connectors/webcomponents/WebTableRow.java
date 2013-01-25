package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlTableRow;

import java.util.Iterator;

public class WebTableRow extends WebContainer<HtmlTableRow> {
  public WebTableRow(WebBrowser browser, HtmlTableRow row) {
    super(browser, row);
  }

  public WebTableCell getCell(int columnIndex) {
    return new WebTableCell(browser, node.getCell(columnIndex));
  }

  public Iterable<WebTableCell> getCells() {
    return new Iterable<WebTableCell>() {
      HtmlTableRow.CellIterator iterator = node.getCellIterator();
      public Iterator<WebTableCell> iterator() {
        return new Iterator<WebTableCell>() {
          public boolean hasNext() {
            return iterator.hasNext();
          }

          public WebTableCell next() {
            return new WebTableCell(browser, iterator.next());
          }

          public void remove() {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }

}
