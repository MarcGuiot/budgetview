package org.designup.picsou.gui.time;

import org.designup.picsou.model.Month;
import org.globsframework.utils.Log;

import java.util.Date;

public class TimeService {
  private static Date today;
  private static int monthId;
  private static int day;

  static {
    reset();
  }

  public TimeService() {
  }

  public TimeService(Date day) {
    today = day;
    monthId = Month.getMonthId(day);
  }

  public static void reset(){
    today = new Date();
    Log.write("new day " + today);
    day = Month.getDay(today);
    monthId = Month.getMonthId(today);
  }

  public static int getCurrentFullDate() {
    return Month.toFullDate(getCurrentMonth(), getCurrentDay());
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
