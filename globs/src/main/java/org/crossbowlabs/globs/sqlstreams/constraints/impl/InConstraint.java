package org.crossbowlabs.globs.sqlstreams.constraints.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;
import org.crossbowlabs.globs.sqlstreams.constraints.ConstraintVisitor;

import java.util.List;

public class InConstraint implements Constraint {
  private Field field;
  private List values;

  public InConstraint(Field field, List values) {
    this.field = field;
    this.values = values;
  }

  public void visit(ConstraintVisitor constraintVisitor) {
    constraintVisitor.visitIn(this);
  }

  public Field getField() {
    return field;
  }

  public List getValues() {
    return values;
  }
}
