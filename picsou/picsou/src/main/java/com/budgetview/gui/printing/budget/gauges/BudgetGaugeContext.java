package com.budgetview.gui.printing.budget.gauges;

import com.budgetview.gui.printing.PrintStyle;
import com.budgetview.gui.components.charts.Gauge;

import java.awt.*;

public class BudgetGaugeContext {

  private Gauge gauge;
  private Integer maxAmountsWidth;

  public BudgetGaugeContext() {
  }

  public Gauge getGauge(PrintStyle style) {
    if (gauge == null) {
      gauge = new Gauge();
      style.setColors(gauge);
    }
    return gauge;
  }

  public BudgetGaugeBlockMetrics getMetrics(Dimension area, Graphics2D g2, Font labelFont, Font amountsFont) {
    return new BudgetGaugeBlockMetrics(area, g2, getMaxAmountWidth(g2, amountsFont), labelFont, amountsFont);
  }

  private int getMaxAmountWidth(Graphics2D g2, Font amountFont) {
    if (maxAmountsWidth == null) {
      maxAmountsWidth = g2.getFontMetrics(amountFont).stringWidth("-99999.99");
    }
    return maxAmountsWidth;
  }
}
