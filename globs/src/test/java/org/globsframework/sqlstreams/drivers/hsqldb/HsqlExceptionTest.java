package org.globsframework.sqlstreams.drivers.hsqldb;

import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.globsframework.sqlstreams.drivers.jdbc.SqlExceptionTest;

public abstract class HsqlExceptionTest extends SqlExceptionTest {
  public SqlConnection getDb() {
    return new JdbcSqlService("jdbc:hsqldb:.", "sa", "").getDb();
  }
}