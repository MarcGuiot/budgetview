package com.budgetview.gui.time;

import com.budgetview.model.Month;

import java.util.Date;

public class TimeService {
  private static Date today;
  private static int monthId;
  private static int day;
  private static Now now = new Now() {
    public long now() {
      return System.currentTimeMillis();
    }
  };

  public interface Now {
    long now();
  }

  public static Now setTimeAccessor(Now now){
    Now tmp = TimeService.now;
    TimeService.now = now;
    return tmp;
  }

  static {
    reset();
  }

  public TimeService() {
  }

  public TimeService(Date day) {
    today = day;
    monthId = Month.getMonthId(day);
  }

  public static boolean reset() {
    Date newDate = new Date(now.now());
    int newDay = Month.getDay(newDate);
    int newMonth = Month.getMonthId(newDate);
    if (newDay != day || monthId != newMonth) {
      today = newDate;
      day = newDay;
      monthId = newMonth;
      return true;
    }
    return false;
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
    now = new Now() {
      public long now() {
        return today.getTime();
      }
    };
  }

  public static Date getCurrentDate() {
    return new Date(now.now());
  }

  public static int getCurrentMonth() {
    return monthId;
  }

  public static int getCurrentDay() {
    return day;
  }
}
