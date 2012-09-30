package org.designup.picsou.gui.description;

import com.budgetview.shared.utils.AmountFormat;
import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.NumericDateType;
import org.designup.picsou.model.TextDateType;
import com.budgetview.shared.utils.Amounts;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Strings;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class Formatting extends AmountFormat {
  public static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("0");
  public static final DecimalFormat TWO_DIGIT_INTEGER_FORMAT = new DecimalFormat("00");

  private static SimpleDateFormat dateFormat;
  private static SimpleDateFormat yearMonthFormat;

  private static MessageFormat dateMessageFormat;
  private static SimpleDateFormat dateAndTimeFormat;
  private static MessageFormat fullLabelFormat;

  public static String toAbsString(Double value) {
    return toString(Math.abs(value));
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

  public static SimpleDateFormat getDateFormat() {
    if (dateFormat == null) {
      updateWithDefaults();
    }
    return dateFormat;
  }

  public static SimpleDateFormat getYearMonthFormat() {
    if (yearMonthFormat == null) {
      updateWithDefaults();
    }
    return yearMonthFormat;
  }

  public static SimpleDateFormat getDateAndTimeFormat() {
    if (dateAndTimeFormat == null) {
      updateWithDefaults();
    }
    return dateAndTimeFormat;
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

  public static String toString(Date date) {
    return dateFormat.format(date);
  }

  public static String toString(int year, int month) {
    GregorianCalendar calendar =
      new GregorianCalendar(year, month - 1, 1);
    return Strings.capitalize(yearMonthFormat.format(calendar.getTime()));
  }

  public static String toString(Double value, BudgetArea area) {
    if (area.isIncome() || area == BudgetArea.UNCATEGORIZED) {
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

  public static String toString(int year, int month, int day) {
    return dateMessageFormat.format(
      new Object[]{
        Integer.toString(year),
        (month < 10 ? "0" : "") + month,
        (day < 10 ? "0" : "") + day
      });
  }

  public static String getFullLabel(int month, int day) {
    return fullLabelFormat.format(
      new Object[]{Integer.toString(Month.toYear(month)), Month.getFullMonthLabel(month), day});
  }

  private static void updateWithDefaults() {
    update(TextDateType.getDefault(), NumericDateType.getDefault());
  }

  public static void update(TextDateType textDate, NumericDateType numericDate) {
    dateFormat = new SimpleDateFormat(numericDate.getFormat());
    dateAndTimeFormat = new SimpleDateFormat(numericDate.getFormat() + " hh:mm:ss");
    dateMessageFormat = Lang.createMessageFormatFromText(numericDate.getMessageFormat());
    yearMonthFormat = new SimpleDateFormat("MMMMMMMMMM yyyy", Lang.getLocale());
    fullLabelFormat = Lang.createMessageFormatFromText(textDate.getFormat());
  }
}
