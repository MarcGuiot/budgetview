package org.designup.picsou.gui.components;

import org.designup.picsou.model.BudgetArea;

public class BudgetAreaGaugeFactory {
  public static Gauge createGauge(BudgetArea budgetArea) {
    return new Gauge(!budgetArea.isOverrunAllowed(), false, budgetArea.isIncome());
  }
}
