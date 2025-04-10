package org.globsframework.sqlstreams.exceptions;

import java.sql.SQLException;

public class ConstraintViolation extends GlobsSqlException {
  public ConstraintViolation(String sql, SQLException e) {
    super(sql, e);
  }

  public ConstraintViolation(SQLException e) {
    super(e);
  }
}
