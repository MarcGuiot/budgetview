package com.budgetview.functests.checkers.printing.pages;

import com.budgetview.functests.checkers.components.HistoChartChecker;
import com.budgetview.functests.checkers.components.StackChecker;
import com.budgetview.gui.printing.budget.overview.BudgetOverviewPage;
import junit.framework.Assert;
import org.uispec4j.Panel;

public class BudgetOverviewPageChecker {
  private Panel panel;
  private BudgetOverviewPage page;

  public BudgetOverviewPageChecker(BudgetOverviewPage page) {
    this.page = page;
    this.panel = new Panel(page.getPanel());
  }

  public void checkTitle(String title) {
    Assert.assertEquals(title, page.getTitle());
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
