package com.budgetview.analytics.utils;

import org.joda.time.DateTime;

import java.util.Date;

public class Weeks {

  public static int getWeekId(Date date) {
    return getWeekId(new DateTime(date));
  }

  public static int getWeekId(DateTime date) {
    return date.getWeekyear() * 100 + date.getWeekOfWeekyear();
  }

  public static int previous(int weekId) {
    int year = weekId / 100;
    int weekNum = weekId % 100;
    if (weekNum <= 1) {
      year--;
      DateTime lastDay = new DateTime(year, 12, 31, 12, 0, 0, 0);
      return year * 100 + lastDay.getWeekOfWeekyear();
    }

    return year * 100 + (weekNum - 1);
  }

  public static int next(int weekId) {
    int year = weekId / 100;
    int weekNum = weekId % 100;
    DateTime lastDay = new DateTime(year, 12, 31, 06, 0, 0, 0);
    int lastWeek = lastDay.getWeekOfWeekyear();
    if (lastWeek == 1) {
      lastDay = lastDay.minusDays(7);
      lastWeek = lastDay.getWeekOfWeekyear();
    }
    if (weekNum >= lastWeek) {
      return (year + 1) * 100 + 1;
    }

    return year * 100 + (weekNum + 1);
  }

  public static Date getLastDay(int weekId) {
    DateTime day = new DateTime(weekId / 100, 1, 1, 12, 0, 0, 0).plusWeeks(weekId % 100);
    return Days.getLastDayOfWeek(day);
  }

  public static Date getFirstDay(int weekId) {
    DateTime day = new DateTime(weekId / 100, 1, 1, 12, 0, 0, 0).plusWeeks(weekId % 100);
    return Days.getFirstDayOfWeek(day);
  }
}
