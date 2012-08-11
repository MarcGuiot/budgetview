package org.designup.picsou.functests.checkers.printing;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.printing.pages.BudgetGaugePageChecker;
import org.designup.picsou.functests.checkers.printing.pages.BudgetOverviewPageChecker;
import org.designup.picsou.functests.checkers.printing.pages.SeriesTablePageChecker;
import org.designup.picsou.gui.printing.PrintableReport;
import org.designup.picsou.gui.printing.report.BudgetReport;
import org.designup.picsou.gui.printing.report.utils.BlockColumnPage;
import org.designup.picsou.gui.printing.report.overview.BudgetOverviewPage;
import org.designup.picsou.gui.printing.report.tables.SeriesTablePage;

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
    if (!(printable instanceof BlockColumnPage)) {
      Assert.fail("Unexpected type for page " + page + ": " + printable.getClass());
    }
    return new BudgetGaugePageChecker(((BlockColumnPage)printable));
  }
}
