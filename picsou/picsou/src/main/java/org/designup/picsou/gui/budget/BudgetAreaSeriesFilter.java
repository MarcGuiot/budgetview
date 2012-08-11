package org.designup.picsou.gui.budget;

import org.designup.picsou.gui.model.PeriodSeriesStat;
import org.designup.picsou.gui.utils.Matchers;
import org.designup.picsou.gui.utils.MonthMatcher;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.SeriesBudget;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.ReadOnlyGlobRepository;
import org.globsframework.model.utils.GlobMatcher;

import java.util.Collections;
import java.util.Set;

public class BudgetAreaSeriesFilter implements GlobMatcher {

  private MonthMatcher seriesDateFilter;
  private boolean monthFilteringEnabled = true;
  private Set<Integer> selectedMonthIds = Collections.emptySet();
  private BudgetArea budgetArea;

  public BudgetAreaSeriesFilter(BudgetArea budgetArea) {
    this.budgetArea = budgetArea;
    if (budgetArea == BudgetArea.SAVINGS) {
      seriesDateFilter =
        Matchers.seriesDateSavingsAndAccountFilter(Account.MAIN_SUMMARY_ACCOUNT_ID);
    }
    else {
      seriesDateFilter = Matchers.seriesActiveInPeriod(budgetArea.getId(), false);
    }
  }

  public void setMonthFilteringEnabled(boolean enabled) {
    this.monthFilteringEnabled = enabled;
  }

  public void toggleMonthFilteringEnabled() {
    this.monthFilteringEnabled = !monthFilteringEnabled;
  }

  public boolean isMonthFilteringEnabled() {
    return monthFilteringEnabled;
  }

  public void setSelectedMonthIds(Set<Integer> selectedMonthIds) {
    this.selectedMonthIds = selectedMonthIds;
    this.seriesDateFilter.filterMonths(selectedMonthIds);
  }

  public boolean matches(Glob periodSeriesStat, GlobRepository repository) {
    Glob series = repository.findLinkTarget(periodSeriesStat, PeriodSeriesStat.SERIES);
    if (series == null) {
      return false;
    }

    if (!monthFilteringEnabled) {
      return budgetArea.getId().equals(series.get(Series.BUDGET_AREA));
    }

    ReadOnlyGlobRepository.MultiFieldIndexed seriesBudgetIndex =
      repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID));
    int notActive = 0;
    for (Integer monthId : selectedMonthIds) {
      GlobList seriesBudget =
        seriesBudgetIndex.findByIndex(SeriesBudget.MONTH, monthId).getGlobs();
      if (seriesBudget.size() == 0 || !seriesBudget.getFirst().isTrue(SeriesBudget.ACTIVE)) {
        notActive++;
      }
    }
    boolean activeMonthsInPeriod = !(selectedMonthIds.size() == notActive);
    return activeMonthsInPeriod && seriesDateFilter.matches(series, repository);
  }
}
