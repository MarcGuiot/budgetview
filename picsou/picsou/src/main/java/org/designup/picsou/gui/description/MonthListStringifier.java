package org.designup.picsou.gui.description;

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

  public static String toString(Collection<Integer> monthIds) {
    if (monthIds.isEmpty()) {
      return "";
    }
    if (monthIds.size() == 1) {
      return Month.getFullLabel(monthIds.iterator().next());
    }

    int[] months = getSortedMonths(monthIds);

    boolean isContinuous = Month.isContinuousSequence(months);

    int firstMonthId = months[0];
    int firstMonth = Month.toMonth(firstMonthId);
    int firstYear = Month.toYear(firstMonthId);

    int lastMonthId = months[months.length - 1];
    int lastMonth = Month.toMonth(lastMonthId);
    int lastYear = Month.toYear(lastMonthId);

    if (isContinuous) {
      if ((firstMonth == 1) && (lastMonth == 12)) {
        if (firstYear == lastYear) {
          return Integer.toString(firstYear);
        }
        else {
          return Integer.toString(firstYear) + " - " + Integer.toString(lastYear);
        }
      }
      else {
        if (firstYear == lastYear) {
          return Month.getFullMonthLabel(firstMonthId) + " - " + Month.getFullMonthLabel(lastMonthId) +
                 " " + Integer.toString(firstYear);
        }
        else {
          return Month.getFullLabel(firstMonthId) + " - " + Month.getFullLabel(lastMonthId);
        }
      }
    }

    return "";
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
