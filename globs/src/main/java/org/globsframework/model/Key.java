package org.globsframework.model;

import org.globsframework.metamodel.GlobType;

public abstract class Key implements FieldValues {
  public abstract GlobType getGlobType();

  public static Key create(GlobType type, Object singleFieldValue) {
    return KeyBuilder.newKey(type, singleFieldValue);
  }

  public static KeyBuilder create(GlobType type) {
    return KeyBuilder.init(type);
  }

}
