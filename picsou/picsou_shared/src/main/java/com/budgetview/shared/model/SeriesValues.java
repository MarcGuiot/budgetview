package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class SeriesValues {
  public static GlobType TYPE;

  @Key @Target(SeriesEntity.class)
  @NoObfuscation
  public static LinkField SERIES_ENTITY;

  @Key
  @NoObfuscation
  public static IntegerField MONTH;

  @Target(BudgetAreaEntity.class)
  @NoObfuscation
  public static LinkField BUDGET_AREA;

  @NoObfuscation
  public static DoubleField AMOUNT;
  @NoObfuscation
  public static DoubleField PLANNED_AMOUNT;
  @NoObfuscation
  public static DoubleField REMAINING_AMOUNT;
  @NoObfuscation
  public static DoubleField OVERRUN_AMOUNT;

  static {
    GlobTypeLoader.init(SeriesValues.class, "seriesValues");
  }
}
