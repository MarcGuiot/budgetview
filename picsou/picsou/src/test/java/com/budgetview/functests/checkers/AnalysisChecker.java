package com.budgetview.functests.checkers;

import com.budgetview.functests.checkers.analysis.BudgetAnalysisChecker;
import com.budgetview.functests.checkers.analysis.TableAnalysisChecker;
import com.budgetview.functests.checkers.analysis.EvolutionAnalysisChecker;
import org.uispec4j.Button;
import org.uispec4j.Panel;
import org.uispec4j.Window;

import javax.swing.*;

import static org.uispec4j.assertion.UISpecAssert.assertFalse;

public class AnalysisChecker extends ViewChecker {

  private static final String PANEL_NAME = "analysisView";

  private Panel panel;
  private TableAnalysisChecker tableAnalysisChecker;
  private BudgetAnalysisChecker budgetAnalysisChecker;
  private EvolutionAnalysisChecker evolutionAnalysisChecker;
  private Panel analysisSelector;
  private Button tableSelector;
  private Button budgetSelector;
  private Button evolutionSelector;

  public AnalysisChecker(Window mainWindow) {
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

  public EvolutionAnalysisChecker evolution() {
    views.selectAnalysis();
    selectEvolution();
    if (evolutionAnalysisChecker == null) {
      evolutionAnalysisChecker = new EvolutionAnalysisChecker(mainWindow);
    }
    return evolutionAnalysisChecker;
  }

  public void selectEvolution() {
    if (budgetSelector == null) {
      budgetSelector = getSelector("selector:evolution");
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

  public AnalysisChecker selectNextMonth() {
    getPanel().getButton("nextMonth").click();
    return this;
  }

  public AnalysisChecker checkNextMonthSelectionDisabled() {
    assertFalse(getPanel().getButton("nextMonth").isEnabled());
    return this;
  }

  public AnalysisChecker selectPreviousMonth() {
    getPanel().getButton("previousMonth").click();
    return this;
  }

  public AnalysisChecker checkPreviousMonthSelectionDisabled() {
    assertFalse(getPanel().getButton("previousMonth").isEnabled());
    return this;
  }

  public AnalysisChecker checkBudgetShown() {
    checkComponentVisible(getPanel(), JPanel.class, "chartsPanel", true);
    checkComponentVisible(getPanel(), JPanel.class, "tablePanel", false);
    return this;
  }

  public AnalysisChecker checkTableShown() {
    checkComponentVisible(getPanel(), JPanel.class, "chartsPanel", false);
    checkComponentVisible(getPanel(), JPanel.class, "tablePanel", true);
    return this;
  }
}
