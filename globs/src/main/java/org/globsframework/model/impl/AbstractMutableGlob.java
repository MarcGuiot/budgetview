package org.globsframework.model.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.FieldValues;
import org.globsframework.model.MutableGlob;

import java.io.IOException;
import java.util.Date;

public abstract class AbstractMutableGlob extends AbstractGlob implements MutableGlob {
  protected AbstractMutableGlob(GlobType type) {
    super(type);
  }

  public AbstractMutableGlob(GlobType type, Object[] values) {
    super(type, values);
  }

  public void set(IntegerField field, Integer value) {
    setObject(field, value);
  }

  public void set(DoubleField field, Double value) {
    setObject(field, value);
  }

  public void set(StringField field, String value) {
    setObject(field, value);
  }

  public void set(DateField field, Date value) {
    setObject(field, value);
  }

  public void set(BooleanField field, Boolean value) {
    setObject(field, value);
  }

  public void set(BlobField field, byte[] value) {
    setObject(field, value);
  }

  public void setValues(FieldValues values) {
    values.safeApply(new FieldValues.Functor() {
      public void process(Field field, Object value) throws IOException {
        setObject(field, value);
      }
    });
  }

  public Object setObject(Field field, Object value) {
    final int index = field.getIndex();
    Object previousValue = values[index];
    values[index] = value;
    return previousValue;
  }
}
