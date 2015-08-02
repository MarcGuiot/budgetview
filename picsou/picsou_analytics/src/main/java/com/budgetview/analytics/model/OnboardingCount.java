package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class OnboardingCount {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Required
  public static DateField LAST_DAY;

  @DefaultInteger(0)
  public static IntegerField FIRST_TRY_COUNT;

  static {
    GlobTypeLoader.init(OnboardingCount.class);
  }

}
