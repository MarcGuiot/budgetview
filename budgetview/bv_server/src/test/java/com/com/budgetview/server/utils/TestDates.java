package com.com.budgetview.server.utils;

import com.budgetview.server.utils.DateConverter;
import org.globsframework.utils.Dates;

import java.util.Date;

public class TestDates {
  public static int today() {
    return DateConverter.toYyyyMmDd(new Date());
  }

  public static int monthsLater(int count) {
    return DateConverter.toYyyyMmDd(Dates.monthsLater(count));
  }
}
