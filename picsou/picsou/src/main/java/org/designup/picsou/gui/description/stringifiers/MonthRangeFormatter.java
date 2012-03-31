package org.designup.picsou.gui.description.stringifiers;

import org.designup.picsou.model.Month;

public interface MonthRangeFormatter {

  String year(int year);

  String yearRange(int firstYear, int lastYear);

  String monthRangeInYear(int firstMonthId, int lastMonthId, int firstYear);

  String monthRangeAcrossYears(int firstMonthId, int lastMonthId);

  public static final MonthRangeFormatter COMPACT = new AbstractMonthRangeFormatter() {

    public String monthRangeInYear(int firstMonthId, int lastMonthId, int year) {
      if (firstMonthId == lastMonthId) {
        return Month.getShortMonthLabel(firstMonthId) + " " + Integer.toString(year);
      }
      return Month.getShortMonthLabel(firstMonthId) + "-" + Month.getShortMonthLabel(lastMonthId) +
             " " + Integer.toString(year);
    }

    public String monthRangeAcrossYears(int firstMonthId, int lastMonthId) {
      return Month.getFullLabel(firstMonthId) + " - " + Month.getFullLabel(lastMonthId);
    }
  };

  public static final MonthRangeFormatter STANDARD = new AbstractMonthRangeFormatter() {

    public String monthRangeInYear(int firstMonthId, int lastMonthId, int year) {
      if (firstMonthId == lastMonthId) {
        return Month.getFullMonthLabel(firstMonthId) + " " + Integer.toString(year);
      }
      return Month.getFullMonthLabel(firstMonthId) + " - " + Month.getFullMonthLabel(lastMonthId) +
             " " + Integer.toString(year);
    }

    public String monthRangeAcrossYears(int firstMonthId, int lastMonthId) {
      return Month.getFullLabel(firstMonthId) + " - " + Month.getFullLabel(lastMonthId);
    }
  };
}
