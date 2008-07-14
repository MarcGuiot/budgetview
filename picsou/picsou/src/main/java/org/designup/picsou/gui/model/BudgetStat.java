package org.designup.picsou.gui.model;

import org.designup.picsou.model.BudgetArea;
import org.designup.picsou.model.Month;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class BudgetStat {
  public static GlobType TYPE;

  @Key
  @Target(Month.class)
  public static IntegerField MONTH;
  @Key
  @Target(BudgetArea.class)
  public static LinkField BUDGET_AREA;

  public static DoubleField AMOUNT;

  static {
    GlobTypeLoader.init(BudgetStat.class);
  }
}