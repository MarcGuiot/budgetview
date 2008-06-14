package org.globsframework.sqlstreams.constraints.impl;

import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.ConstraintVisitor;

public class OrConstraint extends BinaryConstraint implements Constraint {
  public OrConstraint(Constraint leftOperand, Constraint rightOperand) {
    super(leftOperand, rightOperand);
  }

  public void visit(ConstraintVisitor constraintVisitor) {
    constraintVisitor.visitOr(this);
  }
}
