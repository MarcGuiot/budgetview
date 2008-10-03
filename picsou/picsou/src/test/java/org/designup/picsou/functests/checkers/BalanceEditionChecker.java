package org.designup.picsou.functests.checkers;

import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public class BalanceEditionChecker extends DataChecker {
  private Window window;

  public BalanceEditionChecker(Window window) {
    this.window = window;
  }

  public BalanceEditionChecker setAmount(Double amount) {
    window.getInputTextBox().setText(Double.toString(amount));
    return this;
  }

  public BalanceEditionChecker checkLabel(String label) {
    UISpecAssert.assertThat(window.getTextBox("labelInfo").textEquals(label));
    return this;
  }

  public BalanceEditionChecker checkAccountName(String accountName) {
    UISpecAssert.assertThat(window.getTextBox("accountName").textEquals(accountName));
    return this;
  }

  public void validate() {
    window.getButton("ok").click();
    UISpecAssert.assertFalse(window.isVisible());
  }

  public Trigger getValidate() {
    return window.getButton("ok").triggerClick();
  }
}
