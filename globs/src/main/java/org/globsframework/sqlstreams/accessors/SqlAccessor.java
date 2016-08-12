package org.globsframework.sqlstreams.accessors;

import org.globsframework.sqlstreams.drivers.jdbc.SqlGlobStream;
import org.globsframework.streams.accessors.Accessor;

public abstract class SqlAccessor implements Accessor {
  private SqlGlobStream sqlMoStream;
  private int index;

  public void setMoStream(SqlGlobStream sqlMoStream) {
    this.sqlMoStream = sqlMoStream;
  }

  public void setIndex(int index) {
    this.index = index;
  }

  public int getIndex() {
    return index;
  }

  public SqlGlobStream getSqlStream() {
    return sqlMoStream;
  }

}
