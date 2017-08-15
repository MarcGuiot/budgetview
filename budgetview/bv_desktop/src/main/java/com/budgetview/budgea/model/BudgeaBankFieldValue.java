package com.budgetview.budgea.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.fields.StringField;

public class BudgeaBankFieldValue {

  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @Target(BudgeaBankField.class)
  @NoObfuscation
  public static LinkField FIELD;

  @NamingField
  @NoObfuscation
  public static StringField LABEL;

  @NoObfuscation
  public static StringField VALUE;

  static {
    TypeLoader.init(BudgeaBankFieldValue.class, "budgeaBankFieldValue");
  }
}