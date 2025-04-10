package org.globsframework.sqlstreams.exceptions;

import org.globsframework.utils.exceptions.GlobsException;

import java.sql.SQLException;

public class GlobsSqlException extends GlobsException {
  private SQLException e;

  public GlobsSqlException(SQLException e) {
    super(e);
    this.e = e;
  }

  public GlobsSqlException(String message) {
    super(message);
  }

  public GlobsSqlException(String message, SQLException cause) {
    super(message, cause);
    e = cause;
  }

  public String getSqlState() {
    if (e != null) {
      return e.getSQLState();
    }
    return null;
  }

  public int getErrorCode() {
    if (e != null) {
      return e.getErrorCode();
    }
    return -1;
  }
}
