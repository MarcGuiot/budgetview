package com.budgetview.budgea.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class BudgeaBank {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @NoObfuscation
  public static StringField CODE;

  @NamingField
  @NoObfuscation
  public static StringField NAME;

  static {
    GlobTypeLoader.init(BudgeaBank.class, "budgeaBank");
  }
}