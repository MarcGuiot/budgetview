package org.crossbowlabs.globs.sqlstreams.constraints.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.sqlstreams.constraints.Operand;
import org.crossbowlabs.globs.sqlstreams.constraints.OperandVisitor;
import org.crossbowlabs.globs.streams.accessors.Accessor;

public class AccessorOperand implements Operand {
  private Field field;
  private Accessor accessor;

  public AccessorOperand(Field field, Accessor accessor) {
    this.field = field;
    this.accessor = accessor;
  }

  public void visitOperand(OperandVisitor visitor) {
    visitor.visitAccessorOperand(this);
  }

  public Accessor getAccessor() {
    return accessor;
  }

  public Field getField() {
    return field;
  }
}
