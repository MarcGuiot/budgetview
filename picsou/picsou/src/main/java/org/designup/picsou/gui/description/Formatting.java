package org.designup.picsou.gui.description;

import org.globsframework.utils.Strings;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.util.Amounts;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Date;
import java.util.GregorianCalendar;

public class Formatting {
  public static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("0");
  public static final DecimalFormat DECIMAL_FORMAT =
    new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");
  public static SimpleDateFormat YEAR_MONTH_FORMAT = new SimpleDateFormat("MMMMMMMMMM yyyy", Locale.FRANCE);

  public static String toString(Double value) {
    if (value == null) {
      return "";
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

  public static String toString(Date date) {
    return DATE_FORMAT.format(date);
  }

  public static String toString(int year, int month) {
    GregorianCalendar calendar =
      new GregorianCalendar(year, month - 1, 1);
    return Strings.capitalize(YEAR_MONTH_FORMAT.format(calendar.getTime()));
  }

  public static String toString(Double value, BudgetArea area) {
    if (area.isIncome()) {
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
