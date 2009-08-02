package org.designup.picsou.functests.checkers;

import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;
import static org.uispec4j.finder.ComponentMatchers.*;
import org.designup.picsou.model.BudgetArea;

import javax.swing.*;

public class SavingsViewChecker extends GuiChecker {
  private Window window;

  public SavingsViewChecker(Window window) {
    this.window = window;
  }

  public void checkTotalPositionHidden() {
    checkComponentVisible(getPanel(), JLabel.class, "totalSavingsPositionAmount", false);
    checkComponentVisible(getPanel(), JLabel.class, "totalSavingsPositionDate", false);
  }

  public void checkTotalPosition(double amount, String updateDate) {
    TextBox position = getPanel().getTextBox("totalSavingsPositionAmount");
    UISpecAssert.assertThat(position.isVisible());
    UISpecAssert.assertThat(position.textEquals(toString(amount)));

    TextBox date = getPanel().getTextBox("totalSavingsPositionDate");
    UISpecAssert.assertThat(date.isVisible());
    UISpecAssert.assertThat(date.textEquals(updateDate));
  }

  public SavingsViewChecker checkSavingsIn(double observedAmount, double plannedAmount) {
    assertThat(window.getButton(BudgetArea.SAVINGS.getName() + ":in:budgetAreaAmount")
      .textEquals(toString(observedAmount)));
    assertThat(window.getTextBox(BudgetArea.SAVINGS.getName() + ":in:budgetAreaPlannedAmount")
      .textEquals(toString(plannedAmount)));
    GaugeChecker gauge = new GaugeChecker(getPanel(), BudgetArea.SAVINGS.getName() + ":in:budgetAreaGauge");
    gauge.checkActualValue(-observedAmount);
    gauge.checkTargetValue(-plannedAmount);
    return this;
  }

  public SavingsViewChecker checkSavingsOut(double observedAmount, double plannedAmount) {
    assertThat(window.getButton(BudgetArea.SAVINGS.getName() + ":out:budgetAreaAmount").textEquals(toString(observedAmount)));
    assertThat(window.getTextBox(BudgetArea.SAVINGS.getName() + ":out:budgetAreaPlannedAmount").textEquals(toString(plannedAmount)));
    GaugeChecker gauge = new GaugeChecker(getPanel(), BudgetArea.SAVINGS.getName() + ":out:budgetAreaGauge");
    gauge.checkActualValue(observedAmount);
    gauge.checkTargetValue(plannedAmount);
    return this;
  }

  public void checkSavingsIn(String accountName, double observedAmount, double plannedAmount) {
    fail("transfert");
    assertThat(window.getButton(accountName + ":savingsInAmount").textEquals(toString(observedAmount)));
    assertThat(window.getTextBox(accountName + ":savingsPlannedInAmount").textEquals(toString(plannedAmount)));
  }

  public SavingsViewChecker checkSavingsInNotVisible(String accountName) {
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

  public SavingsViewChecker checkSavingsOutNotVisible(String accountName) {
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

  public void checkAmount(String accountName, String seriesName, double observedAmount, double plannedAmount) {
    assertThat(window.getButton(accountName + "." + seriesName + ".observedSeriesAmount").textEquals(toString(observedAmount)));
    assertThat(window.getButton(accountName + "." + seriesName + ".plannedSeriesAmount").textEquals(toString(plannedAmount)));
  }

  public void checkSavingsInNotVisible(String accountName, String seriesName) {
    UISpecAssert.assertFalse(window.getPanel(accountName + "." + seriesName + ".gauge").isVisible());
  }

  public void checkNoAccounts() {
    checkComponentVisible(getPanel(), JPanel.class, "savingsAccountPanel", false);
  }

  public void checkAccountWithNoPosition(String accountName) {
    final Panel accountPanel = getAccountPanel(accountName);
    UISpecAssert.assertTrue(accountPanel.getTextBox("estimatedAccountPosition." + accountName).textIsEmpty());
    UISpecAssert.assertFalse(accountPanel.getTextBox("estimatedAccountPositionDate." + accountName).isVisible());
  }

  public void checkAccount(String accountName, Double position, String updateDate) {
    final Panel accountPanel = getAccountPanel(accountName);
    UISpecAssert.assertTrue(accountPanel.getTextBox("estimatedAccountPosition." + accountName).textEquals(toString(position)));
    UISpecAssert.assertTrue(accountPanel.getTextBox("estimatedAccountPositionDate." + accountName).textEquals(updateDate));
  }

  private Panel getAccountPanel(String accountName) {
    UIComponent[] labels =
      getPanel().getUIComponents(and(fromClass(JLabel.class),
                                     innerNameIdentity("accountName"),
                                     displayedNameIdentity(accountName)));
    if (labels.length == 0) {
      UISpecAssert.fail("No label found for account: " + accountName);
    }
    if (labels.length > 1) {
      UISpecAssert.fail("Several labels found for account name: " + accountName);
    }
    TextBox label = (TextBox)labels[0];
    return label.getContainer("savingsAccountPanel");
  }

  private Panel getPanel() {
    return window.getPanel("savingsView");
  }

  public SeriesEditionDialogChecker createSeries() {
    return SeriesEditionDialogChecker.open(getPanel().getButton("createSavingsSeries"));
  }

  public SeriesEditionDialogChecker editSeries(String accountName, String seriesName) {
    Button button = getSeriesButton(accountName, seriesName);
    return SeriesEditionDialogChecker.open(button);
  }

  public SavingsViewChecker checkSeriesTooltip(String accountName, String seriesName, String tooltipText) {
    assertThat(getSeriesButton(accountName, seriesName).tooltipEquals(tooltipText));
    return this;
  }

  private Button getSeriesButton(String accountName, String seriesName) {
    String buttonName = accountName + "." + seriesName + ".edit";
    return getPanel().getButton(buttonName);
  }
}
