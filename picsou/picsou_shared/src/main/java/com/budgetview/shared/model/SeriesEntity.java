package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class SeriesEntity {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(BudgetAreaEntity.class)
  public static LinkField BUDGET_AREA;

  public static StringField NAME;

  static {
    GlobTypeLoader.init(SeriesEntity.class, "seriesEntity");
  }
}
