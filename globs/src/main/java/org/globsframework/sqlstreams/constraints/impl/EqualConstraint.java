package org.globsframework.sqlstreams.constraints.impl;

import org.globsframework.sqlstreams.constraints.ConstraintVisitor;
import org.globsframework.sqlstreams.constraints.Operand;

public class EqualConstraint extends BinaryOperandConstraint {

  public EqualConstraint(Operand leftOp, Operand rightOp) {
    super(leftOp, rightOp);
  }

  public void visit(ConstraintVisitor constraintVisitor) {
    constraintVisitor.visitEqual(this);
  }

}
