package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.server.cloud.CloudServer;
import com.budgetview.server.cloud.services.EmailValidationService;
import com.budgetview.shared.cloud.CloudConstants;

import java.util.Date;

public class CloudChecker {


  private CloudServer cloudServer;

  public void startServer() throws Exception {
    cloudServer = new CloudServer("budgetview/bv_server/dev/config/bv_cloud_test.properties");
    cloudServer.init();
    cloudServer.start();
  }

  public void forceTokenExpirationDate(final Date date) {
    EmailValidationService.forceTokenExpirationDate(date);
  }

  public void stopServer() throws Exception {
    cloudServer.resetDatabase();
    cloudServer.stop();
  }
}
