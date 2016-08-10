package com.budgetview.server.license;

import com.budgetview.server.license.model.License;
import com.budgetview.server.license.model.MailError;
import com.budgetview.server.license.model.RepoInfo;
import com.budgetview.server.license.model.SoftwareInfo;
import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcGlobsDatabase;

import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class DbCreateTable {

  static GlobType TYPE[] = {License.TYPE, MailError.TYPE, RepoInfo.TYPE, SoftwareInfo.TYPE};

  public static void main(String[] args) throws InterruptedException, SQLException, IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    System.out.print("globsDB :");
    String databaseUrl = input.readLine();
    System.out.print("user :");
    String user = input.readLine();
    System.out.print("password :");
    String passwd = input.readLine();
    GlobsDatabase globsDB = new JdbcGlobsDatabase(databaseUrl, user, passwd);

    SqlConnection connection = globsDB.connect();
    for (GlobType type : TYPE) {
        connection.createTable(type);
    }
    connection.commitAndClose();
  }
}