package org.crossbowlabs.globs.model.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.model.FieldValues;
import org.crossbowlabs.globs.model.MutableGlob;

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

  public void setObject(Field field, Object value) {
    values[field.getIndex()] = value;
  }
}
