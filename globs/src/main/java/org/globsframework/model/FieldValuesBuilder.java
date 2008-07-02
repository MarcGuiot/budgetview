package org.globsframework.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.utils.DefaultFieldValues;

import java.util.Date;
import java.util.Map;

public class FieldValuesBuilder {

  private DefaultFieldValues values = new DefaultFieldValues();

  public static FieldValuesBuilder init() {
    return new FieldValuesBuilder();
  }

  public static FieldValuesBuilder init(Field field, Object value) {
    FieldValuesBuilder builder = new FieldValuesBuilder();
    return builder.setObject(field, value);
  }

  public static FieldValuesBuilder init(Map<Field, Object> map) {
    FieldValuesBuilder builder = new FieldValuesBuilder();
    builder.set(map);
    return builder;
  }

  public FieldValuesBuilder set(Map<Field, Object> map) {
    for (Map.Entry<Field, Object> entry : map.entrySet()) {
      setObject(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public FieldValuesBuilder set(FieldValues values) {
    for (FieldValue fieldValue : values.toArray()) {
      setObject(fieldValue.getField(), fieldValue.getValue());
    }
    return this;
  }

  public FieldValuesBuilder set(BooleanField field, Boolean value) {
    return setObject(field, value);
  }

  public FieldValuesBuilder set(DateField field, Date value) {
    return setObject(field, value);
  }

  public FieldValuesBuilder set(StringField field, String value) {
    return setObject(field, value);
  }

  public FieldValuesBuilder set(IntegerField field, Integer value) {
    return setObject(field, value);
  }

  public FieldValuesBuilder set(DoubleField field, Double value) {
    return setObject(field, value);
  }

  public FieldValuesBuilder set(TimeStampField field, Date value) {
    return setObject(field, value);
  }

  public FieldValuesBuilder set(BlobField field, byte[] value) {
    return setObject(field, value);
  }

  public FieldValuesBuilder set(LongField field, long value) {
    return setObject(field, value);
  }

  public FieldValuesBuilder setObject(Field field, Object value) {
    values.setValue(field, value);
    return this;
  }

  public MutableFieldValues get() {
    return values;
  }

  public boolean contains(Field field) {
    return values.contains(field);
  }

  public FieldValue[] toArray() {
    return values.toArray();
  }

  public static FieldValues removeKeyFields(FieldValues input) {
    final FieldValuesBuilder builder = new FieldValuesBuilder();
    input.safeApply(new FieldValues.Functor() {
      public void process(Field field, Object value) throws Exception {
        builder.setObject(field, value);
      }
    });
    return builder.get();
  }

  public int size() {
    return values.size();
  }
}
