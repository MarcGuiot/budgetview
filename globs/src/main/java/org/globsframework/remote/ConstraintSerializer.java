package org.globsframework.remote;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.sqlstreams.constraints.ConstraintVisitor;
import org.globsframework.sqlstreams.constraints.OperandVisitor;
import org.globsframework.sqlstreams.constraints.impl.*;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedOutput;

import java.util.Date;
import java.util.Iterator;

public class ConstraintSerializer {

  static void write(Constraint constraint, SerializedOutput output) {
    constraint.visit(new ConstraintVisitorSerializer(output));
  }

  static Constraint read(SerializedInput serializedInput) {
    ConstraintVisitorWriter visitorWriter = new ConstraintVisitorWriter(serializedInput);
    return visitorWriter.read();
  }

  private static class ConstraintVisitorWriter {
    private SerializedInput serializedInput;

    public ConstraintVisitorWriter(SerializedInput serializedInput) {
      this.serializedInput = serializedInput;
    }

    public Constraint read() {
//      ConstraintId constraintId = ConstraintId.(serializedInput.readNotNullInt());
//      switch(constraintId){
//        case ConstraintId.AndConstraint : {
//
//        }
//      }
      return null;
    }
  }

  private static class ConstraintVisitorSerializer implements ConstraintVisitor, OperandVisitor, FieldVisitor {
    private final SerializedOutput output;
    private Object value;

    public ConstraintVisitorSerializer(SerializedOutput output) {
      this.output = output;
    }

    public void visitEqual(EqualConstraint constraint) {
      output.writeByte(ConstraintId.EqualConstraint.getId());
      constraint.getLeftOperand().visitOperand(this);
      constraint.getRightOperand().visitOperand(this);
    }

    public void visitAnd(AndConstraint constraint) {
      output.writeByte(ConstraintId.AndConstraint.getId());
      constraint.getLeftConstraint().visit(this);
      constraint.getRightConstraint().visit(this);
    }

    public void visitOr(OrConstraint constraint) {
      output.writeByte(ConstraintId.OrConstraint.getId());
      constraint.getLeftConstraint().visit(this);
      constraint.getRightConstraint().visit(this);
    }

    public void visitLessThan(LessThanConstraint constraint) {
      output.writeByte(ConstraintId.LessThanConstraint.getId());
      constraint.getLeftOperand().visitOperand(this);
      constraint.getRightOperand().visitOperand(this);
    }

    public void visitBiggerThan(BiggerThanConstraint constraint) {
      output.writeByte(ConstraintId.BiggerThanConstraint.getId());
      constraint.getLeftOperand().visitOperand(this);
      constraint.getRightOperand().visitOperand(this);
    }

    public void visitStricklyBiggerThan(StrictlyBiggerThanConstraint constraint) {
      output.writeByte(ConstraintId.StrictlyBiggerThanConstraint.getId());
      constraint.getLeftOperand().visitOperand(this);
      constraint.getRightOperand().visitOperand(this);
    }

    public void visitStricklyLesserThan(StrictlyLesserThanConstraint constraint) {
      output.writeByte(ConstraintId.StrictlyLesserThanConstraint.getId());
      constraint.getLeftOperand().visitOperand(this);
      constraint.getRightOperand().visitOperand(this);
    }

    public void visitIn(InConstraint constraint) {
      output.writeByte(ConstraintId.InConstraint.getId());
      writeField(constraint.getField());
      for (Iterator iterator = constraint.getValues().iterator(); iterator.hasNext();) {
        value = iterator.next();
        constraint.getField().safeVisit(this);
      }
    }

    public void visitValueOperand(ValueOperand valueOperand) {
      output.writeByte(OperandId.ValueOperand.getId());
      writeField(valueOperand.getField());
      value = valueOperand.getValue();
      valueOperand.getField().safeVisit(this);
    }

    private void writeField(Field field) {
      output.writeString(field.getGlobType().getName());
      output.write(field.getIndex());
    }

    public void visitAccessorOperand(AccessorOperand accessorOperand) {
      output.writeByte(OperandId.AccessorOperand.getId());
      value = accessorOperand.getAccessor().getObjectValue();
      accessorOperand.getField().safeVisit(this);
    }

    public void visitFieldOperand(Field field) {
      output.writeByte(OperandId.Field.getId());
    }

    public void visitInteger(IntegerField field) throws Exception {
      output.write(((Integer)value).intValue());
    }

    public void visitDouble(DoubleField field) throws Exception {
      output.write((Double)value);
    }

    public void visitString(StringField field) throws Exception {
      output.writeString((String)value);
    }

    public void visitDate(DateField field) throws Exception {
      output.writeDate((Date)value);
    }

    public void visitBoolean(BooleanField field) throws Exception {
      output.write((Boolean)value);
    }

    public void visitTimeStamp(TimeStampField field) throws Exception {
      output.writeDate((Date)value);
    }

    public void visitBlob(BlobField field) throws Exception {
      output.writeBytes((byte[])value);
    }

    public void visitLong(LongField field) throws Exception {
      output.write((Integer)value);
    }

    public void visitLink(LinkField field) throws Exception {
      output.write((Integer)value);
    }
  }

  static enum ConstraintId {
    EqualConstraint(1),
    AndConstraint(2),
    OrConstraint(3),
    LessThanConstraint(4),
    BiggerThanConstraint(5),
    StrictlyBiggerThanConstraint(6),
    StrictlyLesserThanConstraint(7),
    InConstraint(8);
    private int id;

    ConstraintId(int id) {
      this.id = id;
    }

    int getId() {
      return id;
    }
  }

  static enum OperandId {
    Field(1),
    AccessorOperand(2),
    ValueOperand(3);
    private int id;

    OperandId(int id) {
      this.id = id;
    }

    int getId() {
      return id;
    }

  }
}
