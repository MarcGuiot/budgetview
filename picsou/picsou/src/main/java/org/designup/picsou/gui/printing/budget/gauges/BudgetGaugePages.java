package org.designup.picsou.gui.printing.budget.gauges;

import org.designup.picsou.gui.budget.BudgetAreaSeriesFilter;
import org.designup.picsou.gui.description.DefaultPeriodSeriesStatComparator;
import org.designup.picsou.gui.description.stringifiers.MonthListStringifier;
import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.printing.PrintablePage;
import org.designup.picsou.gui.printing.utils.BlockMultiColumnsPage;
import org.designup.picsou.gui.printing.utils.BudgetReportUtils;
import org.designup.picsou.gui.printing.utils.PageBlock;
import org.designup.picsou.gui.printing.utils.EmptyBlock;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.collections.MultiMap;
import org.globsframework.utils.directory.Directory;

import java.awt.print.PageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public class BudgetGaugePages {

  public static List<PrintablePage> getPages(SortedSet<Integer> selectedMonths, PageFormat format,
                                          GlobRepository repository, Directory directory) {
    BudgetGaugePages budgetGaugePages = new BudgetGaugePages(selectedMonths, format, repository, directory);
    return budgetGaugePages.pages;
  }

  private SortedSet<Integer> selectedMonths;
  private PageFormat format;
  private GlobRepository repository;
  private Directory directory;
  private List<PrintablePage> pages = new ArrayList<PrintablePage>();
  private BlockMultiColumnsPage currentPage;

  private BudgetGaugePages(SortedSet<Integer> selectedMonths, PageFormat format,
                           GlobRepository repository, Directory directory) {
    this.selectedMonths = selectedMonths;
    this.format = format;
    this.repository = repository;
    this.directory = directory;
    addBlocks();
  }

  private void addBlocks() {
    
    MultiMap<Integer, Glob> map = new MultiMap<Integer, Glob>();

    GlobList periodStats = repository.getAll(PeriodSeriesStat.TYPE).sort(new DefaultPeriodSeriesStatComparator(repository));
    for (Glob periodStat : periodStats) {
      Glob series = repository.findLinkTarget(periodStat, PeriodSeriesStat.SERIES);
      if (periodStat.isTrue(PeriodSeriesStat.ACTIVE)) {
        map.put(series.get(Series.BUDGET_AREA), periodStat);
      }
    }

    BudgetGaugeContext budgetGaugeContext = new BudgetGaugeContext();
    boolean first = true;
    for (BudgetArea budgetArea : BudgetReportUtils.BUDGET_AREAS) {
      
      if (!first) {
        addBlockToCurrentPage(new EmptyBlock(20));
      }
      first = false;

      addBlockToCurrentPage(new BudgetAreaGaugeBlock(budgetArea, selectedMonths, budgetGaugeContext, repository, directory));

      int currentSectionIndex = 0;
      BudgetAreaSeriesFilter filter = new BudgetAreaSeriesFilter(budgetArea);
      filter.setSelectedMonthIds(selectedMonths);
      GlobList list = new GlobList(map.get(budgetArea.getId()));
      list.filterSelf(filter, repository);
      for (Glob periodStat : list) {
        addBlockToCurrentPage(new SeriesGaugeBlock(periodStat, budgetGaugeContext, currentSectionIndex++, repository));
      }
    }
  }

  private void addBlockToCurrentPage(PageBlock block) {
    if ((currentPage == null) || !currentPage.hasSpaceLeftFor(block)) {
      String title = Lang.get("print.budgetGauge",
                              MonthListStringifier.toString(selectedMonths).toLowerCase());
      currentPage = new BlockMultiColumnsPage(format, title);
      pages.add(currentPage);      
    }
    currentPage.append(block);
  }
}
