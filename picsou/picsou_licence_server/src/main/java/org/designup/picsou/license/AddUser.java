package org.designup.picsou.license;

import org.designup.picsou.license.model.License;
import org.designup.picsou.license.generator.LicenseGenerator;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AddUser {
  public static void main(String[] args) throws IOException {
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    System.out.print("database :");
    String databaseUrl = input.readLine();
    System.out.print("user :");
    String user = input.readLine();
    System.out.print("password :");
    String passwd = input.readLine();
    SqlService sqlService = new JdbcSqlService(databaseUrl, user, passwd);
    SqlConnection db = sqlService.getDb();
    for (String arg : args) {
      String code = LicenseGenerator.generateActivationCode();
      db.getCreateBuilder(License.TYPE)
        .set(License.MAIL, arg)
        .set(License.ACTIVATION_CODE, code)
        .getRequest()
        .run();
      System.out.println("code d'activation : " + code);
    }
    db.commitAndClose();
  }
}
