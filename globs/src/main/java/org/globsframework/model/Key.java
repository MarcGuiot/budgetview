package org.globsframework.model;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.impl.ThreeFieldKey;
import org.globsframework.model.impl.TwoFieldKey;
import org.globsframework.utils.exceptions.ItemNotFound;

public abstract class Key implements FieldValues {
  public abstract GlobType getGlobType();

  public static Key create(GlobType type, Object singleFieldValue) {
    return KeyBuilder.newKey(type, singleFieldValue);
  }

  public static Key create(Field field1, Object value1, Field field2, Object value2) {
    return new TwoFieldKey(field1, value1, field2, value2);
  }

  public static Key create(Field field1, Object value1, Field field2, Object value2, Field field3, Object value3) {
    return new ThreeFieldKey(field1, value1, field2, value2, field3, value3);
  }

  public static KeyBuilder create(GlobType type) {
    return KeyBuilder.init(type);
  }
}
