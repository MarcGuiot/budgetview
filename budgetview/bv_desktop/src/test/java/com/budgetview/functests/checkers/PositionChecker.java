package com.budgetview.functests.checkers;

import com.budgetview.desktop.description.Formatting;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

import static org.uispec4j.assertion.UISpecAssert.assertThat;

public class PositionChecker extends GuiChecker {
  private Window window;

  public PositionChecker(Window window) {
    this.window = window;
  }

  public PositionChecker checkPast(double amount) {
    assertThat(window.getTextBox("positionPastAmount").textContains(Formatting.toString(amount)));
    return this;
  }

  public PositionChecker checkPresent(double amount, double income, double expence, double savings, double enfOfMonth) {
    assertThat(window.getTextBox("bankPositionAmount").textContains(Formatting.toString(amount)));
    assertThat(window.getTextBox("waitedIncomeAmount").textContains(Formatting.toString(income)));
    assertThat(window.getTextBox("waitedExpenseAmount").textContains(Formatting.toString(expence)));
    assertThat(window.getTextBox("waitedSavingsAmountToMain").textContains(Formatting.toString(savings)));
    assertThat(window.getTextBox("estimatedPosition").textContains(Formatting.toString(enfOfMonth)));
    return this;
  }

  public PositionChecker checkPosition(double amount) {
    UISpecAssert.assertThat(window.getTextBox("estimatedPosition").textContains(Formatting.toString(amount)));
    return this;
  }

  public PositionChecker checkIncome(double amount) {
    UISpecAssert.assertThat(window.getTextBox("waitedIncomeAmount").textContains(Formatting.toString(amount)));
    return this;
  }

  public PositionChecker checkFixed(double amount) {
    System.out.println("PositionChecker.checkFixed: TBD?");
    return this;
  }

  public PositionChecker checkVariable(double amount) {
    System.out.println("PositionChecker.checkVariable: TBD?");
    return this;
  }

  public PositionChecker checkExpense(double amount) {
    UISpecAssert.assertThat(window.getTextBox("waitedExpenseAmount").textContains(Formatting.toString(amount)));
    return this;
  }

  public PositionChecker checkSavingsIn(double amount) {
    UISpecAssert.assertThat(window.getTextBox("waitedSavingsAmountToMain").textContains(Formatting.toString(amount)));
    return this;
  }

  public PositionChecker checkSavingsOut(double amount) {
    UISpecAssert.assertThat(window.getTextBox("waitedSavingsAmountFromMain").textContains(Formatting.toString(amount)));
    return this;
  }

  public PositionChecker checkInitialPosition(double positionAmount) {
    UISpecAssert.assertThat(window.getTextBox("bankPositionAmount").textContains(Formatting.toString(positionAmount)));
    return this;
  }

  public PositionChecker checkBalanceZeroWithSavings() {
    assertThat(window.getTextBox("positionPanelLimit")
                 .textContains("You can reduce your expenses to reinforce your savings"));
    return this;
  }

  public PositionChecker checkBalanceZeroWithoutSavings() {
    assertThat(window.getTextBox("positionPanelLimit")
                 .textContains("You can reduce your expenses to open a savings account"));
    return this;
  }

  public PositionChecker checkSavingsExpected() {
    assertThat(window.getTextBox("positionPanelLimit")
                 .textContains("You can to more savings"));
    return this;
  }

  public PositionChecker checkOpenSavings() {
    assertThat(window.getTextBox("positionPanelLimit")
                 .textContains("Open a savings account"));
    return this;
  }

  public PositionChecker checkTooMuchExpence() {
    assertThat(window.getTextBox("positionPanelLimit")
                 .textContains("You should reduce your expence"));
    return this;
  }

  public void close() {
    window.getButton().click();
    UISpecAssert.assertFalse(window.isVisible());
  }
}
