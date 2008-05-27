package org.crossbowlabs.globs.model;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.fields.*;

import java.util.Date;

public class FieldValue {
  private final Field field;
  private final Object value;

  public static FieldValue value(DoubleField field, Double value) {
    return new FieldValue(field, value);
  }

  public static FieldValue value(DateField field, Date value) {
    return new FieldValue(field, value);
  }

  public static FieldValue value(TimeStampField field, Date value) {
    return new FieldValue(field, value);
  }

  public static FieldValue value(IntegerField field, Integer value) {
    return new FieldValue(field, value);
  }

  public static FieldValue value(LinkField field, Integer value) {
    return new FieldValue(field, value);
  }

  public static FieldValue value(StringField field, String value) {
    return new FieldValue(field, value);
  }

  public static FieldValue value(BooleanField field, Boolean value) {
    return new FieldValue(field, value);
  }

  public static FieldValue value(LongField field, Long value) {
    return new FieldValue(field, value);
  }

  public static FieldValue value(BlobField field, byte[] value) {
    return new FieldValue(field, value);
  }

  public FieldValue(Field field, Object value) {
    this.field = field;
    this.value = value;
  }

  public Field getField() {
    return field;
  }

  public Object getValue() {
    return value;
  }
}
