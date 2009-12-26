package org.designup.picsou.model.util;

import org.globsframework.utils.Strings;

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

  public static boolean isNotZero(Double value) {
    return !isNullOrZero(value);
  }

  public static double normalize(double value) {
    return isNearZero(value) ? 0 : value;
  }

  static public double extractAmount(String amount) {
    double coef = 100.0;
    amount = amount.trim();
    int len = amount.length();
    int commaSep = amount.lastIndexOf(",");
    if (commaSep == len - 2) {
      coef = 10.;
    }
    else if (commaSep == len - 3) {
      coef = 100.;
    }
    else {
      int dotSep = amount.lastIndexOf(".");
      if (dotSep == len - 2) {
        coef = 10.;
      }
      else if (dotSep == len - 3) {
        coef = 100.;
      }
      else {
        coef = 1.;
      }
    }

    String tmp = amount.replaceAll(",", "").replaceAll("\\.", "").replaceAll(" ", "");
    if (Strings.isNullOrEmpty(tmp)) {
      return 0.0;
    }
    return Double.parseDouble(tmp) / coef;
  }

  public static boolean equal(Double val1, Double val2) {
    return Math.abs(val1 - val2) < 0.0001;
  }

  public static double max(Double val1, Double planned, boolean isIncome) {
    if (val1 == null){
      return planned;
    }
    if (planned == null){
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
}
