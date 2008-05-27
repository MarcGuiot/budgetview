package org.crossbowlabs.globs.sqlstreams.constraints.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.sqlstreams.constraints.Operand;
import org.crossbowlabs.globs.sqlstreams.constraints.OperandVisitor;

public class ValueOperand implements Operand {
  private Field field;
  private Object value;

  public ValueOperand(Field field, Object value) {
    this.field = field;
    this.value = value;
  }

  public void visitOperand(OperandVisitor visitor) {
    visitor.visitValueOperand(this);
  }

  public Field getField() {
    return field;
  }

  public Object getValue() {
    return value;
  }
}
