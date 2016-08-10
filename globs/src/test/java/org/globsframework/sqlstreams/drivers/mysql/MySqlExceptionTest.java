package org.globsframework.sqlstreams.drivers.mysql;

import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcGlobsDatabase;
import org.globsframework.sqlstreams.drivers.jdbc.SqlExceptionTest;

public abstract class MySqlExceptionTest extends SqlExceptionTest {
  public SqlConnection getDbConnection() {
    return new JdbcGlobsDatabase("jdbc:mysql://localhost/test", "sa", "").connect();
  }
}
