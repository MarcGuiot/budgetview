package com.budgetview.server.license.tools;

import com.budgetview.server.config.ConfigService;
import com.budgetview.server.license.utils.LicenseDb;

public class CreateLicenseTables {

  public static void main(String[] args) {
    ConfigService.checkCommandLine(args);
    ConfigService configService = new ConfigService(args[0]);
    LicenseDb.create(configService);
  }
}