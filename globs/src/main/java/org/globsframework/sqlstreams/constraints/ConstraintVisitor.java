package org.globsframework.sqlstreams.constraints;

import org.globsframework.sqlstreams.constraints.impl.*;

public interface ConstraintVisitor {
  void visitEqual(EqualConstraint constraint);

  void visitNotEqual(NotEqualConstraint constraint);

  void visitAnd(AndConstraint constraint);

  void visitOr(OrConstraint constraint);

  void visitLessThan(LessThanConstraint constraint);

  void visitBiggerThan(BiggerThanConstraint constraint);

  void visitStrictlyGreaterThan(StrictlyBiggerThanConstraint constraint);

  void visitStrictlyLessThan(StrictlyLesserThanConstraint constraint);

  void visitIn(InConstraint constraint);
}
