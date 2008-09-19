package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.assertion.UISpecAssert;
import org.designup.picsou.gui.components.Gauge;

public class CategorizationGaugeChecker extends DataChecker {
  private Panel panel;

  public CategorizationGaugeChecker(Panel panel) {
    this.panel = panel;
  }

  public void checkLevel(double gaugeLevel, String displayed) {
    UISpecAssert.assertThat(panel.isVisible());
    GaugeChecker gauge = new GaugeChecker(panel.findSwingComponent(Gauge.class));
    gauge.checkFill(gaugeLevel);

    UISpecAssert.assertThat(panel.getTextBox("level").textEquals(displayed));
  }

  public void checkHidden() {
    UISpecAssert.assertFalse(panel.isVisible());    
  }
}
