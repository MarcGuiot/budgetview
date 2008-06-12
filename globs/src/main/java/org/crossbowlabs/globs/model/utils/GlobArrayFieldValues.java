package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.FieldValue;
import org.crossbowlabs.globs.model.impl.AbstractFieldValues;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;

import java.util.Arrays;

public class GlobArrayFieldValues extends AbstractFieldValues {
  private GlobType type;
  private Object[] values;

  public GlobArrayFieldValues(GlobType type, Object[] values) throws InvalidParameter {
    this.type = type;
    this.values = values;
    if (values.length != type.getFieldCount()) {
      throw new InvalidParameter("Values should have " + type.getFieldCount() + " elements instead of " +
                                 values.length + " for type " + type.getName() +
                                 " - array content: " + Arrays.toString(values));
    }
  }

  protected Object doGet(Field field) {
    return values[field.getIndex()];
  }

  public boolean contains(Field field) {
    return type.equals(field.getGlobType());
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
    FieldValue[] result = new FieldValue[values.length];
    int index = 0;
    for (Field field : type.getFields()) {
      result[index++] = new FieldValue(field, values[field.getIndex()]);
    }
    return result;
  }
}
