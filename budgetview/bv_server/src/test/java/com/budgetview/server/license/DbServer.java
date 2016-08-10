package com.budgetview.server.license;

import com.budgetview.server.license.model.License;
import com.budgetview.server.license.model.MailError;
import com.budgetview.server.license.model.RepoInfo;
import com.budgetview.server.license.model.SoftwareInfo;
import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcGlobsDatabase;
import org.hsqldb.Server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DbServer {

  public static void main(String[] args) throws InterruptedException, SQLException {
    Server server = new Server();
    server.setDatabasePath(1, "/home/guiot/.picsouDb/");
    server.setDatabaseName(1, "budgetview");
    server.start();
    GlobsDatabase globsDB = new JdbcGlobsDatabase("jdbc:hsqldb:hsql://localhost/picsou", "sa", "");
    SqlConnection connection = globsDB.connect();
    ResultSet set = connection.getInnerConnection().prepareStatement("SELECT * FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE 1=1 AND " +
                                                                     "  TABLE_TYPE IN ('TABLE','GLOBAL TEMPORARY','VIEW')")
      .executeQuery();
    Set<String> existingTables = new HashSet<String>();
    while (set.next()) {
      existingTables.add(set.getString("TABLE_NAME"));
    }
    connection.commitAndClose();
    createIfNeeded(globsDB, existingTables, License.TYPE, MailError.TYPE, RepoInfo.TYPE, SoftwareInfo.TYPE);
  }

  private static void createIfNeeded(GlobsDatabase globsDB, Set<String> tables, GlobType... globType) {
    SqlConnection db = globsDB.connect();
    for (GlobType type : globType) {
      if (!tables.contains(globsDB.getTableName(type))) {
        db.createTable(type);
      }
    }
    db.commitAndClose();
  }
}
