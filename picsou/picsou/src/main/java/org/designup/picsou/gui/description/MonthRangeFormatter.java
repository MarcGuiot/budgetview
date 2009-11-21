package org.designup.picsou.gui.description;

import org.designup.picsou.model.Month;

public interface MonthRangeFormatter {

  String year(int year);

  String yearRange(int firstYear, int lastYear);

  String monthRangeInYear(int firstMonthId, int lastMonthId, int firstYear);

  String monthRangeAcrossYears(int firstMonthId, int lastMonthId);

  public static final MonthRangeFormatter COMPACT = new MonthRangeFormatter() {
    public String year(int year) {
      return Integer.toString(year);
    }

    public String yearRange(int firstYear, int lastYear) {
      return Integer.toString(firstYear) + " - " + Integer.toString(lastYear);
    }

    public String monthRangeInYear(int firstMonthId, int lastMonthId, int year) {
      return Month.getFullMonthLabel(firstMonthId) + " - " + Month.getFullMonthLabel(lastMonthId) +
             " " + Integer.toString(year);
    }

    public String monthRangeAcrossYears(int firstMonthId, int lastMonthId) {
      return Month.getFullLabel(firstMonthId) + " - " + Month.getFullLabel(lastMonthId);
    }
  };
}
