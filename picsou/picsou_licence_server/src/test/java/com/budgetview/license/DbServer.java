package com.budgetview.license;

import com.budgetview.license.model.License;
import com.budgetview.license.model.MailError;
import com.budgetview.license.model.RepoInfo;
import com.budgetview.license.model.SoftwareInfo;
import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
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
    SqlService sqlService = new JdbcSqlService("jdbc:hsqldb:hsql://localhost/picsou", "sa", "");
    SqlConnection db = sqlService.getDb();
    ResultSet set = db.getConnection().prepareStatement("SELECT * FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE 1=1 AND " +
                                                        "  TABLE_TYPE IN ('TABLE','GLOBAL TEMPORARY','VIEW')")
      .executeQuery();
    Set<String> existingTables = new HashSet<String>();
    while (set.next()) {
      existingTables.add(set.getString("TABLE_NAME"));
    }
    db.commitAndClose();
    createIfNeeded(sqlService, existingTables, License.TYPE, MailError.TYPE, RepoInfo.TYPE, SoftwareInfo.TYPE);
  }

  private static void createIfNeeded(SqlService sqlService, Set<String> tables, GlobType... globType) {
    SqlConnection db = sqlService.getDb();
    for (GlobType type : globType) {
      if (!tables.contains(sqlService.getTableName(type))) {
        db.createTable(type);
      }
    }
    db.commitAndClose();
  }
}
