package org.designup.picsou.functests.general;

import org.designup.picsou.functests.checkers.AddOnsViewChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class AddOnsActivationTest extends LoggedInFunctionalTestCase {

  private AddOnsViewChecker addons;

  public void setUp() throws Exception {
    setNotRegistered();
    super.setUp();
    operations.disableAddOns();
    addons = new AddOnsViewChecker(mainWindow);
  }

  public void test() throws Exception {

    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2013/11/01")
      .addTransaction("2013/11/01", 1000.00, "Income")
      .addTransaction("2013/11/01", 100.00, "Expense 1")
      .load();
    categorization.setNewIncome("INCOME", "Salary");
    views.selectHome();

    // === FREE VERSION ===

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
    addons.checkShown();
    addons.checkButton("http://www.mybudgetview.com/add-ons");
    budgetView.income.checkSeriesActions(
      "Salary",
      "Edit",
      "Show operations",
      "Carry over next month",
      "Delete...");
    budgetView.extras.checkAvailableActions("Add", "Disable month filtering");
    categorization.selectTransaction("EXPENSE 1");
    categorization.selectExtras().checkProjectCreationHidden();
    operations.openPreferences()
      .checkDataPathModificationHidden()
      .cancel();

    // === ENABLE ADD-ONS ===

    operations.enableAddOns();

    // === ADD-ONS ENABLED ===

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
    budgetView.income.checkSeriesActions(
      "Salary",
      "Edit",
      "Show operations",
      "See in Analysis view",
      "Carry over next month",
      "Delete...");
    views.selectHome();
    addons.checkHidden();
    projects.checkVisible();
    projectChart.checkVisible();
    budgetView.extras.checkAvailableActions("Add", "Add project", "Disable month filtering");
    categorization.getExtras().checkProjectCreationVisible();
    operations.openPreferences()
      .checkDataPathModificationShown()
      .cancel();
  }
}
