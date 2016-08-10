package org.globsframework.sqlstreams.metadata;

import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.utils.exceptions.InvalidData;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbChecker {
  private DatabaseMetaData metaData;
  private GlobsDatabase globsDB;

  public DbChecker(GlobsDatabase globsDB, SqlConnection connection) {
    this.globsDB = globsDB;
    metaData = getMetaData(connection);
  }

  private static DatabaseMetaData getMetaData(SqlConnection connection) {
    try {
      return connection.getInnerConnection().getMetaData();
    }
    catch (SQLException e) {
      throw new InvalidData(e);
    }
  }

  public boolean tableExists(GlobType globType) {
    try {
      String[] names = {"TABLE"};
      ResultSet tableNames = metaData.getTables(null, null, "%", names);
      String tableName = globsDB.getTableName(globType);
      while (tableNames.next()) {
        if (tableName.equals(tableNames.getString("TABLE_NAME"))) {
          return true;
        }
      }
    }
    catch (SQLException e) {
      throw new InvalidData(e);
    }
    return false;
  }
}
