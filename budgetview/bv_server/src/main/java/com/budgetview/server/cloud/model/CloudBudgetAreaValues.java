package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class CloudBudgetAreaValues {
  public static GlobType TYPE;

  @Key @Target(CloudBudgetArea.class)
  @NoObfuscation
  public static LinkField BUDGET_AREA;

  @Key
  @NoObfuscation
  public static IntegerField MONTH;

  @NoObfuscation
  public static DoubleField INITIALLY_PLANNED;
  @NoObfuscation
  public static DoubleField ACTUAL;
  @NoObfuscation
  public static DoubleField REMAINDER;
  @NoObfuscation
  public static DoubleField OVERRUN;

  static {
    GlobTypeLoader.init(CloudBudgetAreaValues.class, "budgetAreaValues");
  }
}
