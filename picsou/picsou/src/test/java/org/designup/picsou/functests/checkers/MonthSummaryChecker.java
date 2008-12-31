package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.BalanceGraph;
import org.designup.picsou.gui.components.Gauge;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.utils.Lang;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.finder.ComponentMatchers;

import javax.swing.*;
import java.awt.*;

public class MonthSummaryChecker extends GuiChecker {
  private Window window;
  public final BalanceGraphChecker balanceGraph;

  public final BudgetAreaChecker income = new BudgetAreaChecker(BudgetArea.INCOME);
  public final BudgetAreaChecker recurring = new BudgetAreaChecker(BudgetArea.RECURRING);
  public final BudgetAreaChecker envelopes = new BudgetAreaChecker(BudgetArea.ENVELOPES);
  public final BudgetAreaChecker savings = new BudgetAreaChecker(BudgetArea.SAVINGS);
  public final BudgetAreaChecker special = new BudgetAreaChecker(BudgetArea.SPECIAL);
  public final BudgetAreaChecker occasional = new BudgetAreaChecker(BudgetArea.OCCASIONAL);

  public MonthSummaryChecker(Window window) {
    this.window = window;
    this.balanceGraph = new BalanceGraphChecker(window);
  }

  public class BudgetAreaChecker {
    private BudgetArea budgetArea;

    public BudgetAreaChecker(BudgetArea budgetArea) {
      this.budgetArea = budgetArea;
    }

    public BudgetAreaChecker checkObserved(double amount) {
      MonthSummaryChecker.this.checkObserved(budgetArea, amount);
      return this;
    }

    public BudgetAreaChecker checkPlanned(double amount) {
      MonthSummaryChecker.this.checkPlanned(budgetArea, amount);
      return this;
    }

    public BudgetAreaChecker checkValues(double observed, double planned) {
      checkObserved(observed);
      checkPlanned(planned);
      return this;
    }

    public BudgetAreaChecker checkGauge(double actual, double target) {
      MonthSummaryChecker.this.checkGauge(budgetArea, actual, target);
      return this;
    }

    public BudgetAreaChecker checkGaugeOverrun(double actual, double target, double overrunPart) {
      MonthSummaryChecker.this.checkGauge(budgetArea, actual, target, overrunPart);
      return this;
    }

    public BudgetAreaChecker checkErrorOverrun() {
      TextBox plannedLabel = getPlannedLabel(budgetArea);
      assertThat(plannedLabel.foregroundNear("darkRed"));
      return this;
    }

    public BudgetAreaChecker checkPositiveOverrun() {
      TextBox plannedLabel = getPlannedLabel(budgetArea);
      assertThat(plannedLabel.foregroundNear("darkBlue"));
      return this;
    }
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
    checkObserved(BudgetArea.RECURRING, amount);
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
    checkObserved(BudgetArea.ENVELOPES, amount);
    return this;
  }

  public MonthSummaryChecker checkEnvelope(double amount, double planned) {
    checkBudgetArea(BudgetArea.ENVELOPES, amount, planned);

    TextBox plannedLabel = getPlannedLabel(BudgetArea.ENVELOPES);
    assertThat(plannedLabel.foregroundNear("77787E"));
    assertThat(plannedLabel.tooltipEquals(Lang.get("monthsummary.planned.tooltip.normal")));
    return this;
  }

  public MonthSummaryChecker checkOccasional(double amount, double planned) {
    checkBudgetArea(BudgetArea.OCCASIONAL, amount, planned);
    return this;
  }

  public MonthSummaryChecker checkOccasional(double amount) {
    checkObserved(BudgetArea.OCCASIONAL, amount);
    return this;
  }

  public MonthSummaryChecker checkIncome(double amount, double planned) {
    checkBudgetArea(BudgetArea.INCOME, amount, planned);
    return this;
  }

  public MonthSummaryChecker checkIncome(double amount) {
    checkObserved(BudgetArea.INCOME, amount);
    return this;
  }

  public MonthSummaryChecker checkProjects(double amount) {
    checkObserved(BudgetArea.SPECIAL, amount);
    return this;
  }

  public MonthSummaryChecker checkProjects(String amount) {
    checkObserved(BudgetArea.SPECIAL, amount);
    return this;
  }

  public MonthSummaryChecker checkProjectsPlanned(String amount) {
    checkPlanned(BudgetArea.SPECIAL, amount);
    return this;
  }

  public MonthSummaryChecker checkProjects(double amount, double planned) {
    checkBudgetArea(BudgetArea.SPECIAL, amount, planned);
    return this;
  }

  private void checkBudgetArea(BudgetArea budgetArea, double amount, double planned) {
    checkObserved(budgetArea, amount);
    checkPlanned(budgetArea, planned);
    checkGauge(budgetArea, amount, planned);
  }

  public void gotoTransactions(BudgetArea budgetArea) {
    Button button = getPanel().getButton(budgetArea.getName() + ":budgetAreaAmount");
    button.click();
  }

  private void checkObserved(BudgetArea budgetArea, double amount) {
    final String text = toString(amount);
    checkObserved(budgetArea, text);
  }

  private void checkObserved(BudgetArea budgetArea, String text) {
    Button button = getPanel().getButton(budgetArea.getName() + ":budgetAreaAmount");
    assertThat(button.textEquals(text));
  }

  private void checkPlanned(BudgetArea budgetArea, double amount) {
    final String text = toString(amount);
    checkPlanned(budgetArea, text);
  }

  private void checkPlanned(BudgetArea budgetArea, String text) {
    TextBox textBox = getPlannedLabel(budgetArea);
    assertThat(textBox.textEquals(text));
  }

  private TextBox getPlannedLabel(BudgetArea budgetArea) {
    return getPanel().getTextBox(budgetArea.getName() + ":budgetAreaPlannedAmount");
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
    return ImportChecker.open(getPanel().getButton("import").triggerClick());
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
