package org.globsframework.sqlstreams.drivers.hsqldb;

import org.globsframework.metamodel.fields.BlobField;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.drivers.jdbc.impl.BlobUpdater;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcConnection;
import org.globsframework.sqlstreams.drivers.jdbc.impl.SqlFieldCreationVisitor;
import org.globsframework.sqlstreams.utils.StringPrettyWriter;
import org.hsqldb.jdbc.jdbcBlob;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HsqlConnection extends JdbcConnection {
  public HsqlConnection(Connection connection, GlobsDatabase globsDB) {
    super(connection, globsDB, new BlobUpdater() {
      public void setBlob(PreparedStatement preparedStatement, int index, byte[] bytes) throws SQLException {
        preparedStatement.setBlob(index, new jdbcBlob(bytes));
      }
    });
  }

  protected SqlFieldCreationVisitor getFieldVisitorCreator(StringPrettyWriter prettyWriter) {
    return new SqlFieldCreationVisitor(globsDB, prettyWriter) {
      public String getAutoIncrementKeyWord() {
        return "IDENTITY";
      }

      public void visitBlob(BlobField field) throws Exception {
        add("LONGVARBINARY", field);
      }
    };
  }

}
