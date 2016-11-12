package com.budgetview.server.cloud.tools;

import com.budgetview.server.config.ConfigService;
import com.budgetview.server.cloud.utils.CloudDb;

public class CreateDb {
  public static void main(String[] args) throws Exception {
    ConfigService.checkCommandLine(args);
    ConfigService configService = new ConfigService(args);
    CloudDb.create(configService);
  }
}
