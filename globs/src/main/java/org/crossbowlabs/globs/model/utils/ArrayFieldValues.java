package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.model.FieldValue;
import org.crossbowlabs.globs.model.impl.AbstractFieldValues;

import java.util.HashMap;
import java.util.Map;

public class ArrayFieldValues extends AbstractFieldValues {

  private FieldValue[] values;

  public ArrayFieldValues(FieldValue[] values) {
    this.values = values;
  }

  protected Object doGet(Field field) {
    for (FieldValue value : values) {
      if (value.getField().equals(field)) {
        return value.getValue();
      }
    }
    return null;
  }

  public boolean contains(Field field) {
    for (FieldValue value : values) {
      if (value.getField().equals(field)) {
        return true;
      }
    }
    return false;
  }

  public int size() {
    return values.length;
  }

  public void apply(Functor functor) throws Exception {
    for (FieldValue value : values) {
      functor.process(value.getField(), value.getValue());
    }
  }

  public Map<Field, Object> getMap() {
    Map<Field, Object> result = new HashMap<Field, Object>();
    for (FieldValue value : values) {
      result.put(value.getField(), value.getValue());
    }
    return result;
  }

  public FieldValue[] toArray() {
    return values;
  }
}
