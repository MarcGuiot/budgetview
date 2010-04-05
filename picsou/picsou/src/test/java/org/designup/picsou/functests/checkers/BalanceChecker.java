package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.description.Formatting;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public class BalanceChecker extends GuiChecker {
  private Window window;

  public BalanceChecker(Window window) {
    this.window = window;
  }

  public BalanceChecker check(double beginOfMonth, Double shift, double balance, double endOfMonth) {
    UISpecAssert.assertThat(window.getTextBox("beginOfMonthAmount").textContains(Formatting.toString(beginOfMonth)));
    if (shift != null) {
      UISpecAssert.assertThat(window.getTextBox("shiftAmount").textContains(Formatting.toString(shift)));
    }
    else {
      UISpecAssert.assertFalse(window.getTextBox("shiftAmount").isVisible());
    }
    UISpecAssert.assertThat(window.getTextBox("balanceAmountExplain").textContains(Formatting.toString(balance)));
    UISpecAssert.assertThat(window.getTextBox("endOfMonthAmount").textContains(Formatting.toString(endOfMonth)));
    return this;
  }

  public void close() {
    window.getButton().click();
    UISpecAssert.assertFalse(window.isVisible());
  }
}
