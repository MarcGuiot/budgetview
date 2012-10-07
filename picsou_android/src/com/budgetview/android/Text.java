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
      case 1 : return R.string.month_jan;
      case 2 : return R.string.month_feb;
      case 3 : return R.string.month_mar;
      case 4 : return R.string.month_apr;
      case 5 : return R.string.month_may;
      case 6 : return R.string.month_june;
      case 7 : return R.string.month_july;
      case 8 : return R.string.month_aug;
      case 9 : return R.string.month_sep;
      case 10 : return R.string.month_oct;
      case 11 : return R.string.month_nov;
      case 12 : return R.string.month_dec;
    }
    throw new IndexOutOfBoundsException("Unexpected month " + monthInYear);
  }
}
