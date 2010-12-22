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
    this.histoChart = new HistoChecker(window, "savingsEvolutionPanel", "histoChart");
  }

  public void checkNoTotalPosition() {
    assertThat(getPanel().getTextBox("totalSavingsPositionAmount").textEquals("-"));
  }

  public void checkTotalPosition(String amount, String updateDate) {
    TextBox position = getPanel().getTextBox("totalSavingsPositionAmount");
    assertThat(position.isVisible());
    assertThat(position.textEquals(amount));

    TextBox date = getPanel().getTextBox("totalSavingsPositionDate");
    assertThat(date.isVisible());
    assertThat(date.textContains(updateDate));
  }

  public void checkContainsSeries(String accountName, String seriesName) {
    assertThat(getPanel().containsUIComponent(Button.class, getButtonName(accountName, seriesName)));
  }

  public void checkSeriesAmounts(String accountName, String seriesName, double observedAmount, double plannedAmount) {
    assertThat(getObservedAmountButton(accountName, seriesName).textEquals(toString(observedAmount)));
    assertThat(getPlannedAmountButton(accountName, seriesName).textEquals(toString(plannedAmount)));
  }
  
  public SeriesAmountEditionDialogChecker editPlannedAmount(String accountName, String seriesName) {
    return SeriesAmountEditionDialogChecker.open(getPlannedAmountButton(accountName, seriesName).triggerClick());
  }

  private Button getPlannedAmountButton(String accountName, String seriesName) {
    return getPanel().getButton(accountName + "." + seriesName + ".plannedSeriesAmount");
  }

  private Button getObservedAmountButton(String accountName, String seriesName) {
    return getPanel().getButton(accountName + "." + seriesName + ".observedSeriesAmount");
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
    String buttonName = getButtonName(accountName, seriesName);
    return getPanel().getButton(buttonName);
  }

  private String getButtonName(String accountName, String seriesName) {
    return accountName + "." + seriesName + ".edit";
  }

  public SavingsViewChecker alignAndPropagate(String accountName, String seriesName) {
    editSeries(accountName, seriesName).alignPlannedAndActual().setPropagationEnabled().validate();
    return this;
  }
}
