package org.crossbowlabs.globs.sqlstreams.accessors;

import org.crossbowlabs.globs.streams.accessors.IntegerAccessor;

public class IntegerSqlAccessor extends SqlAccessor implements IntegerAccessor {

  public Integer getInteger() {
    return getSqlMoStream().getInteger(getIndex());
  }

  public int getValue() {
    return getInteger();
  }

  public Object getObjectValue() {
    return getInteger();
  }
}
