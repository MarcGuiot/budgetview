package com.budgetview.server.cloud.functests.testcases;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.server.cloud.functests.checkers.BudgeaChecker;
import com.budgetview.server.cloud.functests.checkers.CloudChecker;
import com.budgetview.server.cloud.functests.checkers.CloudMailbox;
import com.budgetview.server.cloud.functests.checkers.WebServerTestUtils;
import org.apache.log4j.Logger;

public abstract class CloudDesktopTestCase extends LoggedInFunctionalTestCase {

  private static Logger logger = Logger.getLogger("CloudDesktopTestCase");

  protected BudgeaChecker budgea;
  protected CloudChecker cloud;
  protected CloudMailbox mailbox;

  public void setUp() throws Exception {
    setCurrentDate("2016/08/20");
    super.setUp();

    mailbox = new CloudMailbox();
    mailbox.start();

    budgea = new BudgeaChecker();
    budgea.startServer();

    cloud = new CloudChecker();
    cloud.startServer();
  }

  protected void tearDown() throws Exception {
    System.out.println("\n\n ---------------- tearDown ----------------");
    logger.info("TearDown");

    budgea.stopServer();
    budgea = null;
    cloud.stopServer();
    budgea = null;
    mailbox.stop();
    mailbox = null;

    super.tearDown();

    WebServerTestUtils.waitForPorts(8080, 8085);
  }
}
