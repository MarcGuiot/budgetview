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

public class Constraints {
  private Constraints() {
  }

  public static Constraint keyEquals(final KeyConstraint keyAccessor) {
    Field[] list = keyAccessor.getGlobType().getKeyFields();
    Constraint constraint = null;
    for (final Field field : list) {
      constraint = Constraints.and(constraint, Constraints.equalsObject(field,
                                                                        new KeyElementAccessor(keyAccessor, field)));
    }
    return constraint;
  }

  public static Constraint fieldsEqual(FieldValues values) {
    try {
      ConstraintsFunctor functor = new ConstraintsFunctor(values);
      values.apply(functor);
      return functor.getConstraint();
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  /**
   * We use different name to help the IDE giving us the good completion
   */

  public static Constraint equalsObject(Field field, Object value) {
    return new EqualConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint equalsObject(Field field, Accessor accessor) {
    return new EqualConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint fieldEqual(Field field1, Field field2) {
    return new EqualConstraint(new FieldOperand(field1), new FieldOperand(field2));
  }

  public static Constraint equal(IntegerField field, Integer value) {
    return new EqualConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint equal(LongField field, Long value) {
    return new EqualConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint equal(DoubleField field, DoubleAccessor accessor) {
    return new EqualConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint equal(StringField field, StringAccessor accessor) {
    return new EqualConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint equal(StringField field, String value) {
    return new EqualConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint equal(IntegerField field, IntegerAccessor accessor) {
    return new EqualConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint equal(LongField field, LongAccessor accessor) {
    return new EqualConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint greater(Field field, Object value) {
    return new BiggerThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint greater(Field field1, Field field2) {
    return new BiggerThanConstraint(new FieldOperand(field1), new FieldOperand(field2));
  }

  public static Constraint greater(IntegerField field, Integer value) {
    return new BiggerThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint greater(Field field, Accessor accessor) {
    return new BiggerThanConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint lessUncheck(Field field, Object value) {
    return new LessThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint less(Field field1, Field field2) {
    return new LessThanConstraint(new FieldOperand(field1), new FieldOperand(field2));
  }

  public static Constraint less(IntegerField field, Integer value) {
    return new LessThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint less(Field field, Accessor accessor) {
    return new LessThanConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint strictlyGreater(Field field, Object value) {
    return new StrictlyBiggerThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint strictlyGreater(Field field1, Field field2) {
    return new StrictlyBiggerThanConstraint(new FieldOperand(field1), new FieldOperand(field2));
  }

  public static Constraint strictlyGreater(IntegerField field, Integer value) {
    return new StrictlyBiggerThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint strictlyBigger(Field field, Accessor accessor) {
    return new StrictlyBiggerThanConstraint(new FieldOperand(field), new AccessorOperand(field, accessor));
  }

  public static Constraint Lesser(Field field, Object value) {
    return new StrictlyLesserThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint strictlyLess(Field field1, Field field2) {
    return new StrictlyLesserThanConstraint(new FieldOperand(field1), new FieldOperand(field2));
  }

  public static Constraint strictlyLess(IntegerField field, Integer value) {
    return new StrictlyLesserThanConstraint(new FieldOperand(field), new ValueOperand(field, value));
  }

  public static Constraint strictlyLess(Field field, Accessor accessor) {
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

  public static Constraint in(Field field, List infos) {
    return new InConstraint(field, infos);
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
      constraint = Constraints.and(constraint, Constraints.equalsObject(field, new Accessor() {
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
