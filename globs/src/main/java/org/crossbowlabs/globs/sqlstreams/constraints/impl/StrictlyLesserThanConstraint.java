package org.crossbowlabs.globs.sqlstreams.constraints.impl;

import org.crossbowlabs.globs.sqlstreams.constraints.ConstraintVisitor;
import org.crossbowlabs.globs.sqlstreams.constraints.Operand;

public class StrictlyLesserThanConstraint extends BinaryOperandConstraint {
  public StrictlyLesserThanConstraint(Operand leftOperand, Operand rightOperand) {
    super(leftOperand, rightOperand);
  }

  public void visit(ConstraintVisitor constraintVisitor) {
    constraintVisitor.visitStricklyLesserThan(this);
  }
}
