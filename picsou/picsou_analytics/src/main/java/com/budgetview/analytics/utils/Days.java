package com.budgetview.analytics.utils;

import org.globsframework.utils.exceptions.InvalidParameter;
import org.joda.time.DateTime;

import java.util.Date;

public class Days {

  public static int daysBetween(Date from, Date to) {
    if (from == null) {
      throw new InvalidParameter("From date should no be null");
    }
    if (to == null) {
      throw new InvalidParameter("To date should no be null");
    }
    return org.joda.time.Days.daysBetween(new DateTime(from), new DateTime(to)).getDays();
  }

  public static Date getFirstDayOfWeek(DateTime date) {
    int dayOfWeek = date.getDayOfWeek();
    DateTime firstDayOfWeek = date.minusDays(dayOfWeek - 1);
    return firstDayOfWeek.toDate();
  }

  public static Date getLastDayOfWeek(DateTime date) {
    int dayOfWeek = date.getDayOfWeek();
    DateTime lastDayOfWeek = date.plusDays(7 - dayOfWeek);
    return lastDayOfWeek.toDate();
  }
}
