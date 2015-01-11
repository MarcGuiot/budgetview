package org.designup.picsou.gui.series.utils;

import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.gui.utils.MonthMatcher;
import org.designup.picsou.model.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.and;
import static org.globsframework.model.utils.GlobMatchers.fieldEquals;
import static org.globsframework.model.utils.GlobMatchers.not;

public class SeriesMatchers {

  public static MonthMatcher seriesActiveInPeriod(final Integer budgetAreaId, boolean showOnlyForActiveMonths, boolean showOnlyIfAvailableOnAllMonths, boolean showOnPreviousAndNextMonth) {
    return new SeriesFirstEndDateFilter(showOnlyForActiveMonths, showOnlyIfAvailableOnAllMonths, showOnPreviousAndNextMonth) {
      protected boolean isEligible(Glob series, GlobRepository repository) {
        return budgetAreaId.equals(series.get(Series.BUDGET_AREA));
      }
    };
  }

  public static GlobMatcher deferredCardSeries() {
    return and(fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
               not(fieldEquals(Series.ID, Series.ACCOUNT_SERIES_ID)));
  }

  public static GlobMatcher seriesForAccount(final Integer selectedAccountId) {
    return new GlobMatcher() {
      public boolean matches(Glob series, GlobRepository repository) {
        return Series.isSeriesForAccount(series, selectedAccountId, repository);
      }
    };
  }

  public static GlobMatcher seriesForMainOrUnknownAccount() {
    return new GlobMatcher() {
      public boolean matches(Glob series, GlobRepository repository) {
        return Series.isForMainOrUnknownAccount(series, repository);
      }
    };
  }

  public static abstract class SeriesFirstEndDateFilter implements MonthMatcher {
    private boolean showOnlyForActiveMonths;
    private boolean showOnlyIfAvailableOnAllMonths;
    private boolean showOnPreviousAndNextMonth;
    private Set<Integer> selectedMonthIds = Collections.emptySet();
    private Set<Integer> expandedMonthIds = Collections.emptySet();

    private SeriesFirstEndDateFilter(boolean showOnlyForActiveMonths, boolean showOnlyIfAvailableOnAllMonths, boolean showOnPreviousAndNextMonth) {
      this.showOnlyForActiveMonths = showOnlyForActiveMonths;
      this.showOnlyIfAvailableOnAllMonths = showOnlyIfAvailableOnAllMonths;
      this.showOnPreviousAndNextMonth = showOnPreviousAndNextMonth;
    }

    public void filterMonths(Set<Integer> monthIds) {
      this.selectedMonthIds = monthIds;

      this.expandedMonthIds = new HashSet<Integer>();
      for (Integer monthId : monthIds) {
        expandedMonthIds.add(Month.previous(monthId));
        expandedMonthIds.add(Month.next(monthId));
      }
      expandedMonthIds.removeAll(selectedMonthIds);
    }

    public boolean matches(Glob series, GlobRepository repository) {
      if (!isEligible(series, repository)) {
        return false;
      }

      Integer firstMonth = series.get(Series.FIRST_MONTH);
      Integer lastMonth = series.get(Series.LAST_MONTH);
      if (firstMonth == null) {
        firstMonth = 0;
      }
      else if (showOnPreviousAndNextMonth) {
        firstMonth = Month.previous(firstMonth);
      }
      if (lastMonth == null) {
        lastMonth = Integer.MAX_VALUE;
      }
      else if (showOnPreviousAndNextMonth) {
        lastMonth = Month.next(lastMonth);
      }

      boolean monthsInScope = isMonthSelectionInSeriesScope(firstMonth, lastMonth);

      for (Integer monthId : selectedMonthIds) {
        Glob seriesBudget = SeriesBudget.find(series.get(Series.ID), monthId, repository);
        if ((seriesBudget != null)) {
          if (Amounts.isNotZero(seriesBudget.get(SeriesBudget.ACTUAL_AMOUNT))) {
            return true;
          }
          if (monthsInScope && seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
            return true;
          }
        }
      }

      if (monthsInScope) {
        for (Integer monthId : expandedMonthIds) {
          Glob seriesBudget = SeriesBudget.find(series.get(Series.ID), monthId, repository);
          if ((seriesBudget != null) && seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
            return true;
          }
        }
      }

      return false;
    }

    private boolean isMonthSelectionInSeriesScope(Integer firstMonth, Integer lastMonth) {
      boolean inScope;
      if (showOnlyIfAvailableOnAllMonths) {
        inScope = true;
        for (Integer id : selectedMonthIds) {
          if ((id < firstMonth) || (id > lastMonth)) {
            inScope = false;
            break;
          }
        }
      }
      else {
        inScope = false;
        for (Integer id : selectedMonthIds) {
          if ((id >= firstMonth) && (id <= lastMonth)) {
            inScope = true;
            break;
          }
        }
      }
      return inScope;
    }

    public String toString() {
      return "SeriesFirstEndDateFilter(" + selectedMonthIds + ", strict:" + showOnlyForActiveMonths + ")";
    }

    protected abstract boolean isEligible(Glob series, GlobRepository repository);
  }
}