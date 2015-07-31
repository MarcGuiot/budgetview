package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class WeekUsageStats {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Required
  public static DateField LAST_DAY;

  public static IntegerField FIRST_TRY_COUNT;
  public static DoubleField COMPLETION_RATE_ON_FIRST_TRY;
  public static DoubleField LOSS_BEFORE_FIRST_IMPORT;
  public static DoubleField LOSS_DURING_FIRST_IMPORT;
  public static DoubleField LOSS_DURING_FIRST_CATEGORIZATION;
  public static DoubleField LOSS_AFTER_FIRST_CATEGORIZATION;

  public static IntegerField SECOND_TRY_COUNT;
  public static DoubleField COMPLETION_RATE_ON_SECOND_TRY;
  public static DoubleField LOSS_BEFORE_SECOND_IMPORT;
  public static DoubleField LOSS_DURING_SECOND_IMPORT;
  public static DoubleField LOSS_DURING_SECOND_CATEGORIZATION;
  public static DoubleField LOSS_AFTER_SECOND_CATEGORIZATION;

  static {
    GlobTypeLoader.init(WeekUsageStats.class);
  }

}
