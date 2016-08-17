package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

/** @deprecated */
public class MonthEntity {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  static {
    GlobTypeLoader.init(MonthEntity.class, "monthEntity");
  }
}
