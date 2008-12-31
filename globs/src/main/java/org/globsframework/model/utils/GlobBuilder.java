package org.globsframework.model.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.*;
import org.globsframework.model.*;
import org.globsframework.model.impl.DefaultGlob;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.Date;

public class GlobBuilder implements FieldValues.Functor, FieldSetter {
  private FieldValuesBuilder fieldValuesBuilder = new FieldValuesBuilder();
  private GlobType globType;

  public static GlobBuilder init(GlobType globType) {
    return new GlobBuilder(globType);
  }

  public static Glob create(GlobType type, FieldValue... values) {
    return init(type, values).get();
  }

  public static GlobBuilder init(GlobType type, FieldValues values) {
    GlobBuilder builder = new GlobBuilder(type);
    values.safeApply(builder);
    return builder;
  }

  public static GlobBuilder init(GlobType type, FieldValue... values) {
    GlobBuilder builder = new GlobBuilder(type);
    FieldValues fieldValues = new ArrayFieldValues(values);
    fieldValues.safeApply(builder);
    return builder;
  }

  public static GlobBuilder init(Key key, FieldValues values) {
    GlobBuilder builder = new GlobBuilder(key.getGlobType());
    key.safeApply(builder);
    values.safeApply(builder);
    return builder;
  }

  public GlobBuilder set(DoubleField field, Double value) {
    fieldValuesBuilder.set(field, value);
    return this;
  }

  public GlobBuilder set(DateField field, Date value) {
    fieldValuesBuilder.set(field, value);
    return this;
  }

  public GlobBuilder set(TimeStampField field, Date value) {
    fieldValuesBuilder.set(field, value);
    return this;
  }

  public GlobBuilder set(IntegerField field, Integer value) {
    fieldValuesBuilder.set(field, value);
    return this;
  }

  public GlobBuilder set(LinkField field, Integer value) throws ItemNotFound {
    fieldValuesBuilder.set(field, value);
    return this;
  }

  public GlobBuilder set(StringField field, String value) {
    fieldValuesBuilder.set(field, value);
    return this;
  }

  public GlobBuilder set(BooleanField field, Boolean value) {
    fieldValuesBuilder.set(field, value);
    return this;
  }

  public GlobBuilder set(LongField field, Long value) {
    fieldValuesBuilder.set(field, value);
    return this;
  }

  public GlobBuilder set(BlobField field, byte[] value) {
    fieldValuesBuilder.set(field, value);
    return this;
  }

  public GlobBuilder setValue(Field field, Object value) throws ItemNotFound {
    fieldValuesBuilder.setValue(field, value);
    return this;
  }

  public GlobBuilder setObject(Field field, Object objectValue) {
    fieldValuesBuilder.setValue(field, objectValue);
    return this;
  }

  public Glob get() {
    return new DefaultGlob(globType, fieldValuesBuilder.get());
  }

  private GlobBuilder(GlobType globType) {
    this.globType = globType;
    for (Field field : globType.getFields()) {
      fieldValuesBuilder.setValue(field, field.getDefaultValue());
    }
  }

  public void process(Field field, Object value) throws Exception {
    setObject(field, value);
  }
}
