package org.globsframework.sqlstreams.drivers.jdbc;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.drivers.hsqldb.HsqlConnection;
import org.globsframework.sqlstreams.drivers.mysql.MysqlConnection;
import org.globsframework.sqlstreams.utils.AbstractGlobsDatabase;
import org.globsframework.utils.Strings;
import org.globsframework.utils.exceptions.ItemNotFound;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JdbcGlobsDatabase extends AbstractGlobsDatabase {
  private static Map<String, Driver> loadedDrivers = new HashMap<String, Driver>();
  private Driver driver;
  private String url;
  private Properties dbInfo;
  private DbFactory dbFactory;

  public JdbcGlobsDatabase(String url, String user, String password) {
    this.url = url;
    dbInfo = new Properties();
    dbInfo.put("user", user);
    dbInfo.put("password", password);
    loadDriver();
  }

  interface DbFactory {
    SqlConnection create();

    String toSqlName(String name);
  }

  private static final String[] RESERVED_KEYWORDS = {
    "COUNT", "WHERE"
  };

  public String getTableName(GlobType globType) {
    return dbFactory.toSqlName(globType.getName());
  }

  public String getColumnName(Field field) {
    return dbFactory.toSqlName(field.getName());
  }

  private void loadDriver() {
    try {
      if (url.contains("hsqldb")) {
        if (!loadedDrivers.containsKey("hsqldb")) {
          driver = (Driver) Class.forName("org.hsqldb.jdbcDriver").newInstance();
        }
        dbFactory = new DbFactory() {
          public SqlConnection create() {
            Connection connection = getConnection();
            try {
              connection.setAutoCommit(false);
            }
            catch (SQLException e) {
              throw new UnexpectedApplicationState(e);
            }
            return new HsqlConnection(connection, JdbcGlobsDatabase.this);
          }

          public String toSqlName(String name) {
            String upper = Strings.toNiceUpperCase(name);
            for (String keyword : RESERVED_KEYWORDS) {
              if (upper.equals(keyword)) {
                return upper + "_FIELD";
              }
            }
            return upper;
          }
        };
      }
      else if (url.contains("mysql")) {
        if (!loadedDrivers.containsKey("mysql")) {
          driver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
        }
        // dbInfo.put("autoReconnect", Boolean.TRUE);
        dbFactory = new DbFactory() {
          public SqlConnection create() {
            Connection connection = getConnection();
            try {
              connection.setAutoCommit(false);
            }
            catch (SQLException e) {
              throw new UnexpectedApplicationState(e);
            }

            return new MysqlConnection(connection, JdbcGlobsDatabase.this);
          }

          public String toSqlName(String name) {
            String upper = Strings.toNiceUpperCase(name);
            for (String keyword : RESERVED_KEYWORDS) {
              if (upper.equals(keyword)) {
                return "_" + upper + "_";
              }
            }
            return upper;
          }
        };
      }
    }
    catch (Exception e) {
      throw new ItemNotFound(e);
    }
  }

  public SqlConnection connect() {
    return dbFactory.create();
  }

  synchronized private Connection getConnection() {
    try {
      return driver.connect(url, dbInfo);
    }
    catch (SQLException e) {
      throw new UnexpectedApplicationState("for " + dbInfo.get("user") + " on " + url, e);
    }
  }
}
