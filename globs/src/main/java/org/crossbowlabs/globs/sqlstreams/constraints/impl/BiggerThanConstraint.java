package org.crossbowlabs.globs.sqlstreams.constraints.impl;

import org.crossbowlabs.globs.sqlstreams.constraints.ConstraintVisitor;
import org.crossbowlabs.globs.sqlstreams.constraints.Operand;

public class BiggerThanConstraint extends BinaryOperandConstraint {
  public BiggerThanConstraint(Operand leftOperand, Operand rightOperand) {
    super(leftOperand, rightOperand);
  }

  public void visit(ConstraintVisitor visitor) {
    visitor.visitBiggerThan(this);
  }
}
