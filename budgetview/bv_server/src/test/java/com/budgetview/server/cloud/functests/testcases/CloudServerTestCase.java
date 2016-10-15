package com.budgetview.server.cloud.functests.testcases;

import com.budgetview.server.cloud.functests.checkers.BudgeaChecker;
import com.budgetview.server.cloud.functests.checkers.CloudChecker;
import com.budgetview.server.cloud.functests.checkers.CloudMailbox;
import junit.framework.TestCase;

public abstract class CloudServerTestCase extends TestCase {

  protected BudgeaChecker budgea;
  protected CloudChecker cloud;
  protected CloudMailbox mailServer;

  protected void setUp() throws Exception {
    budgea = new BudgeaChecker();
    budgea.startServer();
    cloud = new CloudChecker();
    cloud.startServer();
    mailServer = new CloudMailbox();
    mailServer.start();
  }

  protected void tearDown() throws Exception {
    cloud.stopServer();
    budgea.stopServer();
    mailServer.stop();
    budgea = null;
    cloud = null;
    mailServer = null;
  }
}
