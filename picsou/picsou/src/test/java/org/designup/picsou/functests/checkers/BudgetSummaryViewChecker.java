package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Button;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;

import javax.swing.*;

public class BudgetSummaryViewChecker extends GuiChecker {
  private Panel mainWindow;

  public BudgetSummaryViewChecker(Panel mainWindow) {
    this.mainWindow = mainWindow;
  }

  public BudgetSummaryViewChecker checkMonthBalance(double amount) {
    assertThat(getPanel().getTextBox("balanceLabel").textEquals(toString(amount, true)));
    return this;
  }

  public BudgetSummaryViewChecker checkEndPosition(double amount) {
    assertThat(getPanel().getTextBox("positionLabel").textEquals(toString(amount, false)));
    return this;
  }

  public BudgetSummaryViewChecker checkEndPosition(String title, double amount) {
    assertThat(getPanel().getTextBox("positionTitle").textEquals(title));
    return checkEndPosition(amount);
  }

  public BudgetSummaryViewChecker checkUncategorized(double amount) {
    Button button = getPanel().getButton("uncategorized");
    assertThat(button.textEquals(toString(amount, false)));
    assertThat(button.isEnabled());
    return this;
  }

  public BudgetSummaryViewChecker checkUncategorizedNotShown() {
    Button button = getPanel().getButton("uncategorized");
    assertThat(button.textEquals("-"));
    assertFalse(button.isEnabled());
    return this;
  }

  public void gotoUncategorized() {
    getPanel().getButton("uncategorized").click();
  }

  public void checkEmpty() {
    assertThat(getPanel().getTextBox("balanceLabel").textEquals("-"));
    assertThat(getPanel().getTextBox("positionLabel").textEquals("-"));
    assertThat(getPanel().getTextBox("uncategorizedLabel").textEquals("-"));
  }

  public void checkHelpMessageDisplayed(boolean visible) {
    checkComponentVisible(getPanel(), JLabel.class, "helpMessage", visible);
  }

  public BudgetSummaryViewChecker checkMultiSelection(int count) {
    TextBox label = getPanel().getTextBox("multiSelectionLabel");
    assertThat(label.isVisible());
    assertThat(label.textContains(count + " months total"));
    return this;
  }

  public BudgetSummaryViewChecker checkMultiSelectionNotShown() {
    checkComponentVisible(getPanel(), JLabel.class, "multiSelectionLabel", false);
    return this;
  }

  private Panel getPanel() {
    return mainWindow.getPanel("budgetSummaryView");
  }

  public BudgetWizardPageChecker openBudgetWizardPage() {
    Window wizardWindow = WindowInterceptor.run(getPanel().getButton("openDetailsButton").triggerClick());
    return new BudgetWizardPageChecker(wizardWindow);
  }

  public BudgetSummaryViewChecker checkNoEstimatedPositionDetails() {
    assertFalse(getPanel().getButton("openDetailsButton").isEnabled());
    return this;
  }

  public BudgetSummaryViewChecker checkNoEstimatedPosition() {
    assertThat(getPanel().getTextBox("positionLabel").textEquals("-"));
    return this;
  }

  public void checkEstimatedPositionColor(String color) {
    TextBox label = getPanel().getTextBox("positionLabel");
    assertThat(label.foregroundNear(color));
  }

  public void checkIsEstimatedPosition() {
    TextBox label = getPanel().getTextBox("positionTitle");
    assertThat(label.textContains("End of"));
  }

  public void checkIsRealPosition() {
    TextBox label = getPanel().getTextBox("positionTitle");
    assertThat(label.textEquals("Position"));
  }
}
