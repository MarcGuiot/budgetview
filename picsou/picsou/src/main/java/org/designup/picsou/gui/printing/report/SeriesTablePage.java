package org.designup.picsou.gui.printing.report;

import org.designup.picsou.gui.printing.PrintColors;
import org.designup.picsou.gui.printing.PrintFonts;
import org.designup.picsou.gui.printing.PrintMetrics;
import org.globsframework.gui.views.Alignment;

import java.awt.*;

public class SeriesTablePage extends ReportPage {

  private SeriesTable table;

  public SeriesTablePage(SeriesTable table) {
    this.table = table;
  }

  protected String getTitle() {
    return table.getTitle();
  }

  protected int printContent(Graphics2D g2,
                             PrintFonts fonts,
                             PrintMetrics printMetrics,
                             PrintColors colors) {

    SeriesTableMetrics metrics = new SeriesTableMetrics(printMetrics, g2, fonts, table.getColumnCount());

    g2.setColor(colors.getTableTextColor());
    for (int col = 0; col < table.getColumnCount(); col++) {
      g2.setFont(fonts.getTableTextFont(table.isColumnSelected(col)));
      String label = table.getColumnTitle(col);
      g2.drawString(label, metrics.tableTextX(label, col, Alignment.RIGHT), metrics.tableTextY(0));
    }
    g2.drawLine(metrics.tableLeft(), metrics.tableRowBottom(0),
                metrics.tableRight(), metrics.tableRowBottom(0));

    g2.setColor(colors.getTableRowColor());
    int rowCount = table.getRowCount();
    for (int row = 1; row < rowCount + 1; row++) {
      if (row % 2 != 0) {
        g2.fillRect(metrics.tableLeft(), metrics.tableRowTop(row), metrics.tableWidth(), metrics.tableRowHeight());
      }
    }

    g2.setColor(colors.getTableTextColor());
    int rowIndex = 1;
    for (SeriesTable.SeriesRow row : table.rows()) {
      for (int col = 0; col < table.getColumnCount(); col++) {
        g2.setFont(fonts.getTableTextFont(table.isColumnSelected(col)));
        String value = row.getValue(col);
        g2.drawString(value, metrics.tableTextX(value, col, Alignment.RIGHT), metrics.tableTextY(rowIndex));
      }
      rowIndex++;
    }
    
    g2.setStroke(new BasicStroke(0.5f));
    g2.setColor(colors.getTableLineColor());
    int bottom = metrics.tableRowBottom(table.rows().size());
    g2.drawLine(metrics.tableColumnLeft(1), metrics.tableTop(), metrics.tableColumnLeft(1), bottom);
    g2.drawLine(metrics.tableColumnLeft(2), metrics.tableTop(), metrics.tableColumnLeft(2), bottom);

    return PAGE_EXISTS;
  }

  public SeriesTable getSeriesTable() {
    return table;
  }
}
