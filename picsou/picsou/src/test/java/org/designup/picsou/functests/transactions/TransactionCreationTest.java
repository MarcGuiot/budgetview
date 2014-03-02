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

    transactionCreation.openHelp()
      .checkTitle("Manual input")
      .close();
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

    mainAccounts.edit("Cash").delete();

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
      .setAmountText("10.00")
      .createAndCheckErrorMessage("You must enter a day");
    categorization.checkTableIsEmpty();

    transactionCreation
      .checkAmount(-10.00)
      .setDay(0)
      .createAndCheckErrorMessage("The day must be between 1 and 31");
    categorization.checkTableIsEmpty();

    timeline.selectMonth("2008/09");
    transactionCreation
      .checkAmount(-10.00)
      .setDay(31)
      .createAndCheckErrorMessage("The day must be between 1 and 30");
    categorization.checkTableIsEmpty();

    transactionCreation
      .checkAmount(-10.00)
      .setDay(3)
      .createAndCheckErrorMessage("You must enter a label");
    categorization.checkTableIsEmpty();

    transactionCreation
      .checkAmount(-10.00)
      .checkDay(3)
      .setLabel("a transaction")
      .create()
      .checkNoErrorMessage();

    categorization.checkTable(new Object[][]{
      {"03/09/2008", "", "A TRANSACTION", -10.00},
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
      .shouldUpdatePosition()
      .selectAccount("Cash")
      .enterAmountWithoutValidating(10.00)
      .enterDayWithoutValidating(1)
      .enterLabelWithoutValidating("Transaction 1")
      .create();

    mainAccounts.checkPosition("Cash", 90.00);

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

    mainAccounts.checkPosition("Cash", 80.00);

    views.selectCategorization();
    transactionCreation
      .enterAmountWithoutValidating(20.00)
      .enterDayWithoutValidating(2)
      .enterLabelWithoutValidating("Transaction 2")
      .create();

    mainAccounts.checkPosition("Cash", 60.00);
    resetWindow();
  }

  public void testMirrorTransactionIsUpdated() throws Exception {
    mainAccounts.createNewAccount()
      .setName("Cash")
      .setAccountNumber("012345")
      .setPosition(100.00)
      .selectBank("CIC")
      .validate();

    mainAccounts.createSavingsAccount("Livret A", 100.00);

    budgetView.savings
      .createSeries()
      .setName("virement manuel vers livret A")
      .setFromAccount("Cash")
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

    mainAccounts.checkPosition("Cash", 90.00);
    savingsAccounts.checkPosition("Livret A", 110.00);

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

    mainAccounts.checkPosition("Cash", 80.00);
    savingsAccounts.checkPosition("Livret A", 120.00);

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

    mainAccounts.checkPosition("Cash", 60.00);
    savingsAccounts.checkPosition("Livret A", 140.00);
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
    setCurrentDate("2012/11/03");
    operations.changeDate();
    operations.openPreferences().setFutureMonthsCount(4).validate();

    mainAccounts.createNewAccount()
      .setName("Main account")
      .setAccountNumber("012345")
      .setPosition(1000.00)
      .selectBank("CIC")
      .validate();

    mainAccounts.checkPosition("Main account", 1000.);

    views.selectBudget();
    budgetView.recurring.createSeries().setName("Courses").setAmount(30)
      .setPropagationEnabled().validate();

    timeline.selectMonth("2012/11");
    transactionCreation
      .show()
      .create(2, "Transaction 1", -10);
    categorization.setRecurring("Transaction 1", "Courses");

    mainAccounts.checkPosition("Main account", 990.);

    views.selectCategorization();
    timeline.selectMonth("2012/12");
    transactionCreation
      .shouldNotUpdatePosition()
      .create(10, "Transaction 2", -10);

    mainAccounts.checkPosition("Main account", 990.);

    views.selectCategorization();
    timeline.selectMonth("2012/11");
    transactionCreation
      .shouldNotUpdatePosition()
      .create(3, "Transaction 4", -20);

    mainAccounts.checkPosition("Main account", 990.);

    views.selectCategorization();
    timeline.selectMonth("2013/01");
    transactionCreation
      .shouldUpdatePosition()
      .create(10, "Transaction 3", -10);

    mainAccounts.checkPosition("Main account", 990.);
    mainAccounts.editPosition("Main account").checkOperationLabel("TRANSACTION 4")
      .validate();

    timeline.selectAll();

    views.selectData();
    transactions.showPlannedTransactions().initAmountContent()
      .add("04/03/2013", "Planned: Courses", -30.00, "Courses", 870.00, 870.00, "Main account")
      .add("04/02/2013", "Planned: Courses", -30.00, "Courses", 900.00, 900.00, "Main account")
      .add("10/01/2013", "TRANSACTION 3", -10.00, "Courses", 930.00, 930.00, "Main account")
      .add("04/01/2013", "Planned: Courses", -20.00, "Courses", 940.00, 940.00, "Main account")
      .add("10/12/2012", "TRANSACTION 2", -10.00, "Courses", 960.00, 960.00, "Main account")
      .add("04/12/2012", "Planned: Courses", -20.00, "Courses", 970.00, 970.00, "Main account")
      .add("03/11/2012", "TRANSACTION 4", -20.00, "Courses", 990.00, 990.00, "Main account")
      .add("02/11/2012", "TRANSACTION 1", -10.00, "Courses", 1010.00, 1010.00, "Main account")
      .check();
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
      .createToReconciled();
    
    categorization.initContent()
      .add("15/08/2012", "Gastronomie", "BURGER KING", -10.00)
      .add("16/08/2012", "Gastronomie", "[R] BURGER KING", -15.00)
      .check();
    
    transactionCreation.createToBeReconciled(17, "Auchan", -100.00);
    categorization.initContent()
      .add("17/08/2012", "", "[R] AUCHAN", -100.00)
      .add("15/08/2012", "Gastronomie", "BURGER KING", -10.00)
      .add("16/08/2012", "Gastronomie", "[R] BURGER KING", -15.00)
      .check();

    categorization.setNewVariable("[R] Auchan", "Groceries", -200.00);
    transactionCreation.createToBeReconciled(18, "Auchan", -150.00);
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
      .createToBeReconciled(15, "Auchan", -50.00);

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

  public void testDoNotUpdateAccountPosition() throws Exception {
    mainAccounts.createNewAccount()
      .setName("Main")
      .selectBank("CIC")
      .setPosition(1000.00)
      .validate();

    transactionCreation.show()
      .shouldNotUpdatePosition()
      .create(15, "Auchan", -50.00);
    views.selectData();
    mainAccounts.checkPosition("Main", 1000);

    views.selectCategorization();
    transactionCreation
      .shouldUpdatePosition()
      .create(15, "Auchan", -50.00);
    mainAccounts.checkPosition("Main", 950);
  }

  public void testInDeferredAccount() throws Exception {
    OfxBuilder.init(this)
      .addCardAccount("1111", 0, "2008/06/30")
      .addTransaction("2008/06/27", -50, "Auchan 1")
      .addBankAccount("1234", 1000, "2008/06/30")
      .addTransaction("2008/06/28", -550, "Prelevement")
      .loadDeferredCard("Card n. 1111", "Account n. 1234");

    mainAccounts.edit("Card n. 1111").setDeferred(27, 28, 0).validate();

    transactionCreation.show()
      .selectAccount("Card n. 1111")
      .checkUpdatePositionNotVisible()
      .createToBeReconciled(27, "Auchan 2", -40.00);
    mainAccounts.checkPosition("Card n. 1111", -90);

    OfxBuilder.init(this)
      .addCardAccount("1111", 0, "2008/06/30")
      .addTransaction("2008/06/27", -40, "Auchan imported")
      .load();

    mainAccounts.checkPosition("Card n. 1111", -130);

    views.selectCategorization();
    categorization
      .selectTransaction("[R] Auchan 2")
      .switchToReconciliation()
      .select("AUCHAN IMPORTED")
      .reconcile();

    mainAccounts.checkPosition("Card n. 1111", -90);

  }

  public void testUpdateAccountPositionInImportedAccountWithPlanned() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/16", -30., "Burger King")
      .addTransaction("2008/07/18", -30., "Burger King")
      .addTransaction("2008/08/15", -15, "McDo")
      .load();

    views.selectCategorization();
    categorization
      .setNewRecurring("Burger King", "course")
      .setRecurring("McDo", "course");

    transactionCreation.show()
      .selectAccount("Account n. 00001123")
      .createToBeReconciled(31, "Brico", -10);

    views.selectData();
    transactions.showPlannedTransactions().initAmountContent()
      .add("31/08/2008", "BRICO", -10.00, "To categorize", -25.00, -25.00, "Account n. 00001123")
      .add("19/08/2008", "Planned: course", -15.00, "course", -15.00, -15.00, "Account n. 00001123")
      .add("15/08/2008", "MCDO", -15.00, "course", 0.00, 0.00, "Account n. 00001123")
      .check();
    mainAccounts.checkPosition("Account n. 00001123", 0);
    mainAccounts.checkSummary(0, "2008/08/15");
  }

  public void testUpdateAccountPositionInDifferentImportedAccountWithPlanned() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/16", -30., "Burger King")
      .addTransaction("2008/07/18", -30., "Burger King")
      .addTransaction("2008/08/15", -15, "McDo")
      .load();

    views.selectCategorization();
    categorization
      .setNewRecurring("Burger King", "course")
      .setRecurring("McDo", "course");

    mainAccounts.createNewAccount()
      .setName("Main")
      .selectBank("CIC")
      .setPosition(1000.00)
      .validate();

    transactionCreation.show()
      .selectAccount("Main")
      .create(15, "Brico", -10)
      .createToBeReconciled(31, "Brico", -10);

    views.selectData();
    transactions.showPlannedTransactions().initAmountContent()
      .add("31/08/2008", "BRICO", -10.00, "To categorize", 980.00, 965.00, "Main")
      .add("19/08/2008", "Planned: course", -15.00, "course", -15.00, 975.00, "Account n. 00001123")
      .add("15/08/2008", "BRICO", -10.00, "To categorize", 990.00, 990.00, "Main")
      .add("15/08/2008", "MCDO", -15.00, "course", 0.00, 1000.00, "Account n. 00001123")
      .check();

    mainAccounts.checkPosition("Account n. 00001123", 0);
    mainAccounts.checkPosition("Main", 990);
    mainAccounts.checkSummary(990, "2008/08/15");
  }

  public void testDifferentDateForEachAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/15", -15, "Auchan")
      .load();

    mainAccounts.createNewAccount()
      .setName("Main")
      .selectBank("CIC")
      .setPosition(1000.00)
      .validate();

    transactionCreation.show()
      .shouldUpdatePosition()
      .selectAccount("Main")
      .create(10, "manual op", -50.00)
      .create(28, "manual op", -50.00)
      .create(31, "manual op", -50.00);

    mainAccounts.checkSummary(850, "2008/08/31");

    transactionCreation
      .selectAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .createToBeReconciled(16, "Auchan", -50.00);

    mainAccounts.checkSummary(850, "2008/08/31");

    views.selectCategorization();
    categorization
      .setNewRecurring("AUCHAN", "bricolage", -300.);
    categorization
      .selectTransaction("[R] AUCHAN")
      .selectRecurring().selectSeries("bricolage");

    categorization
      .setNewRecurring("MANUAL OP", "course", -200.);

    views.selectData();
    transactions.showPlannedTransactions().initAmountContent()
      .add("31/08/2008", "Planned: course", -50.00, "course", 800.00, 515.00, "Main")
      .add("31/08/2008", "Planned: bricolage", -235.00, "bricolage", -285.0, 565., "Account n. 00001123")
      .add("31/08/2008", "MANUAL OP", -50.00, "course", 850.00, 800.00, "Main")
      .add("28/08/2008", "MANUAL OP", -50.00, "course", 900.00, 850.00, "Main")
      .add("16/08/2008", "AUCHAN", -50.00, "bricolage", -50.00, 900.00, "Account n. 00001123")
      .add("15/08/2008", "AUCHAN", -15.00, "bricolage", 0.00, 950.00, "Account n. 00001123")
      .add("10/08/2008", "MANUAL OP", -50.00, "course", 950.00, 965.00, "Main")
      .check();

    mainAccounts.checkPosition("Account n. 00001123", 0.);
    mainAccounts.checkAccountUpdateDate("Account n. 00001123", "2008/08/15");
    mainAccounts.checkPosition("Main", 850);
    mainAccounts.checkAccountUpdateDate("Main", "2008/08/31");
    mainAccounts.checkSummary(850, "2008/08/31");
  }
}
