package org.crossbowlabs.globs.sqlstreams.accessors;

import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.SqlGlobStream;
import org.crossbowlabs.globs.streams.accessors.BooleanAccessor;

public class BooleanSqlAccessor extends SqlAccessor implements BooleanAccessor {
  private boolean cachedValue;
  private boolean isNull;
  private int rowId;

  public Boolean getBoolean() {
    return isNull ? null : getValue();
  }

  public boolean getValue() {
    SqlGlobStream moStream = getSqlMoStream();
    if (moStream.getCurrentRowId() == rowId) {
      return cachedValue;
    }
    else {
      cachedValue = moStream.getBoolean(getIndex());
      rowId = moStream.getCurrentRowId();
      isNull = getSqlMoStream().isNull();
      return cachedValue;
    }

  }

  public Object getObjectValue() {
    return getBoolean();
  }
}
