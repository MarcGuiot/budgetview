package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.CloudServer;
import com.budgetview.server.cloud.model.CloudDatabaseModel;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.server.cloud.utils.CloudDb;
import com.budgetview.shared.cloud.CloudConstants;
import org.globsframework.metamodel.GlobType;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;

import java.util.Date;

public class CloudChecker {

  private CloudServer cloudServer;

  public void startServer(final PaymentChecker payments) throws Exception {
    cloudServer = new CloudServer("budgetview/bv_server/dev/config/bv_cloud_test.properties") {
      protected Directory createDirectory() throws Exception {
        Directory directory = new DefaultDirectory(super.createDirectory());
        payments.install(directory);
        return directory;
      }
    };
    cloudServer.init();
    cloudServer.start();
  }

  public void forceTokenExpirationDate(final Date date) {
    EmailValidationService.forceTokenExpirationDate(date);
  }

  public void cleanUpDatabase() {
    GlobsDatabase db = cloudServer.getDirectory().get(GlobsDatabase.class);
    SqlConnection connection = db.connect();
    for (GlobType type : CloudDatabaseModel.getAllTypes()) {
      connection.startDelete(type).execute();
    }
    connection.commit();
  }

  public void stopServer() throws Exception {
    cloudServer.resetDatabase();
    cloudServer.stop();
  }
}
