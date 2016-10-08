package com.budgetview.server.cloud.functests.checkers;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.server.license.checkers.Email;
import com.budgetview.server.license.checkers.MailServerChecker;
import org.apache.log4j.Logger;
import org.junit.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class CloudDesktopTestCase extends LoggedInFunctionalTestCase {

  private static Logger logger = Logger.getLogger("CloudDesktopTestCase");

  protected BudgeaChecker budgea;
  protected CloudChecker cloud;
  protected MailServerChecker mailServer;

  public void setUp() throws Exception {
    setCurrentDate("2016/08/20");
    super.setUp();

    mailServer = new MailServerChecker();
    mailServer.start();

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
    mailServer.stop();
    mailServer = null;

    super.tearDown();

    WebServerTestUtils.waitForPorts(8080, 8085);
  }

  private final Pattern CODE_PATTERN = Pattern.compile(".*<span id=['\"]code['\"]>([A-z0-9]+)</span>.*");

  protected String getVerificationCode(String mailTo) throws InterruptedException {

    Email email = mailServer.checkReceivedMail(mailTo);
    String content = email.getContent();

    Matcher matcher = CODE_PATTERN.matcher(content);
    if (!matcher.matches()) {
      Assert.fail("Email does not contain any code: " + content);
    }

    return matcher.group(1);
  }

}
