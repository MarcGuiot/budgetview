package org.crossbowlabs.globs.sqlstreams.drivers.jdbc.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.sqlstreams.SqlService;
import org.crossbowlabs.globs.sqlstreams.constraints.ConstraintVisitor;
import org.crossbowlabs.globs.sqlstreams.constraints.OperandVisitor;
import org.crossbowlabs.globs.sqlstreams.constraints.impl.*;
import org.crossbowlabs.globs.sqlstreams.utils.StringPrettyWriter;

import java.util.Set;

public class WhereClauseConstraintVisitor implements ConstraintVisitor, OperandVisitor {
  private StringPrettyWriter prettyWriter;
  private SqlService sqlService;
  private Set<GlobType> globTypes;

  public WhereClauseConstraintVisitor(StringPrettyWriter prettyWriter, SqlService sqlService,
                                      Set<GlobType> GlobeTypeSetToUpdate) {
    this.prettyWriter = prettyWriter;
    this.sqlService = sqlService;
    this.globTypes = GlobeTypeSetToUpdate;
  }

  public void visitEqual(EqualConstraint constraint) {
    visitBinary(constraint, " = ");
  }

  public void visitAnd(AndConstraint constraint) {
    visitBinary(constraint, " AND ");
  }

  public void visitOr(OrConstraint constraint) {
    visitBinary(constraint, " OR ");
  }

  public void visitLessThan(LessThanConstraint constraint) {
    visitBinary(constraint, " <= ");
  }

  public void visitBiggerThan(BiggerThanConstraint constraint) {
    visitBinary(constraint, " >= ");
  }

  public void visitStricklyBiggerThan(StrictlyBiggerThanConstraint constraint) {
    visitBinary(constraint, " > ");
  }

  public void visitStricklyLesserThan(StrictlyLesserThanConstraint constraint) {
    visitBinary(constraint, " < ");
  }

  public void visitIn(InConstraint inConstraint) {
    visitFieldOperand(inConstraint.getField());
    prettyWriter.append(" in (");
    int length = inConstraint.getValues().size();
    for (int i = 0; i < length; i++) {
      prettyWriter.append(" ? ").appendIf(", ", i < length - 1);
    }
    prettyWriter.append(")");
  }

  public void visitValueOperand(ValueOperand value) {
    prettyWriter.append(" ? ");
  }

  public void visitAccessorOperand(AccessorOperand accessorOperand) {
    prettyWriter.append(" ? ");
  }

  public void visitFieldOperand(Field field) {
    globTypes.add(field.getGlobType());
    prettyWriter.append(sqlService.getTableName(field.getGlobType()))
      .append(".")
      .append(sqlService.getColumnName(field));
  }

  private void visitBinary(BinaryOperandConstraint constraint, String operator) {
    constraint.getLeftOperand().visitOperand(this);
    prettyWriter.append(operator);
    constraint.getRightOperand().visitOperand(this);
  }

  private void visitBinary(BinaryConstraint constraint, String operator) {
    prettyWriter.append("(");
    constraint.getLeftConstraint().visit(this);
    prettyWriter.append(operator);
    constraint.getRightConstraint().visit(this);
    prettyWriter.append(")").newLine();
  }
}
