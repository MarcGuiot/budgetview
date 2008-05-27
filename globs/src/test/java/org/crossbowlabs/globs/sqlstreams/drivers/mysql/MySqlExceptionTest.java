package org.crossbowlabs.globs.sqlstreams.drivers.mysql;

import org.crossbowlabs.globs.sqlstreams.SqlConnection;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.SqlExceptionTest;

public abstract class MySqlExceptionTest extends SqlExceptionTest {
  public SqlConnection getDb() {
    return new JdbcSqlService("jdbc:mysql://localhost/test", "sa", "").getDb();
  }
}
