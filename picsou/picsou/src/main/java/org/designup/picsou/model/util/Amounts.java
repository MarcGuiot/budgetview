package org.designup.picsou.model.util;

public class Amounts {

  public static boolean isNullOrZero(Double value){
    return value == null || isNearZero(value);
  }

  public static boolean isNearZero(double value){
    return value > -1E-6 && value < 1E-6;
  }

  public static boolean isNotZero(Double value) {
    return !isNullOrZero(value);
  }
}
