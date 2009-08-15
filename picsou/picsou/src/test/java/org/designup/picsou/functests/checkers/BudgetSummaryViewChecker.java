package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;
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

  public BudgetSummaryViewChecker checkUncategorized(double amount) {
    assertThat(getPanel().getTextBox("uncategorizedLabel").textEquals(toString(amount, false)));
    return this;
  }

  public BudgetSummaryViewChecker checkUncategorizedNotShown() {
    assertThat(getPanel().getTextBox("uncategorizedLabel").textEquals("-"));
    return this;
  }

  public void checkEmpty() {
    assertThat(getPanel().getTextBox("balanceLabel").textEquals("-"));
    assertThat(getPanel().getTextBox("positionLabel").textEquals("-"));
    assertThat(getPanel().getTextBox("uncategorizedLabel").textEquals("-"));
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

  public BudgetSummaryDetailsChecker openEstimatedPositionDetails() {
    Window window = WindowInterceptor.getModalDialog(getPanel().getButton().triggerClick());
    return new BudgetSummaryDetailsChecker(window);
  }

  public void checkNoEstimatedPositionDetails() {
    assertFalse(getPanel().getButton().isEnabled());
  }

  public void checkNoEstimatedPosition() {
    assertThat(getPanel().getTextBox("positionLabel").textEquals("-"));
  }

  public void checkEstimatedPositionColor(String color) {
    TextBox label = getPanel().getTextBox("positionLabel");
    assertThat(label.foregroundNear(color));
  }

  public void checkIsEstimatedPosition() {
    TextBox label = getPanel().getTextBox("positionTitle");
    assertThat(label.textEquals("Estimated position"));
  }

  public void checkIsRealPosition() {
    TextBox label = getPanel().getTextBox("positionTitle");
    assertThat(label.textEquals("Position"));
  }
}
