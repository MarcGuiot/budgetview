package org.crossbowlabs.globs.sqlstreams.accessors;

import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.SqlGlobStream;
import org.crossbowlabs.globs.streams.accessors.Accessor;

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

  public SqlGlobStream getSqlMoStream() {
    return sqlMoStream;
  }

}
