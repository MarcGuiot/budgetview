package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;

public abstract class CloudDesktopTestCase extends LoggedInFunctionalTestCase {

  private static Logger logger = Logger.getLogger("CloudDesktopTestCase");

  protected BudgeaChecker budgea;
  protected CloudChecker cloud;

  public void setUp() throws Exception {
    setCurrentDate("2016/08/20");
    super.setUp();

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

    super.tearDown();

    WebServerTestUtils.waitForPorts(8080, 8085);
  }
}
