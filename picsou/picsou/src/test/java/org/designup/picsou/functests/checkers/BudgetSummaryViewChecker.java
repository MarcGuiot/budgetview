package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import static org.uispec4j.assertion.UISpecAssert.*;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public class BudgetSummaryViewChecker extends GuiChecker {
  private Panel mainWindow;

  public BudgetSummaryViewChecker(Panel mainWindow) {
    this.mainWindow = mainWindow;
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

  public BudgetSummaryViewChecker checkNoEstimatedPosition() {
    assertThat(getPanel().getButton("positionLabel").textEquals("-"));
    return this;
  }

  public BalanceChecker openBalancePanel() {
    Window window = WindowInterceptor.getModalDialog(getPanel().getButton("balanceLabel").triggerClick());
    return new BalanceChecker(window);
  }

  public PositionChecker openPositionDialog() {
    Window window = WindowInterceptor.getModalDialog(getPanel().getButton("positionLabel").triggerClick());
    return new PositionChecker(window);
  }

  public void checkPositionSignpostDisplayed() {
    checkSignpostVisible(mainWindow, getPanel().getButton("positionLabel"),
                         "Use the estimated end of month position");
  }
}
