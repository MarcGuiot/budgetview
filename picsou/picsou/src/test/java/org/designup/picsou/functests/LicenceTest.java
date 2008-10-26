package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.TimeService;
import org.globsframework.utils.Dates;
import org.uispec4j.TextBox;
import org.uispec4j.assertion.UISpecAssert;

public class LicenceTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setNotRegistered();
    super.setUp();
  }

  public void testMessage() throws Exception {
    TextBox box = mainWindow.getTextBox("licenceMessage");
    UISpecAssert.assertTrue(box.isVisible());
    UISpecAssert.assertTrue(box.textEquals("Still 30 days."));
    LicenseChecker.enterLicense(mainWindow, "admin", "zz");
    UISpecAssert.assertFalse(box.isVisible());
  }

  public void testStillOneDay() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/09/30"));
    // force call to update
    operations.getPreferences().changeFutureMonth(3).validate();
    TextBox box = mainWindow.getTextBox("licenceMessage");
    UISpecAssert.assertTrue(box.isVisible());
    UISpecAssert.assertTrue(box.textEquals("This is your last day with fourmics."));
  }

  public void testNoMoreValide() throws Exception {
    TimeService.setCurrentDate(Dates.parse("2008/10/01"));
    // force call to update
    operations.getPreferences().changeFutureMonth(3).validate();
    TextBox box = mainWindow.getTextBox("licenceMessage");
    UISpecAssert.assertTrue(box.isVisible());
    UISpecAssert.assertTrue(box.textEquals("You must buy fourmics to continue to use it."));
  }
}
