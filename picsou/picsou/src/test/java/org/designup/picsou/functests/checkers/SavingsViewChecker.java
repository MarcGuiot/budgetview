package org.designup.picsou.functests.checkers;

import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.*;
import static org.uispec4j.assertion.UISpecAssert.assertTrue;
import static org.uispec4j.finder.ComponentMatchers.*;

import javax.swing.*;

public class SavingsViewChecker extends GuiChecker {

  public HistoChecker histoChart;
  
  private Window window;

  public SavingsViewChecker(Window window) {
    this.window = window;
    this.histoChart = new HistoChecker(window, "savingsEvolutionPanel");
  }

  public void checkNoTotalPosition() {
    assertThat(getPanel().getTextBox("totalSavingsPositionAmount").textEquals("-"));
  }

  public void checkTotalPosition(double amount, String updateDate) {
    TextBox position = getPanel().getTextBox("totalSavingsPositionAmount");
    assertThat(position.isVisible());
    assertThat(position.textEquals(toString(amount)));

    TextBox date = getPanel().getTextBox("totalSavingsPositionDate");
    assertThat(date.isVisible());
    assertThat(date.textEquals(updateDate));
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
    assertFalse(window.getPanel(accountName + "." + seriesName + ".gauge").isVisible());
  }

  public void checkNoAccounts() {
    checkComponentVisible(getPanel(), JPanel.class, "savingsAccountPanel", false);
  }

  public void checkAccountWithNoPosition(String accountName) {
    final Panel accountPanel = getAccountPanel(accountName);
    assertThat(accountPanel.getTextBox("estimatedAccountPosition." + accountName).textEquals("-"));
    assertTrue(accountPanel.getTextBox("estimatedAccountPositionDate." + accountName).isVisible());
  }

  public void checkAccount(String accountName, Double position, String updateDate) {
    final Panel accountPanel = getAccountPanel(accountName);
    assertTrue(accountPanel.getTextBox("estimatedAccountPosition." + accountName).textEquals(toString(position)));
    assertTrue(accountPanel.getTextBox("estimatedAccountPositionDate." + accountName).textEquals("on " + updateDate));
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
    assertThat(getSeriesButton(accountName, seriesName).tooltipContains(tooltipText));
    return this;
  }

  private Button getSeriesButton(String accountName, String seriesName) {
    String buttonName = accountName + "." + seriesName + ".edit";
    return getPanel().getButton(buttonName);
  }
}
