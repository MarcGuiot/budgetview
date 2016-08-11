package org.globsframework.sqlstreams.constraints;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LongField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.model.FieldValues;
import org.globsframework.sqlstreams.constraints.impl.*;
import org.globsframework.streams.accessors.*;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.util.List;

public class Where {
  private Where() {
  }

  public static Constraint keyEquals(final KeyConstraint keyAccessor) {
    Field[] list = keyAccessor.getGlobType().getKeyFields();
    Constraint constraint = null;
    for (final Field field : list) {
      constraint = Where.and(constraint,
                             fieldEqualsValue(field, new KeyElementAccessor(keyAccessor, field)));
    }
    return constraint;
  }

  public static Constraint fieldsAreEqual(FieldValues values) {
    try {
      ConstraintsFunctor functor = new ConstraintsFunctor(values);
      values.apply(functor);
      return functor.getConstraint();
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public static Constraint fieldsAreEqual(Field field1, Field field2) {
    return new EqualConstraint(new FieldOperand(field1), new FieldOperand(field2));
  }

  /**
   * We use different name to help the IDE giving us the good completion
   */

  public static Constraint fieldEqualsValue(Field field, Object value) {
    return new EqualConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldEqualsValue(Field field, Accessor accessor) {
    return new EqualConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint fieldEquals(IntegerField field, Integer value) {
    return new EqualConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldEquals(LongField field, Long value) {
    return new EqualConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldEquals(DoubleField field, DoubleAccessor accessor) {
    return new EqualConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint fieldEquals(StringField field, StringAccessor accessor) {
    return new EqualConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint fieldEquals(StringField field, String value) {
    return new EqualConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldEquals(IntegerField field, IntegerAccessor accessor) {
    return new EqualConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint fieldEquals(LongField field, LongAccessor accessor) {
    return new EqualConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint fieldGreaterThan(Field field, Object value) {
    return new BiggerThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldGreaterThan(Field field1, Field field2) {
    return new BiggerThanConstraint(new FieldOperand(field1), new FieldOperand(field2));
  }

  public static Constraint fieldGreaterThan(IntegerField field, Integer value) {
    return new BiggerThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldGreaterThan(DoubleField field, Double value) {
    return new BiggerThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldGreaterThan(Field field, Accessor accessor) {
    return new BiggerThanConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint fieldLessThanValue(Field field, Object value) {
    return new LessThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldLessThan(Field field1, Field field2) {
    return new LessThanConstraint(new FieldOperand(field1), new FieldOperand(field2));
  }

  public static Constraint fieldLessThan(IntegerField field, Integer value) {
    return new LessThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldLessThan(DoubleField field, Double value) {
    return new LessThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldLessThan(Field field, Accessor accessor) {
    return new LessThanConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint fieldStrictlyGreaterThan(Field field, Object value) {
    return new StrictlyBiggerThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldStrictlyGreaterThan(Field field1, Field field2) {
    return new StrictlyBiggerThanConstraint(new FieldOperand(field1), new FieldOperand(field2));
  }

  public static Constraint fieldStrictlyGreaterThan(IntegerField field, Integer value) {
    return new StrictlyBiggerThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldStrictlyGreaterThanValue(Field field, Accessor accessor) {
    return new StrictlyBiggerThanConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint fieldStriclyLessThanValue(Field field, Object value) {
    return new StrictlyLesserThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldStrictlyLessThan(Field field1, Field field2) {
    return new StrictlyLesserThanConstraint(new FieldOperand(field1), new FieldOperand(field2));
  }

  public static Constraint fieldStrictlyLessThan(IntegerField field, Integer value) {
    return new StrictlyLesserThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint fieldStrictlyLessThan(Field field, Accessor accessor) {
    return new StrictlyLesserThanConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint and(Constraint arg1, Constraint arg2) {
    if (arg1 == null) {
      return arg2;
    }
    if (arg2 == null) {
      return arg1;
    }
    return new AndConstraint(arg1, arg2);
  }

  public static Constraint and(Constraint arg1, Constraint arg2, Constraint arg3) {
    return and(arg1, and(arg2, arg3));
  }

  public static Constraint or(Constraint arg1, Constraint arg2) {
    if (arg1 == null) {
      return arg2;
    }
    if (arg2 == null) {
      return arg1;
    }
    return new OrConstraint(arg1, arg2);
  }

  public static Constraint in(Field field, List values) {
    return new InConstraint(field, values);
  }

  public static Constraint notEqual(StringField field, String value) {
    return new NotEqualConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  private static class ConstraintsFunctor implements FieldValues.Functor {
    private Constraint constraint = null;
    private final FieldValues key;

    public ConstraintsFunctor(FieldValues key) {
      this.key = key;
    }

    public void process(final Field field, Object value) throws Exception {
      constraint = Where.and(constraint, Where.fieldEqualsValue(field, new Accessor() {
        public Object getObjectValue() {
          return key.getValue(field);
        }
      }));
    }

    public Constraint getConstraint() {
      return constraint;
    }
  }

  private static class KeyElementAccessor implements Accessor {
    private final KeyConstraint keyAccessor;
    private final Field field;

    public KeyElementAccessor(KeyConstraint keyAccessor, Field field) {
      this.keyAccessor = keyAccessor;
      this.field = field;
    }

    public Object getObjectValue() {
      return keyAccessor.getValue(field);
    }
  }
}
