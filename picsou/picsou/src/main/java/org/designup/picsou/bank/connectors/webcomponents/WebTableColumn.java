package org.designup.picsou.bank.connectors.webcomponents;

import java.util.Iterator;
import java.util.List;

public class WebTableColumn implements Iterable<WebTableCell> {
  private List<WebTableCell> cells;

  protected WebTableColumn(List<WebTableCell> cells) {
    this.cells = cells;
  }

  public Iterator<WebTableCell> iterator() {
    return cells.iterator();
  }

  public WebTableColumn removeLastCells(int count) {
    if (!cells.isEmpty()) {
      cells = cells.subList(0, cells.size() - count - 1);
    }
    return this;
  }
}
