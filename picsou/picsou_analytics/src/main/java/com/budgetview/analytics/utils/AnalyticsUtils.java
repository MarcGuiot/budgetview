package com.budgetview.analytics.utils;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.joda.time.DateTime;

import java.util.Date;

import static org.globsframework.model.FieldValue.value;

public class AnalyticsUtils {

  public static Glob getWeekStat(Date date, GlobType type, IntegerField idField, DateField lastDayField, GlobRepository repository) {
    return getWeekStat(new DateTime(date.getTime()), type, idField, lastDayField, repository);
  }

  public static Glob getWeekStat(DateTime date, GlobType type, IntegerField idField, DateField lastDayField, GlobRepository repository) {
    int weekId = Weeks.getWeekId(date);
    Glob week = repository.find(Key.create(type, weekId));
    if (week == null) {
      Date lastDay = Days.getLastDayOfWeek(date);
      week = repository.create(type,
                               value(idField, weekId),
                               value(lastDayField, lastDay));
    }
    return week;
  }

  public static double round2(double value) {
    double result = value * 1000;
    result = Math.round(result);
    return result / 10;
  }

  public static double ratio(int nominator, int denominator) {
    return round2((double)nominator / (double)denominator);
  }
}
