package com.budgetview.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class BankFormat {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  static {
    GlobTypeLoader.init(BankFormat.class, "bankFormat");
  }
}
