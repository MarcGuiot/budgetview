package org.globsframework.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonGlobFormat {

  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  public static String toString(Date date) {
    if (date == null) {
      return null;
    }
    synchronized (DATE_FORMAT) {
      return DATE_FORMAT.format(date);
    }
  }

  public static Date parseDate(String date) throws ParseException {
    if (date == null) {
      return null;
    }
    synchronized (DATE_FORMAT) {
      return DATE_FORMAT.parse(date);
    }
  }
}
