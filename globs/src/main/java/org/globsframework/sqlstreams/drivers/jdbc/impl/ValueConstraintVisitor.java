package org.globsframework.sqlstreams.drivers.jdbc.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.sqlstreams.constraints.ConstraintVisitor;
import org.globsframework.sqlstreams.constraints.OperandVisitor;
import org.globsframework.sqlstreams.constraints.impl.*;
import org.globsframework.sqlstreams.drivers.jdbc.BlobUpdater;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.sql.PreparedStatement;

public class ValueConstraintVisitor extends SqlValueFieldVisitor implements ConstraintVisitor, OperandVisitor {
  private int index = 0;

  public ValueConstraintVisitor(PreparedStatement preparedStatement, BlobUpdater blobUpdater) {
    super(preparedStatement, blobUpdater);
  }

  public ValueConstraintVisitor(PreparedStatement preparedStatement, int index, BlobUpdater blobUpdater) {
    super(preparedStatement, blobUpdater);
    this.index = index;
  }

  public void visitEqual(EqualConstraint constraint) {
    visitBinary(constraint);
  }

  public void visitNotEqual(NotEqualConstraint constraint) {
    constraint.getLeftOperand().visitOperand(this);
    constraint.getRightOperand().visitOperand(this);
  }

  private void visitBinary(BinaryOperandConstraint operandConstraint) {
    operandConstraint.getLeftOperand().visitOperand(this);
    operandConstraint.getRightOperand().visitOperand(this);
  }

  private void visitBinary(BinaryConstraint constraint) {
    constraint.getLeftConstraint().visit(this);
    constraint.getRightConstraint().visit(this);
  }

  public void visitAnd(AndConstraint constraint) {
    visitBinary(constraint);
  }

  public void visitOr(OrConstraint constraint) {
    visitBinary(constraint);
  }

  public void visitLessThan(LessThanConstraint constraint) {
    visitBinary(constraint);
  }

  public void visitBiggerThan(BiggerThanConstraint constraint) {
    visitBinary(constraint);
  }

  public void visitStricklyBiggerThan(StrictlyBiggerThanConstraint constraint) {
    visitBinary(constraint);
  }

  public void visitStricklyLesserThan(StrictlyLesserThanConstraint constraint) {
    visitBinary(constraint);
  }

  public void visitIn(InConstraint inConstraint) {
    Field field = inConstraint.getField();
    for (Object value : inConstraint.getValues()) {
      setValue(value, ++index);
      field.safeVisit(this);
    }
  }

  public void visitValueOperand(ValueOperand value) {
    Object o = value.getValue();
    if (o == null) {
      throw new UnexpectedApplicationState("null not supported, Should be explicit (is null) ");
    }
    setValue(o, ++index);
    value.getField().safeVisit(this);
  }

  public void visitAccessorOperand(AccessorOperand accessorOperand) {
    setValue(accessorOperand.getAccessor().getObjectValue(), ++index);
    accessorOperand.getField().safeVisit(this);
  }

  public void visitFieldOperand(Field field) {
  }
}
