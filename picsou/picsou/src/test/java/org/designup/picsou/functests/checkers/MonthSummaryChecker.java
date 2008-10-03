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
import static org.uispec4j.assertion.UISpecAssert.*;
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

  public MonthSummaryChecker checkNoDataMessage(String text) {
    return checkMessage(text, "noDataMessage", "noData");
  }

  public MonthSummaryChecker checkNoSeriesMessage(String text) {
    return checkMessage(text, "noSeriesMessage1", "noSeries");
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

  public MonthSummaryChecker checkEnvelope(double amount) {
    check(BudgetArea.ENVELOPES, amount);
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
    Gauge gauge = getPanel().findSwingComponent(Gauge.class, budgetArea.getName() + ":budgetAreaGauge");
    Assert.assertEquals(amount, gauge.getActualValue(), 0.01);
    Assert.assertEquals(planned, gauge.getTargetValue(), 0.01);
  }

  public MonthSummaryChecker total(double received, double spent, boolean receivedGreaterThanExpenses) {
    assertThat(getPanel().getTextBox("totalSpentAmount").textEquals(MonthSummaryChecker.this.toString(spent)));
    assertThat(getPanel().getTextBox("totalReceivedAmount").textEquals(MonthSummaryChecker.this.toString(received)));
    BalanceGraph balanceGraph = (BalanceGraph)getPanel().getSwingComponents(BalanceGraph.class)[0];
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

  public void categorizeAll() {
    getPanel().getButton("categorizeAll").click();
  }

  public ImportChecker openImport() {
    Window dialog = WindowInterceptor.getModalDialog(getPanel().getButton("import").triggerClick());
    return new ImportChecker(dialog);
  }

  private Panel getPanel() {
    return window.getPanel("monthSummaryView");
  }

  public HelpChecker openHelp() {
    return HelpChecker.open(window.getButton("help").triggerClick());
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
