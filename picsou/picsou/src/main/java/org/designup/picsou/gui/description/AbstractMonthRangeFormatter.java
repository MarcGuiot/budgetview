package org.designup.picsou.gui.description;

public abstract class AbstractMonthRangeFormatter implements MonthRangeFormatter {
  public String year(int year) {
    return Integer.toString(year);
  }

  public String yearRange(int firstYear, int lastYear) {
    return Integer.toString(firstYear) + " - " + Integer.toString(lastYear);
  }
}
