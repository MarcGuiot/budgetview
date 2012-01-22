package org.designup.picsou.gui.printing.report;

import org.designup.picsou.gui.printing.PrintableReport;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.util.MonthRange;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;

public class BudgetReport implements PrintableReport {

  private static BudgetArea[] BUDGET_AREA_TABLES =
    {BudgetArea.INCOME, BudgetArea.RECURRING, BudgetArea.VARIABLE, BudgetArea.SAVINGS, BudgetArea.EXTRAS};
  
  private PageFormat format;
  private List<ReportPage> pages = new ArrayList<ReportPage>();

  public BudgetReport(MonthRange monthRange, GlobRepository repository, Directory directory) {
    this.pages.add(new BudgetOverviewPage(monthRange, repository, directory));
    for (BudgetArea budgetArea : BUDGET_AREA_TABLES) {
      this.pages.add(new SeriesTablePage(new SeriesTable(budgetArea, monthRange, repository, directory)));
    }
  }

  public void initFormat(PageFormat defaultFormat) {
    this.format = defaultFormat;
    Paper paper = new Paper();
    double margin = 20;
    paper.setImageableArea(margin, margin,
                           paper.getWidth() - margin * 2,
                           paper.getHeight() - margin * 2);
    defaultFormat.setPaper(paper);
    defaultFormat.setOrientation(PageFormat.LANDSCAPE);
  }

  public int getNumberOfPages() {
    return pages.size();
  }

  public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
    if (format == null) {
      throw new InvalidState("initFormat() should be called first");
    }
    return format;
  }

  public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
    return pages.get(pageIndex);
  }
}
