package org.globsframework.sqlstreams.constraints.impl;

import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.Operand;

public abstract class BinaryOperandConstraint implements Constraint {
  private Operand leftOperand;
  private Operand rightOperand;

  public BinaryOperandConstraint(Operand leftOperand, Operand rightOperand) {
    this.leftOperand = leftOperand;
    this.rightOperand = rightOperand;
  }

  public Operand getLeftOperand() {
    return leftOperand;
  }

  public Operand getRightOperand() {
    return rightOperand;
  }
}
