package com.budgetview.cloud.functests.checkers;

import com.budgetview.server.cloud.CloudServer;
import com.budgetview.shared.cloud.BudgeaConstants;
import com.budgetview.shared.cloud.CloudConstants;
import junit.framework.TestCase;

public class CloudTestCase extends TestCase {

  protected BudgeaChecker budgea;
  protected CloudChecker cloud;

  private CloudServer cloudServer;

  protected void setUp() throws Exception {

    System.setProperty(BudgeaConstants.SERVER_URL_PROPERTY, BudgeaConstants.LOCAL_SERVER_URL);
    System.setProperty(CloudConstants.CLOUD_URL_PROPERTY, CloudConstants.LOCAL_SERVER_URL);

    budgea = new BudgeaChecker();
    budgea.startServer();

    cloud = new CloudChecker();

    cloudServer = new CloudServer("budgetview/bv_server/dev/config/bv_cloud_test.properties");
    cloudServer.init();
    cloudServer.start();
  }

  protected void tearDown() throws Exception {
    cloudServer.stop();
    budgea.stopServer();
    budgea = null;
    cloud = null;
  }
}
