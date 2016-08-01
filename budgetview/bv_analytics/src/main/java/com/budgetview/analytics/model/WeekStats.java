package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class WeekStats {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @DefaultInteger(0)
  public static IntegerField NEW_USERS;

  @DefaultInteger(0)
  public static IntegerField ACTIVATION_COUNT;

  public static DoubleField ACTIVATION_RATIO;

  @DefaultInteger(0)
  public static IntegerField RETENTION_COUNT;

  public static DoubleField RETENTION_RATIO;

  @DefaultInteger(0)
  public static IntegerField REVENUE_COUNT;

  public static DoubleField REVENUE_RATIO;

  @DefaultInteger(0)
  public static IntegerField NEW_PURCHASES;
  public static IntegerField TOTAL_PAID_ACTIVE_USERS;
  public static IntegerField TOTAL_ACTIVE_USERS;

  static {
    GlobTypeLoader.init(WeekStats.class);
  }
}
