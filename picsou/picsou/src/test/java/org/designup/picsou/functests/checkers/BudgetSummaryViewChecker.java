package org.designup.picsou.functests.checkers;

import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.TextBox;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.*;

public class BudgetSummaryViewChecker extends ViewChecker {
  private HistoDailyChecker chart;
  private Panel panel;

  public BudgetSummaryViewChecker(Window mainWindow) {
    super(mainWindow);
  }

  public BudgetSummaryViewChecker checkReferencePosition(double amount) {
    assertThat(getPanel().getTextBox("lastPositionLabel").textEquals(toString(amount, false)));
    return this;
  }

  public BudgetSummaryViewChecker checkReferencePosition(String title, double amount) {
    assertThat(getPanel().getTextBox("lastPositionTitle").textEquals(title));
    return checkReferencePosition(amount);
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

  private HistoDailyChecker getChart() {
    if (chart == null) {
      chart = new HistoDailyChecker(getPanel(), "chart");
    }
    return chart;
  }

  private Panel getPanel() {
    if (panel == null) {
      views.selectBudget();
      panel = mainWindow.getPanel("budgetSummaryView");
    }
    return panel;
  }
}
