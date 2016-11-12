package com.budgetview.server.cloud.utils;

import com.budgetview.server.cloud.model.CloudDatabaseModel;
import com.budgetview.server.config.ConfigService;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcGlobsDatabase;
import org.globsframework.utils.directory.Directory;

public class CloudDb {
  public static final String DATABASE_URL = "budgetview.database.url";
  public static final String DATABASE_USER = "budgetview.database.user";
  public static final String DATABASE_PASSWORD = "budgetview.database.password";

  public static GlobsDatabase create(ConfigService configService) {
    GlobsDatabase database = new JdbcGlobsDatabase(configService.get(DATABASE_URL),
                                                   configService.get(DATABASE_USER),
                                                   configService.get(DATABASE_PASSWORD));
    SqlConnection connection = database.connect();
    connection.createTables(CloudDatabaseModel.getAllTypes());
    connection.commitAndClose();
    return database;
  }

  public static void cleanAllTables(Directory directory) {
    cleanAllTables(directory.get(GlobsDatabase.class));
  }

  public static void cleanAllTables(GlobsDatabase database) {
    SqlConnection connection = database.connect();
    connection.emptyTable(CloudDatabaseModel.getAllTypes());
    connection.commitAndClose();
  }

  public static boolean isJDBC(String databaseUrl) {
    return databaseUrl.startsWith("jdbc:");
  }
}
