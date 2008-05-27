package org.crossbowlabs.globs.sqlstreams.accessors;

import org.crossbowlabs.globs.streams.accessors.LongAccessor;

public class LongSqlAccessor extends SqlAccessor implements LongAccessor {

  public Long getLong() {
    return getSqlMoStream().getLong(getIndex());
  }

  public long getValue() {
    return getLong();
  }

  public Object getObjectValue() {
    return getLong();
  }
}
