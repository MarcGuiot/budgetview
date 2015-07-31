package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultInteger;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class UserProgressInfoEntry {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Required
  public static DateField DATE;

  public static IntegerField COUNT;
  public static BooleanField INITIAL_STEPS_COMPLETED;
  public static BooleanField IMPORT_STARTED;
  public static BooleanField CATEGORIZATION_SELECTION_DONE;
  public static BooleanField CATEGORIZATION_AREA_SELECTION_DONE;
  public static BooleanField FIRST_CATEGORIZATION_DONE;
  public static BooleanField CATEGORIZATION_SKIPPED;
  public static BooleanField GOTO_BUDGET_SHOWN;

  static {
    GlobTypeLoader.init(UserProgressInfoEntry.class);
  }
}
