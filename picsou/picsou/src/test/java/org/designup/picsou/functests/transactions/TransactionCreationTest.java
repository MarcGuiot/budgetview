package org.designup.picsou.functests.transactions;

import org.designup.picsou.functests.checkers.MonthChooserChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class TransactionCreationTest extends LoggedInFunctionalTestCase {

  public void testStandardCreation() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    timeline.checkSelection("2008/08");

    mainAccounts.createNewAccount()
      .setName("Main")
      .selectBank("CIC")
      .validate();

    mainAccounts.createNewAccount()
      .setName("Cash")
      .setAccountNumber("012345")
      .selectBank("CIC")
      .validate();

    transactionCreation
      .checkHidden()
      .show()
      .checkAccounts("Cash", "Main")
      .checkSelectedAccount("Main")
      .selectAccount("Cash")
      .checkNegativeAmountsSelected()
      .setAmount(-12.50)
      .setDay(15)
      .checkMonth("August 2008")
      .setLabel("Transaction 1")
      .create()
      .checkFieldsAreEmpty()
      .checkNegativeAmountsSelected();

    categorization
      .checkTable(new Object[][]{
        {"15/08/2008", "", "TRANSACTION 1", -12.50},
      });

    categorization.checkSelectedTableRow("TRANSACTION 1");

    mainAccounts.createNewAccount()
      .setName("Misc")
      .setAccountNumber("012345")
      .selectBank("CIC")
      .validate();

    timeline.selectMonths("2008/08", "2008/09");

    transactionCreation
      .checkShowing()
      .checkAccounts("Cash", "Main", "Misc")
      .checkSelectedAccount("Cash")
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

  public void testCreationPanelIsAvailableOnlyWhenThereIsAtLeastOneAccount() throws Exception {

    transactionCreation
      .checkHidden()
      .clickAndOpenAccountCreationMessage()
      .setName("Cash")
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
      .checkShowOpensAccountCreationMessage();
  }

  public void testCreationErrors() throws Exception {

    operations.openPreferences().setFutureMonthsCount(3).validate();

    mainAccounts.createNewAccount()
      .setName("Cash")
      .setAccountNumber("012345")
      .selectBank("CIC")
      .validate();

    transactionCreation
      .show()
      .checkSelectedAccount("Cash")
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
      .setName("Cash")
      .setAccountNumber("012345")
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
      .setName("Cash")
      .setAccountNumber("012345")
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

    mainAccounts.checkPosition("Cash", 100.00);

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

    mainAccounts.checkPosition("Cash", 90.00);

    views.selectCategorization();
    transactionCreation
      .enterAmountWithoutValidating(20.00)
      .enterDayWithoutValidating(2)
      .enterLabelWithoutValidating("Transaction 2")
      .create();

    mainAccounts.checkPosition("Cash", 70.00);
    resetWindow();
  }

  public void testMirrorTransactionIsUpdated() throws Exception {
    mainAccounts.createNewAccount()
      .setName("Cash")
      .setAccountNumber("012345")
//      .setUpdateModeToManualInput()
      .setPosition(100.00)
      .selectBank("CIC")
      .validate();

    mainAccounts.createSavingsAccount("Livret A", 100.00);

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

    mainAccounts.checkPosition("Cash", 100.00);
    savingsAccounts.checkPosition("Livret A", 100.00);

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

    mainAccounts.checkPosition("Cash", 90.00);
    savingsAccounts.checkPosition("Livret A", 110.00);

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

    mainAccounts.checkPosition("Cash", 70.00);
    savingsAccounts.checkPosition("Livret A", 130.00);
  }

  public void testTransactionCreationMenuShowsTip() throws Exception {

    operations.hideSignposts();

    mainAccounts.createNewAccount()
      .setName("Cash")
      .setAccountNumber("012345")
      .setPosition(100.00)
      .selectBank("CIC")
      .validate();

    views.selectHome();
    operations.createTransactions();

    views.checkCategorizationSelected();
    transactionCreation.checkSignpostShown("Click here to enter transactions");
    transactionCreation.show();
    transactionCreation.checkSignpostHidden();

    views.selectHome();
    operations.createTransactions();

    views.checkCategorizationSelected();
    transactionCreation.checkPanelSignpostShown("Enter your transactions here");
    transactionCreation.hide();
    transactionCreation.checkSignpostHidden();

    operations.createTransactions();
    transactionCreation.checkHidden();
    transactionCreation.checkSignpostShown("Click here to enter transactions");
    transactionCreation
      .show()
      .checkSelectedAccount("Cash")
      .checkNegativeAmountsSelected()
      .setAmount(-12.50)
      .setDay(15)
      .checkMonth("August 2008")
      .setLabel("Transaction 1")
      .create();
    transactionCreation.checkSignpostHidden();
  }

  public void testSelectingTheMonth() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    mainAccounts.createNewAccount()
      .setName("Cash")
      .setAccountNumber("012345")
      .setPosition(100.00)
      .selectBank("CIC")
      .validate();

    timeline.checkSelection("2008/08");

    transactionCreation
      .show()
      .checkSelectedAccount("Cash")
      .setAmount(-10.00)
      .setLabel("Transaction 1");

    MonthChooserChecker monthChooser = transactionCreation
      .setDay(15)
      .checkMonth("August 2008")
      .editMonth();

    monthChooser
      .checkIsDisabled(200811)
      .checkIsEnabled(200810)
      .checkIsEnabled(200801)
      .selectMonth(200805);

    timeline.checkDisplays("2008/08", "2008/09", "2008/10");
    timeline.checkSelection("2008/08");

    transactionCreation
      .checkMonth("May 2008")
      .create();
    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "TRANSACTION 1", -10.00},
    });

    timeline.checkDisplays("2008/05", "2008/06", "2008/07", "2008/08", "2008/09", "2008/10");
    timeline.checkSelection("2008/05");

    transactionCreation
      .setDay(20)
      .checkMonth("May 2008")
      .setAmount(-100.00)
      .setLabel("Transaction 2")
      .create();

    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "TRANSACTION 1", -10.00},
      {"20/05/2008", "", "TRANSACTION 2", -100.00},
    });

    transactionCreation
      .setDay(25)
      .checkMonth("May 2008")
      .selectMonth(200807)
      .checkMonth("July 2008")
      .setAmount(-200.00)
      .setLabel("Transaction 3");

    timeline.checkSelection("2008/05");

    transactionCreation
      .create();

    timeline.checkSelection("2008/07");

    categorization.checkTable(new Object[][]{
      {"15/05/2008", "", "TRANSACTION 1", -10.00},
      {"20/05/2008", "", "TRANSACTION 2", -100.00},
      {"25/07/2008", "", "TRANSACTION 3", -200.00},
    });

    timeline.selectAll();
    transactions.initContent()
      .add("25/07/2008", "25/07/2008", TransactionType.MANUAL, "TRANSACTION 3", "", -200.00)
      .add("20/05/2008", TransactionType.MANUAL, "TRANSACTION 2", "", -100.00)
      .add("15/05/2008", "15/05/2008", TransactionType.MANUAL, "TRANSACTION 1", "", -10.00)
      .check();

  }

  public void testCreatingAManuallyCreatedTransactionInTheFuturePreservesCurrentMonth() throws Exception {
    fail("tbd - http://support.mybudgetview.fr/tickets/1070");
  }

  public void testCreatedTransactionsAreAutomaticallyCategorized() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount("0001", 0.0, "2012/08/31")
      .addTransaction("2012/08/15", -10.00, "Burger King")
      .load();

    categorization.setNewVariable("BURGER KING", "Gastronomie", -200.00);
    
    transactionCreation.show()
      .setDay(16)
      .setLabel("Burger king")
      .setAmount(-15.00)
      .create();
    
    categorization.initContent()
      .add("15/08/2012", "Gastronomie", "BURGER KING", -10.00)
      .add("16/08/2012", "Gastronomie", "[R] BURGER KING", -15.00)
      .check();
    
    transactionCreation.create(17, "Auchan", -100.00);
    categorization.initContent()
      .add("17/08/2012", "", "[R] AUCHAN", -100.00)
      .add("15/08/2012", "Gastronomie", "BURGER KING", -10.00)
      .add("16/08/2012", "Gastronomie", "[R] BURGER KING", -15.00)
      .check();

    categorization.setNewVariable("[R] Auchan", "Groceries", -200.00);
    transactionCreation.create(18, "Auchan", -150.00);
    categorization.initContent()
      .add("17/08/2012", "Groceries", "[R] AUCHAN", -100.00)
      .add("18/08/2012", "Groceries", "[R] AUCHAN", -150.00)
      .add("15/08/2012", "Gastronomie", "BURGER KING", -10.00)
      .add("16/08/2012", "Gastronomie", "[R] BURGER KING", -15.00)
      .check();
  }

  public void testAutoCompletion() throws Exception {
    mainAccounts.createNewAccount()
      .setName("Main")
      .selectBank("CIC")
      .setPosition(1000.00)
      .validate();
    
    transactionCreation.show()
      .checkSelectedAccount("Main")
      .create(15, "Auchan", -50.00);

    transactionCreation
      .setDay(16)
      .checkLabelAutocompletion("Au", "AUCHAN");
  }

  public void testModifiedLabelsWhichAreNotUsedAnymoreAreExcludedFromAutocompletion() throws Exception {
    mainAccounts.createNewAccount()
      .setName("Main")
      .selectBank("CIC")
      .setPosition(1000.00)
      .validate();

    transactionCreation.show()
      .checkSelectedAccount("Main")
      .create(15, "Auchan", -50.00);

    transactions.edit("AUCHAN")
      .setLabel("Carrefour")
      .validate();

    views.selectCategorization();
    transactionCreation
      .setDay(16)
      .checkLabelAutocompletion("Au", "Au")
      .clearLabel()
      .checkLabelAutocompletion("Ca", "CARREFOUR")
      .setDay(16)
      .setAmount(-100.00)
      .create();

    categorization.initContent()
      .add("15/08/2008", "", "CARREFOUR", -50.00)
      .add("16/08/2008", "", "CARREFOUR", -100.00)
      .check();

    categorization.edit(0)
      .setLabel("ED")
      .validate();

    transactionCreation
      .setDay(17)
      .checkLabelAutocompletion("Ca", "CARREFOUR")
      .setLabel("Auchan")
      .setAmount(-150.00)
      .create();

    categorization.initContent()
      .add("17/08/2008", "", "AUCHAN", -150.00)
      .add("16/08/2008", "", "CARREFOUR", -100.00)
      .add("15/08/2008", "", "ED", -50.00)
      .check();

    categorization.edit(1)
      .setLabel("Monoprix")
      .validate();
    categorization.initContent()
      .add("17/08/2008", "", "AUCHAN", -150.00)
      .add("15/08/2008", "", "ED", -50.00)
      .add("16/08/2008", "", "MONOPRIX", -100.00)
      .check();

    transactionCreation
      .setDay(18)
      .checkLabelAutocompletion("Ca", "Ca");

    categorization.delete("MONOPRIX").validate();
    categorization.initContent()
      .add("17/08/2008", "", "AUCHAN", -150.00)
      .add("15/08/2008", "", "ED", -50.00)
      .check();

    transactionCreation
      .setDay(18)
      .checkLabelAutocompletion("Mo", "Mo");
  }
}
