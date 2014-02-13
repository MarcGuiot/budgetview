package org.designup.picsou.gui.printing.budget.gauges;

import java.awt.*;

public class BudgetGaugeBlockMetrics {

  private static final int PADDING = 2;

  public final int labelX = PADDING;
  public final int labelY;
  public final int labelWidth;
  public final int amountsY;
  public final int gaugeX;
  public final int gaugeY;
  public final int gaugeHeight = 8;
  public final int gaugeWidth = 40;
  public final int columnMargin = 2;

  private Dimension area;
  private final int maxAmountWidth;
  private FontMetrics amountsFontMetrics;

  public BudgetGaugeBlockMetrics(Dimension area, Graphics2D g2, int maxAmountWidth, Font textFont, Font amountsFont) {
    this.area = area;
    this.maxAmountWidth = maxAmountWidth;

    FontMetrics labelFontMetrics = g2.getFontMetrics(textFont);
    this.labelY = area.height / 2 + labelFontMetrics.getAscent() / 2;

    this.amountsFontMetrics = g2.getFontMetrics(amountsFont);
    this.amountsY = area.height / 2 + amountsFontMetrics.getAscent() / 2;

    this.gaugeX = area.width - 2 * maxAmountWidth - columnMargin - gaugeWidth - PADDING;
    this.gaugeY = area.height / 2 - gaugeHeight / 2;

    this.labelWidth = gaugeX - labelX - columnMargin;
  }

  public int labelX(boolean shift) {
    return shift ? labelX + 8 : labelX;
  }

  public int actualAmountX(String amount) {
    return area.width - maxAmountWidth - amountsFontMetrics.stringWidth(amount) - PADDING;
  }

  public int plannedAmountX(String amount) {
    return area.width - amountsFontMetrics.stringWidth(amount) - PADDING;
  }
}
