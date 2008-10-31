package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.model.BudgetArea;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.and;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;
import java.awt.*;

public class MonthSummaryChecker extends DataChecker {
  private Window window;

  public MonthSummaryChecker(Window window) {
    this.window = window;
  }

  public void gotoBudget(BudgetArea budgetArea) {
    getPanel().getButton(budgetArea.getName()).click();
  }

  public MonthSummaryChecker checkNoBudgetAreasDisplayed() {
    Component[] components = getPanel().getSwingComponents(Gauge.class);
    Assert.assertTrue(components.length == 0);
    return this;
  }

  public MonthSummaryChecker checkNoHelpMessageDisplayed() {
    UISpecAssert.assertFalse(getPanel().containsComponent(ComponentMatchers.innerNameIdentity("noData")));
    UISpecAssert.assertFalse(getPanel().containsComponent(ComponentMatchers.innerNameIdentity("noSeries")));
    return this;
  }

  public MonthSummaryChecker checkNoDataMessage() {
    return checkMessage("You must import your financial operations", "noDataMessage", "noData");
  }

  public MonthSummaryChecker checkNoSeriesMessage() {
    return checkMessage("Use the series wizard:", "noSeriesMessage", "noSeries");
  }

  private MonthSummaryChecker checkMessage(String text, String textBoxName, final String panelName) {
    TextBox textBox = window.getPanel(panelName).getTextBox(textBoxName);
    UISpecAssert.assertThat(textBox.textEquals(text));
    return this;
  }

  public MonthSummaryChecker checkRecurring(double amount) {
    check(BudgetArea.RECURRING, amount);
    return this;
  }

  public MonthSummaryChecker checkPlannedRecurring(double amount) {
    checkPlanned(BudgetArea.RECURRING, amount);
    return this;
  }

  public MonthSummaryChecker checkRecurring(double amount, double planned) {
    checkBudgetArea(BudgetArea.RECURRING, amount, planned);
    return this;
  }

  public MonthSummaryChecker checkSavings(double amount, double planned) {
    checkBudgetArea(BudgetArea.SAVINGS, amount, planned);
    return this;
  }

  public MonthSummaryChecker checkEnvelope(double amount) {
    check(BudgetArea.ENVELOPES, amount);
    return this;
  }

  public MonthSummaryChecker checkEnvelope(double amount, double planned) {
    checkBudgetArea(BudgetArea.ENVELOPES, amount, planned);
    return this;
  }

  public MonthSummaryChecker checkEnvelope(double amount, double planned, double overrunPart) {
    checkBudgetArea(BudgetArea.ENVELOPES, amount, planned, overrunPart);
    return this;
  }

  public MonthSummaryChecker checkOccasional(double amount, double planned) {
    checkBudgetArea(BudgetArea.OCCASIONAL, amount, planned);
    return this;
  }

  public MonthSummaryChecker checkOccasional(double amount) {
    check(BudgetArea.OCCASIONAL, amount);
    return this;
  }

  public MonthSummaryChecker checkIncome(double amount, double planned) {
    checkBudgetArea(BudgetArea.INCOME, amount, planned);
    return this;
  }

  public MonthSummaryChecker checkIncome(double amount) {
    check(BudgetArea.INCOME, amount);
    return this;
  }

  public MonthSummaryChecker checkProjects(double amount) {
    check(BudgetArea.SPECIAL, amount);
    return this;
  }

  private void checkBudgetArea(BudgetArea budgetArea, double amount, double planned) {
    check(budgetArea, amount);
    checkPlanned(budgetArea, planned);
    checkGauge(budgetArea, amount, planned);
  }

  private void checkBudgetArea(BudgetArea budgetArea, double amount, double planned, double overrun) {
    check(budgetArea, amount);
    checkPlanned(budgetArea, planned);
    checkGauge(budgetArea, amount, planned, overrun);
  }

