package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class FreeToPaidVersionTest extends LoggedInFunctionalTestCase {

  public void setUp() throws Exception {
    setNotRegistered();
    super.setUp();
    operations.disableAddOns();
  }

  public void test() throws Exception {

    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2013/11/01")
      .addTransaction("2013/11/01", 1000.00, "Income")
      .addTransaction("2013/11/01", 100.00, "Expense 1")
      .load();

    views.selectHome();

    operations.checkFileMenu(
      "Import",
      "Export",
      "Backup",
      "Restore",
      "previous version",
      "Preferences",
      "Register",
      "Exit"
    );
    projects.checkHidden();
    projectChart.checkHidden();
    budgetView.extras.checkAvailableActions("Add", "Disable month filtering");
    categorization.selectTransaction("EXPENSE 1");
    categorization.selectExtras().checkProjectCreationHidden();

    operations.enableAddOns();
    views.selectHome();

    operations.checkFileMenu(
      "Import",
      "Export",
      "Backup",
      "Restore",
      "previous version",
      "Preferences",
      "Register",
      "Protect account with a password",
      "Logout",
      "Delete user...",
      "Print...",
      "Mobile account...",
      "publish data for mobile",
      "Exit"
    );
    projects.checkVisible();
    projectChart.checkVisible();
    budgetView.extras.checkAvailableActions("Add", "Add project", "Disable month filtering");
    categorization.getExtras().checkProjectCreationVisible();
  }
}
