package org.crossbowlabs.globs.sqlstreams.constraints.impl;

import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;
import org.crossbowlabs.globs.sqlstreams.constraints.ConstraintVisitor;

public class OrConstraint extends BinaryConstraint implements Constraint {
  public OrConstraint(Constraint leftOperand, Constraint rightOperand) {
    super(leftOperand, rightOperand);
  }

  public void visit(ConstraintVisitor constraintVisitor) {
    constraintVisitor.visitOr(this);
  }
}
