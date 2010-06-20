package org.designup.picsou.gui.utils;

import org.designup.picsou.model.*;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import static org.globsframework.model.utils.GlobMatchers.*;

import java.util.*;

public class PicsouMatchers {
  private PicsouMatchers() {
  }

  public static SeriesFirstEndDateFilter seriesDateFilter(final Integer budgetAreaId, boolean isExclusive) {
    return new SeriesFirstEndDateFilter(isExclusive) {

      protected boolean isEligible(Glob series, GlobRepository repository) {
        return budgetAreaId.equals(series.get(Series.BUDGET_AREA));
      }
    };
  }

  static public abstract class SeriesFirstEndDateFilter implements GlobMatcher {
    private boolean exclusive;
    private Set<Integer> monthIds = Collections.emptySet();

    private SeriesFirstEndDateFilter(boolean isExclusive) {
      exclusive = isExclusive;
    }

    public void filterDates(Set<Integer> monthIds) {
      this.monthIds = monthIds;
    }

    public boolean matches(Glob series, GlobRepository repository) {
      if (isEligible(series, repository)) {
        Integer firstMonth = series.get(Series.FIRST_MONTH);
        Integer lastMonth = series.get(Series.LAST_MONTH);
        if (firstMonth == null) {
          firstMonth = 0;
        }
        if (lastMonth == null) {
          lastMonth = Integer.MAX_VALUE;
        }
        for (Integer id : monthIds) {
          if ((id < firstMonth || id > lastMonth) == exclusive) {
            return !exclusive;
          }
          if (!exclusive) {
            Glob seriesBudget = repository.findByIndex(SeriesBudget.SERIES_INDEX, SeriesBudget.SERIES, series.get(Series.ID))
              .findByIndex(SeriesBudget.MONTH, id).getGlobs().getFirst();
            if (seriesBudget != null && seriesBudget.isTrue(SeriesBudget.ACTIVE)) {
              return true;
            }
          }
        }
        return exclusive;
      }
      return false;
    }

    protected abstract boolean isEligible(Glob series, GlobRepository repository);
  }

  public static class AccountDateMatcher implements GlobMatcher {
    private Set<Integer> months = new HashSet<Integer>();

    public AccountDateMatcher(GlobList months) {
      this.months.addAll(months.getValueSet(Month.ID));
    }

    public boolean matches(Glob item, GlobRepository repository) {
      Date startDate = item.get(Account.OPEN_DATE);
      Date endDate = item.get(Account.CLOSED_DATE);
      if (startDate == null && endDate == null) {
        return true;
      }
      if (startDate != null && endDate != null) {
        int startMonthId = Month.getMonthId(startDate);
        int endMonthId = Month.getMonthId(endDate);
        for (Integer month : months) {
          if (month >= startMonthId && month <= endMonthId) {
            return true;
          }
        }
        return false;
      }
      if (startDate != null) {
        int startMonthId = Month.getMonthId(startDate);
        for (Integer month : months) {
          if (month >= startMonthId) {
            return true;
          }
        }
        return false;
      }
      // endDate != null
      int endMonthId = Month.getMonthId(endDate);
      for (Integer month : months) {
        if (month <= endMonthId) {
          return true;
        }
      }
      return false;
    }
  }

  public static void main(String[] args) {
    int id = 6;
    boolean ex = false;
    if ((id < 5 || id > 10) == ex) {
      System.out.println("PicsouMatchers.main " + !ex);
    }
  }
}
