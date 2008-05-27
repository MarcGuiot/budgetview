package org.crossbowlabs.globs.sqlstreams.constraints.impl;

import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;

public class BinaryConstraint {
  private Constraint leftConstraint;
  private Constraint rightConstraint;

  public BinaryConstraint(Constraint leftConstraint, Constraint rightConstraint) {
    this.leftConstraint = leftConstraint;
    this.rightConstraint = rightConstraint;
  }

  public Constraint getLeftConstraint() {
    return leftConstraint;
  }

  public Constraint getRightConstraint() {
    return rightConstraint;
  }
}
