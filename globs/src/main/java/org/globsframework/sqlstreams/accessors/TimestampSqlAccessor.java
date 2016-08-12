package org.globsframework.sqlstreams.accessors;

import org.globsframework.streams.accessors.TimestampAccessor;

import java.util.Date;

public class TimestampSqlAccessor extends SqlAccessor implements TimestampAccessor {

  public long getTimestamp() {
    return getSqlStream().getTimeStamp(getIndex()).getTime();
  }

  public Date getDate() {
    return getSqlStream().getTimeStamp(getIndex());
  }

  public Object getObjectValue() {
    return getDate();
  }
}
