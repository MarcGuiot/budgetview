package org.designup.picsou.gui.printing.pages;

import org.designup.picsou.gui.printing.PrintColors;
import org.designup.picsou.gui.printing.PrintFonts;
import org.designup.picsou.gui.printing.PrintMetrics;
import org.designup.picsou.gui.printing.reports.SeriesTable;
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

    System.out.println("SeriesTablePage.printContent: ");

    SeriesTableMetrics metrics = new SeriesTableMetrics(printMetrics, g2, fonts, table.getColumnCount());

    g2.setColor(colors.getTableTextColor());
    g2.setFont(fonts.getTableTextFont());
    for (int col = 0; col < table.getColumnCount(); col++) {
      String label = table.getColumnTitle(col);
      g2.drawString(label, metrics.tableTextX(label, col, Alignment.CENTER), metrics.tableTextY(0));
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
      String label = row.getLabel();
      g2.drawString(label, metrics.tableTextX(label, 0, Alignment.LEFT), metrics.tableTextY(rowIndex));
      for (int col = 1; col < table.getColumnCount(); col++) {
        String value = row.getValue(col);
        g2.drawString(value, metrics.tableTextX(value, col, Alignment.RIGHT), metrics.tableTextY(rowIndex));
      }
      rowIndex++;
    }

    return PAGE_EXISTS;
  }
}
