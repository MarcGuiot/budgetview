package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.description.Formatting;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public class BalanceChecker extends GuiChecker {
  private Window window;

  public BalanceChecker(Window window) {
    this.window = window;
  }

  public BalanceChecker checkBalance(double budgetBalance, Double realBalance) {
    UISpecAssert.assertThat(window.getTextBox("budgetBalanceLabel").textContains(Formatting.toString(budgetBalance)));
    if (realBalance == null) {
      UISpecAssert.assertFalse(window.getTextBox("realBalanceLabel").isVisible());
    }
    else {
      UISpecAssert.assertThat(window.getTextBox("realBalanceLabel").textContains(Formatting.toString(realBalance)));
    }
    return this;
  }

  public void close() {
    window.getButton().click();
    UISpecAssert.assertFalse(window.isVisible());
  }
}
