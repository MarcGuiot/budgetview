package com.budgetview.analytics.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DateField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class Experiment {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static IntegerField WEEK;

  public static StringField ACTION;

  static {
    GlobTypeLoader.init(Experiment.class);
  }
}
