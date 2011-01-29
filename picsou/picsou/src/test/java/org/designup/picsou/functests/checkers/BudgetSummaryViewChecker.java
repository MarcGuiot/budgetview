package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public class BudgetSummaryViewChecker extends GuiChecker {
  private Window mainWindow;
  private HistoDailyChecker chart;

  public BudgetSummaryViewChecker(Window mainWindow) {
    this.mainWindow = mainWindow;
  }

  /** @deprecated  */
  public BudgetSummaryViewChecker checkMonthBalance(double amount) {
    throw new RuntimeException("to be removed");
  }

  public BudgetSummaryViewChecker checkReferencePosition(double amount) {
    assertThat(getPanel().getTextBox("lastPositionLabel").textEquals(toString(amount, false)));
    return this;
  }

  public BudgetSummaryViewChecker checkReferencePosition(String title, double amount) {
    assertThat(getPanel().getTextBox("lastPositionTitle").textEquals(title));
    return checkReferencePosition(amount);
  }

  private HistoDailyChecker getChart() {
    if (chart == null) {
      chart = new HistoDailyChecker(mainWindow, "budgetSummaryView", "chart");
    }
    return chart;
  }

  public BudgetSummaryViewChecker checkEndPosition(double amount) {
    getChart().checkEndOfMonthValue(amount);
    return this;
  }

  public BudgetSummaryViewChecker checkNoEstimatedPosition() {
    getChart().checkEndOfMonthValue(0.0);
    return this;
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
}
