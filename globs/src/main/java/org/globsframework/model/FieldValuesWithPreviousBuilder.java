package org.globsframework.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.fields.*;

import java.util.Date;

public class FieldValuesWithPreviousBuilder {
  public static FieldValuesWithPreviousBuilder init() {
    return new FieldValuesWithPreviousBuilder();
  }

  public void setObject(Field field, Object value) {
  }

  public FieldValuesWithPrevious get() {
    return null;
  }

  <<<<<<<local

  public void set(IntegerField field, Integer newValue, Integer previousValue) {
  }

  public void set(DoubleField field, Double newValue, Double previousValue) {
  }

  public void set(StringField field, String newValue, String previousValue) {
  }

  public void set(DateField field, Date newValue, Date previousValue) {
  }

  public void set(BooleanField field, Boolean newValue, Boolean previousValue) {
  }

  public void set(TimeStampField field, Date newValue, Date previousValue) {
  }

  public void set(BlobField field, byte[] newValue, byte[] previousValue) {
  }

  public void set(LongField field, Long newValue, Long previousValue) {
  }

  =======
    >>>>>>>other
}
