package org.crossbowlabs.globs.sqlstreams.constraints;

public interface Constraint {
  void visit(ConstraintVisitor constraintVisitor);
}
