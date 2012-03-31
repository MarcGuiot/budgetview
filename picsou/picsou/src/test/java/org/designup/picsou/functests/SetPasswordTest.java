package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class SetPasswordTest extends LoggedInFunctionalTestCase {

  public void testNominal() throws Exception {

    operations.setPasswordForAnonymous()
      .setPassword("user1", "password1")
      .validate("Password set",
                "Your account is now protected with a password");
    operations.logout();

    password = "password1";

    LoginChecker.init(mainWindow).logExistingUser("user1", password, true);

    operations.changeAccountIdentifiers()
      .changePassword(password, "user2", "password2")
      .validate("User and password changed",
                "Your user and password have been successfully changed");

    operations.logout();

    password = "password2";
    LoginChecker.init(mainWindow).logExistingUser("user2", password, true);
  }

  public void testRenameFromAnonymous() throws Exception {
    operations.logout();
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    loginChecker.clickAutoLogin();

    operations.setPasswordForAnonymous()
      .setPassword("user1", "password1")
      .validate("Password set",
                "Your account is now protected with a password");
    operations.logout();

    password = "password1";
    loginChecker.logExistingUser("user1", password, false);
  }
}
