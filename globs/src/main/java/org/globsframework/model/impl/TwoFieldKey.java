package org.globsframework.model.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Key;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.MissingInfo;

import java.util.Arrays;
import java.util.Date;

public class TwoFieldKey extends Key {
  private Field keyField1;
  private Object value1;
  private Field keyField2;
  private Object value2;
  private int hashCode = 0;

  public TwoFieldKey(Field keyField1, Object value1,
                     Field keyField2, Object value2) throws MissingInfo {

    SingleFieldKey.checkValue(keyField1, value1);
    SingleFieldKey.checkValue(keyField2, value2);
    Field[] keyFields = keyField1.getGlobType().getKeyFields();
    if (keyFields.length != 2) {
      throw new InvalidParameter("Cannot use a two-fields key for type " + keyField1.getGlobType() + " - " +
                                 "key fields=" + Arrays.toString(keyFields));
    }
    Field field;
    field = keyFields[0];
    this.keyField1 = field;
    this.value1 = field == keyField1 ? value1 : value2;
    field = keyFields[1];
    this.keyField2 = field;
    this.value2 = field == keyField2 ? value2 : value1;

    this.keyField1.checkValue(value1);
    this.keyField2.checkValue(value2);
  }

  public GlobType getGlobType() {
    return keyField1.getGlobType();
  }

  public void apply(Functor functor) throws Exception {
    functor.process(keyField1, value1);
    functor.process(keyField2, value2);
  }

  public boolean contains(Field field) {
    return keyField1.equals(field) || keyField2.equals(field);
  }

  public void safeApply(Functor functor) {
    try {
      functor.process(keyField1, value1);
      functor.process(keyField2, value2);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int size() {
    return 2;
  }

  public byte[] get(BlobField field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return (byte[])value1;
    }
    if (field == keyField2) {
      return (byte[])value2;
    }
    return null;
  }

  public Boolean get(BooleanField field, boolean defaultIfNull) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return (Boolean)(value1 == null ? defaultIfNull : value1);
    }
    if (field == keyField2) {
      return (Boolean)(value2 == null ? defaultIfNull : value2);
    }
    return null;
  }

  public Boolean get(BooleanField field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return (Boolean)value1;
    }
    if (field == keyField2) {
      return (Boolean)value2;
    }
    return null;
  }

  public boolean isTrue(BooleanField field) {
    return Boolean.TRUE.equals(get(field));
  }

  public Date get(DateField field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return (Date)value1;
    }
    if (field == keyField2) {
      return (Date)value2;
    }
    return null;
  }

  public Date get(DateField field, Date valueIfNull) throws ItemNotFound {
    Date value = get(field);
    if (value == null) {
      return valueIfNull;
    }
    return value;
  }

  public Double get(DoubleField field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return (Double)value1;
    }
    if (field == keyField2) {
      return (Double)value2;
    }
    return null;
  }

  public Double get(DoubleField field, double valueIfNull) throws ItemNotFound {
    return get(field);
  }

  public Object getValue(Field field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return value1;
    }
    if (field == keyField2) {
      return value2;
    }
    return null;
  }

  public Integer get(IntegerField field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return (Integer)value1;
    }
    if (field == keyField2) {
      return (Integer)value2;
    }
    return null;
  }

  public int get(IntegerField field, int valueIfNull) throws ItemNotFound {
    Integer value = get(field);
    if (value == null) {
      return valueIfNull;
    }
    return value;
  }

  public Integer get(LinkField field) {
    return get((IntegerField)field);
  }

  public Long get(LongField field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return (Long)value1;
    }
    if (field == keyField2) {
      return (Long)value2;
    }
    return null;

  }

  public String get(StringField field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return (String)value1;
    }
    if (field == keyField2) {
      return (String)value2;
    }
    return null;
  }

  public Date get(TimeStampField field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return (Date)value1;
    }
    if (field == keyField2) {
      return (Date)value2;
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
    if (o.getClass().equals(TwoFieldKey.class)) {
      TwoFieldKey twoFieldKey = (TwoFieldKey)o;
      return keyField1.getGlobType().equals(twoFieldKey.getGlobType()) &&
             Utils.equal(twoFieldKey.value1, value1) &&
             Utils.equal(twoFieldKey.value2, value2);
    }

    if (!Key.class.isAssignableFrom(o.getClass())) {
      return false;
    }
    Key otherKey = (Key)o;
    return keyField1.getGlobType().equals(otherKey.getGlobType())
           && Utils.equal(value1, otherKey.getValue(keyField1)) &&
           keyField2.getGlobType().equals(otherKey.getGlobType())
           && Utils.equal(value2, otherKey.getValue(keyField2));
  }

  // optimized - do not use generated code
  public int hashCode() {
    if (hashCode != 0) {
      return hashCode;
    }
    hashCode = keyField1.hashCode();
    hashCode = 31 * hashCode + value1.hashCode();
    hashCode = 31 * hashCode + keyField2.hashCode();
    hashCode = 31 * hashCode + value2.hashCode();
    hashCode = 31 * hashCode + hashCode;
    if (hashCode == 0) {
      hashCode = 23;
    }
    return hashCode;
  }

  public FieldValue[] toArray() {
    return new FieldValue[]{
      new FieldValue(keyField1, value1),
      new FieldValue(keyField2, value2),
    };
  }

  public String toString() {
    return getGlobType().getName() + "[" + keyField1.getName() + "=" + value1 + ", " +
           keyField2.getName() + "=" + value2 + "]";
  }
}