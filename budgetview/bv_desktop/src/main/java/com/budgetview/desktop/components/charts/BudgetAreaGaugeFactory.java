package com.budgetview.desktop.components.charts;

import com.budgetview.model.BudgetArea;
import com.budgetview.shared.gui.gauge.GaugeModel;

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
