package org.globsframework.sqlstreams.exceptions;

import java.sql.SQLException;

public class DbConstraintViolation extends GlobsSqlException {
  public DbConstraintViolation(SQLException e) {
    super(e);
  }
}
