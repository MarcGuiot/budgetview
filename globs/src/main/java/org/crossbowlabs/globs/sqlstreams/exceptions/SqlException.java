package org.crossbowlabs.globs.sqlstreams.exceptions;

import org.crossbowlabs.globs.utils.exceptions.GlobsException;

import java.sql.SQLException;

public class SqlException extends GlobsException {
  private SQLException e;

  public SqlException(SQLException e) {
    super(e);
    this.e = e;
  }

  public SqlException(String message) {
    super(message);
  }

  public SqlException(String message, SQLException cause) {
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
