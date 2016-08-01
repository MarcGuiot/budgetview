package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class SeriesEntity {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @Target(BudgetAreaEntity.class)
  @NoObfuscation
  public static LinkField BUDGET_AREA;

  @NoObfuscation
  public static StringField NAME;

  static {
    GlobTypeLoader.init(SeriesEntity.class, "seriesEntity");
  }
}
