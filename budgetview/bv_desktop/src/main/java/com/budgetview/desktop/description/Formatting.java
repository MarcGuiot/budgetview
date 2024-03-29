package com.budgetview.desktop.description;

import com.budgetview.model.Month;
import com.budgetview.model.NumericDateType;
import com.budgetview.model.TextDateType;
import com.budgetview.shared.model.BudgetArea;
import com.budgetview.shared.utils.AmountFormat;
import com.budgetview.utils.Lang;
import org.globsframework.utils.Strings;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

public class Formatting extends AmountFormat {

  private static SimpleDateFormat dateFormat;
  private static SimpleDateFormat yearMonthFormat;

  private static MessageFormat dateMessageFormat;
  private static SimpleDateFormat dateAndTimeFormat;
  private static MessageFormat fullLabelFormat;

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

  public static String toString(Date date) {
    if (date == null) {
      return "";
    }
    return dateFormat.format(date);
  }

  public static String toString(int year, int month) {
    GregorianCalendar calendar =
      new GregorianCalendar(year, month - 1, 1);
    return Strings.capitalize(yearMonthFormat.format(calendar.getTime()));
  }

  public static String toString(Double value, BudgetArea area) {
    boolean invert = BudgetArea.shouldInvertAmounts(area);
    return toString(value, invert);
  }

  public static String toString(int year, int month, int day) {
    return dateMessageFormat.format(
      new Object[]{
        Integer.toString(year),
        (month < 10 ? "0" : "") + month,
        (day < 10 ? "0" : "") + day
      });
  }

  public static String toString(int fullDate) {
    int monthId = Month.getMonthIdFromFullDate(fullDate);
    return toString(Month.toYear(monthId), Month.toMonth(monthId), Month.getDayFromFullDate(fullDate));
  }

  public static String getFullLabel(int month, int day) {
    return fullLabelFormat.format(
      new Object[]{Integer.toString(Month.toYear(month)), Month.getFullMonthLabel(month, true), day});
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
