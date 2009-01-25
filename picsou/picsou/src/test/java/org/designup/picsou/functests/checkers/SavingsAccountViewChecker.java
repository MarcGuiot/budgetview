package org.designup.picsou.functests.checkers;

import org.uispec4j.Window;
import org.uispec4j.Button;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class SavingsAccountViewChecker extends AccountViewChecker {

  public SavingsAccountViewChecker(Window window) {
    super(window, "savingsAccountView");
  }

  public SavingsAccountViewChecker checkPosition(String accountName, double position) {
    UISpecAssert.assertThat(panel.getTextBox("estimatedAccountPosition." + accountName).textEquals(toString(position)));
    return this;
  }

  public AccountViewChecker checkEstimatedPosition(double amount, String expected) {
    TextBox estimatedPosition = panel.getTextBox("estimatedPosition");
    assertThat(estimatedPosition.isVisible());
    assertThat(estimatedPosition.textEquals(toString(amount)));
    TextBox textBox = panel.getTextBox("estimatedPositionDate");
    assertThat(textBox.textEquals("on " + expected));
    return this;
  }

}
