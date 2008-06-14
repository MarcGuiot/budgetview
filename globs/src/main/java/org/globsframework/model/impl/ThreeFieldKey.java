package org.globsframework.model.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Key;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.MissingInfo;

import java.util.Date;

public class ThreeFieldKey extends Key {
  private Field keyField1;
  private Object value1;
  private Field keyField2;
  private Object value2;
  private Field keyField3;
  private Object value3;
  private int hashCode = 0;

  public ThreeFieldKey(Field keyField1, Object value1,
                       Field keyField2, Object value2,
                       Field keyField3, Object value3) throws MissingInfo {
    SingleFieldKey.checkValue(keyField1, value1);
    SingleFieldKey.checkValue(keyField2, value2);
    SingleFieldKey.checkValue(keyField3, value3);

    this.keyField1 = keyField1;
    this.value1 = value1;
    this.keyField2 = keyField2;
    this.value2 = value2;
    this.keyField3 = keyField3;
    this.value3 = value3;
    this.keyField1.checkValue(value1);
    this.keyField2.checkValue(value2);
    this.keyField3.checkValue(value3);
  }

  public GlobType getGlobType() {
    return keyField1.getGlobType();
  }

  public void apply(Functor functor) throws Exception {
    functor.process(keyField1, value1);
    functor.process(keyField2, value2);
    functor.process(keyField3, value3);
  }

  public boolean contains(Field field) {
    return keyField1.equals(field) || keyField2.equals(field) || keyField3.equals(field);
  }

  public void safeApply(Functor functor) {
    try {
      functor.process(keyField1, value1);
      functor.process(keyField2, value2);
      functor.process(keyField3, value3);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int size() {
    return 3;
  }

  public byte[] get(BlobField field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return (byte[])value1;
    }
    if (field == keyField2) {
      return (byte[])value2;
    }
    if (field == keyField3) {
      return (byte[])value3;
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
    if (field == keyField3) {
      return (Boolean)(value3 == null ? defaultIfNull : value3);
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
    if (field == keyField3) {
      return (Boolean)value3;
    }
    return null;
  }

  public Date get(DateField field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return (Date)value1;
    }
    if (field == keyField2) {
      return (Date)value2;
    }
    if (field == keyField3) {
      return (Date)value3;
    }
    return null;
  }

  public Double get(DoubleField field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return (Double)value1;
    }
    if (field == keyField2) {
      return (Double)value2;
    }
    if (field == keyField3) {
      return (Double)value3;
    }
    return null;
  }

  public Object getValue(Field field) {
    checkIsKeyField(field);
    if (field == keyField1) {
      return value1;
    }
    if (field == keyField2) {
      return value2;
    }
    if (field == keyField3) {
      return value3;
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
    if (field == keyField3) {
      return (Integer)value3;
    }
    return null;
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
    if (field == keyField3) {
      return (Long)value3;
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
    if (field == keyField3) {
      return (String)value3;
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
    if (field == keyField3) {
      return (Date)value3;
    }
    return null;
  }

  private void checkIsKeyField(Field field) {
    if (!keyField1.equals(field) && !keyField2.equals(field) && !keyField3.equals(field)) {
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
    if (o.getClass() == ThreeFieldKey.class || o.getClass().equals(ThreeFieldKey.class)) {
      ThreeFieldKey otherSingleFieldKey = (ThreeFieldKey)o;
      return
        otherSingleFieldKey.keyField1.getGlobType().equals(keyField1.getGlobType()) &&
        otherSingleFieldKey.value1.equals(value1) &&
        otherSingleFieldKey.value2.equals(value2) &&
        otherSingleFieldKey.value3.equals(value3);
    }

    if (!Key.class.isAssignableFrom(o.getClass())) {
      return false;
    }
    Key otherKey = (Key)o;
    return keyField1.getGlobType().equals(otherKey.getGlobType())
           && value1.equals(otherKey.getValue(keyField1))
           && value2.equals(otherKey.getValue(keyField2))
           && value3.equals(otherKey.getValue(keyField3));
  }

  // optimized - do not use generated code
  public int hashCode() {
    if (hashCode != 0) {
      return hashCode;
    }
    hashCode = keyField1.getGlobType().hashCode();
    hashCode = 31 * hashCode + value1.hashCode();
    hashCode = 31 * hashCode + value2.hashCode();
    hashCode = 31 * hashCode + value3.hashCode();
    if (hashCode == 0) {
      hashCode = 23;
    }
    return hashCode;
  }

  public FieldValue[] toArray() {
    return new FieldValue[]{
      new FieldValue(keyField1, value1),
      new FieldValue(keyField2, value2),
      new FieldValue(keyField3, value3)
    };
  }

  public String toString() {
    return getGlobType().getName() + "[" + keyField1.getName() + "=" + value1 + ", " +
           keyField2.getName() + "=" + value2 + ", " +
           keyField3.getName() + "=" + value3 + "]";
  }
}