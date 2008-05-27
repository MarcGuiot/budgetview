package org.crossbowlabs.globs.metamodel.utils;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.index.MultiFieldIndex;

public abstract class AbstractMultiFieldIndex implements MultiFieldIndex {
  private String name;
  private Field[] fields;

  public AbstractMultiFieldIndex(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Field[] getFields() {
    return fields;
  }

  public void setField(Field[] fields) {
    this.fields = fields;
  }
}
