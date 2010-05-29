package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.components.charts.Gauge;
import org.uispec4j.*;
import static org.uispec4j.assertion.UISpecAssert.*;

public class CategorizationGaugeChecker extends GuiChecker {
  private Window mainWindow;
  private Panel panel;

  public CategorizationGaugeChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
    this.panel = mainWindow.getPanel("gaugePanel");
  }

  public CategorizationGaugeChecker checkLevel(double gaugeLevel) {
    assertThat(panel.isVisible());
    GaugeChecker gauge = new GaugeChecker(panel.findSwingComponent(Gauge.class));
    gauge.checkFill(gaugeLevel);
    return this;
  }

  public void checkHidden() {
    assertFalse(panel.isVisible());
  }

  public void checkProgressMessageHidden() {
    checkSignpostHidden(mainWindow, getBudgetToggle());
  }

  public void checkQuasiCompleteProgressMessageShown() {
    checkSignpostVisible(mainWindow, getBudgetToggle(),
                         "Categorization is quasi complete. You can now see the budget page.");
  }

  public void checkCompleteProgressMessageShown() {
    checkSignpostVisible(mainWindow, getBudgetToggle(),
                         "Categorization is completed. You can now see the budget page.");
  }

  private ToggleButton getBudgetToggle() {
    return mainWindow.getToggleButton("budgetCardToggle");
  }
}
