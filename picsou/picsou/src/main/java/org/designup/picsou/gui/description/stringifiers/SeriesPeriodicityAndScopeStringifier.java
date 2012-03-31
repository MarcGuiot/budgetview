package org.designup.picsou.gui.description.stringifiers;

import org.designup.picsou.model.Month;
import org.designup.picsou.model.ProfileType;
import org.designup.picsou.model.Series;
import org.designup.picsou.utils.Lang;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobListStringifier;
import org.globsframework.utils.Strings;

public class SeriesPeriodicityAndScopeStringifier implements GlobListStringifier {
  public String toString(GlobList list, GlobRepository repository) {
    if (list.isEmpty() || list.size() > 1) {
      return "";
    }

    Glob series = list.getFirst();
    ProfileType profile = ProfileType.get(series.get(Series.PROFILE_TYPE));
    Integer firstMonth = series.get(Series.FIRST_MONTH);
    Integer lastMonth = series.get(Series.LAST_MONTH);
    if ((firstMonth != null) && firstMonth.equals(lastMonth)) {
      return Lang.get("monthRange.singleMonth", toString(firstMonth));
    }

    return Strings.join(profile.getLabel(), formatRange(firstMonth, lastMonth).toLowerCase());
  }

  private String formatRange(Integer firstMonth, Integer lastMonth) {
    if ((firstMonth != null) && (lastMonth != null)) {
      return MonthListStringifier.toString(firstMonth, lastMonth, RANGE_FORMATER);
    }
    else if (firstMonth != null) {
      return Lang.get("monthRange.from", toString(firstMonth));
    }
    else if (lastMonth != null) {
      return Lang.get("monthRange.to", toString(lastMonth));
    }
    return "";
  }

  private String toString(Integer firstMonth) {
    return Month.getFullMonthLabelWith4DigitYear(firstMonth);
  }

  private static final MonthRangeFormatter RANGE_FORMATER = new MonthRangeFormatter() {
    public String year(int year) {
      return Lang.get("monthRange.year",
                      Integer.toString(year));
    }

    public String yearRange(int firstYear, int lastYear) {
      return Lang.get("monthRange.yearRange",
                      Integer.toString(firstYear),
                      Integer.toString(lastYear));
    }

    public String monthRangeInYear(int firstMonthId, int lastMonthId, int year) {
      return Lang.get("monthRange.monthRangeInYear",
                      Month.getFullMonthLabel(firstMonthId),
                      Month.getFullMonthLabel(lastMonthId),
                      Integer.toString(year));
    }

    public String monthRangeAcrossYears(int firstMonthId, int lastMonthId) {
      return Lang.get("monthRange.monthRangeAcrossYears",
                      Month.getFullLabel(firstMonthId),
                      Month.getFullLabel(lastMonthId));
    }
  };
}
