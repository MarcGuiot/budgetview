package org.designup.picsou.functests.checkers;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.gui.components.BalanceGraph;
import org.uispec4j.Panel;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import junit.framework.Assert;

public class MonthSummaryChecker extends DataChecker {
  private Window window;

  public MonthSummaryChecker(Window window) {
    this.window = window;
  }

  public Summary init() {
    Panel panel = window.getPanel("monthSummaryView");
    return new Summary(panel);
  }

  public class Summary {
    private Panel panel;

    public Summary(Panel panel) {
      this.panel = panel;
    }

    public Summary checkRecurring(double amount) {
      check(BudgetArea.RECURRING_EXPENSES.getName(), amount);
      return this;
    }

    public Summary checkPlannedRecurring(double amount) {
      checkPlanned(BudgetArea.RECURRING_EXPENSES.getName(), amount);
      return this;
    }

    public Summary checkEnvelope(double amount) {
      check(BudgetArea.EXPENSES_ENVELOPE.getName(), amount);
      return this;
    }

    public Summary checkOccasional(double amount) {
      check(BudgetArea.OCCASIONAL_EXPENSES.getName(), amount);
      return this;
    }

    public Summary checkIncome(double amount) {
      check(BudgetArea.INCOME.getName(), amount);
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

    public Summary total(double received, double spent, boolean receivedGreaterThanExpenses) {
      UISpecAssert.assertThat(panel.getTextBox("totalSpentAmount")
        .textEquals(MonthSummaryChecker.this.toString(spent)));
      UISpecAssert.assertThat(panel.getTextBox("totalReceivedAmount")
        .textEquals(MonthSummaryChecker.this.toString(received)));
      BalanceGraph balanceGraph = (BalanceGraph)panel.getSwingComponents(BalanceGraph.class)[0];
      if (receivedGreaterThanExpenses) {
        Assert.assertEquals(1.0, balanceGraph.getReceivedPercent());
        Assert.assertEquals(spent / received, balanceGraph.getSpentPercent(), 0.1);
      }
      else {
        Assert.assertEquals(received/spent, balanceGraph.getReceivedPercent(), 0.1);
        Assert.assertEquals(1.0, balanceGraph.getSpentPercent());
      }
      return this;
    }
  }
}
