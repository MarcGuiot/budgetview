package org.crossbowlabs.globs.model.utils;

import org.crossbowlabs.globs.metamodel.Field;

public interface FieldValueGetter {
  boolean contains(Field field);

  Object get(Field field);
}
