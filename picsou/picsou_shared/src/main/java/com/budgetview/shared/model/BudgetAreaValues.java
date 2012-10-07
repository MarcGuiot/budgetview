package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class BudgetAreaValues {
  public static GlobType TYPE;

  @Key @Target(BudgetAreaEntity.class)
  public static LinkField BUDGET_AREA;

  @Key
  public static IntegerField MONTH;

  public static DoubleField INITIALLY_PLANNED;
  public static DoubleField ACTUAL;
  public static DoubleField REMAINDER;
  public static DoubleField OVERRUN;

  static {
    GlobTypeLoader.init(BudgetAreaValues.class, "budgetAreaValues");
  }
}
