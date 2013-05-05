package org.designup.picsou.gui.printing.transactions;

import java.awt.*;

public class TransactionBlockMetrics {
  private static final int LINE_MARGIN = 2;
  private static final int COLUMN_MARGIN = 10;

  private final int firstRowY;
  private final int secondRowY;
  private final int thirdRowY;
  private final int left;
  private final int labelColumnX;
  private final int right;
  private FontMetrics defaultFontMetrics;
  private FontMetrics labelFontMetrics;

  public TransactionBlockMetrics(Dimension area, Graphics2D g2, Font labelFont, Font defaultFont) {
    labelFontMetrics = g2.getFontMetrics(labelFont);
    int labelHeight = labelFontMetrics.getHeight();
    defaultFontMetrics = g2.getFontMetrics(defaultFont);
    int defaultHeight = defaultFontMetrics.getHeight();

    firstRowY = labelHeight;
    secondRowY = firstRowY + LINE_MARGIN + defaultHeight;
    thirdRowY = secondRowY + LINE_MARGIN + defaultHeight;

    left = 0;
    labelColumnX = defaultFontMetrics.stringWidth("99/99/9999") + COLUMN_MARGIN;
    right = area.width;
  }

  public int getBankDateX() {
    return left;
  }

  public int getBankDateY() {
    return firstRowY;
  }

  public int getLabelX() {
    return labelColumnX;
  }

  public int getLabelY() {
    return firstRowY;
  }

  public int getAmountX(String text) {
    return right - labelFontMetrics.stringWidth(text);
  }

  public int getAmountY() {
    return firstRowY;
  }

  public int getAccountLabelX(String text) {
    return right - defaultFontMetrics.stringWidth(text);
  }

  public int getAccountLabelY() {
    return secondRowY;
  }

  public int getNoteX() {
    return labelColumnX;
  }

  public int getNoteY() {
    return thirdRowY;
  }

  public int getSeriesX() {
    return labelColumnX;
  }

  public int getSeriesY() {
    return secondRowY;
  }
}
