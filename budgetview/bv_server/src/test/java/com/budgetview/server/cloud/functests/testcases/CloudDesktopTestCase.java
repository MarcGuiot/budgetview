package com.budgetview.server.cloud.functests.testcases;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.server.cloud.functests.checkers.*;
import com.budgetview.server.cloud.utils.WebsiteUrls;
import com.budgetview.shared.cloud.CloudConstants;
import com.budgetview.shared.license.LicenseConstants;
import org.apache.log4j.Logger;

public abstract class CloudDesktopTestCase extends LoggedInFunctionalTestCase {

  private static Logger logger = Logger.getLogger("CloudDesktopTestCase");

  protected BudgeaChecker budgea;
  protected CloudChecker cloud;
  protected CloudMailbox mailbox;
  protected SubscriptionChecker subscriptions;
  protected PaymentChecker payments;
  protected WebsiteChecker website;

  public void setUp() throws Exception {
    createDefaultSeries = true;
    setCurrentDate("2016/08/20");
    super.setUp();
    System.clearProperty(LicenseConstants.LICENSE_URL_PROPERTY);
    System.setProperty(CloudConstants.CLOUD_URL_PROPERTY, CloudConstants.LOCAL_SERVER_URL);
    System.setProperty(WebsiteUrls.WEBSITE_URL_PROPERTY, WebsiteUrls.LOCAL_SERVER_URL);

    mailbox = new CloudMailbox();
    mailbox.start();

    budgea = new BudgeaChecker();
    budgea.startServer();

    payments = new PaymentChecker();

    cloud = new CloudChecker();
    cloud.startServer(payments);

    website = new WebsiteChecker();
    website.startServer();

    subscriptions = new SubscriptionChecker();
  }

  protected void tearDown() throws Exception {
    System.err.flush();
    System.out.flush();
    System.out.println("\n\n ---------------- tearDown ----------------");
    logger.info("TearDown");

    budgea.stopServer();
    budgea = null;
    cloud.stopServer();
    cloud = null;
    mailbox.stop();
    mailbox = null;
    subscriptions = null;
    website.stopServer();
    website = null;

    super.tearDown();

    WebServerTestUtils.waitForPorts(8080, 8085);
  }
}
