package org.designup.picsou.license;

import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;
import org.globsframework.model.GlobList;
import org.globsframework.model.Glob;
import org.globsframework.utils.collections.MultiMap;
import org.designup.picsou.license.model.License;
import org.designup.picsou.license.servlet.NewUserServlet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DuplicateLine {

  public static void main(String[] args) throws IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    System.out.print("database :");
    String databaseUrl = input.readLine();
    System.out.print("user :");
    String user = input.readLine();
    System.out.print("password :");
    String passwd = input.readLine();
    SqlService sqlService = new JdbcSqlService(databaseUrl, user, passwd);

    complete(sqlService.getDb());
  }

  public static void complete(SqlConnection connection) {
    GlobList licences = connection.getQueryBuilder(License.TYPE)
      .selectAll().getQuery().executeAsGlobs();
    MultiMap<String, Glob> map = new MultiMap<String, Glob>();
    for (Glob licence : licences) {
      map.put(licence.get(License.MAIL), licence);
    }
    for (Map.Entry<String, List<Glob>> entry : map.entries()) {
      List<Glob> licencesByUser = entry.getValue();
      if (!licencesByUser.isEmpty()) {
        Glob glob = licencesByUser.get(0);
        String currentCode = glob.get(License.ACTIVATION_CODE);
        SqlRequest sqlRequest = connection.getCreateBuilder(License.TYPE)
          .set(License.ACCESS_COUNT, 1L)
          .set(License.SIGNATURE, glob.get(License.SIGNATURE))
          .set(License.ACTIVATION_CODE, currentCode == null ? glob.get(License.LAST_ACTIVATION_CODE) : currentCode)
          .set(License.MAIL, glob.get(License.MAIL))
          .set(License.TRANSACTION_ID, glob.get(License.TRANSACTION_ID))
          .getRequest();
        for (int i = licencesByUser.size(); i < NewUserServlet.LICENCE_COUNT; i++){
          sqlRequest.run();
        }
        sqlRequest.close();
      }
    }
    connection.commitAndClose();
  }
}