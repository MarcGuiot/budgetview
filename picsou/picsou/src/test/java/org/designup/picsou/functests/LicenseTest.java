package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.TimeService;
import org.globsframework.utils.Dates;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;

public class LicenseTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setNotRegistered();
    super.setUp();
  }

  protected void selectInitialView() {
    views.selectHome();
  }

  public void testMessage() throws Exception {
    TextBox box = mainWindow.getTextBox("licenseMessage");
    UISpecAssert.assertTrue(box.isVisible());
    UISpecAssert.assertTrue(box.textEquals("Still 30 days."));

    LicenseChecker.enterLicense(mainWindow, "admin", "zz");
    UISpecAssert.assertFalse(box.isVisible());
  }

  public void testOneDayLeft() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/09/30"));

    // force call to update
    operations.openPreferences().setFutureMonthsCount(3).validate();
    TextBox box = mainWindow.getTextBox("licenseMessage");
    UISpecAssert.assertTrue(box.isVisible());
    UISpecAssert.assertTrue(box.textEquals("This is your last day with cashpilot."));
  }

  public void testLicenseExpired() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/01"));

    // force call to update
    operations.openPreferences().setFutureMonthsCount(3).validate();
    TextBox box = mainWindow.getTextBox("licenseMessage");
    UISpecAssert.assertTrue(box.isVisible());
    UISpecAssert.assertTrue(box.textEquals("<html>Your free trial period is over. You can buy...</html>"));
  }
}
