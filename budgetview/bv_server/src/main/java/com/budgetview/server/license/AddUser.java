package com.budgetview.server.license;

import com.budgetview.server.license.model.License;
import com.budgetview.server.license.generator.LicenseGenerator;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcGlobsDatabase;

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
      System.out.print("globsDB :");
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
    GlobsDatabase globsDB = new JdbcGlobsDatabase(databaseUrl, user, passwd);
    SqlConnection db = globsDB.connect();
    for (String arg : arguments) {
      String code = LicenseGenerator.generateActivationCode();
      db.startCreate(License.TYPE)
        .set(License.MAIL, arg)
        .set(License.ACTIVATION_CODE, code)
        .run();
      db.startCreate(License.TYPE)
        .set(License.MAIL, arg)
        .set(License.ACTIVATION_CODE, code)
        .run();
      System.out.println("code d'activation : " + code);
    }
    db.commitAndClose();
  }
}
