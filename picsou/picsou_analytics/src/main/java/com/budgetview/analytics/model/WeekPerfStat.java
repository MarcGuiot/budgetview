package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class WeekPerfStat {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Required
  public static DateField LAST_DAY;

  @DefaultInteger(0)
  public static IntegerField NEW_USERS;

  @DefaultInteger(0)
  public static IntegerField RETAINED_USERS;

  public static DoubleField RETENTION_RATIO;

  public static DoubleField EVALUATIONS_RATIO;

  public static DoubleField EVALUATIONS_RESULT;

  @DefaultInteger(0)
  public static IntegerField POTENTIAL_BUYERS;

  @DefaultInteger(0)
  public static IntegerField PURCHASES;

  public static DoubleField REVENUE_RATIO;

  static {
    GlobTypeLoader.init(WeekPerfStat.class);
  }

}
