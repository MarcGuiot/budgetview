package com.budgetview.gui.description.stringifiers;

import com.budgetview.model.Month;

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
      return Month.getFullLabel(firstMonthId, true) + " - " + Month.getFullLabel(lastMonthId, true);
    }
  };

  public static final MonthRangeFormatter STANDARD = new AbstractMonthRangeFormatter() {

    public String monthRangeInYear(int firstMonthId, int lastMonthId, int year) {
      if (firstMonthId == lastMonthId) {
        return Month.getFullMonthLabel(firstMonthId, true) + " " + Integer.toString(year);
      }
      return Month.getFullMonthLabel(firstMonthId, true) + " - " + Month.getFullMonthLabel(lastMonthId, true) +
             " " + Integer.toString(year);
    }

    public String monthRangeAcrossYears(int firstMonthId, int lastMonthId) {
      return Month.getFullLabel(firstMonthId, true) + " - " + Month.getFullLabel(lastMonthId, true);
    }
  };
}
