package org.designup.picsou.gui.description;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.utils.Strings;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Formatting {
  public static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("0");
  public static final DecimalFormat TWO_DIGIT_INTEGER_FORMAT = new DecimalFormat("00");
  public static final DecimalFormat DECIMAL_FORMAT =
    new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
  public static SimpleDateFormat YEAR_MONTH_FORMAT = new SimpleDateFormat("MMMMMMMMMM yyyy", Locale.FRANCE);

  public static String toString(Double value) {
    if (value == null) {
      return "";
    }
    if (Amounts.isNearZero(value)) {
      value = +0.00;
    }
    return DECIMAL_FORMAT.format(value);
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

  public static String toMinimumValueString(Double value, boolean future) {
    if (value == null) {
      return "";
    }

    if (future) {
      value = Math.floor((value + 2.5) / 5) * 5;
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

  public static String toString(Date date) {
    return DATE_FORMAT.format(date);
  }

  public static String toString(int year, int month) {
    GregorianCalendar calendar =
      new GregorianCalendar(year, month - 1, 1);
    return Strings.capitalize(YEAR_MONTH_FORMAT.format(calendar.getTime()));
  }

  public static String toString(Double value, BudgetArea area) {
    if (area.isIncome() || area == BudgetArea.UNCATEGORIZED) {
      return toString(value);
    }
    else if (Amounts.isNearZero(value)) {
      return "0.00";
    }
    else if (value < 0) {
      return toString(-value);
    }
    else {
      return "+" + toString(value);
    }
  }
}
