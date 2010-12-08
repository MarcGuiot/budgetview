package org.designup.picsou.license.functests;

import org.designup.picsou.functests.checkers.FeedbackDialogChecker;
import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.functests.checkers.StartupChecker;
import org.designup.picsou.license.ConnectedTestCase;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.config.ConfigService;
import org.uispec4j.Window;

public class FeedbackTest extends ConnectedTestCase {
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

  private void startPicsou() {
    StartupChecker startupChecker = new StartupChecker();
    window = startupChecker.enterMain();
    picsouApplication = startupChecker.getApplication();
  }

  public void testNotConnected() throws Exception {
    startPicsou();
    OperationChecker operation = new OperationChecker(window);
    FeedbackDialogChecker feedback = operation.openFeedback();
    feedback.checkNotConnected();
    licenseServer.init();
    startServers();
    feedback.checkConnected();
    feedback.cancel();
  }
  
  public void testSendFeedback() throws Exception {
    licenseServer.init();
    startServers();
    startPicsou();
    OperationChecker operation = new OperationChecker(window);
    FeedbackDialogChecker feedback = operation.openFeedback();
    feedback.send("my title", "me@gg.fr", "some content");
    String messageCode = checkReceivedMail("support@mybudgetview.fr");
    assertTrue(messageCode.contains("some content"));
    assertTrue(messageCode.contains("me@gg.fr"));
  }

}
