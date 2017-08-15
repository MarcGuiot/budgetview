package com.budgetview.budgea.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.LinkField;

public class BudgeaConnection {

  public static GlobType TYPE;

  @Key
  @Target(BudgeaBank.class)
  @NoObfuscation
  public static LinkField BANK;

  static {
    TypeLoader.init(BudgeaConnection.class, "budgeaConnection");
  }
}