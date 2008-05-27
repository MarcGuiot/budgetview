package org.crossbowlabs.globs.sqlstreams.metadata;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.sqlstreams.SqlConnection;
import org.crossbowlabs.globs.sqlstreams.SqlService;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;

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
