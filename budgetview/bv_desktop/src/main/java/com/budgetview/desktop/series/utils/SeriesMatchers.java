package com.budgetview.desktop.series.utils;

import com.budgetview.desktop.utils.MonthMatcher;
import com.budgetview.model.Account;
import com.budgetview.model.Month;
import com.budgetview.model.Series;
import com.budgetview.model.SeriesBudget;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.shared.utils.Amounts;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class SeriesMatchers {

  public static GlobMatcher activeInMonth(int monthId) {
    return new GlobMatcher() {
      public boolean matches(Glob series, GlobRepository repository) {
        if (series == null) {
          return false;
        }
        Integer first = series.get(Series.FIRST_MONTH);
        if (first != null && monthId < first) {
          return false;
        }
        Integer last = series.get(Series.LAST_MONTH);
        if (last != null && monthId > last) {
          return false;
        }
        return true;
      }
    };
  }

  public static MonthMatcher seriesActiveInPeriod(final BudgetArea budgetArea, boolean showOnlyForActiveMonths, boolean showOnlyIfAvailableOnAllMonths, boolean showOnPreviousAndNextMonth) {
    return new SeriesFirstEndDateFilter(showOnlyForActiveMonths, showOnlyIfAvailableOnAllMonths, showOnPreviousAndNextMonth) {
      protected boolean isEligible(Glob series, GlobRepository repository) {
        return budgetArea.getId().equals(series.get(Series.BUDGET_AREA));
      }
    };
  }

  public static GlobMatcher deferredCardSeries() {
    return and(fieldEquals(Series.BUDGET_AREA, BudgetArea.OTHER.getId()),
               not(fieldEquals(Series.ID, Series.ACCOUNT_SERIES_ID)));
  }

  public static GlobMatcher seriesForAccount(final Glob account) {
    return seriesForAccount(account != null ? account.get(Account.ID) : null);
  }

  public static GlobMatcher seriesForAccount(final Integer accountId) {
    if (accountId == null) {
      return GlobMatchers.ALL;
    }
    return new GlobMatcher() {
      public boolean matches(Glob series, GlobRepository repository) {
        return Series.isSeriesForAccount(series, accountId, repository);
      }
    };
  }

  public static GlobMatcher seriesForGlobalBudget() {
    return new GlobMatcher() {
      public boolean matches(Glob series, GlobRepository repository) {
        Glob account = repository.findLinkTarget(series, Series.TARGET_ACCOUNT);
        if (account != null && !Account.isMain(account) && !Account.isSavings(account)) {
          return false;
        }
        if (Series.isTransfer(series)) {
          // Select only one of the two mirrors
          if (Utils.equal(Account.EXTERNAL_ACCOUNT_ID, series.get(Series.FROM_ACCOUNT))) {
            return Utils.equal(series.get(Series.TARGET_ACCOUNT), series.get(Series.TO_ACCOUNT));
          }
          if (Utils.equal(Account.EXTERNAL_ACCOUNT_ID, series.get(Series.TO_ACCOUNT))) {
            return Utils.equal(series.get(Series.TARGET_ACCOUNT), series.get(Series.FROM_ACCOUNT));
          }
          if (Account.isMain(series.get(Series.FROM_ACCOUNT), repository)) {
            return Utils.equal(series.get(Series.TARGET_ACCOUNT), series.get(Series.FROM_ACCOUNT));
          }
          return Utils.equal(series.get(Series.TARGET_ACCOUNT), series.get(Series.TO_ACCOUNT));
        }
        return true;
      }
    };
  }

  private static abstract class SeriesFirstEndDateFilter implements MonthMatcher {
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
