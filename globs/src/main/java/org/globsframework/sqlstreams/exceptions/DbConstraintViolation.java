package org.globsframework.sqlstreams.exceptions;

import java.sql.SQLException;

public class DbConstraintViolation extends GlobsSQLException {
  public DbConstraintViolation(SQLException e) {
    super(e);
  }
}
