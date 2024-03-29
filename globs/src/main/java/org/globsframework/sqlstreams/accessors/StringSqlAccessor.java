package org.globsframework.sqlstreams.accessors;

import org.globsframework.streams.accessors.StringAccessor;

public class StringSqlAccessor extends SqlAccessor implements StringAccessor {

  public String getString() {
    return getSqlStream().getString(getIndex());
  }

  public Object getObjectValue() {
    return getString();
  }
}
