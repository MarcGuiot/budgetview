package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class SeriesValues {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(BudgetAreaEntity.class)
  public static LinkField BUDGET_AREA;

  public static IntegerField MONTH;

  public static StringField NAME;

  public static DoubleField AMOUNT;
  public static DoubleField PLANNED_AMOUNT;
  public static DoubleField REMAINING_AMOUNT;
  public static DoubleField OVERRUN_AMOUNT;

  static {
    GlobTypeLoader.init(SeriesValues.class, "seriesValues");
  }
}
