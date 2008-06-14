package org.globsframework.sqlstreams.accessors;

import org.globsframework.streams.accessors.IntegerAccessor;

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
