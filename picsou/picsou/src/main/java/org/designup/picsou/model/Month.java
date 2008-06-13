package org.designup.picsou.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.annotations.Key;
import org.crossbowlabs.globs.metamodel.fields.IntegerField;
import org.crossbowlabs.globs.metamodel.utils.GlobTypeLoader;
import org.crossbowlabs.globs.model.GlobList;
import org.crossbowlabs.globs.utils.Utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class Month {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  static {
    GlobTypeLoader.init(Month.class);
  }

  private static NumberFormat YEAR_FORMAT = new DecimalFormat("00");
  private static final Calendar CALENDAR = Calendar.getInstance();

  public static String toString(int yyyymm) {
    return Month.toMonth(yyyymm) + "/" + toYearString(yyyymm);
  }

  public static String toYearString(int yyyymm) {
    return YEAR_FORMAT.format(Month.toYear(yyyymm) % 100);
  }

  public static int toYear(int yyyymm) {
    return yyyymm / 100;
  }

  public static int toMonth(int yyyymm) {
    return yyyymm % 100;
  }

  public static int toYyyyMm(int year, int month) {
    return year * 100 + month;
  }

  public static int getMonthId(Date date) {
    CALENDAR.setTime(date);
    return toYyyyMm(CALENDAR.get(Calendar.YEAR), CALENDAR.get(Calendar.MONTH) + 1);
  }

  public static Calendar createCalendar(int yyyymm) {
    Calendar calendar = new GregorianCalendar();
    calendar.clear();
    calendar.set(Calendar.YEAR, toYear(yyyymm));
    calendar.set(Calendar.MONTH, toMonth(yyyymm) - 1);
    return calendar;
  }

  public static int getDay(Date date) {
    CALENDAR.setTime(date);
    return CALENDAR.get(Calendar.DAY_OF_MONTH);
  }

  public static Date toDate(int yyyymm, int day) {
    CALENDAR.clear();
    CALENDAR.set(toYear(yyyymm), toMonth(yyyymm) - 1, day);
    return CALENDAR.getTime();
  }

  public static int next(int yyyymm) {
    return normalize(yyyymm + 1);
  }

  public static int previous(int yyyymm) {
    return normalize(yyyymm - 1);
  }

  public static int normalize(int yyyymm) {
    int year = toYear(yyyymm);
    int month = toMonth(yyyymm);

    if (month == 0) {
      year--;
      month = 12;
    }
    else {
      int years = (month - 1) / 12;
      month = (month - 1) % 12 + 1;
      year += years;
    }
    return toYyyyMm(year, month);
  }

  public static Iterable<Integer> range(final int minYyyyMm, final int maxYyyyMm) {
    return new Iterable<Integer>() {
      public Iterator<Integer> iterator() {
        return new RangeIterator(minYyyyMm, maxYyyyMm);
      }
    };
  }

  public static int[] range(Integer startYyyyMm, Integer endYyyyMm) {
    List<Integer> result = new ArrayList<Integer>();
    for (int month = startYyyyMm; month <= endYyyyMm; month = next(month)) {
      result.add(month);
    }
    return Utils.toArray(result);
  }

  public static int[] getRange(GlobList months) {
    return new int[0];
  }

  private static class RangeIterator implements Iterator<Integer> {
    private int current;
    private int max;

    public RangeIterator(int minYyyyMm, int maxYyyyMm) {
      this.current = minYyyyMm;
      this.max = maxYyyyMm;
    }

    public boolean hasNext() {
      return current <= max;
    }

    public Integer next() throws NoSuchElementException {
      if (current > max) {
        throw new NoSuchElementException();
      }
      int previous = current;
      current = Month.next(current);
      return previous;
    }

    public void remove() throws UnsupportedOperationException {
      throw new UnsupportedOperationException();
    }
  }

}
