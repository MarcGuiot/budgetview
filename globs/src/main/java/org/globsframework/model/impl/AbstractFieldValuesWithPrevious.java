package org.globsframework.model.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldValuesWithPrevious;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.Date;

public abstract class AbstractFieldValuesWithPrevious implements FieldValuesWithPrevious {
  protected abstract Object doGet(Field field);

  protected abstract Object doGetPrevious(Field field);

  public Object getValue(Field field) throws ItemNotFound {
    return doGet(field);
  }

  public Double get(DoubleField field) throws ItemNotFound {
    return (Double)doGet(field);
  }

  public Date get(DateField field) throws ItemNotFound {
    return (Date)doGet(field);
  }

  public Date get(TimeStampField field) throws ItemNotFound {
    return (Date)doGet(field);
  }

  public Integer get(IntegerField field) throws ItemNotFound {
    return (Integer)doGet(field);
  }

  public Integer get(LinkField field) throws ItemNotFound {
    return (Integer)doGet(field);
  }

  public String get(StringField field) throws ItemNotFound {
    return (String)doGet(field);
  }

  public Boolean get(BooleanField field) throws ItemNotFound {
    return (Boolean)doGet(field);
  }

  public Boolean get(BooleanField field, boolean defaultIfNull) {
    return (Boolean)doGet(field);
  }

  public Long get(LongField field) throws ItemNotFound {
    return (Long)doGet(field);
  }

  public byte[] get(BlobField field) throws ItemNotFound {
    return (byte[])doGet(field);
  }

  public Object getPreviousValue(Field field) throws ItemNotFound {
    return doGetPrevious(field);
  }

  public Double getPrevious(DoubleField field) throws ItemNotFound {
    return (Double)doGetPrevious(field);
  }

  public Date getPrevious(DateField field) throws ItemNotFound {
    return (Date)doGetPrevious(field);
  }

  public Date getPrevious(TimeStampField field) throws ItemNotFound {
    return (Date)doGetPrevious(field);
  }

  public Integer getPrevious(IntegerField field) throws ItemNotFound {
    return (Integer)doGetPrevious(field);
  }

  public Integer getPrevious(LinkField field) throws ItemNotFound {
    return (Integer)doGetPrevious(field);
  }

  public String getPrevious(StringField field) throws ItemNotFound {
    return (String)doGetPrevious(field);
  }

  public Boolean getPrevious(BooleanField field) throws ItemNotFound {
    return (Boolean)doGetPrevious(field);
  }

  public Boolean getPrevious(BooleanField field, boolean defaultIfNull) {
    return (Boolean)doGetPrevious(field);
  }

  public Long getPrevious(LongField field) throws ItemNotFound {
    return (Long)doGetPrevious(field);
  }

  public byte[] getPrevious(BlobField field) throws ItemNotFound {
    return (byte[])doGetPrevious(field);
  }

  public void safeApply(FieldValues.Functor functor) {
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

  public void safeApply(FieldValuesWithPrevious.Functor functor) {
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
