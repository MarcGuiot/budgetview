package org.designup.picsou.gui.time;

import org.designup.picsou.model.Month;

import java.util.Date;

public class TimeService {
  private static Date today = new Date();
  private static int monthId = Month.getMonthId(today);
  private static int day = Month.getDay(today);

  public TimeService() {
  }

  public TimeService(Date day) {
    today = day;
    monthId = Month.getMonthId(day);
  }

  public int getCurrentMonthId() {
    return monthId;
  }

  public static Date getToday() {
    return today;
  }

  public static void setCurrentDate(Date date) {
    today = date;
    monthId = Month.getMonthId(date);
    day = Month.getDay(date);
  }

  public static int getCurrentMonth() {
    return monthId;
  }

  public static int getCurrentDay() {
    return day;
  }
}
