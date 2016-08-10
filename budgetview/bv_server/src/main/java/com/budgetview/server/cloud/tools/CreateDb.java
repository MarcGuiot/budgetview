package com.budgetview.server.cloud.tools;

import com.budgetview.server.cloud.model.CloudModel;
import com.budgetview.server.config.ConfigService;
import com.budgetview.server.utils.DbInit;
import org.globsframework.sqlstreams.GlobsDatabase;

public class CreateDb {
  public static void main(String[] args) throws Exception {
    ConfigService.checkCommandLine(args);
    ConfigService configService = new ConfigService(args);
    GlobsDatabase globsDB = DbInit.create(configService);
    globsDB.connect().createTable(CloudModel.getAllTypes());
  }
}
