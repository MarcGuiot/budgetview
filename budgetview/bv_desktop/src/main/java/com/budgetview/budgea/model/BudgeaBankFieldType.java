package com.budgetview.budgea.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.ItemNotFound;

import static org.globsframework.model.FieldValue.value;

public enum BudgeaBankFieldType implements GlobConstantContainer {
  LIST("list", 1),
  TEXT("text", 2),
  DATE("date", 3),
  PASSWORD("password", 4);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private String name;
  private int id;

  BudgeaBankFieldType(String name, int id) {
    this.name = name;
    this.id = id;
  }

  public static BudgeaBankFieldType get(String text) {
    for (BudgeaBankFieldType fieldType : values()) {
      if (fieldType.name.equalsIgnoreCase(text)) {
        return fieldType;
      }
    }
    throw new ItemNotFound("'" + text + "' not associated to any BudgeaBankFieldType enum value");
  }

  public static BudgeaBankFieldType get(int id) {
    for (BudgeaBankFieldType fieldType : values()) {
      if (Utils.equal(fieldType.id, id)) {
        return fieldType;
      }
    }
    throw new ItemNotFound("'" + id + "' does not correspond to any BudgeaBankFieldType enum value");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(TYPE,
                            value(ID, id),
                            value(NAME, name));
  }

  public int getId() {
    return id;
  }

  static {
    TypeLoader.init(BudgeaBankFieldType.class, "budgeaBankFieldType");
  }
}