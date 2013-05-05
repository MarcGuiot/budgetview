package org.designup.picsou.gui.printing.budget.tables;

import org.designup.picsou.gui.printing.PrintStyle;
import org.designup.picsou.gui.printing.PrintMetrics;
import org.designup.picsou.gui.printing.PrintablePage;
import org.globsframework.gui.views.Alignment;

import java.awt.*;

public class SeriesTablePage extends PrintablePage {

  private SeriesTable table;

  public SeriesTablePage(SeriesTable table) {
    this.table = table;
  }

  protected String getTitle() {
    return table.getTitle();
  }

  protected int printContent(Graphics2D g2,
                             PrintMetrics printMetrics,
                             PrintStyle style) {

    SeriesTableMetrics metrics = new SeriesTableMetrics(printMetrics, g2, style, table.getColumnCount());

    g2.setColor(style.getTextColor());
    for (int col = 0; col < table.getColumnCount(); col++) {
      g2.setFont(style.getTextFont(table.isColumnSelected(col)));
      String label = table.getColumnTitle(col);
      g2.drawString(label, metrics.tableTextX(label, col, Alignment.RIGHT), metrics.tableTextY(0));
    }
    g2.drawLine(metrics.tableLeft(), metrics.tableRowBottom(0),
                metrics.tableRight(), metrics.tableRowBottom(0));

    g2.setColor(style.getTableRowColor());
    int rowCount = table.getRowCount();
    for (int row = 1; row < rowCount + 1; row++) {
      if (row % 2 != 0) {
        g2.fillRect(metrics.tableLeft(), metrics.tableRowTop(row), metrics.tableWidth(), metrics.tableRowHeight());
      }
    }

    g2.setColor(style.getTextColor());
    int rowIndex = 1;
    for (SeriesTable.SeriesRow row : table.rows()) {
      for (int col = 0; col < table.getColumnCount(); col++) {
        g2.setFont(style.getTextFont(table.isColumnSelected(col)));
        String value = row.getValue(col);
        g2.drawString(value, metrics.tableTextX(value, col, Alignment.RIGHT), metrics.tableTextY(rowIndex));
      }
      rowIndex++;
    }
    
    g2.setStroke(new BasicStroke(0.5f));
    g2.setColor(style.getDividerColor());
    int bottom = metrics.tableRowBottom(table.rows().size());
    g2.drawLine(metrics.tableColumnLeft(1), metrics.tableTop(), metrics.tableColumnLeft(1), bottom);
    g2.drawLine(metrics.tableColumnLeft(2), metrics.tableTop(), metrics.tableColumnLeft(2), bottom);

    return PAGE_EXISTS;
  }

  public SeriesTable getSeriesTable() {
    return table;
  }
}
