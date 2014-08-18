package org.designup.picsou.functests.useraccounts;

import org.designup.picsou.functests.checkers.license.LicenseActivationChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.time.TimeService;
import org.globsframework.utils.Dates;

public class FreeTrialTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    resetWindow();
    setNotRegistered();
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }

  protected void selectInitialView() {
    views.selectHome();
  }

  public void testMessageIsNotDisplayedInitially() throws Exception {
    license.checkInfoMessageHidden();
  }

  public void test15DaysLeft() throws Exception {

    TimeService.setCurrentDate(Dates.parse("2008/10/01"));
    restartApplication();
    license.checkInfoMessage("15 days left for trying BudgetView.");

    LicenseActivationChecker.enterLicense(mainWindow, "admin", "1234");
    license.checkInfoMessageHidden();
  }

  public void testOneDayLeft() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/15"));

    restartApplication();
    license.checkInfoMessage("You have one day left for trying BudgetView.");
  }

  public void testLastDay() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/16"));

    restartApplication();
    license.checkInfoMessage("This is your last day for trying BudgetView.");
  }

  public void testTrialOver() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/18"));

    restartApplication();
    license.checkInfoMessage("Your free trial period is over.");
  }

  public void testCannotCreateTransactionsWhenTrialIsOver() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/18"));

    restartApplication();
    views.selectCategorization();
    transactionCreation.checkTrialExpiredMessage();
  }

  public void testFeedbackUsesEmailWhenAvailable() throws Exception {
    operations.checkFeedbackLink();

    LicenseActivationChecker.enterLicense(mainWindow, "admin", "1234");
    operations.checkFeedbackLink();
  }
}
