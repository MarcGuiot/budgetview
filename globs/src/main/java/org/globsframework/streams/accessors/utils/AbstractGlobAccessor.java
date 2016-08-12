package org.globsframework.streams.accessors.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.FieldValue;
import org.globsframework.model.impl.AbstractFieldValues;
import org.globsframework.streams.accessors.GlobAccessor;

public abstract class AbstractGlobAccessor extends AbstractFieldValues implements GlobAccessor {

  private GlobType globType;

  public AbstractGlobAccessor(GlobType globType) {
    this.globType = globType;
  }

  public boolean contains(Field field) {
    return globType.equals(field.getGlobType());
  }

  public int size() {
    return globType.getFieldCount();
  }

  public void apply(Functor functor) throws Exception {
    for (Field field : globType.getFields()) {
      functor.process(field, doGet(field));
    }
  }

  public FieldValue[] toArray() {
    FieldValue[] array = new FieldValue[globType.getFields().length];
    int index = 0;
    for (Field field : globType.getFields()) {
      array[index] = new FieldValue(field, getValue(field));
      index++;
    }
    return array;
  }


}
