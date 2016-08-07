package com.budgetview.cloud.functests.utils;

import com.budgetview.server.cloud.CloudServer;
import junit.framework.TestCase;

public class CloudTestCase extends TestCase {

  protected BudgeaChecker budgea;

  private CloudServer cloudServer;

  protected void setUp() throws Exception {

    budgea = new BudgeaChecker();

    cloudServer = new CloudServer("budgetview/bv_server/dev/config/bv_cloud_test.properties");
    cloudServer.init();
    cloudServer.start();
  }

  protected void tearDown() throws Exception {
    cloudServer.stop();
  }
}
