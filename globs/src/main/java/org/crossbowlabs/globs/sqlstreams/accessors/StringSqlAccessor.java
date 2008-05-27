package org.crossbowlabs.globs.sqlstreams.accessors;

import org.crossbowlabs.globs.streams.accessors.StringAccessor;

public class StringSqlAccessor extends SqlAccessor implements StringAccessor {

  public String getString() {
    return getSqlMoStream().getString(getIndex());
  }

  public Object getObjectValue() {
    return getString();
  }
}
