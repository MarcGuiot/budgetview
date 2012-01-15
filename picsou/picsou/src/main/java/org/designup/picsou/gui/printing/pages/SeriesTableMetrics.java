package org.designup.picsou.gui.printing.pages;

import org.designup.picsou.gui.printing.PrintFonts;
import org.designup.picsou.gui.printing.PrintMetrics;
import org.globsframework.gui.views.Alignment;
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

  public SeriesTableMetrics(PrintMetrics metrics, Graphics2D g2, PrintFonts fonts, int columnCount) {
    Rectangle contentArea = metrics.getContentArea();
    this.x0 = contentArea.x;
    this.y0 = contentArea.y;
    this.width = contentArea.width;
    this.height = contentArea.height;
    this.columnCount = columnCount;

    tableFontMetrics = g2.getFontMetrics(fonts.getTableTextFont());
    int textHeight = tableFontMetrics.getHeight();
    rowHeight = textHeight + 4;
    textYOffset = (rowHeight - textHeight) / 2 + tableFontMetrics.getDescent();
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
    int left = x0 + (col + 0) * width / columnCount;
    int right = x0 + (col + 1) * width / columnCount;
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
