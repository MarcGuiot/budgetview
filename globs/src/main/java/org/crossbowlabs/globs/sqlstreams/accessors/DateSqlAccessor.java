package org.crossbowlabs.globs.sqlstreams.accessors;

import org.crossbowlabs.globs.streams.accessors.DateAccessor;

import java.util.Date;

public class DateSqlAccessor extends SqlAccessor implements DateAccessor {

  public Date getDate() {
    return getSqlMoStream().getDate(getIndex());
  }

  public Object getObjectValue() {
    return getDate();
  }
}
