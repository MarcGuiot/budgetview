package org.designup.picsou.functests.checkers;

import org.uispec4j.TextBox;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;

public class AccountPositionThresholdChecker extends GuiChecker {
  private Window window;

  public AccountPositionThresholdChecker(Window window) {
    this.window = window;
  }

  public AccountPositionThresholdChecker changeAmount(double amount) {
    final TextBox textField = window.getInputTextBox("editor");
    final String text = toString(amount);

    textField.clear();
    textField.appendText(text);
    return this;
  }

  public void setAmountAndClose(double amount) {
    final TextBox textField = window.getInputTextBox("editor");
    final String text = toString(amount);
    textField.setText(text);
    assertFalse(window.isVisible());
  }

  public void close() {
    window.getButton("OK").click();
    assertFalse(window.isVisible());
  }
}
