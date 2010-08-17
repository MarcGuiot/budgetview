package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.components.charts.Gauge;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class NavigationViewChecker extends GuiChecker {
  private Window window;

  public NavigationViewChecker(Window window) {
    this.window = window;
  }

  public void gotoBudget() {
    getPanel().getButton("Budget").click();
  }

  public void gotoCategorization() {
    getPanel().getButton("Categorization").click();
  }

  public void checkCategorizationLabel(String label) {
    assertThat(getCategorizationPanel().getTextBox().textEquals(label));
  }

  public void checkCategorizationLevel(double percentage) {
    GaugeChecker gauge = new GaugeChecker(getCategorizationPanel(), "gauge");
    gauge.checkFill(percentage);
  }

  public NavigationViewChecker checkCategorizationGaugeHidden() {
    checkComponentVisible(getCategorizationPanel(), Gauge.class, "gauge", false);
    return this;
  }

  private Panel getCategorizationPanel() {
    return getPanel().getPanel("categorization");
  }

  private Panel getPanel() {
    return window.getPanel("navigationView");
  }
}
