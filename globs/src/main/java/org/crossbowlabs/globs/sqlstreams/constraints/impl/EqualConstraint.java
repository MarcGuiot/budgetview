package org.crossbowlabs.globs.sqlstreams.constraints.impl;

import org.crossbowlabs.globs.sqlstreams.constraints.ConstraintVisitor;
import org.crossbowlabs.globs.sqlstreams.constraints.Operand;

public class EqualConstraint extends BinaryOperandConstraint {

  public EqualConstraint(Operand leftOp, Operand rightOp) {
    super(leftOp, rightOp);
  }

  public void visit(ConstraintVisitor constraintVisitor) {
    constraintVisitor.visitEqual(this);
  }

}
