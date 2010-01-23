package org.designup.picsou.gui.components.charts;

import org.designup.picsou.model.BudgetArea;

public class BudgetAreaGaugeFactory {
  public static Gauge createGauge(BudgetArea area) {
    return new Gauge(area == BudgetArea.SAVINGS);
  }

  public static Gauge createSavingsGauge(boolean invert) {
    return new Gauge(invert);
  }
}
