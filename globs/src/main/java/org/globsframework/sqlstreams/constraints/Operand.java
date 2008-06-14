package org.globsframework.sqlstreams.constraints;

public interface Operand {
  void visitOperand(OperandVisitor visitor);
}
