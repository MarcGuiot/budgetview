package com.budgetview.budgea.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;

public class BudgeaBankField {

  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @Target(BudgeaBank.class)
  @NoObfuscation
  public static LinkField BANK;

  @NoObfuscation
  public static StringField REGEX;

  @NoObfuscation
  public static StringField NAME;

  @NamingField
  @NoObfuscation
  public static StringField LABEL;

  @NoObfuscation
  @Name("type")
  public static StringField FIELD_TYPE;

  static {
    GlobTypeLoader.init(BudgeaBankField.class, "budgeaBankField");
  }
}