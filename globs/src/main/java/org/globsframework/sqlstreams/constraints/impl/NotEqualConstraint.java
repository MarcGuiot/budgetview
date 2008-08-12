package org.globsframework.sqlstreams.constraints.impl;

import org.globsframework.sqlstreams.constraints.ConstraintVisitor;
import org.globsframework.sqlstreams.constraints.Operand;

public class NotEqualConstraint extends BinaryOperandConstraint {

  public NotEqualConstraint(Operand left, Operand right) {
    super(left, right);
  }

  public void visit(ConstraintVisitor constraintVisitor) {
    constraintVisitor.visitNotEqual(this);
  }
}
