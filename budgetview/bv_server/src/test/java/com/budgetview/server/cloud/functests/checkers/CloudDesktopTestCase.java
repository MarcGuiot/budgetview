package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import org.apache.log4j.Logger;

public abstract class CloudDesktopTestCase extends LoggedInFunctionalTestCase {

  private static Logger logger = Logger.getLogger("CloudDesktopTestCase");

  private BudgeaChecker budgea;
  private CloudChecker cloud;

  public void setUp() throws Exception {
    super.setUp();

    budgea = new BudgeaChecker();
    budgea.startServer();

    cloud = new CloudChecker();
    cloud.startServer();
  }

  protected void tearDown() throws Exception {
    logger.info("TearDown\n");

    budgea.stopServer();
    budgea = null;
    cloud.stopServer();
    budgea = null;

    super.tearDown();
  }
}
