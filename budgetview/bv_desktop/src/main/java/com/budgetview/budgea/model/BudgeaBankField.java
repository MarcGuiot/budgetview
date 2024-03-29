package com.budgetview.budgea.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.*;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.Glob;

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
  @Target(BudgeaBankFieldType.class) @Name("type")
  public static LinkField FIELD_TYPE;

  public static IntegerField SEQUENCE_INDEX;

  static {
    TypeLoader.init(BudgeaBankField.class, "budgeaBankField");
  }

  public static BudgeaBankFieldType getFieldType(Glob field) {
    return BudgeaBankFieldType.get(field.get(FIELD_TYPE));
  }
}