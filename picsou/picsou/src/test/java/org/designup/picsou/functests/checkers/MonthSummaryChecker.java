package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.BudgetArea;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;

public class MonthSummaryChecker extends DataChecker {
  private Window window;

  public MonthSummaryChecker(Window window) {
    this.window = window;
  }

  public Summary on(String month) {
    Panel panel = window.getPanel("monthSummaryView");
    TextBox monthLabel = panel.getTextBox("monthLabel");
    UISpecAssert.assertThat(monthLabel.textEquals(month));
    return new Summary(panel);
  }

  public class Summary {
    private Panel panel;

    public Summary(Panel panel) {
      this.panel = panel;
    }

    public Summary checkRecurring(double amount) {
      check(BudgetArea.RECURRING_EXPENSES.getGlob().get(BudgetArea.NAME), amount);
      return this;
    }

    public Summary checkPlannedRecurring(double amount) {
      checkPlanned(BudgetArea.RECURRING_EXPENSES.getGlob().get(BudgetArea.NAME), amount);
      return this;
    }

    public Summary checkEnvelope(double amount) {
      check(BudgetArea.EXPENSES_ENVELOPE.getGlob().get(BudgetArea.NAME), amount);
      return this;
    }

    public Summary checkOccasional(double amount) {
      check(BudgetArea.OCCASIONAL_EXPENSES.getGlob().get(BudgetArea.NAME), amount);
      return this;
    }

    public Summary checkIncome(double amount) {
      check(BudgetArea.INCOME.getGlob().get(BudgetArea.NAME), amount);
      return this;
    }

    private void check(String budgetAreaName, double amount) {
      UISpecAssert.assertThat(panel.getTextBox(budgetAreaName).getContainer("budgetAreaRow")
        .getTextBox("budgetAreaAmount").textEquals(MonthSummaryChecker.this.toString(amount)));
    }

    private void checkPlanned(String budgetAreaName, double amount) {
      UISpecAssert.assertThat(panel.getTextBox(budgetAreaName).getContainer("budgetAreaRow")
        .getTextBox("budgetAreaPlannedAmount").textEquals(MonthSummaryChecker.this.toString(amount)));
    }

    public Summary total(double received, double expence) {
      UISpecAssert.assertThat(panel.getTextBox("totalSpentAmount")
        .textEquals(MonthSummaryChecker.this.toString(expence)));
      UISpecAssert.assertThat(panel.getTextBox("totalReceivedAmount")
        .textEquals(MonthSummaryChecker.this.toString(received)));
      return this;
    }
  }
}
