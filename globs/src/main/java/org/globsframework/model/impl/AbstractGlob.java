package org.globsframework.model.impl;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.Link;
import org.globsframework.metamodel.links.FieldMappingFunctor;
import org.globsframework.model.*;
import org.globsframework.utils.Ref;
import org.globsframework.utils.Utils;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.globsframework.utils.exceptions.InvalidState;
import org.globsframework.utils.exceptions.ItemNotFound;

import java.util.List;

public abstract class AbstractGlob extends AbstractFieldValues implements Glob {

  protected GlobType type;
  protected Object[] values;
  private boolean disposed = false;
  private Key key;

  protected AbstractGlob(GlobType type) {
    this(type, new Object[type.getFieldCount()]);
  }

  public AbstractGlob(GlobType type, Object[] values) {
    this.type = type;
    this.values = values;
  }

  protected AbstractGlob(final GlobType type, FieldValues fieldValues) {
    this(type);
    fieldValues.safeApply(new FieldValues.Functor() {
      public void process(Field field, Object value) throws Exception {
        if (field.getGlobType().equals(type)) {
          values[field.getIndex()] = value;
        }
      }
    });
  }

  public GlobType getType() {
    return type;
  }

  public boolean exists() {
    return !disposed;
  }

  public boolean contains(Field field) {
    return field.getGlobType().equals(type);
  }

  public int size() {
    return values.length;
  }

  public void apply(Functor functor) throws Exception {
    for (Field field : type.getFields()) {
      functor.process(field, values[field.getIndex()]);
    }
  }

  public FieldValue[] toArray() {
    FieldValue[] array = new FieldValue[values.length];
    int index = 0;
    for (Field field : type.getFields()) {
      array[index] = new FieldValue(field, values[index]);
      index++;
    }
    return array;
  }

  public FieldValues getTargetValues(Link link) {
    if (!link.getSourceType().equals(type)) {
      throw new InvalidParameter("Link '" + link + " cannot be used with " + this);
    }
    final FieldValuesBuilder builder = FieldValuesBuilder.init();
    link.apply(new FieldMappingFunctor() {
      public void process(Field sourceField, Field targetField) {
        builder.setObject(targetField, getValue(sourceField));
      }
    });
    return builder.get();
  }

  public Object doGet(Field field) {
    if (disposed) {
      throw new InvalidState("Using a deleted instance of '" + type.getName() + "'");
    }
    if (!field.getGlobType().equals(type)) {
      throw new ItemNotFound("Field '" + field.getName() + "' is declared for type '" +
                             field.getGlobType().getName() + "' and not for '" + type.getName() + "'");
    }
    return values[field.getIndex()];
  }

  public FieldValues getValues() {
    FieldValuesBuilder builder = FieldValuesBuilder.init();
    for (Field field : type.getFields()) {
      if (!field.isKeyField()) {
        builder.setObject(field, values[field.getIndex()]);
      }
    }
    return builder.get();
  }

  protected Object[] duplicateValues() {
    Object[] newValues = new Object[values.length];
    System.arraycopy(values, 0, newValues, 0, values.length);
    return newValues;
  }

  public String toString() {
    return getKey().toString();
  }

  public final boolean matches(FieldValues values) {
    final Ref<Boolean> result = new Ref<Boolean>(true);
    values.safeApply(new FieldValues.Functor() {
      public void process(Field field, Object value) {
        if (!Utils.equal(value, getValue(field))) {
          result.set(false);
        }
      }
    });
    return result.get();
  }

  public boolean matches(FieldValue... values) {
    for (FieldValue value : values) {
      if (!Utils.equal(value.getValue(), getValue(value.getField()))) {
        return false;
      }
    }
    return true;
  }

  public Key getKey() {
    if (key == null) {
      KeyBuilder keyBuilder = KeyBuilder.init(type);
      List<Field> keyFields = type.getKeyFields();
      for (Field field : keyFields) {
        keyBuilder.add(field, values[field.getIndex()]);
      }
      key = keyBuilder.get();
    }
    return key;
  }

  void dispose() {
    disposed = true;
  }
}
