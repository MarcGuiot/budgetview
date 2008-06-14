package org.globsframework.sqlstreams.constraints.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.sqlstreams.constraints.Operand;
import org.globsframework.sqlstreams.constraints.OperandVisitor;
import org.globsframework.streams.accessors.Accessor;

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
