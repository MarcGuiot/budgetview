package org.designup.picsou.gui.components;

import org.designup.picsou.model.BudgetArea;

public class BudgetAreaGaugeFactory {
  public static Gauge createGauge(BudgetArea budgetArea) {
    if (budgetArea.isIncome()) {
      return new Gauge(false, false, true);
    }
    else {
      return new Gauge(true, false, false);
    }
  }
}
