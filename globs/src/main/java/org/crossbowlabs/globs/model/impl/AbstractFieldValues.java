package org.crossbowlabs.globs.model.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.model.FieldValues;

import java.util.Date;

public abstract class AbstractFieldValues implements FieldValues {
  public Double get(DoubleField field) {
    return (Double) doGet(field);
  }

  public Date get(DateField field) {
    return (Date) doGet(field);
  }

  public Date get(TimeStampField field) {
    return (Date) doGet(field);
  }

  public Integer get(IntegerField field) {
    return (Integer) doGet(field);
  }

  public Integer get(LinkField field) {
    return (Integer) doGet(field);
  }

  public String get(StringField field) {
    return (String) doGet(field);
  }

  public Boolean get(BooleanField field) {
    return (Boolean) doGet(field);
  }

  public Object getValue(Field field) {
    return doGet(field);
  }

  public byte[] get(BlobField field) {
    return (byte[]) doGet(field);
  }

  public Boolean get(BooleanField field, boolean defaultIfNull) {
    Object value = doGet(field);
    if (value == null) {
      return defaultIfNull;
    }
    return (Boolean) value;
  }

  public Long get(LongField field) {
    return (Long) doGet(field);
  }

  protected abstract Object doGet(Field field);

  public void safeApply(Functor functor) {
    try {
      apply(functor);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
