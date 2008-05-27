package org.crossbowlabs.globs.sqlstreams.drivers.jdbc;

import org.crossbowlabs.globs.sqlstreams.SqlConnection;
import org.crossbowlabs.globs.sqlstreams.drivers.hsqldb.HsqlConnection;
import org.crossbowlabs.globs.sqlstreams.drivers.mysql.MysqlConnection;
import org.crossbowlabs.globs.sqlstreams.utils.AbstractSqlService;
import org.crossbowlabs.globs.utils.exceptions.ItemNotFound;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

public class JdbcSqlService extends AbstractSqlService {
  private Driver driver;
  private String dbName;
  private Properties dbInfo;
  private DbFactory dbFactory;

  public JdbcSqlService(String dbName, String user, String password) {
    this.dbName = dbName;
    dbInfo = new Properties();
    dbInfo.put("user", user);
    dbInfo.put("passwd", password);
    loadDriver();
  }

  interface DbFactory {
    SqlConnection create();
  }

  private void loadDriver() {
    try {
      if (dbName.contains("hsqldb")) {
        driver = (Driver)Class.forName("org.hsqldb.jdbcDriver").newInstance();
        dbFactory = new DbFactory() {
          public SqlConnection create() {
            Connection connection = getConnection();
            try {
              connection.setAutoCommit(false);
            }
            catch (SQLException e) {
              throw new UnexpectedApplicationState(e);
            }
            return new HsqlConnection(connection, JdbcSqlService.this);
          }
        };
      }
      else if (dbName.contains("mysql")) {
        driver = (Driver)Class.forName("com.mysql.jdbc.Driver").newInstance();
        dbFactory = new DbFactory() {
          public SqlConnection create() {
            Connection connection = getConnection();
            try {
              connection.setAutoCommit(false);
            }
            catch (SQLException e) {
              throw new UnexpectedApplicationState(e);
            }

            return new MysqlConnection(connection, JdbcSqlService.this);
          }
        };
      }
    }
    catch (Exception e) {
      throw new ItemNotFound(e);
    }
  }

  public SqlConnection getDb() {
    return dbFactory.create();
  }

  public Connection getConnection() {
    try {
      return driver.connect(dbName, dbInfo);
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("for " + dbInfo.get("user") + " on " + dbName, e);
    }
  }
}
