package com.budgetview.server.utils;

import java.util.Calendar;
import java.util.Date;

public class DateConverter {
  private static final Calendar CALENDAR = Calendar.getInstance();

  public static int getMonthId(Date date) {
    synchronized (CALENDAR) {
      CALENDAR.setTime(date);
      return toYyyyMm(CALENDAR.get(Calendar.YEAR), CALENDAR.get(Calendar.MONTH) + 1);
    }
  }

  public static int getDay(Date date) {
    synchronized (CALENDAR) {
      CALENDAR.setTime(date);
      return CALENDAR.get(Calendar.DAY_OF_MONTH);
    }
  }

  public static int toYyyyMm(int year, int month) {
    return year * 100 + month;
  }

  public static int toYyyyMmDd(Date date) {
    return getMonthId(date) * 100 + getDay(date);
  }
}
