package org.globsframework.sqlstreams.constraints.impl;

import org.globsframework.sqlstreams.constraints.ConstraintVisitor;
import org.globsframework.sqlstreams.constraints.Operand;

public class BiggerThanConstraint extends BinaryOperandConstraint {
  public BiggerThanConstraint(Operand leftOperand, Operand rightOperand) {
    super(leftOperand, rightOperand);
  }

  public void visit(ConstraintVisitor visitor) {
    visitor.visitBiggerThan(this);
  }
}
