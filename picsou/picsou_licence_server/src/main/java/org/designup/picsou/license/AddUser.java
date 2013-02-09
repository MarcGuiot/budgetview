package org.designup.picsou.license;

import org.designup.picsou.license.generator.LicenseGenerator;
import org.designup.picsou.license.model.License;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcSqlService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AddUser {
  public static void main(String... args) throws IOException {
    String databaseUrl = null;
    String user = null;
    String passwd = null;
    List<String> arguments = new ArrayList(Arrays.asList(args));
    for (Iterator<String> iterator = arguments.iterator(); iterator.hasNext();) {
      String element = iterator.next();
      if (element.equals("-d") && iterator.hasNext()) {
        iterator.remove();
        databaseUrl = iterator.next();
        iterator.remove();
      }
      if (element.equals("-u") && iterator.hasNext()) {
        iterator.remove();
        user = iterator.next();
        iterator.remove();
      }
      if (element.equals("-p") && iterator.hasNext()) {
        iterator.remove();
        passwd = iterator.next();
        iterator.remove();
      }
    }
    BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
    if (databaseUrl == null) {
      System.out.print("database :");
      databaseUrl = input.readLine();
    }
    if (user == null) {
      System.out.print("user :");
      user = input.readLine();
    }
    if (passwd == null) {
      System.out.print("password :");
      passwd = input.readLine();
    }
    SqlService sqlService = new JdbcSqlService(databaseUrl, user, passwd);
    SqlConnection db = sqlService.getDb();
    for (String arg : arguments) {
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
