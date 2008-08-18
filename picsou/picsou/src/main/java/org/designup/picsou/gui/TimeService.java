package org.designup.picsou.gui;

import org.designup.picsou.model.Month;

import java.util.Date;

public class TimeService {
  private static int today = Month.getMonthId(new Date());
  private static int lastTransactionMonthId;

  public TimeService() {
  }

  public TimeService(Date day) {
    TimeService.today = Month.getMonthId(day);
  }

  public int getCurrentMonthId() {
    return today;
  }

  public int getLastAvailableTransactionMonthId() {
    return lastTransactionMonthId;
  }

  public static void setLastAvailableTransactionMonthId(Integer monthId) {
    lastTransactionMonthId = monthId;
  }

  public static void setCurrentDate(Date date) {
    today = Month.getMonthId(date);
  }
}
