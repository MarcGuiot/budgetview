package com.budgetview.license.functests;

import com.budgetview.functests.checkers.FeedbackDialogChecker;
import com.budgetview.functests.checkers.OperationChecker;
import com.budgetview.gui.PicsouApplication;
import com.budgetview.functests.checkers.ApplicationChecker;
import com.budgetview.gui.startup.components.AppLogger;
import com.budgetview.http.HttpBudgetViewConstants;
import com.budgetview.license.ConnectedTestCase;
import com.budgetview.utils.Lang;
import org.globsframework.utils.Files;

import java.io.File;
import java.util.Locale;

public class FeedbackTest extends ConnectedTestCase {
  private ApplicationChecker application;
  private int previousRetry;

  protected void setUp() throws Exception {
    previousRetry = HttpBudgetViewConstants.RETRY_PERIOD;
    HttpBudgetViewConstants.RETRY_PERIOD = 500;
    super.setUp();
    application = new ApplicationChecker();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    application.dispose();
    application = null;
    HttpBudgetViewConstants.RETRY_PERIOD = previousRetry;
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

  public void testSendFeedbackWithServerError() throws Exception {
    startServers();
    application.start();
    Lang.setLocale(Locale.ENGLISH);

    FeedbackDialogChecker feedbackDialog = openFeedback();
    licenseServer.stop();
    feedbackDialog.sendWithError("me@gg.fr", "some content\n\n-----\nfooter",
                                 "Your message could not be sent");
    mailServer.checkEmpty();

    Lang.setLocale(Lang.ROOT);
  }
}
