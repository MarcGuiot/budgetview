package org.crossbowlabs.globs.sqlstreams.constraints;

import org.crossbowlabs.globs.sqlstreams.constraints.impl.*;

public interface ConstraintVisitor {
  void visitEqual(EqualConstraint constraint);

  void visitAnd(AndConstraint constraint);

  void visitOr(OrConstraint constraint);

  void visitLessThan(LessThanConstraint constraint);

  void visitBiggerThan(BiggerThanConstraint constraint);

  void visitStricklyBiggerThan(StrictlyBiggerThanConstraint constraint);

  void visitStricklyLesserThan(StrictlyLesserThanConstraint constraint);

  void visitIn(InConstraint constraint);
}
