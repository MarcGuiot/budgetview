package org.globsframework.sqlstreams.exceptions;

import java.sql.SQLException;

public class RollbackFailed extends GlobsSqlException {

  public RollbackFailed(SQLException e) {
    super(e);
  }
}
