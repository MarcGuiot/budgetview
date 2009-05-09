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

  static public double extractAmount(final String amount) {
    String tmp = amount.replaceAll(",", "").replaceAll("\\.", "");
    if (Strings.isNullOrEmpty(tmp)) {
      return 0.0;
    }
    return Double.parseDouble(tmp) / 100.0;
  }

  public static boolean equal(Double val1, Double val2) {
    return Math.abs(val1 - val2) < 0.0001;
  }
}
