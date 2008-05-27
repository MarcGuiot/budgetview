package org.crossbowlabs.globs.sqlstreams.constraints.impl;

import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;
import org.crossbowlabs.globs.sqlstreams.constraints.ConstraintVisitor;

public class AndConstraint extends BinaryConstraint implements Constraint {

  public AndConstraint(Constraint leftOperand, Constraint rightOperand) {
    super(leftOperand, rightOperand);
  }

  public void visit(ConstraintVisitor constraintVisitor) {
    constraintVisitor.visitAnd(this);
  }
}
