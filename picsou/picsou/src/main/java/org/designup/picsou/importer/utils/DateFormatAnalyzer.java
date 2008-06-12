package org.designup.picsou.importer.utils;

import org.crossbowlabs.globs.utils.exceptions.InvalidData;

import java.util.*;

public class DateFormatAnalyzer {
  private Date referenceDate;

  public DateFormatAnalyzer(Date referenceDate) {
    this.referenceDate = referenceDate;
  }

  public List<String> parse(Set<String> dates) throws InvalidData {
    List<String> result = new ArrayList<String>();
    result.add("yy/MM/dd");
    result.add("MM/dd/yy");
    result.add("dd/MM/yy");

    int maxFirst = 0;
    int maxSecond = 0;
    int maxThird = 0;

    for (String date : dates) {
      String[] items = date.split("[-\\./]");
      if (items.length != 3) {
        throw new InvalidData(date + " - items: " + Arrays.toString(items));
      }
      maxFirst = getMax(maxFirst, items, 0);
      maxSecond = getMax(maxSecond, items, 1);
      maxThird = getMax(maxThird, items, 2);
    }

    Calendar calendar = new GregorianCalendar();
    calendar.setTime(referenceDate);
    int maxYear = calendar.get(Calendar.YEAR) + 1;
    if (toYear(maxFirst) > maxYear) {
      result.remove("yy/MM/dd");
    }
    if (toYear(maxThird) > maxYear) {
      result.remove("dd/MM/yy");
      result.remove("MM/dd/yy");
    }

    if (maxFirst > 12) {
      result.remove("MM/dd/yy");
    }
    if (maxSecond > 12) {
      result.remove("dd/MM/yy");
    }

    if (maxFirst > 31) {
      result.remove("dd/MM/yy");
    }
    if (maxSecond > 31) {
      result.remove("MM/dd/yy");
    }
    if (maxThird > 31) {
      result.remove("yy/MM/dd");
    }

    return result;
  }

  private int toYear(int maxFirst) {
    return maxFirst < 100 ? maxFirst < 70 ? 2000 + maxFirst : 1900 + maxFirst : maxFirst;
  }

  private int getMax(int maxFirst, String[] items, int pos) {
    try {
      int value = Integer.parseInt(items[pos]);
      return Math.max(maxFirst, value);
    }
    catch (NumberFormatException e) {
      throw new InvalidData("Invalid date element '" + items[pos] + "' in " + Arrays.toString(items));
    }
  }
}
