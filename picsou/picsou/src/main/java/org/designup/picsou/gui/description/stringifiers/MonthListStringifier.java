package org.designup.picsou.gui.description.stringifiers;

import org.designup.picsou.model.Month;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class MonthListStringifier implements GlobListStringifier {
  public String toString(GlobList months, GlobRepository repository) {
    Set<Integer> monthIds = months.getValueSet(Month.ID);
    return toString(monthIds);
  }

  public static String toString(int firstMonthId, int lastMonthId, MonthRangeFormatter rangeFormatter) {
    int firstMonth = Month.toMonth(firstMonthId);
    int firstYear = Month.toYear(firstMonthId);

    int lastMonth = Month.toMonth(lastMonthId);
    int lastYear = Month.toYear(lastMonthId);

    if ((firstMonth == 1) && (lastMonth == 12)) {
      if (firstYear == lastYear) {
        return rangeFormatter.year(firstYear);
      }
      else {
        return rangeFormatter.yearRange(firstYear, lastYear);
      }
    }
    else {
      if (firstYear == lastYear) {
        return rangeFormatter.monthRangeInYear(firstMonthId, lastMonthId, firstYear);
      }
      else {
        return rangeFormatter.monthRangeAcrossYears(firstMonthId, lastMonthId);
      }
    }
  }

  public static String toString(Collection<Integer> monthIds) {
    return toString(monthIds, MonthRangeFormatter.STANDARD);
  }

  public static String toString(Collection<Integer> monthIds, MonthRangeFormatter rangeFormatter) {
    if (monthIds.isEmpty()) {
      return "";
    }
    if (monthIds.size() == 1) {
      Integer monthId = monthIds.iterator().next();
      return rangeFormatter.monthRangeInYear(monthId, monthId, Month.toYear(monthId));
    }

    int[] months = getSortedMonths(monthIds);

    boolean isContinuous = Month.isContinuousSequence(months);
    if (!isContinuous) {
      return "";
    }

    int firstMonthId = months[0];
    int lastMonthId = months[months.length - 1];

    return toString(firstMonthId, lastMonthId, rangeFormatter);
  }

  private static int[] getSortedMonths(Collection<Integer> monthIds) {
    int[] months = new int[monthIds.size()];
    int index = 0;
    for (Integer monthId : monthIds) {
      months[index++] = monthId;
    }
    Arrays.sort(months);
    return months;
  }

}
