package com.budgetview.server.cloud.functests.testcases;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.server.cloud.functests.checkers.*;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.license.LicenseConstants;
import org.apache.log4j.Logger;

public abstract class CloudDesktopTestCase extends LoggedInFunctionalTestCase {

  private static Logger logger = Logger.getLogger("CloudDesktopTestCase");

  protected BudgeaChecker budgea;
  protected CloudLicenseChecker cloudLicense;
  protected CloudChecker cloud;
  protected CloudMailbox mailbox;

  public void setUp() throws Exception {
    setCurrentDate("2016/08/20");
    super.setUp();
    System.clearProperty(LicenseConstants.LICENSE_URL_PROPERTY);
    System.setProperty(CloudConstants.CLOUD_URL_PROPERTY, CloudConstants.LOCAL_SERVER_URL);

    mailbox = new CloudMailbox();
    mailbox.start();

    budgea = new BudgeaChecker();
    budgea.startServer();

    cloudLicense = new CloudLicenseChecker();
    cloudLicense.startServer();

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
    cloudLicense.stopServer();
    cloudLicense = null;
    mailbox.stop();
    mailbox = null;

    super.tearDown();

    WebServerTestUtils.waitForPorts(8080, 8085);
  }
}
