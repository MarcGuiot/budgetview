package com.budgetview.shared.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NamingField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.ItemNotFound;

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

  private final int id;
  private final String name;

  Provider(String name, int id) {
    this.name = name;
    this.id = id;
  }

  static {
    GlobTypeLoader.init(Provider.class, "provider");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(TYPE,
                            value(ID, id),
                            value(NAME, name));
  }

  public int getId() {
    return id;
  }

  public static Provider get(int id) {
    for (Provider provider : values()) {
      if (provider.getId() == id) {
        return provider;
      }
    }
    throw new ItemNotFound("No provider found for id " + id);
  }
}
