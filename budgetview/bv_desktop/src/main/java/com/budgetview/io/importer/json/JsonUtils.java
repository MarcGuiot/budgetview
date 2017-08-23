package com.budgetview.io.importer.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonUtils {
  public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
  public static final SimpleDateFormat DEFAULT_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public static Date parseDate(String yyyyMMdd_hhmmss) {
    synchronized (DEFAULT_DATE_FORMAT) {
      try {
        return DEFAULT_DATE_FORMAT.parse(yyyyMMdd_hhmmss);
      }
      catch (ParseException e) {
        throw new RuntimeException("Format should be: " + DEFAULT_DATE_FORMAT.toPattern(), e);
      }
    }
  }

  public static Date parseTimestamp(String yyyyMMdd_hhmmss) {
    synchronized (DEFAULT_TIMESTAMP_FORMAT) {
      try {
        return DEFAULT_TIMESTAMP_FORMAT.parse(yyyyMMdd_hhmmss);
      }
      catch (ParseException e) {
        throw new RuntimeException("Format should be: " + DEFAULT_TIMESTAMP_FORMAT.toPattern(), e);
      }
    }
  }

  public static String toTimeStampString(Date date) {
    return DEFAULT_TIMESTAMP_FORMAT.format(date);
  }
}
