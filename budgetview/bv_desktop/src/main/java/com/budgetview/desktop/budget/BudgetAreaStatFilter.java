package com.budgetview.desktop.budget;

import com.budgetview.desktop.model.PeriodSeriesStat;
import com.budgetview.desktop.series.utils.SeriesMatchers;
import com.budgetview.desktop.utils.MonthMatcher;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesBudget;
import com.budgetview.model.SeriesGroup;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;

import java.util.Collections;
import java.util.Set;

public class BudgetAreaStatFilter implements GlobMatcher {

  private MonthMatcher seriesDateFilter;
  private boolean monthFilteringEnabled = true;
  private Set<Integer> selectedMonthIds = Collections.emptySet();
  private BudgetArea budgetArea;
  private GlobMatcher accountMatcher = GlobMatchers.ALL;

  public BudgetAreaStatFilter(BudgetArea budgetArea) {
    this.budgetArea = budgetArea;
    this.seriesDateFilter = SeriesMatchers.seriesActiveInPeriod(budgetArea, true, false, false);
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
    if (!periodSeriesStat.isTrue(PeriodSeriesStat.VISIBLE)) {
      return false;
    }

    if (!accountMatcher.matches(periodSeriesStat, repository)) {
      return false;
    }

    if (PeriodSeriesStat.isForGroup(periodSeriesStat)) {
      Glob group = repository.get(Key.create(SeriesGroup.TYPE, periodSeriesStat.get(PeriodSeriesStat.TARGET)));
      Integer budgetAreaId = group.get(SeriesGroup.BUDGET_AREA);
      if (!monthFilteringEnabled) {
        return budgetArea.getId().equals(budgetAreaId);
      }
      return Utils.equal(budgetAreaId, budgetArea.getId())
                       && periodSeriesStat.get(PeriodSeriesStat.ACTIVE);
    }

    if (!PeriodSeriesStat.isForSeries(periodSeriesStat)) {
      return false;
    }

    Glob series = repository.find(Key.create(Series.TYPE, periodSeriesStat.get(PeriodSeriesStat.TARGET)));
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

  public void setAccountMatcher(GlobMatcher matcher) {
    accountMatcher = matcher;
  }

  public String toString() {
    return new StringBuilder()
      .append("BudgetStatFilter")
      .append("\n  budgetArea: ").append(budgetArea)
      .append("\n  dates: ").append(seriesDateFilter)
      .append("\n  monthFiltering: ").append(monthFilteringEnabled)
      .append("\n  selectedMonthIds: ").append(selectedMonthIds)
      .append("\n  accounts: ").append(accountMatcher)
      .toString();
  }
}
