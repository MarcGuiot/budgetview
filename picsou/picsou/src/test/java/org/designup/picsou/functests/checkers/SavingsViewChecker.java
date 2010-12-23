package org.designup.picsou.functests.checkers;

import junit.framework.Assert;
import org.uispec4j.*;
import org.uispec4j.assertion.UISpecAssert;

import static org.uispec4j.assertion.UISpecAssert.*;
import static org.uispec4j.finder.ComponentMatchers.*;

import javax.swing.*;

public class SavingsViewChecker extends ViewChecker {

  private Panel savingsView;

  public SavingsViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public void checkNoEstimatedTotalPosition() {
    assertThat(getPanel().getTextBox("totalEstimatedSavingsPositionAmount").textEquals("-"));
  }

  public void checkTotalReferencePosition(String amount, String updateDate) {
    TextBox position = getPanel().getTextBox("totalReferenceSavingsPositionAmount");
    assertThat(position.isVisible());
    assertThat(position.textEquals(amount));

    TextBox date = getPanel().getTextBox("totalReferenceSavingsPositionDate");
    assertThat(date.isVisible());
    assertThat(date.textContains(updateDate));
  }

  public SavingsViewChecker checkNoEstimatedPosition() {
    Assert.fail("TBD");
    return this;
  }

  public SavingsViewChecker checkTotalEstimatedPosition(double amount) {
    checkTotalEstimatedPosition(toString(amount));
    return this;
  }

  public SavingsViewChecker checkTotalEstimatedPosition(String amount) {
    TextBox position = getPanel().getTextBox("totalEstimatedSavingsPositionAmount");
    assertThat(position.isVisible());
    assertThat(position.textEquals(amount));
    return this;
  }

  public void checkTotalEstimatedPositionDate(String updateDate) {
    TextBox date = getPanel().getTextBox("totalEstimatedSavingsPositionDate");
    assertThat(date.isVisible());
    assertThat(date.textContains(updateDate));
  }

  public void checkTotalEstimatedPosition(String amount, String updateDate) {
    checkTotalEstimatedPosition(amount);
    checkTotalEstimatedPositionDate(updateDate);
  }

  public void checkTotalEstimatedPositionColor(String color) {
    assertThat(getPanel().getTextBox("totalEstimatedSavingsPositionAmount").foregroundNear(color));
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
    Panel accountPanel = getAccountPanel(accountName);
    assertThat(accountPanel.getTextBox("estimatedAccountPosition." + accountName).textEquals("-"));
    assertTrue(accountPanel.getTextBox("estimatedAccountPositionDate." + accountName).isVisible());
  }

  public void checkAccount(String accountName, Double position, String updateDate) {
    Panel accountPanel = getAccountPanel(accountName);
    assertTrue(accountPanel.getTextBox("estimatedAccountPosition." + accountName).textEquals(toString(position)));
    assertTrue(accountPanel.getTextBox("estimatedAccountPositionDate." + accountName).textEquals("on " + updateDate));
  }

  public void checkEstimatedPosition(String accountName, double position) {
    Panel accountPanel = getAccountPanel(accountName);
    assertTrue(accountPanel.getTextBox("estimatedAccountPosition." + accountName).textEquals(toString(position)));
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
    if (savingsView == null) {
      views.selectSavings();
      savingsView = mainWindow.getPanel("savingsView");
    }

    return savingsView;
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
