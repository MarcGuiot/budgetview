package com.budgetview.analytics.utils;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Date;

import static org.globsframework.model.FieldValue.value;

public class AnalyticsUtils {
  public static int daysBetween(Date from, Date to) {
    if (from == null) {
      throw new InvalidParameter("From date should no be null");
    }
    if (to == null) {
      throw new InvalidParameter("To date should no be null");
    }
    return Days.daysBetween(new DateTime(from), new DateTime(to)).getDays();
  }

  public static Glob getWeekStat(Date date, GlobType type, IntegerField idField, DateField lastDayField, GlobRepository repository) {
    return getWeekStat(new DateTime(date.getTime()), type, idField, lastDayField, repository);
  }

  public static Glob getWeekStat(DateTime date, GlobType type, IntegerField idField, DateField lastDayField, GlobRepository repository) {
    int weekId = date.getWeekyear() * 100 + date.getWeekOfWeekyear();
    Glob week = repository.find(Key.create(type, weekId));
    if (week == null) {
      int dayOfWeek = date.getDayOfWeek();
      DateTime lastDayOfWeek = date.plusDays(7 - dayOfWeek);
      week = repository.create(type,
                               value(idField, weekId),
                               value(lastDayField, lastDayOfWeek.toDate()));
    }
    return week;
  }

  public static double round2(double value) {
    double result = value * 1000;
    result = Math.round(result);
    return result / 10;
  }
}
