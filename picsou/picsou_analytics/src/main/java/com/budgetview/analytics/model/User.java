package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class User {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField EMAIL;
  
  public static DateField FIRST_DATE;
  public static DateField LAST_DATE;
  public static BooleanField PREVIOUS_USER;
  public static IntegerField PING_COUNT;
  public static DateField PURCHASE_DATE;
  public static IntegerField DAYS_BEFORE_PURCHASE;
  public static IntegerField PING_COUNT_ON_PURCHASE;

  static {
    GlobTypeLoader.init(User.class);
  }

}
