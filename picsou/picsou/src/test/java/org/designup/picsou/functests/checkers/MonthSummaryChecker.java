package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.designup.picsou.gui.components.charts.BalanceGraph;
import org.designup.picsou.gui.components.charts.Gauge;
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
  public final BalanceGraphChecker mainBalanceGraph;
  public final BalanceGraphChecker savingsBalanceGraph;

  public final BudgetAreaChecker income = new BudgetAreaChecker(BudgetArea.INCOME);
  public final BudgetAreaChecker recurring = new BudgetAreaChecker(BudgetArea.RECURRING);
  public final BudgetAreaChecker envelopes = new BudgetAreaChecker(BudgetArea.ENVELOPES);
  public final BudgetAreaChecker savings = new BudgetAreaChecker(BudgetArea.SAVINGS);
  public final BudgetAreaChecker special = new BudgetAreaChecker(BudgetArea.SPECIAL);

  public MonthSummaryChecker(Window window) {
    this.window = window;
    this.mainBalanceGraph = new BalanceGraphChecker("mainAccountsTotalBalance", window);
    this.savingsBalanceGraph = new BalanceGraphChecker("savingsTotalBalance", window);
  }

  public void checkSavingsIn(String accountName, double observedAmount, double plannedAmount) {
    fail("transfert");
    assertThat(window.getButton(accountName + ":savingsInAmount").textEquals(toString(observedAmount)));
    assertThat(window.getTextBox(accountName + ":savingsPlannedInAmount").textEquals(toString(plannedAmount)));
  }

  public MonthSummaryChecker checkSavingsInNotVisible(String accountName) {
    fail("transfert");
    assertFalse(window.getButton(accountName + ":savingsInAmount").isVisible());
    assertFalse(window.getTextBox(accountName + ":savingsPlannedInAmount").isVisible());
    return this;
  }

  public void checkSavingsOut(String accoutName, double observedAmount, double plannedAmount) {
    fail("transfert");
    assertThat(window.getButton(accoutName + ":savingsOutAmount").textEquals(toString(observedAmount)));
    assertThat(window.getTextBox(accoutName + ":savingsPlannedOutAmount").textEquals(toString(plannedAmount)));
  }

  public MonthSummaryChecker checkSavingsOutNotVisible(String accountName) {
    assertFalse(window.getButton(accountName + ":savingsOutAmount").isVisible());
    assertFalse(window.getTextBox(accountName + ":savingsPlannedOutAmount").isVisible());
    return this;
  }

  public void checkSavingsBalance(double balance) {
    fail("transfert");
    assertThat(getPanel().getTextBox("savingsBalanceAmount").textEquals(toString(balance, true)));
  }

  public void checkSavingsNotVisible(String accountName) {
    assertFalse(window.getPanel("accountGroup:" + accountName).isVisible());
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
    assertFalse(getPanel().containsComponent(ComponentMatchers.innerNameIdentity("noData")));
    assertFalse(getPanel().containsComponent(ComponentMatchers.innerNameIdentity("noSeries")));
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

  public MonthSummaryChecker checkSavingsIn(double observedAmount, double plannedAmount) {
    assertThat(window.getButton(BudgetArea.SAVINGS.getName() + ":in:budgetAreaAmount")
      .textEquals(toString(observedAmount)));
    assertThat(window.getTextBox(BudgetArea.SAVINGS.getName() + ":in:budgetAreaPlannedAmount")
      .textEquals(toString(plannedAmount)));
    GaugeChecker gauge = new GaugeChecker(getPanel(), BudgetArea.SAVINGS.getName() + ":in:budgetAreaGauge");
    gauge.checkActualValue(-observedAmount);
    gauge.checkTargetValue(-plannedAmount);
    return this;
  }

  public MonthSummaryChecker checkSavingsOut(double observedAmount, double plannedAmount) {
    assertThat(window.getButton(BudgetArea.SAVINGS.getName() + ":out:budgetAreaAmount").textEquals(toString(observedAmount)));
    assertThat(window.getTextBox(BudgetArea.SAVINGS.getName() + ":out:budgetAreaPlannedAmount").textEquals(toString(plannedAmount)));
    GaugeChecker gauge = new GaugeChecker(getPanel(), BudgetArea.SAVINGS.getName() + ":out:budgetAreaGauge");
    gauge.checkActualValue(observedAmount);
    gauge.checkTargetValue(plannedAmount);
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

  /** @deprecated */
  public MonthSummaryChecker checkOccasional(double amount, double planned) {
    Assert.fail("plus d'occasional");
    return this;
  }

  /** @deprecated */
  public MonthSummaryChecker checkOccasional(double amount) {
    Assert.fail("plus d'occasional");
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

  public MonthSummaryChecker checkSpecial(double amount) {
    checkObserved(BudgetArea.SPECIAL, amount);
    return this;
  }

  public MonthSummaryChecker checkProjects(String amount) {
    checkObserved(BudgetArea.SPECIAL, amount);
    return this;
  }

  public MonthSummaryChecker checkPlannedSpecial(String amount) {
    checkPlanned(BudgetArea.SPECIAL, amount);
    return this;
  }

  public MonthSummaryChecker checkSpecial(double amount, double planned) {
    checkBudgetArea(BudgetArea.SPECIAL, amount, planned);
    return this;
  }

  private void checkBudgetArea(BudgetArea budgetArea, double amount, double planned) {
    checkObserved(budgetArea, amount);
    checkPlanned(budgetArea, planned);
    if (budgetArea == BudgetArea.INCOME) {
      checkGauge(budgetArea, amount, planned);
    }
    else {
      checkGauge(budgetArea, -amount, -planned);
    }
  }

  public void gotoTransactions(BudgetArea budgetArea) {
    Button button = getPanel().getButton(budgetArea.getName() + ":budgetAreaAmount");
    button.click();
  }

  public void gotoTransactions(String accoutName) {
    Button button = getPanel().getButton(accoutName + ":savingsInAmount");
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

  public MonthSummaryChecker checkTotal(double received, double spent) {
    checkBalance(received - spent);
    spent = Math.abs(spent);
    received = Math.abs(received);
    if (received > spent) {
      return checkBalanceGraph("mainAccountsTotalBalance", 1., spent / received);
    }
    else {
      return checkBalanceGraph("mainAccountsTotalBalance", received / spent, 1.);
    }
  }

  public MonthSummaryChecker checkMainBalanceGraph(double incomePercent, double expensesPercent) {
    return checkBalanceGraph("mainAccountsTotalBalance", incomePercent, expensesPercent);
  }

  public MonthSummaryChecker checkSavingsBalanceGraph(double incomePercent, double expensesPercent) {
    return checkBalanceGraph("savingsTotalBalance", incomePercent, expensesPercent);
  }

  public MonthSummaryChecker checkBalanceGraph(String name, double incomePercent, double expensesPercent) {
    BalanceGraph graph = getPanel().findSwingComponent(BalanceGraph.class, name);
    String actual = "Actual: " + graph.getIncomePercent() + " / " + graph.getExpensesPercent();
    Assert.assertEquals(actual, incomePercent, graph.getIncomePercent(), 0.01);
    Assert.assertEquals(actual, expensesPercent, graph.getExpensesPercent(), 0.01);
    return this;
  }

  public MonthSummaryChecker checkBalance(double balance) {
    assertThat(getPanel().getTextBox("mainAccountsBalanceAmount").textEquals(toString(balance, true)));
    return this;
  }

  public MonthSummaryChecker checkEmptyBalance() {
    UISpecAssert.assertThat(getPanel().getTextBox("mainAccountsBalanceAmount").textEquals(""));
    return this;
  }

  public MonthSummaryChecker checkEmptySavingsBalance() {
    UISpecAssert.assertThat(getPanel().getTextBox("savingsBalanceAmount").textEquals(""));
    return this;
  }

  public MonthSummaryChecker checkUncategorized(String amount) {
    assertThat(getPanel().getTextBox("uncategorizedAmountLabel").textEquals(amount));
    return this;
  }

  public MonthSummaryChecker checkNoUncategorized() {
    assertFalse(getPanel().getTextBox("uncategorizedAmountLabel").isVisible());
    assertFalse(getPanel().getButton("To Categorize").isVisible());
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
