package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.model.BudgetArea;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

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
      check(BudgetArea.RECURRING_EXPENSES, amount);
      return this;
    }

    public Summary checkPlannedRecurring(double amount) {
      checkPlanned(BudgetArea.RECURRING_EXPENSES, amount);
      return this;
    }

    public Summary checkEnvelope(double amount) {
      check(BudgetArea.EXPENSES_ENVELOPE, amount);
      return this;
    }

    public Summary checkOccasional(double amount, double planned) {
      checkBudgetArea(BudgetArea.OCCASIONAL_EXPENSES, amount, planned);
      return this;
    }

    public Summary checkOccasional(double amount) {
      check(BudgetArea.OCCASIONAL_EXPENSES, amount);
      return this;
    }

    public Summary checkIncome(double amount, double planned) {
      checkBudgetArea(BudgetArea.INCOME, amount, planned);
      return this;
    }

    public Summary checkIncome(double amount) {
      check(BudgetArea.INCOME, amount);
      return this;
    }

    private void checkBudgetArea(BudgetArea budgetArea, double amount, double planned) {
      check(budgetArea, amount);
      checkPlanned(budgetArea, planned);
      checkGauge(budgetArea, amount, planned);
    }

    private void check(BudgetArea budgetArea, double amount) {
      TextBox textBox = panel.getTextBox(budgetArea.getName() + ":budgetAreaAmount");
      assertThat(textBox.textEquals(MonthSummaryChecker.this.toString(amount)));
    }

    private void checkPlanned(BudgetArea budgetArea, double amount) {
      TextBox textBox = panel.getTextBox(budgetArea.getName() + ":budgetAreaPlannedAmount");
      assertThat(textBox.textEquals(MonthSummaryChecker.this.toString(amount)));
    }

    private void checkGauge(BudgetArea budgetArea, double amount, double planned) {
      Gauge gauge = panel.findSwingComponent(Gauge.class, budgetArea.getName() + ":budgetAreaGauge");
      Assert.assertEquals(amount, gauge.getActualValue(), 0.01);
      Assert.assertEquals(planned, gauge.getTargetValue(), 0.01);
    }

    public Summary total(double received, double spent, boolean receivedGreaterThanExpenses) {
      assertThat(panel.getTextBox("totalSpentAmount").textEquals(MonthSummaryChecker.this.toString(spent)));
      assertThat(panel.getTextBox("totalReceivedAmount").textEquals(MonthSummaryChecker.this.toString(received)));
      BalanceGraph balanceGraph = (BalanceGraph)panel.getSwingComponents(BalanceGraph.class)[0];
      if (receivedGreaterThanExpenses) {
        Assert.assertEquals(1.0, balanceGraph.getReceivedPercent());
        Assert.assertEquals(spent / received, balanceGraph.getSpentPercent(), 0.1);
      }
      else {
        Assert.assertEquals(received / spent, balanceGraph.getReceivedPercent(), 0.1);
        Assert.assertEquals(1.0, balanceGraph.getSpentPercent());
      }
      return this;
    }

    public Summary checkUncategorized(String amount) {
      assertThat(panel.getTextBox("uncategorizedAmountLabel").textEquals(amount));
      return this;
    }

    public Summary checkNoUncategorized() {
      UISpecAssert.assertFalse(panel.getTextBox("uncategorizedAmountLabel").isVisible());
      UISpecAssert.assertFalse(panel.getButton("To Categorize").isVisible());
      return this;
    }

    public void categorize() {
      panel.getButton("categorize").click();
    }
  }
}
