package org.globsframework.sqlstreams.accessors;

import org.globsframework.sqlstreams.drivers.jdbc.SqlGlobStream;
import org.globsframework.streams.accessors.BooleanAccessor;

public class BooleanSqlAccessor extends SqlAccessor implements BooleanAccessor {
  private boolean cachedValue;
  private boolean isNull;
  private int rowId;

  public Boolean getBoolean() {
    return isNull ? null : getValue();
  }

  public boolean getValue() {
    SqlGlobStream moStream = getSqlStream();
    if (moStream.getCurrentRowId() == rowId) {
      return cachedValue;
    }
    else {
      cachedValue = moStream.getBoolean(getIndex());
      rowId = moStream.getCurrentRowId();
      isNull = getSqlStream().isNull();
      return cachedValue;
    }

  }

  public Object getObjectValue() {
    return getBoolean();
  }
}
