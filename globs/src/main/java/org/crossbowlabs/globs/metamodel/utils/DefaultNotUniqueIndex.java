package org.crossbowlabs.globs.metamodel.utils;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.index.NotUniqueIndex;
import org.crossbowlabs.globs.metamodel.index.IndexVisitor;

public class DefaultNotUniqueIndex implements NotUniqueIndex {
  private Field field;
  private String name;

  public DefaultNotUniqueIndex(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Field getField() {
    return field;
  }

  public void visitIndex(IndexVisitor visitor) {
    visitor.visiteNotUniqueIndex(this);
  }

  public void setField(Field field) {
    this.field = field;
  }
}
