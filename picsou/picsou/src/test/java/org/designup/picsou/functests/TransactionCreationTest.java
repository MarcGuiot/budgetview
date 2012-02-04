package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

import java.util.Date;

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
      .show()
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

  public void testCreateOperationUpdateAccountPosition() throws Exception {
    setInMemory(false);
    restartApplication(true);
    setDeleteLocalPrevayler(false);

    mainAccounts.createNewAccount()
      .setAccountName("Cash")
      .setAccountNumber("012345")
      .setUpdateModeToManualInput()
      .setPosition(100.)
      .selectBank("CIC")
      .validate();

    transactionCreation
      .show()
      .selectAccount("Cash")
      .enterAmountWithoutValidating(10.00)
      .enterDayWithoutValidating(1)
      .enterLabelWithoutValidating("Transaction 1")
      .create();

    mainAccounts.checkPosition("Cash", 100.);
    
    setCurrentDate("2008/09/02");
    restartApplication();
    timeline.selectMonth(200809);

    transactionCreation
      .show()
      .selectAccount("Cash")
      .enterAmountWithoutValidating(10.00)
      .enterDayWithoutValidating(1)
      .enterLabelWithoutValidating("Transaction 1")
      .create();

    mainAccounts.checkPosition("Cash", 90.);

    views.selectCategorization();
    transactionCreation
      .enterAmountWithoutValidating(20.00)
      .enterDayWithoutValidating(2)
      .enterLabelWithoutValidating("Transaction 2")
      .create();

    mainAccounts.checkPosition("Cash", 70.);
  }


  public void testMiroirOperationIsUpdated() throws Exception {
    mainAccounts.createNewAccount()
      .setAccountName("Cash")
      .setAccountNumber("012345")
      .setUpdateModeToManualInput()
      .setPosition(100.)
      .selectBank("CIC")
      .validate();

    mainAccounts.createSavingsAccount("Livret A", 100.);

    budgetView.savings
      .createSeries()
      .setName("virement manuel vers livret A")
      .setFromAccount("Main account")
      .setToAccount("Livret A")
      .validate();

    transactionCreation
      .show()
      .selectAccount("Cash")
      .enterAmountWithoutValidating(10.00)
      .enterDayWithoutValidating(1)
      .enterLabelWithoutValidating("Transaction 1")
      .create();
    categorization.selectTransaction("Transaction 1")
      .selectSavings()
      .selectSeries("virement manuel vers livret A");

    mainAccounts.checkPosition("Cash", 100.);
    savingsAccounts.checkPosition("Livret A", 100.);

    setCurrentDate("2008/09/05");
    restartApplicationFromBackup();
    timeline.selectMonth(200809);

    transactionCreation
      .show()
      .selectAccount("Cash")
      .enterAmountWithoutValidating(10.00)
      .enterDayWithoutValidating(4)
      .enterLabelWithoutValidating("Transaction 2")
      .create();
    categorization.selectTransaction("Transaction 2")
      .selectSavings()
      .selectSeries("virement manuel vers livret A");

    mainAccounts.checkPosition("Cash", 90.);
    savingsAccounts.checkPosition("Livret A", 110.);

    views.selectCategorization();
    transactionCreation
      .selectAccount("Cash")
      .enterAmountWithoutValidating(20.00)
      .enterDayWithoutValidating(4)
      .enterLabelWithoutValidating("Transaction 3")
      .create();
    categorization.selectTransaction("Transaction 3")
      .selectSavings()
      .selectSeries("virement manuel vers livret A");

    mainAccounts.checkPosition("Cash", 70.);
    savingsAccounts.checkPosition("Livret A", 130.);

  }
}
