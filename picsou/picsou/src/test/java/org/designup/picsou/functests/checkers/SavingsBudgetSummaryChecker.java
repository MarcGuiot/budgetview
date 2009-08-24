package org.designup.picsou.functests.checkers;

import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.Button;
import org.uispec4j.interception.WindowInterceptor;
import static org.uispec4j.assertion.UISpecAssert.assertThat;
import static org.uispec4j.assertion.UISpecAssert.assertFalse;

import javax.swing.*;

public class SavingsBudgetSummaryChecker extends GuiChecker {
  private Panel mainWindow;

  public SavingsBudgetSummaryChecker(Panel mainWindow) {
    this.mainWindow = mainWindow;
  }

  public SavingsBudgetSummaryChecker checkMonthBalance(double amount) {
    assertThat(getPanel().getTextBox("balanceLabel").textEquals(toString(amount, true)));
    return this;
  }

  public SavingsBudgetSummaryChecker checkEndPosition(double amount) {
    assertThat(getPanel().getTextBox("positionLabel").textEquals(toString(amount, false)));
    return this;
  }

  public void checkEmpty() {
    assertThat(getPanel().getTextBox("balanceLabel").textEquals("-"));
    assertThat(getPanel().getTextBox("positionLabel").textEquals("-"));
  }

  public SavingsBudgetSummaryChecker checkMultiSelection(int count) {
    TextBox label = getPanel().getTextBox("multiSelectionLabel");
    assertThat(label.isVisible());
    assertThat(label.textContains(count + " months total"));
    return this;
  }

  public SavingsBudgetSummaryChecker checkMultiSelectionNotShown() {
    checkComponentVisible(getPanel(), JLabel.class, "multiSelectionLabel", false);
    return this;
  }

  private Panel getPanel() {
    return mainWindow.getPanel("budgetSummaryView");
  }

  public void checkNoEstimatedPosition() {
    Button totalButton = getPanel().getButton("positionLabel");
    assertFalse(totalButton.isVisible());
  }

  public void checkEstimatedPositionColor(String color) {
    Button button = getPanel().getButton("positionLabel");
    assertThat(button.foregroundNear(color));
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