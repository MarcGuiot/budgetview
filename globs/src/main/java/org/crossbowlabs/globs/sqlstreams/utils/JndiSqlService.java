package org.crossbowlabs.globs.sqlstreams.utils;

import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.sqlstreams.SqlConnection;
import org.crossbowlabs.globs.sqlstreams.drivers.hsqldb.HsqlConnection;
import org.crossbowlabs.globs.sqlstreams.drivers.mysql.MysqlConnection;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

public class JndiSqlService extends AbstractSqlService {
  private DbFactory dbFactory;
  private GlobModel model;
  private DataSource ds;

  interface DbFactory {
    SqlConnection createConnection(DataSource ds) throws SQLException;
  }

  public JndiSqlService(GlobModel model) {
    this.model = model;
    try {
      Context initCtx = new InitialContext();
      Context envCtx = (Context)initCtx.lookup("java:comp/env");
      ds = (DataSource)envCtx.lookup("jdbc/Categories");
      Connection connection = ds.getConnection();
      String driverName = connection.getMetaData().getDriverName();
      if (driverName.startsWith("MySQL")) {
        dbFactory = new DbFactory() {
          public SqlConnection createConnection(DataSource ds) throws SQLException {
            return new MysqlConnection(ds.getConnection(), JndiSqlService.this);
          }
        };
      }
      if (driverName.startsWith("HSQL")) {
        dbFactory = new DbFactory() {
          public SqlConnection createConnection(DataSource ds) throws SQLException {
            return new HsqlConnection(ds.getConnection(), JndiSqlService.this);
          }
        };
      }
      if (dbFactory == null) {
        throw new UnexpectedApplicationState("no driver for " + driverName);
      }
      connection.close();
    }
    catch (Exception e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public SqlConnection getDb() {
    try {
      return dbFactory.createConnection(ds);
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState(e);
    }
  }

  public void init() {
    Collection<GlobType> all = model.getAll();
    SqlConnection sqlConnection = getDb();
    for (GlobType globType : all) {
      sqlConnection.createTable(globType);
    }
    sqlConnection.commitAndClose();
  }
}
