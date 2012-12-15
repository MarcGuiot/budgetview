package org.designup.picsou.license.functests;

import org.designup.picsou.functests.checkers.ApplicationChecker;
import org.designup.picsou.functests.checkers.FeedbackDialogChecker;
import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.startup.components.AppLogger;
import org.designup.picsou.license.ConnectedTestCase;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Files;

import java.io.File;
import java.util.Locale;

public class FeedbackTest extends ConnectedTestCase {
  private ApplicationChecker application;
  private int previousRetry;

  protected void setUp() throws Exception {
    previousRetry = ConfigService.RETRY_PERIOD;
    ConfigService.RETRY_PERIOD = 500;
    super.setUp();
    application = new ApplicationChecker();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    application.dispose();
    application = null;
    ConfigService.RETRY_PERIOD = previousRetry;
  }

  private FeedbackDialogChecker openFeedback() {
    OperationChecker operation = new OperationChecker(application.getWindow());
    return operation.openFeedback();
  }

  public void testNotConnected() throws Exception {
    application.start();
    FeedbackDialogChecker feedback = openFeedback();
    feedback.checkNotConnected();

    startServers();

    feedback.checkConnected();
    feedback.cancel();
  }

  public void testSendFeedback() throws Exception {
    startServers();
    application.start();

    Lang.setLocale(Locale.ENGLISH);

    openFeedback().send("me@gg.fr", "some content\n\n-----\nfooter");

    mailServer.checkReceivedMail("support@mybudgetview.fr")
      .checkContains("some content",
                     "footer",
                     "me@gg.fr",
                     PicsouApplication.APPLICATION_VERSION,
                     "lang:en");

    Lang.setLocale(Lang.ROOT);
  }

  public void testSendFeedbackWithLogs() throws Exception {
    startServers();
    application.start();

    Files.dumpStringToFile(AppLogger.getLogFile(), "Something in the logs");

    openFeedback()
      .setLogsAdded()
      .send("me@gg.fr", "some content");

    mailServer.checkReceivedMail("support@mybudgetview.fr")
      .checkContains("some content",
                     "me@gg.fr",
                     PicsouApplication.APPLICATION_VERSION,
                     "Something in the logs");
  }

  public void testSendFeedbackWithMissingLogs() throws Exception {
    startServers();
    application.start();

    File logFile = AppLogger.getLogFile();
    if (logFile.exists()) {
      logFile.delete();
    }

    openFeedback()
      .setLogsAdded()
      .send("me@gg.fr", "some content");

    mailServer.checkReceivedMail("support@mybudgetview.fr")
      .checkContains("some content",
                     "me@gg.fr",
                     PicsouApplication.APPLICATION_VERSION,
                     "[no log file found]");
  }
}
