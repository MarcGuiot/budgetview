package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.IntegerField;

public class BankFormat {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  static {
    TypeLoader.init(BankFormat.class, "bankFormat");
  }
}
