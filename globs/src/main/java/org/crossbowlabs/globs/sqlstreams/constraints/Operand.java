package org.crossbowlabs.globs.sqlstreams.constraints;

public interface Operand {
  void visitOperand(OperandVisitor visitor);
}
