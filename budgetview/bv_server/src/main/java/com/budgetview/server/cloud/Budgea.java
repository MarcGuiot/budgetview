package com.budgetview.server.cloud;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Budgea {
  public static final String CLIENT_ID = "60443827";
  public static final String CLIENT_SECRET = "E9W5QStthEi7mh7+ARAZV2wIRS0eY4o7";

  private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

  // Ex: 2016-08-10 17:44:28
  public static Date parseTimestamp(String timestamp) throws ParseException {
    return TIMESTAMP_FORMAT.parse(timestamp);
  }

  // Ex: 2016-08-10
  public static Date parseDate(String timestamp) throws ParseException {
    return DATE_FORMAT.parse(timestamp);
  }
}
