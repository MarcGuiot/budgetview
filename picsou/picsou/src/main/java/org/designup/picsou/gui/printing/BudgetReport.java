package org.designup.picsou.gui.printing;

import org.designup.picsou.gui.printing.pages.ReportPage;
import org.designup.picsou.gui.printing.pages.SeriesTablePage;
import org.designup.picsou.gui.printing.pages.BudgetOverviewPage;
import org.designup.picsou.gui.printing.reports.SeriesTable;
import org.designup.picsou.model.BudgetArea;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.List;

public class BudgetReport implements Pageable {

  private PageFormat format;
  private List<ReportPage> pages = new ArrayList<ReportPage>();

  public BudgetReport(GlobRepository repository, Directory directory, PageFormat format) {
    this.format = initFormat(format);
    this.pages.add(new BudgetOverviewPage(repository, directory));
    this.pages.add(new SeriesTablePage(new SeriesTable(BudgetArea.INCOME, repository, directory)));
    this.pages.add(new SeriesTablePage(new SeriesTable(BudgetArea.RECURRING, repository, directory)));
    this.pages.add(new SeriesTablePage(new SeriesTable(BudgetArea.VARIABLE, repository, directory)));
    this.pages.add(new SeriesTablePage(new SeriesTable(BudgetArea.SAVINGS, repository, directory)));
    this.pages.add(new SeriesTablePage(new SeriesTable(BudgetArea.EXTRAS, repository, directory)));
  }

  private static PageFormat initFormat(PageFormat format) {
    Paper paper = new Paper();
    double margin = 36; // half inch
    paper.setImageableArea(margin, margin,
                           paper.getWidth() - margin * 2,
                           paper.getHeight() - margin * 2);
    format.setPaper(paper);
    format.setOrientation(PageFormat.LANDSCAPE);
    return format;
  }

  public int getNumberOfPages() {
    return pages.size();
  }

  public PageFormat getPageFormat(int i) throws IndexOutOfBoundsException {
    return format;
  }

  public Printable getPrintable(int i) throws IndexOutOfBoundsException {
    return pages.get(i);
  }
}
