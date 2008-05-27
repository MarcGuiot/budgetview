package org.crossbowlabs.globs.metamodel.utils;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.index.UniqueIndex;
import org.crossbowlabs.globs.metamodel.index.IndexVisitor;

public class DefaultUniqueIndex implements UniqueIndex {
  private Field field;
  private String name;

  public DefaultUniqueIndex(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Field getField() {
    return field;
  }

  public void visitIndex(IndexVisitor visitor) {
    visitor.visiteUniqueIndex(this);
  }

  public void setField(Field field) {
    this.field = field;
  }
}
