package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Button;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;

import javax.swing.*;

import junit.framework.Assert;

public class BudgetSummaryViewChecker extends GuiChecker {
  private Panel mainWindow;

  public BudgetSummaryViewChecker(Panel mainWindow) {
    this.mainWindow = mainWindow;
  }

  public BudgetSummaryViewChecker checkMonthBalanceHidden() {
    checkComponentVisible(getPanel(), JButton.class, "balanceLabel", false);
    return this;
  }

  public BudgetSummaryViewChecker checkMonthBalance(double amount) {
    assertThat(getPanel().getButton("balanceLabel").textEquals(toString(amount, true)));
    return this;
  }

  public BudgetSummaryViewChecker checkEndPosition(double amount) {
    assertThat(getPanel().getButton("positionLabel").textEquals(toString(amount, false)));
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

  public void checkHelpMessageDisplayed() {
    checkComponentVisible(getPanel(), JEditorPane.class, "helpMessage", true);
    checkMonthBalanceHidden();
  }

  public void checkHelpMessageHidden() {
    checkComponentVisible(getPanel(), JEditorPane.class, "helpMessage", false);
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
    Assert.fail("Budget wizard inline: il faut reecrire avec BalanceDialog / PositionDialog");
    Window wizardWindow = WindowInterceptor.run(getPanel().getButton("openDetails").triggerClick());
    return new BudgetWizardPageChecker(wizardWindow);
  }

  public BudgetSummaryViewChecker skipWizard() {
    openBudgetWizardPage().close();
    return this;
  }

  public BudgetSummaryViewChecker checkNoEstimatedPositionDetails() {
    assertFalse(getPanel().getButton("openDetails").isEnabled());
    return this;
  }

  public BudgetSummaryViewChecker checkNoEstimatedPosition() {
    assertThat(getPanel().getButton("positionLabel").textEquals("-"));
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

  public BalanceChecker openBalancePanel() {
    Window window = WindowInterceptor.getModalDialog(getPanel().getButton("balanceLabel").triggerClick());
    return new BalanceChecker(window);
  }

  public PositionChecker openPositionPanel() {
    Window window = WindowInterceptor.getModalDialog(getPanel().getButton("positionLabel").triggerClick());
    return new PositionChecker(window);
  }
}
