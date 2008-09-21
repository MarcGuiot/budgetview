package org.designup.picsou.gui;

import org.designup.picsou.model.Month;

import java.util.Date;

public class TimeService {
  private static int today = Month.getMonthId(new Date());

  public TimeService() {
  }

  public TimeService(Date day) {
    TimeService.today = Month.getMonthId(day);
  }

  public int getCurrentMonthId() {
    return today;
  }

  public static void setCurrentDate(Date date) {
    today = Month.getMonthId(date);
  }

  public static int getCurrentMonth() {
    return today;
  }
}
