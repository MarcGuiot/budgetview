package org.crossbowlabs.globs.sqlstreams.constraints.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.sqlstreams.constraints.Operand;
import org.crossbowlabs.globs.sqlstreams.constraints.OperandVisitor;

public class FieldOperand implements Operand {
  private Field field;

  public FieldOperand(Field field) {
    this.field = field;
  }

  public void visitOperand(OperandVisitor visitor) {
    visitor.visitFieldOperand(field);
  }
}
