package org.designup.picsou.server.model;

import junit.framework.TestCase;
import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

public abstract class DbTestCase extends TestCase {
  protected Directory directory = new DefaultDirectory();
  private SqlConnection sqlConnection;
  protected SqlService sqlService;

  protected void setUp() throws Exception {
    super.setUp();
    this.sqlService = new JdbcSqlService("jdbc:hsqldb:mem:.", "sa", "");
    this.sqlConnection = sqlService.getDb();
    for (GlobType globType : ServerModel.get().getAll()) {
      sqlConnection.createTable(globType);
      sqlConnection.emptyTable(globType);
    }
  }

  public SqlConnection getConnection() {
    return sqlConnection;
  }

}

