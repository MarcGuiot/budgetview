package com.budgetview.server.license.tools;

import com.budgetview.server.config.ConfigService;
import com.budgetview.server.license.model.License;
import com.budgetview.server.license.generator.LicenseGenerator;
import com.budgetview.server.license.utils.LicenseDb;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcGlobsDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class AddUser {
  public static void main(String... args) throws IOException {
    if (args.length < 2) {
      System.out.println("Usage: AddUser <config_file> <email_address> <email_address> ...");
      return;
    }

    ConfigService configService = new ConfigService(args[0]);
    GlobsDatabase globsDB = LicenseDb.get(configService);
    SqlConnection db = globsDB.connect();
    for (int i = 1; i < args.length; i++) {
      String email = args[i];
      String code = LicenseGenerator.generateActivationCode();
      db.startCreate(License.TYPE)
        .set(License.MAIL, email)
        .set(License.ACTIVATION_CODE, code)
        .run();
      db.startCreate(License.TYPE)
        .set(License.MAIL, email)
        .set(License.ACTIVATION_CODE, code)
        .run();
      System.out.println("Activation code for " + email + ": " + code);
    }
    db.commitAndClose();
  }
}
