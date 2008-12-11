package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public class SavingsAccountViewChecker extends AccountViewChecker {

  public SavingsAccountViewChecker(Window window) {
    super(window, "savingsAccountView");
  }

  public SavingsAccountViewChecker checkPosition(String accountName, double position) {
    UISpecAssert.assertThat(panel.getTextBox("estimatedAccountPosition." + accountName).textEquals(toString(position)));
    return this;
  }
}
