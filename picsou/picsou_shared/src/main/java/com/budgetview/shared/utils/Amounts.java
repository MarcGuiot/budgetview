package com.budgetview.shared.utils;

import org.globsframework.utils.Strings;
import org.globsframework.utils.Utils;

public class Amounts {

  public static boolean sameSign(double value1, double value2) {
    return Math.signum(value1) == Math.signum(value2) || Amounts.isNearZero(value1) || Amounts.isNearZero(value2);
  }

  public static boolean isNullOrZero(Double value) {
    return value == null || isNearZero(value);
  }

  public static boolean isNearZero(double value) {
    return value > -1E-6 && value < 1E-6;
  }

  public static double zeroIfNull(Double value) {
    return Utils.zeroIfNull(value);
  }

  public static boolean isNotZero(Double value) {
    return !isNullOrZero(value);
  }

  public static boolean isUnset(Double value) {
    return value == null;
  }

  public static double normalize(double value) {
    return isNearZero(value) ? 0 : value;
  }

  static public double extractAmount(String amount) {
    amount = amount.trim();
    amount = amount.replaceAll("[^0-9-+,.]", "");

    double coef = getCoef(amount);

    String tmp = amount.replaceAll("[^0-9-]", "");
    if (Strings.isNullOrEmpty(tmp)) {
      return 0.0;
    }
    return Double.parseDouble(tmp) / coef;
  }

  private static double getCoef(String amount) {
    int index = -1;
    double coef = 1.;
    for (int i = amount.length() - 1; i >= 0; i--) {
      if (amount.charAt(i) == '.' || amount.charAt(i) == ',') {
        index = amount.length() - i - 1;
        break;
      }
    }
    if (index != -1) {
      coef = Math.pow(10, index);
    }
    return coef;
  }

  public static boolean equal(Double val1, Double val2) {
    return Math.abs(val1 - val2) < 0.0001;
  }

  public static double diff(Double val1, Double val2) {
    if ((val1 == null) && (val2 == null)) {
      return 0.0;
    }
    if (val1 == null) {
      return -val2;
    }
    if (val2 == null) {
      return val1;
    }
    return val1 - val2;
  }

  public static double max(Double val1, Double planned, boolean isIncome) {
    if (val1 == null) {
      return planned;
    }
    if (planned == null) {
      return val1;
    }
    if (isIncome) {
      return Math.max(val1, planned);
    }
    else {
      if (planned > 0) {
        return Math.max(val1, planned);
      }
      if (planned < 0) {
        return Math.min(val1, planned);
      }
    }
    return val1;
  }

  public static double upperOrder(double value) {
    int power = (int)Math.log10(value);
    return Math.pow(10, power + 1);
  }

  public static boolean isSameSign(double first, double second) {
    return Math.signum(first) * Math.signum(second) > 0;
  }
}
