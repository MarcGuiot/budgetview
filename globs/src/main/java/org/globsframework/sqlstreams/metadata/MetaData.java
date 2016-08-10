package org.globsframework.sqlstreams.metadata;

import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.utils.exceptions.InvalidData;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MetaData {
  private GlobsDatabase db;
  private DatabaseMetaData metaData;

  public MetaData(GlobsDatabase db, SqlConnection connection) {
    this.db = db;
    this.metaData = getMetaData(connection);
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
      String tableName = db.getTableName(globType);
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
