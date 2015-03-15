package org.globsframework.model.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.FieldValue;
import org.globsframework.model.Key;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.MissingInfo;

import java.util.Arrays;
import java.util.Date;

public class FourFieldKey extends Key {
  private Field keyField1;
  private Object value1;
  private Field keyField2;
  private Object value2;
  private Field keyField3;
  private Object value3;
  private Field keyField4;
  private Object value4;
  private int hashCode = 0;

  public FourFieldKey(Field keyField1, Object value1,
                      Field keyField2, Object value2,
                      Field keyField3, Object value3,
                      Field keyField4, Object value4) throws MissingInfo {
    SingleFieldKey.checkValue(keyField1, value1);
    SingleFieldKey.checkValue(keyField2, value2);
    SingleFieldKey.checkValue(keyField3, value3);
    SingleFieldKey.checkValue(keyField4, value4);

    Field[] keyFields = keyField1.getGlobType().getKeyFields();
    if (keyFields.length != 4) {
      throw new InvalidParameter("Cannot use a three-fields key for type " + keyField1.getGlobType() + " - " +
                                 "key fields=" + Arrays.toString(keyFields));
    }
    Field field;
    field = keyFields[0];
    this.keyField1 = field;
    this.value1 = field == keyField4 ? value4 : field == keyField3 ? value3 : field == keyField2 ? value2 : value1;

    field = keyFields[1];
    this.keyField2 = field;
    this.value2 = field == keyField4 ? value4 : field == keyField3 ? value3 : field == keyField2 ? value2 : value1;

    field = keyFields[2];
    this.keyField3 = field;
    this.value3 = field == keyField4 ? value4 : field == keyField3 ? value3 : field == keyField2 ? value2 : value1;

    field = keyFields[3];
    this.keyField4 = field;
    this.value4 = field == keyField4 ? value4 : field == keyField3 ? value3 : field == keyField2 ? value2 : value1;

    this.keyField1.checkValue(value1);
    this.keyField2.checkValue(value2);
    this.keyField3.checkValue(value3);
    this.keyField4.checkValue(value4);
  }

  public GlobType getGlobType() {
    return keyField1.getGlobType();
  }

  public void apply(Functor functor) throws Exception {
    functor.process(keyField1, value1);
    functor.process(keyField2, value2);
    functor.process(keyField3, value3);
    functor.process(keyField4, value4);
  }

  public boolean contains(Field field) {
    return keyField1.equals(field) || keyField2.equals(field) || keyField3.equals(field) || keyField4.equals(field);
  }

  public void safeApply(Functor functor) {
    try {
      functor.process(keyField1, value1);
      functor.process(keyField2, value2);
      functor.process(keyField3, value3);
      functor.process(keyField4, value4);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int size() {
    return 4;
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
    if (field == keyField4) {
      return (byte[])value4;
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
    if (field == keyField4) {
      return (Boolean)value4;
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
    if (field == keyField3) {
      return (Date)value3;
    }
    if (field == keyField4) {
      return (Date)value4;
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
    if (field == keyField4) {
      return (Double)value4;
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
    if (field == keyField4) {
      return value4;
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
    if (field == keyField4) {
      return (Integer)value4;
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
    if (field == keyField4) {
      return (Long)value4;
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
    if (field == keyField4) {
      return (String)value4;
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
    if (field == keyField4) {
      return (Date)value4;
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
    if (o.getClass() == FourFieldKey.class || o.getClass().equals(FourFieldKey.class)) {
      FourFieldKey otherSingleFieldKey = (FourFieldKey)o;
      return
        otherSingleFieldKey.keyField1.getGlobType().equals(keyField1.getGlobType()) &&
        Utils.equal(otherSingleFieldKey.value1, value1) &&
        Utils.equal(otherSingleFieldKey.value2, value2) &&
        Utils.equal(otherSingleFieldKey.value3, value3) &&
        Utils.equal(otherSingleFieldKey.value4, value4);
    }

    if (!Key.class.isAssignableFrom(o.getClass())) {
      return false;
    }
    Key otherKey = (Key)o;
    return keyField1.getGlobType().equals(otherKey.getGlobType())
           && value1.equals(otherKey.getValue(keyField1))
           && value2.equals(otherKey.getValue(keyField2))
           && value3.equals(otherKey.getValue(keyField3))
           && value4.equals(otherKey.getValue(keyField4));
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
    hashCode = 31 * hashCode + value4.hashCode();
    if (hashCode == 0) {
      hashCode = 23;
    }
    return hashCode;
  }

  public FieldValue[] toArray() {
    return new FieldValue[]{
      new FieldValue(keyField1, value1),
      new FieldValue(keyField2, value2),
      new FieldValue(keyField3, value3),
      new FieldValue(keyField4, value4)
    };
  }

  public String toString() {
    return getGlobType().getName() + "[" +
           keyField1.getName() + "=" + value1 + ", " +
           keyField2.getName() + "=" + value2 + ", " +
           keyField3.getName() + "=" + value3 + ", " +
           keyField4.getName() + "=" + value4 + "]";
  }
}