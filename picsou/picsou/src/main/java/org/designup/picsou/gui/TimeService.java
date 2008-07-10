package org.designup.picsou.gui;

import org.designup.picsou.model.Month;

import java.util.Date;

public class TimeService {
  private static Date today = new Date();

  public TimeService() {
  }

  public TimeService(Date day) {
    TimeService.today = day;
  }


  public int getCurrentMonthId() {
    return Month.getMonthId(today);
  }

  public static void setCurrentDate(Date date) {
    today = date;
  }
}
