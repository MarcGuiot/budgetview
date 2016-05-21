package com.budgetview.gui.utils;

public class TimeUtils {

  public static String get(double amount, double rate, int workingHoursPerDay) {
    if ((amount == 0) || (rate == 0) || (workingHoursPerDay == 0)) {
      return "";
    }
    double totalHours = Math.abs(amount / rate);
    long days = (long) Math.floor(totalHours / workingHoursPerDay);
    long hours = (long) Math.floor(totalHours % workingHoursPerDay);

    StringBuilder result = new StringBuilder();

    if (days > 0) {
      result.append(days).append("j");
    }
    if (hours > 0) {
      if (days > 0) {
        result.append(' ');
      }
      result.append(hours).append("h");
    }
    if (days == 0) {
      long minutes = getMinutes(totalHours);
      if (minutes > 0) {
        if (hours == 0) {
          result.append(minutes).append("min");
        }
        else {
          if (minutes < 10) {
            result.append('0');
          }
          result.append(minutes);
        }
      }
    }

    return result.toString();
  }

  private static long getMinutes(double totalHours) {
    double minutes = 60 * (totalHours - Math.floor(totalHours));
    double result = Math.round(minutes / 5) * 5;
    return (long) result;
  }
}
