package com.budgetview.functests.useraccounts;

import com.budgetview.functests.checkers.LoginChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import org.junit.Test;

public class SetPasswordTest extends LoggedInFunctionalTestCase {

  @Test
  public void testNominal() throws Exception {

    operations.setPasswordForAnonymous()
      .setPassword("user1", "password1")
      .validate("Password set",
                "Your account is now protected with a password");
    operations.logout();

    password = "password1";

    LoginChecker.init(mainWindow).logExistingUser("user1", password);

    operations.changeAccountIdentifiers()
      .changePassword(password, "user2", "password2")
      .validate("User and password changed",
                "Your user and password have been successfully changed");

    operations.logout();

    password = "password2";
    LoginChecker.init(mainWindow).logExistingUser("user2", password);
  }

  @Test
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
    loginChecker.logExistingUser("user1", password);
  }
}
