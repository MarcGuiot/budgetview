package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.components.GaugeChecker;
import com.budgetview.desktop.components.charts.Gauge;
import org.uispec4j.Panel;
import org.uispec4j.Window;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class CategorizationGaugeChecker extends ViewChecker {
  private Panel panel;

  public CategorizationGaugeChecker(Window mainWindow) {
    super(mainWindow);
  }

  public CategorizationGaugeChecker checkLevel(double gaugeLevel) {
    Panel panel = getPanel();
    assertThat(panel.isVisible());
    GaugeChecker gauge = new GaugeChecker(panel.findSwingComponent(Gauge.class));
    gauge.checkFill(gaugeLevel);
    return this;
  }

  public void checkHidden() {
    checkComponentVisible(getPanel(), Gauge.class, "gauge", false);
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectCategorization();
      panel = mainWindow.getPanel("gaugePanel");
    }
    return panel;
  }
}
