package org.crossbowlabs.globs.model.impl;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.metamodel.fields.*;
import org.crossbowlabs.globs.model.FieldValue;
import org.crossbowlabs.globs.model.FieldValues;
import org.crossbowlabs.globs.model.Key;
import org.crossbowlabs.globs.utils.Utils;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;
import org.crossbowlabs.globs.utils.exceptions.MissingInfo;

import java.util.Date;
import java.util.List;

public class SingleFieldKey extends Key {

  private Object value;
  private Field keyField;
  private int hashCode = -1;

  public SingleFieldKey(Field field, Object value) throws MissingInfo {
    checkValue(field, value);
    this.keyField = field;
    this.keyField.checkValue(value);
    this.value = value;
  }

  static void checkValue(Field field, Object value) throws MissingInfo {
    if (value == null) {
      throw new MissingInfo("Field '" + field.getName() +
                            "' missing (should not be NULL) for identifying a Glob of type: " + field.getGlobType().getName());
    }
  }

  public SingleFieldKey(GlobType type, Object value) throws InvalidParameter {
    this(getKeyField(type), value);
  }

  private static Field getKeyField(GlobType type) throws InvalidParameter {
    List<Field> keyFields = type.getKeyFields();
    if (keyFields.size() != 1) {
      throw new InvalidParameter("Cannot use a single field key for type " + type + " - " +
                                 "key fields=" + keyFields);
    }
    return keyFields.get(0);
  }

  public GlobType getGlobType() {
    return keyField.getGlobType();
  }

  public void apply(FieldValues.Functor functor) throws Exception {
    functor.process(keyField, value);
  }

  public boolean contains(Field field) {
    return keyField.equals(field);
  }

  public void safeApply(FieldValues.Functor functor) {
    try {
      functor.process(keyField, value);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int size() {
    return 1;
  }

  public byte[] get(BlobField field) {
    checkIsKeyField(field);
    return (byte[])value;
  }

  public Boolean get(BooleanField field, boolean defaultIfNull) {
    checkIsKeyField(field);
    if (value == null) {
      return defaultIfNull;
    }
    return (Boolean)value;
  }

  public Boolean get(BooleanField field) {
    checkIsKeyField(field);
    return (Boolean)value;
  }

  public Date get(DateField field) {
    checkIsKeyField(field);
    return (Date)value;
  }

  public Double get(DoubleField field) {
    checkIsKeyField(field);
    return (Double)value;
  }

  public Object getValue(Field field) {
    checkIsKeyField(field);
    return value;
  }

  public Integer get(IntegerField field) {
    checkIsKeyField(field);
    return (Integer)value;
  }

  public Integer get(LinkField field) {
    return get((IntegerField)field);
  }

  public Long get(LongField field) {
    checkIsKeyField(field);
    return (Long)value;
  }

  public String get(StringField field) {
    checkIsKeyField(field);
    return (String)value;
  }

  public Date get(TimeStampField field) {
    checkIsKeyField(field);
    return (Date)value;
  }

  private void checkIsKeyField(Field field) {
    if (!keyField.equals(field)) {
      throw new ItemNotFound("'" + field.getName() + "' is not a key of type " + getGlobType().getName());
    }
  }

  // optimized - do not use generated code
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }
    if (o.getClass().equals(SingleFieldKey.class)) {
      SingleFieldKey otherSingleFieldKey = (SingleFieldKey)o;
      return otherSingleFieldKey.keyField.equals(keyField) &&
             Utils.equal(otherSingleFieldKey.value, value);
    }

    if (!Key.class.isAssignableFrom(o.getClass())) {
      return false;
    }
    Key otherKey = (Key)o;
    return keyField.getGlobType().equals(otherKey.getGlobType())
           && Utils.equal(value, otherKey.getValue(keyField));
  }

  // optimized - do not use generated code
  public int hashCode() {
    if (hashCode < 0) {
      hashCode = (value != null ? value.hashCode() : 0);
      hashCode = 29 * hashCode + keyField.hashCode();
    }
    return hashCode;
  }

  public FieldValue[] toArray() {
    return new FieldValue[]{
      new FieldValue(keyField, value),
    };
  }

  public String toString() {
    return getGlobType().getName() + "[" + keyField.getName() + "=" + value + "]";
  }
}
