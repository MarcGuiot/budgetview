package org.designup.picsou.gui.printing.budget.tables;

import org.designup.picsou.gui.printing.PrintFonts;
import org.designup.picsou.gui.printing.PrintMetrics;
import org.globsframework.gui.views.Alignment;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.awt.*;

public class SeriesTableMetrics {
  private FontMetrics tableFontMetrics;

  private int x0;
  private int y0;
  private int width;
  private int height;
  private int rowHeight;
  private int columnCount;

  private int textYOffset;
  private int firstColumnWidth;

  public SeriesTableMetrics(PrintMetrics metrics, Graphics2D g2, PrintFonts fonts, int columnCount) {
    Rectangle contentArea = metrics.getContentArea();
    this.x0 = contentArea.x;
    this.y0 = contentArea.y;
    this.width = contentArea.width;
    this.height = contentArea.height;
    this.columnCount = columnCount;

    this.tableFontMetrics = g2.getFontMetrics(fonts.getTextFont(true));
    int textHeight = tableFontMetrics.getHeight();
    this.rowHeight = textHeight + 4;
    this.textYOffset = (rowHeight - textHeight) / 2 + tableFontMetrics.getDescent();

    this.firstColumnWidth = tableFontMetrics.stringWidth(Strings.repeat("a", 20));
  }

  public int tableLeft() {
    return x0;
  }

  public int tableRight() {
    return x0 + width;
  }

  public int tableTop() {
    return y0;
  }

  public int tableWidth() {
    return width;
  }

  public int tableHeight() {
    return height;
  }

  public int tableTextX(String text, int col, Alignment alignment) {
    int left = tableColumnLeft(col);
    int right = tableColumnLeft(col + 1);
    switch (alignment) {
      case LEFT:
        return left;

      case CENTER:
        int columnWidth = right - left;
        return left + columnWidth / 2 - tableFontMetrics.stringWidth(text) / 2;

      case RIGHT:
        return right - tableFontMetrics.stringWidth(text) - 1;
    }
    throw new InvalidParameter("Unknown alignment: " + alignment);
  }

  public int tableColumnLeft(int col) {
    if (col == 0) {
      return x0;
    }
    if (col == 1) {
      return x0 + firstColumnWidth;
    }
    return x0 + firstColumnWidth + (col -1) * (width - firstColumnWidth) / (columnCount - 1);
  }

  public int tableTextY(int row) {
    return tableRowBottom(row) - textYOffset;
  }

  public int tableRowTop(int row) {
    return tableTop() + row * rowHeight;
  }

  public int tableRowBottom(int row) {
    return tableTop() + (row + 1) * rowHeight;
  }

  public int tableRowHeight() {
    return rowHeight;
  }
}
