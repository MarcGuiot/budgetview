package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class TransactionCreationTest extends LoggedInFunctionalTestCase {

  public void testStandardCreation() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    timeline.checkSelection("2008/08");

    views.selectHome();
    mainAccounts.createNewAccount()
      .setAccountName("Main")
      .setUpdateModeToFileImport()
      .selectBank("CIC")
      .validate();
    mainAccounts.createNewAccount()
      .setAccountName("Cash")
      .setAccountNumber("012345")
      .setUpdateModeToManualInput()
      .selectBank("CIC")
      .validate();

    views.selectCategorization();
    transactionCreation
      .checkHidden()
      .show()
      .checkAccounts("Cash")
      .checkAccount("Cash")
      .setAmount(12.50)
      .setDay(15)
      .setLabel("Transaction 1")
      .create()
      .checkFieldsAreEmpty();

    categorization.checkTable(new Object[][]{
      {"15/08/2008", "", "TRANSACTION 1", 12.50},
    });

    categorization.checkSelectedTableRow("TRANSACTION 1");

    views.selectHome();
    mainAccounts.createNewAccount()
      .setAccountName("Misc")
      .setAccountNumber("012345")
      .setUpdateModeToManualInput()
      .selectBank("CIC")
      .validate();

    timeline.selectMonths("2008/08", "2008/09");

    views.selectCategorization();
    transactionCreation
      .checkShowing()
      .checkAccounts("Cash", "Misc")
      .checkAccount("Cash")
      .selectAccount("Misc")
      .setAmount(20.00)
      .setDay(3)
      .setLabel("Transaction 2")
      .create()
      .checkFieldsAreEmpty();

    categorization.checkTable(new Object[][]{
      {"15/08/2008", "", "TRANSACTION 1", 12.50},
      {"03/09/2008", "", "TRANSACTION 2", 20.00},
    });

    categorization.checkSelectedTableRow("TRANSACTION 2");
  }

  public void testCreationPanelIsAvailableOnlyWhenManualInputAccountsExist() throws Exception {

    views.selectCategorization();
    transactionCreation
      .checkHidden()
      .checkNoAccountAvailableMessage();

    views.selectHome();
    mainAccounts.createNewAccount()
      .setAccountName("Main")
      .setUpdateModeToFileImport()
      .selectBank("CIC")
      .validate();

    views.selectCategorization();
    transactionCreation
      .checkHidden()
      .checkNoAccountMessageAndOpenCreationDialog()
      .checkUpdateModeIsManualInput()
      .setAccountName("Cash")
      .setAccountNumber("012345")
      .selectBank("CIC")
      .validate();

    transactionCreation
      .checkShowing()
      .hide()
      .checkHidden()
      .show();

    views.selectHome();
    mainAccounts.edit("Cash").doDelete();

    views.selectCategorization();
    transactionCreation
      .checkHidden()
      .checkNoAccountAvailableMessage();
  }

  public void testCreationErrors() throws Exception {

    views.selectHome();
    mainAccounts.createNewAccount()
      .setAccountName("Cash")
      .setAccountNumber("012345")
      .setUpdateModeToManualInput()
      .selectBank("CIC")
      .validate();

    views.selectCategorization();
    transactionCreation
      .checkNoErrorMessage()
      .create()
      .checkErrorMessage("You must enter an amount")
      .setAmount(10.00)
      .create()
      .checkErrorMessage("You must enter a day");
    categorization.checkTableIsEmpty();

    transactionCreation
      .checkAmount(10.00)
      .setDay(3)
      .create()
      .checkErrorMessage("You must enter a label");
    categorization.checkTableIsEmpty();

    transactionCreation
      .checkAmount(10.00)
      .checkDay(3)
      .setLabel("a transaction")
      .create()
      .checkNoErrorMessage();

    categorization.checkTable(new Object[][]{
      {"03/08/2008", "", "A TRANSACTION", 10.00},
    });

    categorization.checkSelectedTableRow("A TRANSACTION");
  }

  public void testInputDisabledInDemoMode() throws Exception {
    fail("tbd");
  }
}
