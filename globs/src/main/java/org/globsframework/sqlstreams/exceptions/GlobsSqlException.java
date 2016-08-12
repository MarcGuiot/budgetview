package org.globsframework.sqlstreams.exceptions;

import org.globsframework.utils.exceptions.GlobsException;

import java.sql.SQLException;

public class GlobsSQLException extends GlobsException {
  private SQLException e;

  public GlobsSQLException(SQLException e) {
    super(e);
    this.e = e;
  }

  public GlobsSQLException(String message) {
    super(message);
  }

  public GlobsSQLException(String message, SQLException cause) {
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
