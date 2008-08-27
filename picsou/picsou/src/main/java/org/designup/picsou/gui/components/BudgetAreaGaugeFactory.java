package org.designup.picsou.gui.components;

import org.designup.picsou.model.BudgetArea;

public class BudgetAreaGaugeFactory {
  public static Gauge createGauge(BudgetArea budgetArea) {
    if (budgetArea.isIncome()) {
      return new Gauge(false, true, true);
    }
    else {
      return new Gauge(true, true, false);
    }
  }
}
