package com.budgetview.model;

import com.budgetview.model.util.TypeLoader;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.InvalidData;

import static org.globsframework.model.FieldValue.value;

public enum ImportType implements GlobConstantContainer {
  OFX(0),
  QIF(1),
  JSON(2),
  CSV(3);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private final int id;

  ImportType(int id) {
    this.id = id;
  }

  static {
    TypeLoader.init(ImportType.class, "importType");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(ImportType.TYPE, value(ImportType.ID, id));
  }

  public int getId() {
    return id;
  }

  public static ImportType get(Integer id) {
    if (id == null) {
      return null;
    }
    switch (id) {
      case 0: return OFX;
      case 1: return QIF;
      case 2: return JSON;
      case 3: return CSV;
    }
    throw new InvalidData("Unexpected import type:" + id);
  }
}
