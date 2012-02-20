package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class UserEvaluationEntry {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Required
  public static DateField DATE;

  @Required
  public static IntegerField WEEK_ID;

  public static BooleanField SATISFIED;

  static {
    GlobTypeLoader.init(UserEvaluationEntry.class);
  }
}
