package com.budgetview.budgea.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

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