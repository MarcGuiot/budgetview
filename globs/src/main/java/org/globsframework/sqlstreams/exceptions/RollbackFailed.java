package org.globsframework.sqlstreams.exceptions;

import java.sql.SQLException;

public class RollbackFailed extends GlobsSQLException {

  public RollbackFailed(SQLException e) {
    super(e);
  }
}
