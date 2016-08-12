package org.globsframework.streams.accessors;

import org.globsframework.metamodel.Field;

public interface GlobAccessor {
  Object getValue(Field field);
}
