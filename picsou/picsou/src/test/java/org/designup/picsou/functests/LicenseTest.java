package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LicenseActivationChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.time.TimeService;
import org.globsframework.utils.Dates;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;

public class LicenseTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    resetWindow();
    setNotRegistered();
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  protected void selectInitialView() {
    views.selectHome();
  }

  public void testMessageIsNotDisplayedInitially() throws Exception {
    licenseMessage.checkHidden();
  }

  public void test15DaysLeft() throws Exception {

    TimeService.setCurrentDate(Dates.parse("2008/10/01"));
    restartApplication();
    licenseMessage.checkVisible("15 days left for trying BudgetView.");

    LicenseActivationChecker.enterLicense(mainWindow, "admin", "1234");
    licenseMessage.checkHidden();
  }

  public void testOneDayLeft() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/15"));

    restartApplication();
    licenseMessage.checkVisible("You have one day left for trying BudgetView.");
  }

  public void testLastDay() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/16"));

    restartApplication();
    licenseMessage.checkVisible("This is your last day for trying BudgetView.");
  }

  public void testTrialOver() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/18"));

    restartApplication();
    licenseMessage.checkVisible("Your free trial period is over.");
  }

  public void testCannotCreateTransactionsWhenTrialIsOver() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/18"));

    restartApplication();
    views.selectCategorization();
    transactionCreation.checkTrialExpiredMessage();
  }

  public void testFeedbackUsesEmailWhenAvailable() throws Exception {
    feedbackView.checkFeedbackLinksTo("http://support.mybudgetview.fr/anonymous_requests/new");

    LicenseActivationChecker.enterLicense(mainWindow, "admin", "1234");
    feedbackView.checkFeedbackLinksTo("http://support.mybudgetview.fr/anonymous_requests/new?email=admin");
  }
}
