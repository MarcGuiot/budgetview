package org.designup.picsou.gui;

import org.designup.picsou.model.Month;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;

import java.util.Calendar;
import java.util.Date;

public class TimeService {
  private static Date today = new Date();
  private int count;

  public void createFuturMonth(GlobRepository repository, boolean isFull) {
    count = isFull ? 24 : 1;
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(today);
    calendar.set(Calendar.DAY_OF_MONTH, 1);
    for (int i = 0; i < count; i++) {
      Key month = Key.create(Month.TYPE, Month.toYyyyMm(calendar.get(Calendar.YEAR),
                                                        calendar.get(Calendar.MONTH) + 1));
      repository.findOrCreate(month);
      calendar.add(Calendar.MONTH, 1);
    }
  }

  public int getCurrentMonthId() {
    return Month.getMonthId(today);
  }

  public int getFuturMonthCount() {
    return count;
  }

  public static void setCurrentDate(Date date) {
    today = date;
  }
}
