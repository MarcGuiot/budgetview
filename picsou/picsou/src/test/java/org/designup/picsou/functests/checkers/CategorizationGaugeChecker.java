package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;
import org.designup.picsou.gui.components.Gauge;

public class CategorizationGaugeChecker extends DataChecker {
  private Panel panel;
  private TextBox progressMessage;

  public CategorizationGaugeChecker(Panel panel) {
    this.panel = panel.getPanel("gaugePanel");
    this.progressMessage = panel.getTextBox("progressMessage");
  }

  public CategorizationGaugeChecker checkLevel(double gaugeLevel, String displayed) {
    UISpecAssert.assertThat(panel.isVisible());
    GaugeChecker gauge = new GaugeChecker(panel.findSwingComponent(Gauge.class));
    gauge.checkFill(gaugeLevel);

    UISpecAssert.assertThat(panel.getTextBox("level").textEquals(displayed));
    return this;
  }

  public void checkHidden() {
    UISpecAssert.assertFalse(panel.isVisible());
  }

  public void checkProgressMessageHidden() {
    UISpecAssert.assertFalse(progressMessage.isVisible());
  }

  public void checkQuasiCompleteProgressMessageShown() {
    UISpecAssert.assertTrue(progressMessage.isVisible());
    UISpecAssert.assertTrue(progressMessage.textContains("quasi"));
  }

  public void checkCompleteProgressMessageShown() {
    UISpecAssert.assertTrue(progressMessage.isVisible());
    UISpecAssert.assertTrue(progressMessage.textContains("completed"));
  }

  public void clickOnProgressMessageLink() {
    UISpecAssert.assertTrue(progressMessage.isVisible());
    progressMessage.clickOnHyperlink("budget");
  }
}
