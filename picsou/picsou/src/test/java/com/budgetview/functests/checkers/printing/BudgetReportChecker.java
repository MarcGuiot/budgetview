package com.budgetview.functests.checkers.printing;

import com.budgetview.functests.checkers.printing.pages.BudgetOverviewPageChecker;
import com.budgetview.functests.checkers.printing.pages.SeriesTablePageChecker;
import com.budgetview.gui.printing.budget.BudgetReport;
import com.budgetview.gui.printing.budget.overview.BudgetOverviewPage;
import com.budgetview.gui.printing.budget.tables.SeriesTablePage;
import junit.framework.Assert;
import com.budgetview.functests.checkers.printing.pages.BudgetGaugePageChecker;
import com.budgetview.gui.printing.PrintableReport;
import com.budgetview.gui.printing.utils.BlockMultiColumnsPage;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;

public class BudgetReportChecker {
  private BudgetReport report;

  public BudgetReportChecker(PrintableReport report) {
    if (!(report instanceof BudgetReport)) {
      Assert.fail("Unexpected report type: " + report.getClass());
    }
    this.report = (BudgetReport)report;

    Paper paper = new Paper();
    paper.setImageableArea(0, 0, 1000, 900);
    PageFormat format = new PageFormat();
    format.setPaper(paper);
    this.report.init(format);
  }

  public BudgetReportChecker checkPageCount(int count) {
    Assert.assertEquals(count, report.getNumberOfPages());
    return this;
  }

  public BudgetOverviewPageChecker getOverviewPage() {
    Printable printable = report.getPrintable(0);
    if (!(printable instanceof BudgetOverviewPage)) {
      Assert.fail("Unexpected type for page 0: " + printable.getClass());
    }
    return new BudgetOverviewPageChecker((BudgetOverviewPage)printable);
  }

  public SeriesTablePageChecker initTablePage(int page) {
    Printable printable = report.getPrintable(page);
    if (!(printable instanceof SeriesTablePage)) {
      Assert.fail("Unexpected type for page " + page + ": " + printable.getClass());
    }
    return new SeriesTablePageChecker(((SeriesTablePage)printable).getSeriesTable());
  }

  public BudgetGaugePageChecker initGaugesPage(int page) {
    Printable printable = report.getPrintable(page);
    if (!(printable instanceof BlockMultiColumnsPage)) {
      Assert.fail("Unexpected type for page " + page + ": " + printable.getClass());
    }
    return new BudgetGaugePageChecker(((BlockMultiColumnsPage)printable));
  }
}
