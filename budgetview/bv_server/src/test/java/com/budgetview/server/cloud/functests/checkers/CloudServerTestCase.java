package com.budgetview.server.cloud.functests.checkers;

import junit.framework.TestCase;

public abstract class CloudServerTestCase extends TestCase {

  protected BudgeaChecker budgea;
  protected CloudChecker cloud;

  protected void setUp() throws Exception {
    budgea = new BudgeaChecker();
    budgea.startServer();
    cloud = new CloudChecker();
    cloud.startServer();
  }

  protected void tearDown() throws Exception {
    cloud.stopServer();
    budgea.stopServer();
    budgea = null;
    cloud = null;
  }
}
