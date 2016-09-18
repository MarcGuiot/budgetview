package com.budgetview.server.utils;

import com.budgetview.server.cloud.model.CloudDatabaseModel;
import com.budgetview.server.config.ConfigService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.drivers.jdbc.JdbcGlobsDatabase;
import org.globsframework.utils.directory.Directory;

public class DbInit {
  public static final String DATABASE_URL = "budgetview.database.url";
  public static final String DATABASE_USER = "budgetview.database.user";
  public static final String DATABASE_PASSWORD = "budgetview.database.password";
  public static final String JDBC_HSQLDB = "jdbc:hsqldb:.";

  public static GlobsDatabase create(ConfigService configService, Directory directory) {
    GlobsDatabase db = create(configService);
    directory.add(GlobsDatabase.class, db);
    return db;
  }

  public static GlobsDatabase create(ConfigService configService) {
    GlobsDatabase database = new JdbcGlobsDatabase(configService.get(DbInit.DATABASE_URL),
                                                   configService.get(DbInit.DATABASE_USER),
                                                   configService.get(DbInit.DATABASE_PASSWORD));
    database.connect().createTables(CloudDatabaseModel.getAllTypes());
    return database;
  }

  public static void cleanAllTables(GlobsDatabase database) {
    SqlConnection connection = database.connect();
    connection.emptyTable(CloudDatabaseModel.getAllTypes());
    connection.commitAndClose();
  }
}
