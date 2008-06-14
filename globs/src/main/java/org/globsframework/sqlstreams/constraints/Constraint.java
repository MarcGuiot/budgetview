package org.globsframework.sqlstreams.constraints;

public interface Constraint {
  void visit(ConstraintVisitor constraintVisitor);
}