  public void gotoTransactions(BudgetArea budgetArea) {
    Button button = getPanel().getButton(budgetArea.getName() + ":budgetAreaAmount");
    button.click();
  }

  private void check(BudgetArea budgetArea, double amount) {
    Button button = getPanel().getButton(budgetArea.getName() + ":budgetAreaAmount");
    assertThat(button.textEquals(MonthSummaryChecker.this.toString(amount)));
  }

  private void checkPlanned(BudgetArea budgetArea, double amount) {
    TextBox textBox = getPanel().getTextBox(budgetArea.getName() + ":budgetAreaPlannedAmount");
    assertThat(textBox.textEquals(MonthSummaryChecker.this.toString(amount)));
  }

  private void checkGauge(BudgetArea budgetArea, double amount, double planned) {
    GaugeChecker gauge = new GaugeChecker(getPanel(), budgetArea.getName() + ":budgetAreaGauge");
    gauge.checkActualValue(amount);
    gauge.checkTargetValue(planned);
  }

  private void checkGauge(BudgetArea budgetArea, double amount, double planned, double overrun) {
    GaugeChecker gauge = new GaugeChecker(getPanel(), budgetArea.getName() + ":budgetAreaGauge");
    gauge.checkActualValue(amount);
    gauge.checkTargetValue(planned);
    gauge.checkOverrunPart(overrun);
  }

  public MonthSummaryChecker total(double received, double spent) {
    checkBalance(received - spent);
    spent = Math.abs(spent);
    received = Math.abs(received);
    if (received > spent) {
      checkBalanceGraph(1., spent / received);
    }
    else {
      checkBalanceGraph(received / spent, 1.);
    }
    return this;
  }

  public MonthSummaryChecker checkBalanceGraph(double incomePercent, double expensesPercent) {
    BalanceGraph graph = getPanel().findSwingComponent(BalanceGraph.class);
    String actual = "Actual: " + graph.getIncomePercent() + " / " + graph.getExpensesPercent();
    Assert.assertEquals(actual, incomePercent, graph.getIncomePercent(), 0.01);
    Assert.assertEquals(actual, expensesPercent, graph.getExpensesPercent(), 0.01);
    return this;
  }

  public MonthSummaryChecker checkBalance(double balance) {
    assertThat(getPanel().getTextBox("balanceAmount").textEquals(toString(balance, true)));
    return this;
  }

  public MonthSummaryChecker checkEmptyBalance() {
    UISpecAssert.assertThat(getPanel().getTextBox("balanceAmount").textEquals(""));
    return this;
  }

  public MonthSummaryChecker checkUncategorized(String amount) {
    assertThat(getPanel().getTextBox("uncategorizedAmountLabel").textEquals(amount));
    return this;
  }

  public MonthSummaryChecker checkNoUncategorized() {
    UISpecAssert.assertFalse(getPanel().getTextBox("uncategorizedAmountLabel").isVisible());
    UISpecAssert.assertFalse(getPanel().getButton("To Categorize").isVisible());
    return this;
  }

  public void categorize() {
    getPanel().getButton("categorize").click();
  }

  public ImportChecker openImport() {
    Window dialog = WindowInterceptor.getModalDialog(getPanel().getButton("import").triggerClick());
    return new ImportChecker(dialog);
  }

  private Panel getPanel() {
    return window.getPanel("monthSummaryView");
  }

  public HelpChecker openImportHelp() {
    return HelpChecker.open(window.getTextBox("noDataMessage").triggerClickOnHyperlink("import"));
  }

  public SeriesWizardChecker openSeriesWizard() {
    return SeriesWizardChecker.open(getPanel().getButton("openSeriesWizard").triggerClick());
  }

  public void checkSeriesWizardButtonVisible(boolean visible) {
    if (visible) {
      Button button = getPanel().getButton("openSeriesWizard");
      assertThat(and(button.isVisible(), button.isEnabled()));
    }
    else {
      checkComponentVisible(getPanel(), JButton.class, "openSeriesWizard", false);
    }
  }
}
