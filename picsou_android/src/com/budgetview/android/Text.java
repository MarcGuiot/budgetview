package com.budgetview.android;

import android.content.res.Resources;

public class Text {

  public static String monthToString(int month, Resources resources) {
    return resources.getText(getResourceIdForMonth(month % 100))
           + " "
           + (month / 100);
  }

  public static int getResourceIdForMonth(int monthInYear) {
    switch (monthInYear) {
      case 1:
        return R.string.month_jan;
      case 2:
        return R.string.month_feb;
      case 3:
        return R.string.month_mar;
      case 4:
        return R.string.month_apr;
      case 5:
        return R.string.month_may;
      case 6:
        return R.string.month_june;
      case 7:
        return R.string.month_july;
      case 8:
        return R.string.month_aug;
      case 9:
        return R.string.month_sep;
      case 10:
        return R.string.month_oct;
      case 11:
        return R.string.month_nov;
      case 12:
        return R.string.month_dec;
    }
    throw new IndexOutOfBoundsException("Unexpected month " + monthInYear);
  }

  public static int getResourceIdForShortMonth(int monthInYear) {
    switch (monthInYear) {
      case 1:
        return R.string.short_month_jan;
      case 2:
        return R.string.short_month_feb;
      case 3:
        return R.string.short_month_mar;
      case 4:
        return R.string.short_month_apr;
      case 5:
        return R.string.short_month_may;
      case 6:
        return R.string.short_month_june;
      case 7:
        return R.string.short_month_july;
      case 8:
        return R.string.short_month_aug;
      case 9:
        return R.string.short_month_sep;
      case 10:
        return R.string.short_month_oct;
      case 11:
        return R.string.short_month_nov;
      case 12:
        return R.string.short_month_dec;
    }
    throw new IndexOutOfBoundsException("Unexpected month " + monthInYear);
  }

  public static String toOnDayMonthString(Integer day, Integer monthId, Resources resources) {
    return resources.getText(R.string.on_date) + " " + day + "/" + (monthId % 100);
  }

  public static String toShortMonthString(int monthId, Resources resources) {
    return resources.getText(getResourceIdForShortMonth(monthId % 100)) + "";
  }
}
