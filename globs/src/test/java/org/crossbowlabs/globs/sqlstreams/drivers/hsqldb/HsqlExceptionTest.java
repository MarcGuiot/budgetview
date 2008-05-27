package org.crossbowlabs.globs.sqlstreams.drivers.hsqldb;

import org.crossbowlabs.globs.sqlstreams.SqlConnection;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.SqlExceptionTest;

public abstract class HsqlExceptionTest extends SqlExceptionTest {
  public SqlConnection getDb() {
    return new JdbcSqlService("jdbc:hsqldb:.", "sa", "").getDb();
  }
}