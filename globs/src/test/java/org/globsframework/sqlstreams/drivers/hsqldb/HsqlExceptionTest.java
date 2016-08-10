package org.globsframework.sqlstreams.drivers.hsqldb;

import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcGlobsDatabase;
import org.globsframework.sqlstreams.drivers.jdbc.SqlExceptionTest;

public class HsqlExceptionTest extends SqlExceptionTest {

  public SqlConnection getDbConnection() {
    return new JdbcGlobsDatabase("jdbc:hsqldb:.", "sa", "").connect();
  }
}