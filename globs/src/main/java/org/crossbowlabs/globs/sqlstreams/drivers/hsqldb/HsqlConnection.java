package org.crossbowlabs.globs.sqlstreams.drivers.hsqldb;

import org.crossbowlabs.globs.metamodel.fields.BlobField;
import org.crossbowlabs.globs.sqlstreams.SqlService;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.BlobUpdater;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.JdbcConnection;
import org.crossbowlabs.globs.sqlstreams.drivers.jdbc.impl.SqlFieldCreationVisitor;
import org.crossbowlabs.globs.sqlstreams.utils.StringPrettyWriter;
import org.hsqldb.jdbc.jdbcBlob;
import org.hsqldb.util.DatabaseManagerSwing;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class HsqlConnection extends JdbcConnection {
  public HsqlConnection(Connection connection, SqlService sqlService) {
    super(connection, sqlService, new BlobUpdater() {
      public void setBlob(PreparedStatement preparedStatement, int index, byte[] bytes) throws SQLException {
        preparedStatement.setBlob(index, new jdbcBlob(bytes));
      }
    });
  }

  protected SqlFieldCreationVisitor getFieldVisitorCreator(StringPrettyWriter prettyWriter) {
    return new SqlFieldCreationVisitor(sqlService, prettyWriter) {
      public String getAutoIncrementKeyWord() {
        return "IDENTITY";
      }

      public void visitBlob(BlobField field) throws Exception {
        add("LONGVARBINARY", field);
      }
    };
  }

  public void showDb() {
    Thread thread = new Thread() {

      public void run() {
        try {
          DatabaseManagerSwing.main(new String[0]);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
    };
    thread.setDaemon(true);
    thread.start();
  }
}
