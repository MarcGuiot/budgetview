package org.designup.picsou.bank.importer.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import org.designup.picsou.bank.importer.webcomponents.utils.WebParsingError;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WebTable extends WebComponent<HtmlTable> {

  protected WebTable(WebBrowser browser, HtmlTable table) {
    super(browser, table);
  }

  public WebPanel getRowPanelWithText(String label) {
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
    return new WebPanel(browser, result);
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

  public String[][] getContent() {
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
}
