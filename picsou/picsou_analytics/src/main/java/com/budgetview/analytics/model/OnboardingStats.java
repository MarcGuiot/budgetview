package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class OnboardingStats {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Required
  public static DateField LAST_DAY;

  @DefaultInteger(0)
  public static IntegerField FIRST_TRY_COUNT;

  @DefaultInteger(0)
  public static IntegerField IMPORT_STARTED_ON_FIRST_TRY;

  @DefaultInteger(0)
  public static IntegerField CATEGORIZATION_STARTED_ON_FIRST_TRY;

  @DefaultInteger(0)
  public static IntegerField CATEGORIZATION_FINISHED_ON_FIRST_TRY;

  @DefaultInteger(0)
  public static IntegerField ONBOARDING_COMPLETED_ON_FIRST_TRY;

  public static DoubleField FIRST_TRY_COMPLETION_RATIO;
  public static DoubleField BOUNCE_BEFORE_IMPORT_RATIO;
  public static DoubleField COMPLETE_IMPORT_RATIO;
  public static DoubleField COMPLETE_CATEGORIZATION_RATIO;


  static {
    GlobTypeLoader.init(OnboardingStats.class);
  }
}
