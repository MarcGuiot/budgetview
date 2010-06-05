package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.checkers.LoginChecker;

public class ProtectTest extends LoggedInFunctionalTestCase {

  public void testNominal() throws Exception {
    operations.protect("newUserName", "newPassword");
    operations.logout();
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    password = "newPassword";
    loginChecker.logExistingUser("newUserName", password, true);
  }

  public void testRenameFromAnonymous() throws Exception {
    operations.logout();
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    loginChecker.clickFirstAutoLogin();
    operations.protectFromAnonymous("newUserName", "newPassword");
    operations.logout();
    loginChecker = new LoginChecker(mainWindow);
    password = "newPassword";
    loginChecker.logExistingUser("newUserName", password, true);
  }
}
