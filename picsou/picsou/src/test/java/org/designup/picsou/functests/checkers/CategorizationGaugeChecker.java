package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Button;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import org.designup.picsou.gui.components.Gauge;

public class CategorizationGaugeChecker extends GuiChecker {
  private Panel panel;
  private TextBox progressMessage;
  private Button hideButton;

  public CategorizationGaugeChecker(Panel panel) {
    this.panel = panel.getPanel("gaugePanel");
    this.progressMessage = panel.getTextBox("progressMessage");
    this.hideButton = panel.getButton("hideProgressMessage");
  }

  public CategorizationGaugeChecker checkLevel(double gaugeLevel, String displayed) {
    assertThat(panel.isVisible());
    GaugeChecker gauge = new GaugeChecker(panel.findSwingComponent(Gauge.class));
    gauge.checkFill(gaugeLevel);

    assertThat(panel.getTextBox("level").textEquals(displayed));
    return this;
  }

  public void checkHidden() {
    assertFalse(panel.isVisible());
  }

  public void checkProgressMessageHidden() {
    assertFalse(progressMessage.isVisible());
  }

  public void checkQuasiCompleteProgressMessageShown() {
    assertTrue(progressMessage.isVisible());
    assertTrue(progressMessage.textContains("quasi"));
  }

  public void checkCompleteProgressMessageShown() {
    assertTrue(progressMessage.isVisible());
    assertTrue(progressMessage.textContains("completed"));
  }

  public void hideProgressMessage() {
    hideButton.click();
  }

  public void clickOnProgressMessageLink() {
    assertTrue(progressMessage.isVisible());
    progressMessage.clickOnHyperlink("budget");
  }
}
