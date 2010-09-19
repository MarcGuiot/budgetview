package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LicenseActivationChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.TimeService;
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

  public void testMessage() throws Exception {
    TextBox box = mainWindow.getTextBox("licenseMessage");
    assertThat(box.isVisible());
    assertThat(box.textEquals("30 days left for trying BudgetView."));

    LicenseActivationChecker.enterLicense(mainWindow, "admin", "zz");
    UISpecAssert.assertFalse(box.isVisible());
  }

  public void testOneDayLeft() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/09/29"));

    restartApplication();
    TextBox box = mainWindow.getTextBox("licenseMessage");
    assertThat(box.isVisible());
    assertThat(box.textEquals("You have one day left for trying BudgetView."));
  }

  public void testLastDay() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/09/30"));

    restartApplication();
    TextBox box = mainWindow.getTextBox("licenseMessage");
    assertThat(box.isVisible());
    assertThat(box.textEquals("This is your last day for trying BudgetView."));
  }

  public void testTrialOver() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/01"));

    restartApplication();
    TextBox box = mainWindow.getTextBox("licenseMessage");
    assertThat(box.isVisible());
    assertThat(box.textContains("Your free trial period is over."));
  }

  public void testCannotCreateTransactionsWhenTrialIsOver() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/01"));

    restartApplication();
    views.selectCategorization();
    transactionCreation.checkTrialExpiredMessage();
  }
}
