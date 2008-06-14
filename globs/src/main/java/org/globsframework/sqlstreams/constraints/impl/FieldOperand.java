package org.globsframework.sqlstreams.constraints.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.sqlstreams.constraints.Operand;
import org.globsframework.sqlstreams.constraints.OperandVisitor;

public class FieldOperand implements Operand {
  private Field field;

  public FieldOperand(Field field) {
    this.field = field;
  }

  public void visitOperand(OperandVisitor visitor) {
    visitor.visitFieldOperand(field);
  }
}
