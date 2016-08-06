package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class CloudSeries {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @Target(CloudBudgetArea.class)
  @NoObfuscation
  public static LinkField BUDGET_AREA;

  @NoObfuscation
  public static StringField NAME;

  static {
    GlobTypeLoader.init(CloudSeries.class, "seriesEntity");
  }
}
