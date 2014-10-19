package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.checkers.analysis.BudgetAnalysisChecker;
import org.designup.picsou.functests.checkers.analysis.TableAnalysisChecker;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.Window;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;

public class SeriesAnalysisChecker extends ViewChecker {

  private static final String PANEL_NAME = "analysisView";

  private Panel panel;
  private TableAnalysisChecker tableAnalysisChecker;
  private BudgetAnalysisChecker budgetAnalysisChecker;
  private Panel analysisSelector;
  private Button tableSelector;
  private Button budgetSelector;

  public SeriesAnalysisChecker(Window mainWindow) {
    super(mainWindow);
  }

  public TableAnalysisChecker table() {
    views.selectAnalysis();
    selectTable();
    if (tableAnalysisChecker == null) {
      tableAnalysisChecker = new TableAnalysisChecker(mainWindow);
    }
    return tableAnalysisChecker;
  }

  public void selectTable() {
    if (tableSelector == null) {
      tableSelector = getSelector("selector:table");
    }
    tableSelector.click();
  }

  public BudgetAnalysisChecker budget() {
    views.selectAnalysis();
    selectBudget();
    if (budgetAnalysisChecker == null) {
      budgetAnalysisChecker = new BudgetAnalysisChecker(mainWindow);
    }
    return budgetAnalysisChecker;
  }

  public void selectBudget() {
    if (budgetSelector == null) {
      budgetSelector = getSelector("selector:budget");
    }
    budgetSelector.click();
  }

  private org.uispec4j.Button getSelector(String buttonName) {
    if (analysisSelector == null) {
      analysisSelector = mainWindow.getPanel("analysisSelector");
    }
    return analysisSelector.getButton(buttonName);
  }

  protected Panel getPanel() {
    views.selectAnalysis();
    if (panel == null) {
      panel = mainWindow.getPanel(PANEL_NAME);
    }
    return panel;
  }

  public SeriesAnalysisChecker selectNextMonth() {
    getPanel().getButton("nextMonth").click();
    return this;
  }

  public SeriesAnalysisChecker checkNextMonthSelectionDisabled() {
    assertFalse(getPanel().getButton("nextMonth").isEnabled());
    return this;
  }

  public SeriesAnalysisChecker selectPreviousMonth() {
    getPanel().getButton("previousMonth").click();
    return this;
  }

  public SeriesAnalysisChecker checkPreviousMonthSelectionDisabled() {
    assertFalse(getPanel().getButton("previousMonth").isEnabled());
    return this;
  }

  public SeriesAnalysisChecker checkBudgetShown() {
    checkComponentVisible(getPanel(), JPanel.class, "chartsPanel", true);
    checkComponentVisible(getPanel(), JPanel.class, "tablePanel", false);
    return this;
  }

  public SeriesAnalysisChecker checkTableShown() {
    checkComponentVisible(getPanel(), JPanel.class, "chartsPanel", false);
    checkComponentVisible(getPanel(), JPanel.class, "tablePanel", true);
    return this;
  }
}
