package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class User {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static IntegerField COHORT_WEEK;

  public static StringField EMAIL;

  public static DateField FIRST_DATE;
  public static DateField LAST_DATE;

  public static IntegerField JAR_VERSION;

  public static BooleanField PREVIOUS_USER;

  public static IntegerField PING_COUNT;

  public static DoubleField WEEKLY_USAGE;

  public static BooleanField ACTIVATED;
  public static BooleanField RETAINED;
  public static BooleanField PURCHASED;
  public static BooleanField LOST;

  public static DateField PURCHASE_DATE;
  public static IntegerField DAYS_BEFORE_PURCHASE;
  public static IntegerField PING_COUNT_ON_PURCHASE;

  static {
    GlobTypeLoader.init(User.class);
  }
}
