package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Month {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  static {
    GlobTypeLoader.init(Month.class, "month");
  }

  private static NumberFormat MONTH_FORMAT = new DecimalFormat("00");
  private static NumberFormat TWO_DIGITS_YEAR_FORMAT = new DecimalFormat("00");
  private static final Calendar CALENDAR = Calendar.getInstance();

  public static String toString(int yyyymm) {
    synchronized (MONTH_FORMAT){
      return toYearString(yyyymm) + "/" + MONTH_FORMAT.format(Month.toMonth(yyyymm));
    }
  }

  public static String toString(int yyyymm, int day) {
    synchronized (MONTH_FORMAT){
      return toYearString(yyyymm) + "/" + MONTH_FORMAT.format(Month.toMonth(yyyymm)) + "/" + MONTH_FORMAT.format(day);
    }
  }

  public static String toCompactString(int yyyymm, int day) {
    synchronized (MONTH_FORMAT){
      return toYearString(yyyymm) + MONTH_FORMAT.format(Month.toMonth(yyyymm)) + MONTH_FORMAT.format(day);
    }
  }

  public static String toYearString(int yyyymm) {
    return Integer.toString(Month.toYear(yyyymm));
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
    synchronized (CALENDAR) {
      CALENDAR.setTime(date);
      return toYyyyMm(CALENDAR.get(Calendar.YEAR), CALENDAR.get(Calendar.MONTH) + 1);
    }
  }

  public static Calendar createCalendar(int yyyymm) {
    Calendar calendar = new GregorianCalendar();
    calendar.clear();
    calendar.set(Calendar.YEAR, toYear(yyyymm));
    calendar.set(Calendar.MONTH, toMonth(yyyymm) - 1);
    return calendar;
  }

  public static int getDay(Date date) {
    synchronized (CALENDAR) {
      CALENDAR.setTime(date);
      return CALENDAR.get(Calendar.DAY_OF_MONTH);
    }
  }

  public static Date toDate(int yyyymm, int day) {
    synchronized (CALENDAR) {
      CALENDAR.clear();
      CALENDAR.set(toYear(yyyymm), toMonth(yyyymm) - 1, day);
      return CALENDAR.getTime();
    }
  }

  public static int offset(int yyyymm, int offset) {
    if (offset < 0) {
      return previous(yyyymm, -offset);
    }
    else if (offset > 0) {
      return next(yyyymm, offset);
    }
    return yyyymm;
  }

  public static int next(int yyyymm) {
    return next(yyyymm, 1);
  }

  public static int next(int yyyymm, int monthsLater) {
    int year = toYear(yyyymm);
    int month = toMonth(yyyymm);

    year += (monthsLater / 12);
    month += monthsLater % 12;

    if (month > 12) {
      year++;
      month = month - 12;
    }
    return toYyyyMm(year, month);
  }

  public static int previous(int yyyymm) {
    return previous(yyyymm, 1);
  }

  public static int previous(int yyyymm, int monthsBack) {
    int year = toYear(yyyymm);
    int month = toMonth(yyyymm);

    year -= (monthsBack / 12);
    month -= monthsBack % 12;

    if (month <= 0) {
      year--;
      month = 12 + month;
    }

    return toMonthId(year, month);
  }

  public static Iterable<Integer> range(final int minYyyyMm, final int maxYyyyMm) {
    return new Iterable<Integer>() {
      public Iterator<Integer> iterator() {
        return new RangeIterator(minYyyyMm, maxYyyyMm);
      }
    };
  }

  public static int toInt(int yyyymm, int dd) {
    return yyyymm * 100 + dd;
  }

  public static int toMonthId(int yyyy, int mm) {
    return yyyy * 100 + mm;
  }

  public static int intToMonthId(int yyyymmdd) {
    return yyyymmdd / 100;
  }

  public static int intToDay(int yyyymmdd) {
    return yyyymmdd % 100;
  }

  public static String getFullLabel(Integer monthId, boolean capitalizeAccordingToLanguage) {
    if (monthId == null) {
      return "";
    }
    int month = toMonth(monthId);
    int year = toYear(monthId);
    return getFullMonthLabel(toMonth(month), capitalizeAccordingToLanguage) + " " + year;
  }

  public static String getFullMonthLabel(Integer monthId, boolean capitalizeAccordingToLanguage) {
    if (monthId == null) {
      return "";
    }
    String text = Lang.get("month." + toMonth(monthId) + ".long");
    return capitalizeAccordingToLanguage ? Lang.capitalizeMonth(text) : text;
  }

  public static String getFullMonthLabelWith4DigitYear(int monthId, boolean capitalizeAccordingToLanguage) {
    return getFullMonthLabel(monthId, capitalizeAccordingToLanguage) + " " + toYear(monthId);
  }

  public static String getShortMonthLabel(Integer monthId) {
    if (monthId == null) {
      return "";
    }
    int month = toMonth(monthId);
    return Lang.get("month." + toMonth(month) + ".medium");
  }

  public static String getShortMonthLabelWithYear(Integer monthId) {
    if (monthId == null) {
      return "";
    }
    int month = toMonth(monthId);
    int year = toYear(monthId);
    return Lang.get("month." + toMonth(month) + ".medium") + " " + year;
  }

  public static String getShortMonthLabelWithShortYear(Integer monthId) {
    if (monthId == null) {
      return "";
    }
    int month = toMonth(monthId);
    int year = toYear(monthId);
    return Lang.get("month." + toMonth(month) + ".medium") + " " + (year % 100);
  }

  private static String getTwoDigitsYearLabel(int year) {
    synchronized (TWO_DIGITS_YEAR_FORMAT){
      return TWO_DIGITS_YEAR_FORMAT.format(year % 100);
    }
  }

  public static String getOneLetterMonthLabel(Integer month) {
    return Lang.get("month." + toMonth(month) + ".short").toUpperCase();
  }

  public static List<Integer> createMonths(int firstMonth, int lastMonth) {
    if (firstMonth > lastMonth) {
      int tmp = lastMonth;
      lastMonth = firstMonth;
      firstMonth = tmp;
    }
    List<Integer> month = new ArrayList<Integer>();
    int currentMonth = firstMonth;
    while (currentMonth <= lastMonth) {
      month.add(currentMonth);
      currentMonth = next(currentMonth);
    }
    return month;
  }

  public static int[] createCountMonthsWithFirst(int monthId, int count) {
    int[] monthIds = new int[count + 1];
    monthIds[0] = monthId;
    for (int i = 1; i < monthIds.length; i++) {
      monthId = next(monthId);
      monthIds[i] = monthId;
    }
    return monthIds;
  }

  static Map<Integer, Integer> monthIdToLastDay = new ConcurrentHashMap<Integer, Integer>();

  public static int getLastDayNumber(int monthId) {
    Integer lastDay = monthIdToLastDay.get(monthId);
    if (lastDay == null) {
      synchronized (CALENDAR) {
        CALENDAR.clear();
        CALENDAR.set(toYear(monthId), toMonth(monthId) - 1, 1);
        lastDay = CALENDAR.getActualMaximum(Calendar.DAY_OF_MONTH);
      }
      monthIdToLastDay.put(monthId, lastDay);
      return lastDay;
    }
    else {
      return lastDay;
    }
  }

  public static Date getLastDay(int monthId) {
    synchronized (CALENDAR) {
      int lastDayNumber = getLastDayNumber(monthId);
      CALENDAR.clear();
      CALENDAR.set(toYear(monthId), toMonth(monthId) - 1, lastDayNumber);
      return CALENDAR.getTime();
    }
  }

  public static Integer getDay(Integer day, int monthId) {
    int lastDay = getLastDayNumber(monthId);
    if (day == null || day <= 0 || day > lastDay) {
      return lastDay;
    }
    return day;
  }

  public static Date addDays(Date date, int day) {
    synchronized (CALENDAR) {
      CALENDAR.setTime(date);
      CALENDAR.add(Calendar.DAY_OF_MONTH, day);
      return CALENDAR.getTime();
    }
  }

  public static boolean isContinuousSequence(int[] months) {
    int previous = months[0];
    for (int i = 1; i < months.length; i++) {
      if (months[i] != next(previous)) {
        return false;
      }
      previous = months[i];
    }
    return true;
  }

  public static int distance(int fromMonth, int toMonth) {
    int distance = 0;
    if (fromMonth <= toMonth) {
      while (fromMonth < toMonth) {
        fromMonth = next(fromMonth);
        distance++;
      }
      return distance;
    }
    else {
      while (fromMonth > toMonth) {
        fromMonth = previous(fromMonth);
        distance--;
      }
      return distance;
    }
  }

  public static int distance(int fromMonth, int fromDay, int toMonth, int toDay) {
    if (fromMonth == toMonth) {
      return Math.abs(fromDay - toDay);
    }

    int distance = 0;
    if (fromMonth < toMonth) {
      distance += getLastDayNumber(fromMonth) - fromDay;
      for (int month = Month.next(fromMonth); month < toMonth; month = Month.next(month)) {
        distance += getLastDayNumber(month);
      }
      distance += toDay;
    }
    else {
      distance += getLastDayNumber(toMonth) - toDay;
      for (int month = Month.next(toMonth); month < fromMonth; month = Month.next(month)) {
        distance += getLastDayNumber(month);
      }
      distance += fromDay;
    }
    return distance;
  }

  public static Iterable<Integer> yearRange(int monthId) {
    int firstMonth = Month.toYear(monthId) * 100 + 1;
    int lastMonth = firstMonth + 11;
    return range(firstMonth, lastMonth);
  }

  public static int toFullDate(int monthId, int day) {
    return monthId * 100 + day;
  }

  public static int getDayFromFullDate(int fullDate){
    return fullDate % 100;
  }

  public static int getMonthIdFromFullDate(int fullDate){
    return fullDate / 100;
  }

  public static int toFullDate(Date date) {
    return toFullDate(getMonthId(date), getDay(date));
  }

  public static int getFullDate(Date date) {
    return toFullDate(date);
  }

  public static class RangeIterator implements Iterator<Integer> {
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

  public static class Serializer implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(ID));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(ID, input.readInteger());
    }

    public int getWriteVersion() {
      return 1;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }
  }
}
