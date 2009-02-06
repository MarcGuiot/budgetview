package org.designup.picsou.functests.checkers;

import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.model.Month;
import org.uispec4j.*;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class EstimatedPositionDetailsChecker extends GuiChecker {
  private Window window;

  public EstimatedPositionDetailsChecker(Window window) {
    this.window = window;
  }

  public EstimatedPositionDetailsChecker checkInitialPosition(double amount) {
    TextBox textBox = window.getTextBox("initialPosition");
    assertThat(textBox.textEquals(toString(amount, false)));
    return this;
  }

  public EstimatedPositionDetailsChecker checkIncome(double amount) {
    return check(amount, "remainingIncome");
  }

  public EstimatedPositionDetailsChecker checkFixed(double amount) {
    return check(amount, "remainingFixed");
  }

  public EstimatedPositionDetailsChecker checkEnvelope(double amount) {
    return check(amount, "remainingEnvelope");
  }

  public EstimatedPositionDetailsChecker checkSavingsIn(double amount) {
    return check(amount, "remainingInSavings");
  }

  public EstimatedPositionDetailsChecker checkSavingsOut(double amount) {
    return check(amount, "remainingOutSavings");
  }

  public EstimatedPositionDetailsChecker checkOccasional(double amount) {
    return check(amount, "remainingOccasional");
  }

  public EstimatedPositionDetailsChecker checkProjects(double amount) {
    return check(amount, "remainingSpecial");
  }

  public EstimatedPositionDetailsChecker checkFutureTotalLabel(int monthId) {
    TextBox textBox = window.getTextBox("amountSummary");
    assertThat(textBox.textEquals("Estimated balance at " + Formatting.toString(Month.getLastDay(monthId))));
    return this;
  }

  public EstimatedPositionDetailsChecker checkTotalLabel(int monthId) {
    TextBox textBox = window.getTextBox("amountSummary");
    assertThat(textBox.textEquals("Balance at " + Formatting.toString(Month.getLastDay(monthId))));
    return this;
  }

  public EstimatedPositionDetailsChecker checkTotal(double amount) {
    TextBox textBox = window.getTextBox("estimatedPosition");
    assertThat(textBox.textEquals(toString(amount, false)));
    return this;
  }

  private EstimatedPositionDetailsChecker check(double amount, String name) {
    TextBox textBox = window.getTextBox(name);
    assertThat(textBox.textEquals(toString(amount)));
    return this;
  }

  public void close() {
    window.getButton("Close").click();
  }
}