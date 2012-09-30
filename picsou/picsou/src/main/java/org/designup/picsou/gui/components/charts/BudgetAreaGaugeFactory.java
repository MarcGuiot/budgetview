package org.designup.picsou.gui.components.charts;

import com.budgetview.shared.gui.GaugeModel;
import org.designup.picsou.model.BudgetArea;

public class BudgetAreaGaugeFactory {
  public static Gauge createGauge(BudgetArea area) {
    GaugeModel model = new GaugeModel();
    model.setInvertAll(area == BudgetArea.SAVINGS);
    return new Gauge(model);
  }

  public static Gauge createSavingsGauge(boolean invert) {
    GaugeModel model = new GaugeModel();
    model.setInvertAll(invert);
    return new Gauge(model);
  }
}
