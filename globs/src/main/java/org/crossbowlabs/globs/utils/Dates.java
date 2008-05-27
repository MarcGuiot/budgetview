package org.crossbowlabs.globs.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;

public class Dates {
  public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
  private static final DateFormat dateFormat = DEFAULT_DATE_FORMAT;
  public static final SimpleDateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
  private static final DateFormat timestampFormat = DEFAULT_TIMESTAMP_FORMAT;
  public static final SimpleDateFormat DEFAULT_MONTH_FORMAT = new SimpleDateFormat("yyyy/MM");
  private static final DateFormat monthFormat = DEFAULT_MONTH_FORMAT;

  private Dates() {
  }

  public static Date parse(String yyyyMMdd) {
    synchronized (dateFormat) {
      try {
        return dateFormat.parse(yyyyMMdd);
      }
      catch (ParseException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static String toString(Date date) {
    if (date == null) {
      return "";
    }
    synchronized (dateFormat) {
      return dateFormat.format(date);
    }
  }

  /** Sample format: "03/10/2002 12:34:20" */
  public static Date parseTimestamp(String yyyyMMdd_hhmmss) {
    synchronized (timestampFormat) {
      try {
        return timestampFormat.parse(yyyyMMdd_hhmmss);
      }
      catch (ParseException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static String toTimestampString(Date date) {
    synchronized (timestampFormat) {
      return timestampFormat.format(date);
    }
  }

  public static String getStandardDate(Date date) {
    synchronized (dateFormat) {
      return dateFormat.format(date);
    }
  }

  public static Date parseMonth(String yyyymm) {
    synchronized (monthFormat) {
      try {
        return monthFormat.parse(yyyymm);
      }
      catch (ParseException e){
        throw new RuntimeException(e);
      }
    }
  }

  public static String toMonth(Date date) {
    synchronized (monthFormat) {
      return monthFormat.format(date);
    }
  }

  public static boolean isNear(Date now, Date target, long marginInMillis) {
      return millisBetween(now, target) < marginInMillis;
  }

  public static boolean isNear(Calendar now, Calendar target, long marginInMillis) {
      return millisBetween(now, target) < marginInMillis;
  }

  public static long millisBetween(Date date1, Date date2) {
    return Math.abs(date1.getTime() - date2.getTime());
  }

  public static long millisBetween(Calendar date1, Calendar date2) {
    return Math.abs(date1.getTimeInMillis() - date2.getTimeInMillis());
  }
}
