package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public class SavingsAccountViewChecker extends DataChecker {
  private Panel panel;

  public SavingsAccountViewChecker(Window window) {
    panel = window.getPanel("savingsAccountPositionView");
  }

  public SavingsAccountViewChecker checkPosition(String accountName, double position) {
    UISpecAssert.assertThat(panel.getTextBox("accountPosition." + accountName).textEquals(toString(position)));
    return this;
  }
}
