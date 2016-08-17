package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;

import static org.globsframework.model.FieldValue.value;

public enum Provider implements GlobConstantContainer {
  FILE_IMPORT("FILE_IMPORT", 0),
  MANUAL_INPUT("MANUAL_INPUT", 1),
  BUDGEA("BUDGEA", 2);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @NamingField
  public static StringField NAME;

  private String name;
  private int id;

  Provider(String name, int id) {
    this.name = name;
    this.id = id;
  }

  static {
    GlobTypeLoader.init(Provider.class, "provider");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(Provider.TYPE,
                            value(ID, id),
                            value(NAME, name));
  }

  public int getId() {
    return id;
  }
}
