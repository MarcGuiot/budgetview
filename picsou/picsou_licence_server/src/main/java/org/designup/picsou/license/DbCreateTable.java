package org.designup.picsou.license;

import org.designup.picsou.license.model.SoftwareInfo;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.model.MailError;
import org.designup.picsou.license.model.RepoInfo;
import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;

import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class DbCreateTable {

  static GlobType TYPE[] = {License.TYPE, MailError.TYPE, RepoInfo.TYPE, SoftwareInfo.TYPE};

  public static void main(String[] args) throws InterruptedException, SQLException, IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    System.out.print("database :");
    String databaseUrl = input.readLine();
    System.out.print("user :");
    String user = input.readLine();
    System.out.print("password :");
    String passwd = input.readLine();
    SqlService sqlService = new JdbcSqlService(databaseUrl, user, passwd);

    SqlConnection connection = sqlService.getDb();
    for (GlobType type : TYPE) {
        connection.createTable(type);
    }
    connection.commitAndClose();
  }
}