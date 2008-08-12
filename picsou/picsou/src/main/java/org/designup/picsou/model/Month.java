package org.designup.picsou.model;

import org.designup.picsou.server.serialization.PicsouGlobSerializer;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.FieldSetter;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.Utils;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class Month {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  static {
    GlobTypeLoader.init(Month.class, "month");
  }

  private static NumberFormat MONTH_FORMAT = new DecimalFormat("00");
  private static final Calendar CALENDAR = Calendar.getInstance();

  public static String toString(int yyyymm) {
    return toYearString(yyyymm) + "/" + MONTH_FORMAT.format(Month.toMonth(yyyymm));
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

  public static int toInt(int yyyymm, int dd) {
    return yyyymm * 100 + dd;
  }

  public static int intToMonthId(int yyyymmdd) {
    return yyyymmdd / 100;
  }

  public static int intToDay(int yyyymmdd) {
    return yyyymmdd % 100;
  }

  public static String getLabel(Integer monthId) {
    int month = toMonth(monthId);
    int year = toYear(monthId);
    return Lang.get("month." + toMonth(month) + ".long").toLowerCase() + " " + year;
  }

  public static int[] createMonths(int monthId, int count) {
    int[] monthIds = new int[count];
    for (int i = 0; i < monthIds.length; i++) {
      monthId = next(monthId);
      monthIds[i] = monthId;
    }
    return monthIds;
  }

  public static int[] createMonthsWithFirst(int monthId, int count) {
    int[] monthIds = new int[count + 1];
    monthIds[0] = monthId;
    for (int i = 1; i < monthIds.length; i++) {
      monthId = next(monthId);
      monthIds[i] = monthId;
    }
    return monthIds;
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

  public static class Serialization implements PicsouGlobSerializer {

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeInteger(values.get(ID));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data) {
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
  }
}
