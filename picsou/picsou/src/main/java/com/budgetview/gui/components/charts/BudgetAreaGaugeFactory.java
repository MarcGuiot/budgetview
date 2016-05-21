package com.budgetview.gui.components.charts;

import com.budgetview.shared.gui.gauge.GaugeModel;
import com.budgetview.model.BudgetArea;

public class BudgetAreaGaugeFactory {
  public static Gauge createGauge(BudgetArea area) {
    return createGauge(area == BudgetArea.TRANSFER);
  }

  public static Gauge createGauge(boolean invert) {
    GaugeModel model = new GaugeModel(new LangGaugeTextSource());
    model.setInvertAll(invert);
    return new Gauge(model);
  }
}
