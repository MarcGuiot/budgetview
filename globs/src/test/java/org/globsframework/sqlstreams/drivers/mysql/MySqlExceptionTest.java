package org.globsframework.sqlstreams.drivers.mysql;

import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.globsframework.sqlstreams.drivers.jdbc.SqlExceptionTest;

public abstract class MySqlExceptionTest extends SqlExceptionTest {
  public SqlConnection getDb() {
    return new JdbcSqlService("jdbc:mysql://localhost/test", "sa", "").getDb();
  }
}
