package org.designup.picsou.license.functests;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.FeedbackDialogChecker;
import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.functests.checkers.StartupChecker;
import org.designup.picsou.license.ConnectedTestCase;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.config.ConfigService;
import org.globsframework.utils.Files;
import org.uispec4j.Window;

import java.io.File;
import java.net.URLEncoder;

/* Fenetre de feedback debranchee pour le moment */
public abstract class FeedbackTest extends ConnectedTestCase {
  private Window window;
  private PicsouApplication picsouApplication;
  private int previousRetry;

  protected void setUp() throws Exception {
    previousRetry = ConfigService.RETRY_PERIOD;
    ConfigService.RETRY_PERIOD = 500;
    super.setUp();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    window.dispose();
    picsouApplication.shutdown();
    window = null;
    picsouApplication = null;
    ConfigService.RETRY_PERIOD = previousRetry;
  }

  private void startApplication() {
    StartupChecker startupChecker = new StartupChecker();
    window = startupChecker.enterMain();
    picsouApplication = startupChecker.getApplication();
  }

  private FeedbackDialogChecker openFeedback() {
    OperationChecker operation = new OperationChecker(window);
    return operation.openFeedback();
  }

  public void testNotConnected() throws Exception {
    startApplication();
    FeedbackDialogChecker feedback = openFeedback();
    feedback.checkNotConnected();

    licenseServer.init();
    startServers();

    feedback.checkConnected();
    feedback.cancel();
  }

  public void testSendFeedback() throws Exception {
    licenseServer.init();
    startServers();
    startApplication();

    openFeedback().send("my title", "me@gg.fr", "some content\n\n-----\nfooter");

    String messageCode = checkReceivedMail("support@mybudgetview.fr");

    checkMessage(messageCode, "some content");
    checkMessage(messageCode, "footer");
    checkMessage(messageCode, "me@gg.fr");
    checkMessage(messageCode, PicsouApplication.APPLICATION_VERSION);
  }

  public void testSendFeedbackWithLogs() throws Exception {
    licenseServer.init();
    startServers();
    startApplication();

    Files.dumpStringToFile(PicsouApplication.getLogFile(), "Something in the logs");

    openFeedback()
      .setLogsAdded()
      .send("my title", "me@gg.fr", "some content");

    String messageCode = checkReceivedMail("support@mybudgetview.fr");
    checkMessage(messageCode, "some content");
    checkMessage(messageCode, "me@gg.fr");
    checkMessage(messageCode, PicsouApplication.APPLICATION_VERSION);
    checkMessage(messageCode, "Something in the logs");
  }

  public void testSendFeedbackWithMissingLogs() throws Exception {
    licenseServer.init();
    startServers();
    startApplication();

    File logFile = PicsouApplication.getLogFile();
    if (logFile.exists()) {
      logFile.delete();
    }

    openFeedback()
      .setLogsAdded()
      .send("my title", "me@gg.fr", "some content");

    String messageCode = checkReceivedMail("support@mybudgetview.fr");
    checkMessage(messageCode, "some content");
    checkMessage(messageCode, "me@gg.fr");
    checkMessage(messageCode, PicsouApplication.APPLICATION_VERSION);
    checkMessage(messageCode, "[no log file found]");
  }

  private void checkMessage(String messageCode, String text) {
    assertTrue("text '" + text + "' not found in messageCode: " + messageCode,
               messageCode.contains(text));
  }

}
