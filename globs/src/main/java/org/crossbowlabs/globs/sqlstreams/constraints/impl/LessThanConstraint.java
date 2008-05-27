package org.crossbowlabs.globs.sqlstreams.constraints.impl;

import org.crossbowlabs.globs.sqlstreams.constraints.ConstraintVisitor;
import org.crossbowlabs.globs.sqlstreams.constraints.Operand;

public class LessThanConstraint extends BinaryOperandConstraint {
  public LessThanConstraint(Operand leftOperand, Operand rightOperand) {
    super(leftOperand, rightOperand);
  }

  public void visit(ConstraintVisitor constraintVisitor) {
    constraintVisitor.visitLessThan(this);
  }
}
