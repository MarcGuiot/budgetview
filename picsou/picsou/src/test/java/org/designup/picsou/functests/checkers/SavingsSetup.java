package org.designup.picsou.functests.checkers;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.Month;

public class SavingsSetup {
  public static void run(LoggedInFunctionalTestCase testCase, int monthId) throws Exception {
    
    String month1 = Month.toString(monthId);
    String month2 = Month.toString(Month.next(monthId));
    
    OfxBuilder.init(testCase)
      .addBankAccount(-1, 10674, "0001", 1000.0, month2 + "/30")
      .addTransaction(month1 + "/01", -150.00, "MAIN TO IMPORTED")
      .addTransaction(month1 + "/01", 70.00, "MAIN FROM IMPORTED")
      .addTransaction(month1 + "/01", -50.00, "MAIN TO NON IMPORTED")
      .addTransaction(month1 + "/01", 40.00, "MAIN FROM NON IMPORTED")
      .addTransaction(month1 + "/01", 500.00, "WORLDCO")
      .addTransaction(month2 + "/01", -200.00, "MAIN TO IMPORTED")
      .addTransaction(month2 + "/01", -50.00, "MAIN TO NON IMPORTED")
      .addTransaction(month2 + "/01", 500.00, "WORLDCO")
      .load();

    OfxBuilder.init(testCase)
      .addBankAccount(-1, 10674, "0002", 20000.0, month2 + "/30")
      .addTransaction(month1 + "/01", 150.00, "IMPORTED FROM MAIN")
      .addTransaction(month1 + "/01", -70.00, "IMPORTED TO MAIN")
      .addTransaction(month1 + "/01", 220.00, "IMPORTED FROM NON IMPORTED")
      .addTransaction(month1 + "/01", 200.00, "IMPORTED FROM EXTERNAL")
      .addTransaction(month1 + "/01", -300.00, "IMPORTED TO EXTERNAL ")
      .load();

    testCase.timeline.selectMonth(month1);

    testCase.mainAccounts.edit("Account n. 0001")
      .setName("Main")
      .validate();
    testCase.mainAccounts.edit("Account n. 0002")
      .setName("Imported Savings")
      .setAsSavings()
      .selectBank("ING Direct")
      .validate();
    testCase.accounts.createNewAccount()
      .setName("Non-imported Savings")
      .setAsSavings()
      .selectBank("ING Direct")
      .setPosition(5000.00)
      .validate();

    testCase.categorization.setNewTransfer("MAIN TO IMPORTED", "MainToImported", "Main", "Imported Savings");
    testCase.categorization.setTransfer("IMPORTED FROM MAIN", "MainToImported");
    testCase.categorization.setNewTransfer("MAIN FROM IMPORTED", "ImportedToMain", "Imported Savings", "Main");
    testCase.categorization.setTransfer("IMPORTED TO MAIN", "ImportedToMain");
    testCase.categorization.setNewTransfer("MAIN TO NON IMPORTED", "MainToNonImported", "Main", "Non-imported Savings");
    testCase.categorization.setNewTransfer("MAIN FROM NON IMPORTED", "MainFromNonImported", "Non-imported Savings", "Main");
    testCase.categorization.setNewTransfer("IMPORTED FROM NON IMPORTED", "ImportedFromNonImported", "Non-imported Savings", "Imported Savings");
    testCase.categorization.setNewTransfer("IMPORTED FROM EXTERNAL", "ImportedFromExternal", "External account", "Imported Savings");
    testCase.categorization.setNewTransfer("IMPORTED TO EXTERNAL", "ImportedToExternal", "Imported Savings", "External account");

  }
}
