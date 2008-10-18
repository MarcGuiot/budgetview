package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Month;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

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
    assertThat(textBox.textEquals("Estimated balance at " + Formatting.toString(Month.getLastDay(monthId))));
    return this;
  }

  public BalanceSummaryChecker checkTotalLabel(int monthId) {
    TextBox textBox = getPanel().getTextBox("amountSummaryLabel");
    assertThat(textBox.textEquals("Balance at " + Formatting.toString(Month.getLastDay(monthId))));
    return this;
  }

  public BalanceSummaryChecker checkTotal(double amount) {
    TextBox textBox = getPanel().getTextBox("totalLabel");
    assertThat(textBox.textEquals(toString(amount, false)));
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
    assertThat(textBox.textEquals(toString(amount)));
    return this;
  }

  public BalanceSummaryChecker checkLimit(double amount) {
    Button button = getPanel().getButton("accountBalanceLimit");
    assertThat(button.textEquals("Limit: " + toString(amount)));
    return this;
  }

  public BalanceSummaryChecker checkTotalColor(String color) {
    TextBox textBox = getPanel().getTextBox("totalLabel");
    assertThat(textBox.foregroundNear(color));
    return this;
  }

  public BalanceSummaryChecker setLimit(final double amount, final boolean validateThroughTextField) {
    WindowInterceptor.init(getPanel().getButton("accountBalanceLimit").triggerClick())
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          final TextBox textField = window.getInputTextBox("editor");
          final String text = BalanceSummaryChecker.this.toString(amount);

          if (validateThroughTextField) {
            return new Trigger() {
              public void run() throws Exception {
                textField.setText(text);
              }
            };
          }
          else {
            textField.clear();
            textField.appendText(text);
            return window.getButton("OK").triggerClick();
          }
        }
      })
      .run();
    return this;
  }
}