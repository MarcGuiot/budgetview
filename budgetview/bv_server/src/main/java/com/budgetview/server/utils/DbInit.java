package com.budgetview.server.utils;

import com.budgetview.server.cloud.model.CloudModel;
import com.budgetview.server.cloud.model.CloudUser;
import com.budgetview.server.config.ConfigService;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcGlobsDatabase;
import org.globsframework.utils.directory.Directory;

public class DbInit {
  public static final String DATABASE_URL = "budgetview.database.url";
  public static final String DATABASE_USER = "budgetview.database.user";
  public static final String DATABASE_PASSWORD = "budgetview.database.password";
  public static final String JDBC_HSQLDB = "jdbc:hsqldb:.";

  public static void create(ConfigService configService, Directory directory) {
    directory.add(GlobsDatabase.class, create(configService));
  }

  public static GlobsDatabase create(ConfigService configService) {
    GlobsDatabase database = new JdbcGlobsDatabase(configService.get(DbInit.DATABASE_URL),
                                                   configService.get(DbInit.DATABASE_USER),
                                                   configService.get(DbInit.DATABASE_PASSWORD));
    database.connect().createTables(CloudModel.getAllTypes());
    return database;
  }
}