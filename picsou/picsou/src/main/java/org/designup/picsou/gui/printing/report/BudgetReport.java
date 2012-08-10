package org.designup.picsou.gui.printing.report;

import org.designup.picsou.gui.printing.PrintableReport;
import org.designup.picsou.gui.printing.report.gauges.BudgetGaugePages;
import org.designup.picsou.gui.printing.report.overview.BudgetOverviewPage;
import org.designup.picsou.gui.printing.report.tables.SeriesTable;
import org.designup.picsou.gui.printing.report.tables.SeriesTablePage;
import org.designup.picsou.model.util.ClosedMonthRange;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidState;

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public class BudgetReport implements PrintableReport {

  private PageFormat format;
  private List<ReportPage> pages = new ArrayList<ReportPage>();
  private SortedSet<Integer> selectedMonths;
  private Integer currentMonth;
  private ClosedMonthRange monthRange;
  private GlobRepository repository;
  private Directory directory;

  public BudgetReport(SortedSet<Integer> selectedMonths, Integer currentMonth, ClosedMonthRange monthRange, GlobRepository repository, Directory directory) {
    this.selectedMonths = selectedMonths;
    this.currentMonth = currentMonth;
    this.monthRange = monthRange;
    this.repository = repository;
    this.directory = directory;
  }

  public void init(PageFormat defaultFormat) {
    setFormat(defaultFormat);
    createPages();
  }

  private void setFormat(PageFormat defaultFormat) {
    this.format = defaultFormat;
    Paper paper = new Paper();
    double margin = 20;
    paper.setImageableArea(margin, margin,
                           paper.getWidth() - margin * 2,
                           paper.getHeight() - margin * 2);
    defaultFormat.setPaper(paper);
    defaultFormat.setOrientation(PageFormat.LANDSCAPE);
  }

  private void createPages() {
    this.pages.add(new BudgetOverviewPage(currentMonth, monthRange, repository, directory));
    this.pages.addAll(BudgetGaugePages.getPages(selectedMonths, format, repository, directory));
    List<SeriesTable> tables = SeriesTable.getAll(currentMonth, monthRange, repository);
    for (SeriesTable table : tables) {
      this.pages.add(new SeriesTablePage(table));
    }
  }

  public int getNumberOfPages() {
    return pages.size();
  }

  public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
    if (format == null) {
      throw new InvalidState("init(PageFormat) should be called first");
    }
    return format;
  }

  public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
    return pages.get(pageIndex);
  }
}
