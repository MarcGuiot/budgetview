package com.budgetview.server.license.tools;

import com.budgetview.server.config.ConfigService;
import com.budgetview.server.license.model.License;
import com.budgetview.server.license.model.MailError;
import com.budgetview.server.license.model.RepoInfo;
import com.budgetview.server.license.model.SoftwareInfo;
import com.budgetview.server.license.utils.LicenseDb;
import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcGlobsDatabase;

import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class CreateLicenseTables {

  public static void main(String[] args) throws InterruptedException, SQLException, IOException {
    ConfigService.checkCommandLine(args);
    ConfigService configService = new ConfigService(args[0]);
    LicenseDb.create(configService);
  }
}