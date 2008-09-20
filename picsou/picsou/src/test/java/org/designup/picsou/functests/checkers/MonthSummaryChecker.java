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
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;

import java.awt.*;

public class MonthSummaryChecker extends DataChecker {
  private Window window;

  public MonthSummaryChecker(Window window) {
    this.window = window;
  }

  public Summary init() {
    Panel panel = window.getPanel("monthSummaryView");
    return new Summary(panel);
  }

  public MonthDetail initDetails() {
    return new MonthDetail(window);
  }

  public class MonthDetail {
    private Window panel;

    public MonthDetail(Window panel) {
      this.panel = panel;
    }

    public MonthDetail balance(double amount) {
      return check(amount, "detailBalance");
    }

    private MonthDetail check(double amount, String name) {
      TextBox textBox = panel.getTextBox(name);
      assertThat(textBox.textEquals(MonthSummaryChecker.this.toString(amount)));
      return this;
    }

    public MonthDetail income(double amount) {
      return check(amount, "detailIncome");
    }

    public MonthDetail fixe(double amount) {
      return check(amount, "detailFixe");
    }

    public MonthDetail saving(double amount) {
      return check(amount, "detailSaving");
    }

    public MonthDetail total(double amount) {
      return check(amount, "detailTotal");
    }
  }

  public class Summary {
    private Panel panel;

    public Summary(Panel panel) {
      this.panel = panel;
    }

    public Summary checkNoBudgetAreasDisplayed() {
      Component[] components = panel.getSwingComponents(Gauge.class);
      Assert.assertTrue(components.length == 0);
      return this;
    }

    public Summary checkNoHelpMessageDisplayed() {
      UISpecAssert.assertFalse(panel.containsComponent(ComponentMatchers.innerNameIdentity("noData")));
      UISpecAssert.assertFalse(panel.containsComponent(ComponentMatchers.innerNameIdentity("noSeries")));
      return this;
    }

    public Summary checkNoDataMessage(String text) {
      return checkMessage(text, "noData");
    }

    public Summary checkNoSeriesMessage(String text) {
      return checkMessage(text, "noSeries");
    }

    private Summary checkMessage(String text, final String panelName) {
      TextBox textBox = window.getPanel(panelName).getTextBox();
      UISpecAssert.assertThat(textBox.textEquals(text));
      return this;
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

    public void categorizeAll() {
      panel.getButton("categorizeAll").click();
    }

    public ImportChecker openImport() {
      Window dialog = WindowInterceptor.getModalDialog(panel.getButton("import").triggerClick());
      return new ImportChecker(dialog);
    }
  }
}
