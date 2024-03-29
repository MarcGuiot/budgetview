package com.budgetview.budgea.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;

public class BudgeaConnectionValue {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Target(BudgeaConnection.class)
  public static LinkField CONNECTION;

  @Target(BudgeaBankField.class)
  public static LinkField FIELD;

  public static StringField VALUE;

  public static IntegerField SEQUENCE_INDEX;

  static {
    TypeLoader.init(BudgeaConnectionValue.class, "budgeaConnectionValue");
  }
}