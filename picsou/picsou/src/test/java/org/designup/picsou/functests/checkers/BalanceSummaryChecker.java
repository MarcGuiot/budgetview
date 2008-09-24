package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class BalanceSummaryChecker extends DataChecker {
  private Window window;

  public BalanceSummaryChecker(Window window) {
    this.window = window;
  }

  public void checkNothingShown() {
    Panel panel = getPanel();
    UISpecAssert.assertFalse(panel.getPanel("content").isVisible());
    UISpecAssert.assertTrue(panel.getTextBox("totalLabel").textIsEmpty());
  }

  public BalanceSummaryChecker checkBalance(double amount) {
    return check(amount, "balanceLabel");
  }

  public BalanceSummaryChecker checkIncome(double amount) {
    return check(amount, "incomeLabel");
  }

  public BalanceSummaryChecker checkFixed(double amount) {
    return check(amount, "fixedLabel");
  }

  public BalanceSummaryChecker checkSavings(double amount) {
    return check(amount, "savingsLabel");
  }

  public BalanceSummaryChecker checkTotal(double amount) {
    return check(amount, "totalLabel");
  }
  private Panel getPanel() {
    return window.getPanel("balanceSummary");
  }

  private BalanceSummaryChecker check(double amount, String name) {
    TextBox textBox = getPanel().getTextBox(name);
    assertThat(textBox.textEquals(BalanceSummaryChecker.this.toString(amount)));
    return this;
  }
}