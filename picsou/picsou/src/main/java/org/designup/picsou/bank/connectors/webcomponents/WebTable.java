package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.*;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WebTable extends WebComponent<HtmlTable> {

  protected WebTable(WebBrowser browser, HtmlTable table) {
    super(browser, table);
  }

  public List<WebTableRow> getAllRows() {
    return convertRows(node.getRows());
  }

  public List<WebTableRow> getRowsWithoutHeaderAndFooters() {
    List<WebTableRow> result = new ArrayList<WebTableRow>();
    for (HtmlElement row : node.getElementsByTagName("tr")) {
      if (row.getParentNode().getNodeName().equals("thead") ||
          row.getParentNode().getNodeName().equals("tfoot")) {
        continue;
      }
      result.add(new WebTableRow(browser, (HtmlTableRow)row));
    }
    return result;
  }

  public List<WebTableRow> getRowsExceptLast(int count) {
    List<HtmlTableRow> rows = node.getRows();
    rows= rows.subList(0, rows.size() - count - 1);
    return convertRows(rows);
  }

  private List<WebTableRow> convertRows(List<HtmlTableRow> rows) {
    List<WebTableRow> result = new ArrayList<WebTableRow>();
    for (HtmlTableRow row : rows) {
      result.add(new WebTableRow(browser, row));
    }
    return result;
  }

  public WebTableRow getRowWithText(String label) throws WebParsingError {
    HtmlTableRow result = null;
    for (Iterator iter = node.getRows().iterator(); iter.hasNext(); ) {
      HtmlTableRow row = (HtmlTableRow)iter.next();
      if (row.asText().contains(label)) {
        if (result != null) {
          throw new WebParsingError(this, "Several rows were found with text '" + label + "' - actual table content:\n" +
                                          dump(node));
        }
        result = row;
      }
    }
    if (result == null) {
      throw new WebParsingError(this, "No row was found with text '" + label + "' - actual table content:\n" + dump(node));
    }
    return new WebTableRow(browser, result);
  }

  public WebTableColumn getColumn(int columnIndex) {
    List<WebTableCell> cells = new ArrayList<WebTableCell>();
    for (HtmlTableRow row : node.getRows()) {
      List<HtmlTableCell> rowCells = row.getCells();
      if (columnIndex < rowCells.size()) {
        cells.add(new WebTableCell(browser, row.getCell(columnIndex)));
      }
    }
    return new WebTableColumn(cells);
  }

  public String[][] getContentAsText() {
    List list = new ArrayList();
    for (Iterator iterator = node.getRows().iterator(); iterator.hasNext(); ) {
      HtmlTableRow row = (HtmlTableRow)iterator.next();
      String[] cells = convertRow(row);
      list.add(cells);
    }
    String[][] content = new String[list.size()][];
    int index = 0;
    for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
      content[index++] = (String[])iterator.next();
    }
    return content;
  }

  private String[] convertRow(HtmlTableRow row) {
    List cells = row.getCells();
    String[] result = new String[cells.size()];
    int columnIndex = 0;
    for (Iterator iterator = cells.iterator(); iterator.hasNext(); ) {
      HtmlTableCell cell = (HtmlTableCell)iterator.next();
      result[columnIndex++] = getTextInCell(columnIndex, cell);
    }
    return result;
  }

  private String getTextInCell(int columnIndex, HtmlTableCell cell) {
    return cell.getTextContent();
  }

  public HtmlTable getTable() {
    return node;
  }
}
