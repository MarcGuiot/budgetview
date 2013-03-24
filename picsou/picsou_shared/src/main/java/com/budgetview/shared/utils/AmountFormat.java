package com.budgetview.shared.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AmountFormat {

  public static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("0");
  public static final DecimalFormat TWO_DIGIT_INTEGER_FORMAT = new DecimalFormat("00");

  public static final DecimalFormat DECIMAL_FORMAT =
    new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));

  public static String toAbsString(Double value) {
    return toString(Math.abs(value));
  }

  public static String toString(Double value, boolean invert) {
    if (!invert) {
      return toString(value);
    }
    else if (Amounts.isNullOrZero(value)) {
      return "0.00";
    }
    else if (value < 0) {
      return toString(-value);
    }
    else {
      return "+" + toString(value);
    }
  }

  public static String toString(Double value) {
    if (value == null) {
      return "";
    }
    if (Amounts.isNearZero(value)) {
      value = +0.00;
    }
    return DECIMAL_FORMAT.format(value);
  }

  public static String toStandardValueString(Double value) {
    if (value == null) {
      return "";
    }
    if (Amounts.isNearZero(value)) {
      return "0";
    }
    if (value < 0) {
      return INTEGER_FORMAT.format(value);
    }
    else {
      return "+" + INTEGER_FORMAT.format(value);
    }
  }

  public static String toStringWithPlus(Double value) {
    if (value == null) {
      return "";
    }
    if (value <= 0) {
      return DECIMAL_FORMAT.format(value);
    }
    else {
      return "+" + DECIMAL_FORMAT.format(value);
    }
  }
}
