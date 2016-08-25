package com.budgetview.shared.mobile.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.KeyBuilder;

/** @deprecated */
public class BudgetViewVersion {
  public static GlobType TYPE;

  public static org.globsframework.model.Key key;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @NoObfuscation
  public static IntegerField MAJOR_VERSION;

  @NoObfuscation
  public static IntegerField MINOR_VERSION;

  static {
    GlobTypeLoader.init(BudgetViewVersion.class, "BVVersion");
    key = KeyBuilder.newKey(TYPE, 0);
  }


}
