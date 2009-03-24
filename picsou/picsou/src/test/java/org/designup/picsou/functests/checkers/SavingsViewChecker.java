package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.UIComponent;
import org.uispec4j.Window;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.finder.ComponentMatchers.*;

import javax.swing.*;
import java.util.Arrays;

public class SavingsViewChecker extends GuiChecker {
  private Window mainWindow;

  public SavingsViewChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
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

  /** @deprecated ??? */
  public SavingsViewChecker checkSavingsBalance(double balance) {
    UISpecAssert.fail("not implemented");
    return this;
  }

  /** @deprecated ??? */
  public void checkSavingsIn(String accountName, double observedAmount, double plannedAmount) {
    UISpecAssert.fail("not implemented - toujours d'actualite ?");
//    assertThat(window.getButton(accountName + ":savingsInAmount").textEquals(toString(observedAmount)));
//    assertThat(window.getTextBox(accountName + ":savingsPlannedInAmount").textEquals(toString(plannedAmount)));
  }

  /** @deprecated ??? */
  public void checkSavingsOut(String accoutName, double observedAmount, double plannedAmount) {
    UISpecAssert.fail("not implemented - toujours d'actualite ?");
//    assertThat(window.getButton(accoutName + ":savingsOutAmount").textEquals(toString(observedAmount)));
//    assertThat(window.getTextBox(accoutName + ":savingsPlannedOutAmount").textEquals(toString(plannedAmount)));
  }

  public void checkAmount(String accountName, String seriesName, double observedAmount, double plannedAmount) {
    assertThat(mainWindow.getButton(accountName + "." + seriesName + ".observedSeriesAmount").textEquals(toString(observedAmount)));
    assertThat(mainWindow.getButton(accountName + "." + seriesName + ".plannedSeriesAmount").textEquals(toString(plannedAmount)));
  }

  public void checkSavingsInNotVisible(String accountName, String seriesName) {
    UISpecAssert.assertFalse(mainWindow.getPanel(accountName + "." + seriesName + ".gauge").isVisible());
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
    return mainWindow.getPanel("savingsView");
  }

  public SeriesEditionDialogChecker createSavingsSeries() {
    return SeriesEditionDialogChecker.open(mainWindow.getButton("createSavingsSeries"), true);
  }

  public SeriesEditionDialogChecker editSavingsSeries(String accountName, String seriesName) {
    String buttonName = accountName + "." + seriesName + ".edit";
    return SeriesEditionDialogChecker.open(mainWindow.getButton(buttonName), true);
  }
}