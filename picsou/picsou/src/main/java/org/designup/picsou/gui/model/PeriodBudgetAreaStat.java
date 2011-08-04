package org.designup.picsou.gui.model;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.DefaultDouble;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class PeriodBudgetAreaStat {
  public static GlobType TYPE;

  @Key
  @Target(BudgetArea.class)
  public static LinkField BUDGET_AREA;

  @DefaultDouble(0.0)
  public static DoubleField ABS_SUM_AMOUNT;

  static {
    GlobTypeLoader.init(PeriodBudgetAreaStat.class);
  }
}
