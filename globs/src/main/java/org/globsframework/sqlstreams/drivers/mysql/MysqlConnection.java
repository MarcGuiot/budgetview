package org.globsframework.sqlstreams.drivers.mysql;

import org.globsframework.metamodel.fields.BlobField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.drivers.jdbc.impl.BlobUpdater;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcConnection;
import org.globsframework.sqlstreams.drivers.jdbc.impl.SqlFieldCreationVisitor;
import org.globsframework.sqlstreams.utils.StringPrettyWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MysqlConnection extends JdbcConnection {
  public MysqlConnection(Connection connection, GlobsDatabase globsDB) {
    super(connection, globsDB, new BlobUpdater() {
      public void setBlob(PreparedStatement preparedStatement, int index, byte[] bytes) throws SQLException {
        preparedStatement.setBytes(index, bytes);
      }
    });
  }

  protected SqlFieldCreationVisitor getFieldVisitorCreator(StringPrettyWriter prettyWriter) {
    return new SqlFieldCreationVisitor(db, prettyWriter) {

      public void visitString(StringField field) throws Exception {
        add(field, "VARCHAR(" + field.getMaxSize() + ")");
      }

      public void visitBlob(BlobField field) throws Exception {
        add(field, "LONGBLOB");
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
