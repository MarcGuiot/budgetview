package org.globsframework.sqlstreams.accessors;

import org.globsframework.streams.accessors.DateAccessor;

import java.util.Date;

public class DateSqlAccessor extends SqlAccessor implements DateAccessor {

  public Date getDate() {
    return getSqlStream().getDate(getIndex());
  }

  public Object getObjectValue() {
    return getDate();
  }
}
