package org.globsframework.model.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.Date;

public abstract class AbstractFieldValues implements FieldValues {
  public Double get(DoubleField field) {
    return (Double)doGet(field);
  }

  public Double get(DoubleField field, double valueIfNull) throws ItemNotFound {
    Object o = doGet(field);
    if (o == null){
      return valueIfNull;
    }
    return (Double)o;
  }

  public Date get(DateField field) {
    return (Date)doGet(field);
  }

  public Date get(DateField field, Date valueIfNull) {
    Object date = doGet(field);
    if (date == null) {
      return valueIfNull;
    }
    return (Date)date;
  }

  public Date get(TimeStampField field) {
    return (Date)doGet(field);
  }

  public Integer get(IntegerField field) {
    return (Integer)doGet(field);
  }

  public int get(IntegerField field, int valueIfNull) throws ItemNotFound {
    Integer value = (Integer)doGet(field);
    if (value == null) {
      return valueIfNull;
    }
    return value;
  }

  public Integer get(LinkField field) {
    return (Integer)doGet(field);
  }

  public String get(StringField field) {
    return (String)doGet(field);
  }

  public Boolean get(BooleanField field) {
    return (Boolean)doGet(field);
  }

  public boolean isTrue(BooleanField field) {
    return Boolean.TRUE.equals(doGet(field));
  }

  public Object getValue(Field field) {
    return doGet(field);
  }

  public byte[] get(BlobField field) {
    return (byte[])doGet(field);
  }

  public Boolean get(BooleanField field, boolean defaultIfNull) {
    Object value = doGet(field);
    if (value == null) {
      return defaultIfNull;
    }
    return (Boolean)value;
  }

  public Long get(LongField field) {
    return (Long)doGet(field);
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
