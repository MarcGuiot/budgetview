package org.designup.picsou.functests.checkers.printing.pages;

import org.designup.picsou.functests.checkers.components.HistoChartChecker;
import org.designup.picsou.functests.checkers.components.StackChecker;
import org.designup.picsou.gui.printing.report.overview.BudgetOverviewPage;
import org.uispec4j.Panel;

public class BudgetOverviewPageChecker {
  private Panel panel;

  public BudgetOverviewPageChecker(BudgetOverviewPage page) {
    this.panel = new Panel(page.getPanel());
  }

  public StackChecker getOverviewStack() {
    return new StackChecker(panel.getPanel("balanceChart"));
  }

  public StackChecker getExpensesStack() {
    return new StackChecker(panel.getPanel("seriesChart"));
  }

  public HistoChartChecker getHistoChart() {
    return new HistoChartChecker(panel.getPanel("histoChart"));
  }
}
