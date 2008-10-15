package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.model.Month;
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
  }

  public BalanceSummaryChecker checkBalance(double amount) {
    TextBox textBox = getPanel().getTextBox("balanceLabel");
    assertThat(textBox.textEquals(toString(amount, false)));
    return this;
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

  public BalanceSummaryChecker checkOccasional(double amount) {
    return check(amount, "occasionalLabel");
  }

  public BalanceSummaryChecker checkProjects(double amount) {
    return check(amount, "specialLabel");
  }

  public BalanceSummaryChecker checkEnvelope(double amount) {
    return check(amount, "envelopeLabel");
  }

  public BalanceSummaryChecker checkFutureTotalLabel(int monthId) {
    TextBox textBox = getPanel().getTextBox("amountSummaryLabel");
    assertThat(textBox.textEquals("Estimated balance at " + PicsouDescriptionService.toString(Month.getLastDay(monthId))));
    return this;
  }

  public BalanceSummaryChecker checkPastTotalLabel(int monthId) {
    TextBox textBox = getPanel().getTextBox("amountSummaryLabel");
    assertThat(textBox.textEquals("Balance at " + PicsouDescriptionService.toString(Month.getLastDay(monthId))));
    return this;
  }

  public BalanceSummaryChecker checkTotal(double amount) {
    TextBox textBox = getPanel().getTextBox("totalLabel");
    assertThat(textBox.textEquals(toString(amount, true)));
    return this;
  }

  public BalanceSummaryChecker checkNoTotal() {
    TextBox textBox = getPanel().getTextBox("totalLabel");
    assertThat(textBox.textEquals(""));
    return this;
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