package org.crossbowlabs.globs.sqlstreams.accessors;

import org.crossbowlabs.globs.streams.accessors.TimestampAccessor;

import java.util.Date;

public class TimestampSqlAccessor extends SqlAccessor implements TimestampAccessor {

  public long getTimestamp() {
    return getSqlMoStream().getTimeStamp(getIndex()).getTime();
  }

  public Date getDate() {
    return getSqlMoStream().getTimeStamp(getIndex());
  }

  public Object getObjectValue() {
    return getDate();
  }
}
