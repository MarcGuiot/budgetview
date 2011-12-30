package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class WeekStat  {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @DefaultInteger(0)
  public static IntegerField NEW_USERS;

  @DefaultInteger(0)
  public static IntegerField RETAINED_USERS;

  public static DoubleField RETENTION_RATIO;

  @DefaultInteger(0)
  public static IntegerField PURCHASES;

  @DefaultInteger(0)
  public static IntegerField POTENTIAL_BUYERS; // Number of users of that week who could still buy today

  static {
    GlobTypeLoader.init(WeekStat.class);
  }

}
