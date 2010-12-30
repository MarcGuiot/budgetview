package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.model.TransactionType;

public class TransactionCreationTest extends LoggedInFunctionalTestCase {

  public void testStandardCreation() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    timeline.checkSelection("2008/08");

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

    transactionCreation
      .checkHidden()
      .show()
      .checkAccounts("Cash")
      .checkAccount("Cash")
      .checkNegativeAmountsSelected()
      .setAmount(-12.50)
      .setDay(15)
      .checkMonth("August 2008")
      .setLabel("Transaction 1")
      .create()
      .checkFieldsAreEmpty()
      .checkNegativeAmountsSelected();

    categorization.checkTable(new Object[][]{
      {"15/08/2008", "", "TRANSACTION 1", -12.50},
    });

    categorization.checkSelectedTableRow("TRANSACTION 1");

    mainAccounts.createNewAccount()
      .setAccountName("Misc")
      .setAccountNumber("012345")
      .setUpdateModeToManualInput()
      .selectBank("CIC")
      .validate();

    timeline.selectMonths("2008/08", "2008/09");

    transactionCreation
      .checkShowing()
      .checkAccounts("Cash", "Misc")
      .checkAccount("Cash")
      .selectAccount("Misc")
      .checkNegativeAmountsSelected()
      .setAmount(20.00)
      .checkPositiveAmountsSelected()
      .setDay(3)
      .checkMonth("September 2008")
      .setLabel("Transaction 2")
      .create()
      .checkFieldsAreEmpty()
      .checkPositiveAmountsSelected();

    categorization.checkTable(new Object[][]{
      {"15/08/2008", "", "TRANSACTION 1", -12.50},
      {"03/09/2008", "", "TRANSACTION 2", 20.00},
    });

    categorization.checkSelectedTableRow("TRANSACTION 2");

    transactions.initContent()
      .add("03/09/2008", TransactionType.MANUAL, "TRANSACTION 2", "", 20.00)
      .add("15/08/2008", TransactionType.MANUAL, "TRANSACTION 1", "", -12.50)
      .check();

    transactions.selectAccount("Cash");
    transactions.checkClearFilterButtonShown();
    transactions.initContent()
      .add("15/08/2008", TransactionType.MANUAL, "TRANSACTION 1", "", -12.50)
      .check();

    transactions.selectAccount("Misc");
    transactions.checkClearFilterButtonShown();
    transactions.initContent()
      .add("03/09/2008", TransactionType.MANUAL, "TRANSACTION 2", "", 20.00)
      .check();

    transactions.clearFilters();
    transactions.checkSelectedAccount("All accounts");
    transactions.checkClearFilterButtonHidden();
  }

  public void testCreationPanelIsAvailableOnlyWhenManualInputAccountsExist() throws Exception {

    transactionCreation
      .checkHidden()
      .checkNoAccountAvailableMessage();

    mainAccounts.createNewAccount()
      .setAccountName("Main")
      .setUpdateModeToFileImport()
      .selectBank("CIC")
      .validate();

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

    mainAccounts.edit("Cash").doDelete();

    transactionCreation
      .checkHidden()
      .checkNoAccountAvailableMessage();
  }

  public void testCreationErrors() throws Exception {

    operations.openPreferences().setFutureMonthsCount(3).validate();

    mainAccounts.createNewAccount()
      .setAccountName("Cash")
      .setAccountNumber("012345")
      .setUpdateModeToManualInput()
      .selectBank("CIC")
      .validate();

    transactionCreation
      .checkNoErrorMessage()
      .createAndCheckErrorMessage("You must enter an amount")
      .checkNegativeAmountsSelected()
      .setAmount(10.00)
      .createAndCheckErrorMessage("You must enter a day");
    categorization.checkTableIsEmpty();

    transactionCreation
      .checkAmount(10.00)
      .setDay(0)
      .createAndCheckErrorMessage("The day must be between 1 and 31");
    categorization.checkTableIsEmpty();

    timeline.selectMonth("2008/09");
    transactionCreation
      .checkAmount(10.00)
      .setDay(31)
      .createAndCheckErrorMessage("The day must be between 1 and 30");
    categorization.checkTableIsEmpty();

    transactionCreation
      .checkAmount(10.00)
      .setDay(3)
      .createAndCheckErrorMessage("You must enter a label");
    categorization.checkTableIsEmpty();

    transactionCreation
      .checkAmount(10.00)
      .checkDay(3)
      .setLabel("a transaction")
      .create()
      .checkNoErrorMessage();

    categorization.checkTable(new Object[][]{
      {"03/09/2008", "", "A TRANSACTION", 10.00},
    });

    categorization.checkSelectedTableRow("A TRANSACTION");
  }

  public void testCreateButtonValidatesAllFields() throws Exception {
    mainAccounts.createNewAccount()
      .setAccountName("Cash")
      .setAccountNumber("012345")
      .setUpdateModeToManualInput()
      .selectBank("CIC")
      .validate();

    transactionCreation
      .show()
      .enterAmountWithoutValidating(10.00)
      .enterDayWithoutValidating(3)
      .enterLabelWithoutValidating("Transaction 1")
      .create()
      .checkNoErrorMessage();

    categorization.checkTable(new Object[][]{
      {"03/08/2008", "", "TRANSACTION 1", -10.00},
    });
  }
}
