package com.budgetview.desktop.model;

import com.budgetview.model.util.TypeLoader;
import com.budgetview.shared.model.BudgetArea;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;

public class PeriodBudgetAreaStat {
  public static GlobType TYPE;

  @Key
  @Target(BudgetArea.class)
  public static LinkField BUDGET_AREA;

  @DefaultDouble(0.0)
  public static DoubleField AMOUNT;

  public static DoubleField PLANNED_AMOUNT;

  @DefaultDouble(0.0)
  public static DoubleField PAST_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField FUTURE_REMAINING;

  @DefaultDouble(0.0)
  public static DoubleField PAST_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField FUTURE_OVERRUN;

  @DefaultDouble(0.0)
  public static DoubleField ABS_SUM_AMOUNT;

  @DefaultBoolean(true)
  public static BooleanField ACTIVE;

  static {
    TypeLoader.init(PeriodBudgetAreaStat.class, "periodBudgetAreaStat");
  }
}
