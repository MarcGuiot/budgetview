package org.globsframework.sqlstreams.metadata;

import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.utils.exceptions.InvalidData;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbChecker {
  private DatabaseMetaData metaData;
  private SqlService sqlService;

  public DbChecker(SqlService sqlService, SqlConnection sqlConnection) {
    this.sqlService = sqlService;
    metaData = getMetaData(sqlConnection);
  }

  private static DatabaseMetaData getMetaData(SqlConnection sqlConnection) {
    try {
      return sqlConnection.getConnection().getMetaData();
    }
    catch (SQLException e) {
      throw new InvalidData(e);
    }
  }

  public boolean tableExists(GlobType globType) {
    try {
      String[] names = {"TABLE"};
      ResultSet tableNames = metaData.getTables(null, null, "%", names);
      String tableName = sqlService.getTableName(globType);
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
