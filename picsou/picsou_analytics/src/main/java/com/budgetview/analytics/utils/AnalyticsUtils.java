package com.budgetview.analytics.utils;

import org.globsframework.utils.exceptions.InvalidParameter;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Date;

public class AnalyticsUtils {
  public static int daysBetween(Date from, Date to) {
    if (from == null) {
      throw new InvalidParameter("From date should no be null");
    }
    if (to == null) {
      throw new InvalidParameter("To date should no be null");
    }
    return Days.daysBetween(new DateTime(from), new DateTime(to)).getDays();
  }
}
