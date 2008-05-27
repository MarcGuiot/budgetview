package org.crossbowlabs.globs.sqlstreams.drivers.mysql;

import org.crossbowlabs.globs.metamodel.fields.StringField;
import org.crossbowlabs.globs.sqlstreams.SqlService;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.BlobUpdater;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.JdbcConnection;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.impl.SqlFieldCreationVisitor;
import org.crossbowlabs.globs.sqlstreams.utils.StringPrettyWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MysqlConnection extends JdbcConnection {
  public MysqlConnection(Connection connection, SqlService sqlService) {
    super(connection, sqlService, new BlobUpdater() {
      public void setBlob(PreparedStatement preparedStatement, int index, byte[] bytes) throws SQLException {
        preparedStatement.setBytes(index, bytes);
      }
    });
  }

  protected SqlFieldCreationVisitor getFieldVisitorCreator(StringPrettyWriter prettyWriter) {
    return new SqlFieldCreationVisitor(sqlService, prettyWriter) {

      public void visitString(StringField field) throws Exception {
        add("VARCHAR(" + field.getMaxSize() + ")", field);
      }

      public String getAutoIncrementKeyWord() {
        return "AUTO_INCREMENT";
      }
    };

  }

  protected boolean isRollbackSQLState(SQLException e) {
    return e.getErrorCode() == 1099 && "HY000".equals(e.getSQLState());
  }
}
