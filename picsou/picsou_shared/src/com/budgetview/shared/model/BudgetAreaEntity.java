package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class BudgetAreaEntity {
  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  public static StringField LABEL;

  static {
    GlobTypeLoader.init(BudgetAreaEntity.class, "budgetAreaEntity");
  }
}
