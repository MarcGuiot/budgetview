package org.crossbowlabs.globs.model.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.model.FieldValue;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.model.utils.FieldValueGetter;
import org.crossbowlabs.globs.utils.Utils;
import org.crossbowlabs.globs.utils.exceptions.MissingInfo;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CompositeKey extends Key {
  private GlobType type;
  private Object[] values;
  private int hashCode = -1;

  public CompositeKey(GlobType type, FieldValueGetter getter) {
    this.type = type;
    List<Field> keyFields = type.getKeyFields();
    this.values = new Object[keyFields.size()];
    int index = 0;
    for (Field field : keyFields) {
      if (!getter.contains(field)) {
        throw new MissingInfo("Field '" + field.getName() +
                              "' missing for identifying a Glob of type: " + type.getName());
      }
      values[index++] = getter.get(field);
    }
  }

  CompositeKey(GlobType type, Object[] globValues) {
    this.type = type;
    List<Field> keyFields = type.getKeyFields();
    this.values = new Object[keyFields.size()];
    int index = 0;
    for (Field field : keyFields) {
      values[index++] = globValues[field.getIndex()];
    }
  }

  public GlobType getGlobType() {
    return type;
  }

  public boolean contains(Field field) {
    return type.getKeyFields().contains(field);
  }

  public int size() {
    return type.getKeyFields().size();
  }

  public void apply(Functor functor) throws Exception {
    List<Field> fields = type.getKeyFields();
    int index = 0;
    for (Field field : fields) {
      functor.process(field, values[index++]);
    }
  }

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

  protected Object doGet(Field field) {
    List<Field> fields = type.getKeyFields();
    int index = 0;
    for (Field keyField : fields) {
      if (keyField.equals(field)) {
        return values[index];
      }
      index++;
    }

    return null;
  }

  // optimized - do not use generated code
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }

    if (o.getClass().equals(CompositeKey.class)) {
      CompositeKey other = (CompositeKey)o;
      return type.equals(other.type) && Arrays.equals(values, other.values);
    }

    if (!Key.class.isAssignableFrom(o.getClass())) {
      return false;
    }

    Key otherKey = (Key)o;
    if (!type.equals(otherKey.getGlobType())) {
      return false;
    }
    for (Field field : type.getKeyFields()) {
      if (!Utils.equal(getValue(field), otherKey.getValue(field))) {
        return false;
      }
    }
    return true;
  }

  // optimized - do not use generated code
  public int hashCode() {
    if (hashCode < 0) {
      hashCode = 0;
      for (Field keyField : type.getKeyFields()) {
        Object value = getValue(keyField);
        hashCode = 29 * hashCode + (value != null ? value.hashCode() : 0);
        hashCode = 29 * hashCode + keyField.hashCode();

      }
    }
    return hashCode;
  }

  // Overwritten so that fields are always in the same order
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(type.getName());
    builder.append('[');
    List<Field> fields = type.getKeyFields();
    int i = 0;
    for (Field field : fields) {
      builder.append(field.getName());
      builder.append('=');
      builder.append(getValue(field));
      i++;
      if (i < fields.size()) {
        builder.append(',');
      }
    }
    builder.append(']');
    return builder.toString();
  }

  public Double get(DoubleField field) {
    return (Double)doGet(field);
  }

  public Date get(DateField field) {
    return (Date)doGet(field);
  }

  public Date get(TimeStampField field) {
    return (Date)doGet(field);
  }

  public Integer get(IntegerField field) {
    return (Integer)doGet(field);
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

  public FieldValue[] toArray() {
    List<Field> keyFields = type.getKeyFields();
    FieldValue[] array = new FieldValue[keyFields.size()];
    int index = 0;
    for (Field field : keyFields) {
      array[index] = new FieldValue(field, values[index]);
      index++;
    }
    return array;
  }
}
