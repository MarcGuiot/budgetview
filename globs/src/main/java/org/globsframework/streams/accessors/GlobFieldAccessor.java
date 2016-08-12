package org.globsframework.streams.accessors;

import org.globsframework.metamodel.Field;

public class GlobFieldAccessor implements Accessor {
  private final GlobAccessor accessor;
  private final Field field;

  public GlobFieldAccessor(Field field, GlobAccessor accessor) {
    this.accessor = accessor;
    this.field = field;
  }

  public Object getObjectValue() {
    return accessor.getValue(field);
  }
}
