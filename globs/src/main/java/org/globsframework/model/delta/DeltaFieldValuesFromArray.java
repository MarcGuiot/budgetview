package org.globsframework.model.delta;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.FieldValue;
import org.globsframework.model.FieldValues;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.Unset;

import java.util.Date;

class DeltaFieldValuesFromArray implements FieldValues {
  private GlobType type;
  private Object[] values;

  public DeltaFieldValuesFromArray(GlobType type, Object[] values) {
    this.type = type;
    this.values = values;
  }

  public Object getValue(Field field) throws ItemNotFound {
    Object value = values[field.getIndex()];
    if (value == Unset.VALUE) {
      throw new ItemNotFound(field.getName() + " not set.");
    }
    return value;
  }

  public Double get(DoubleField field) throws ItemNotFound {
    return (Double)getValue(field);
  }

  public Date get(DateField field) throws ItemNotFound {
    return (Date)getValue(field);
  }

  public Date get(TimeStampField field) throws ItemNotFound {
    return (Date)getValue(field);
  }

  public Integer get(IntegerField field) throws ItemNotFound {
    return (Integer)getValue(field);
  }

  public Integer get(LinkField field) throws ItemNotFound {
    return (Integer)getValue(field);
  }

  public String get(StringField field) throws ItemNotFound {
    return (String)getValue(field);
  }

  public Boolean get(BooleanField field) throws ItemNotFound {
    return (Boolean)getValue(field);
  }

  public Boolean get(BooleanField field, boolean defaultIfNull) {
    Boolean value = (Boolean)getValue(field);
    if (value == null) {
      return defaultIfNull;
    }
    return value;
  }

  public Long get(LongField field) throws ItemNotFound {
    return (Long)getValue(field);
  }

  public byte[] get(BlobField field) throws ItemNotFound {
    return (byte[])getValue(field);
  }

  public boolean contains(Field field) {
    if (field.isKeyField()) {
      return false;
    }
    return values[field.getIndex()] != Unset.VALUE;
  }

  public int size() {
    int count = 0;
    for (Field field : type.getFields()) {
      if (!field.isKeyField() && values[field.getIndex()] != Unset.VALUE) {
        count++;
      }
    }
    return count;
  }

  public void apply(Functor functor) throws Exception {
    for (Field field : type.getFields()) {
      Object value = values[field.getIndex()];
      if (value != Unset.VALUE && !field.isKeyField()) {
        functor.process(field, value);
      }
    }
  }

  public void safeApply(Functor functor) {
    try {
      for (Field field : type.getFields()) {
        Object value = values[field.getIndex()];
        if (value != Unset.VALUE && !field.isKeyField()) {
          functor.process(field, value);
        }
      }
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public FieldValue[] toArray() {
    FieldValue[] fieldValues = new FieldValue[size()];
    int i = 0;
    for (Field field : type.getFields()) {
      Object value = values[field.getIndex()];
      if (value != Unset.VALUE && !field.isKeyField()) {
        fieldValues[i] = new FieldValue(field, value);
        i++;
      }
    }
    return fieldValues;
  }

  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (Field field : type.getFields()) {
      builder.append(field.getName()).append(":").append(values[field.getIndex()]).append(("\n"));
    }
    return builder.toString();
  }
}
