package com.budgetview.functests.transactions;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.TransactionType;

public class TransactionEditionTest extends LoggedInFunctionalTestCase {

  public void testLabel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/16", -27.50, "Burger King 1")
      .addTransaction("2008/06/18", -15.10, "Burger King 2")
      .addTransaction("2008/06/15", -15.50, "McDo")
      .load();

    categorization.selectTableRow(2);
    transactionDetails.checkLabelIsNotEditable();
    transactionDetails.checkLabel("MCDO");
    transactionDetails.edit()
      .checkTitle("Edit operations")
      .checkOriginalLabel("MCDO")
      .checkLabelSelected()
      .setLabel("Mac Donalds")
      .validate();
    categorization.initContent()
      .add("16/06/2008", "", "BURGER KING 1", -27.50)
      .add("18/06/2008", "", "BURGER KING 2", -15.10)
      .add("15/06/2008", "", "MAC DONALDS", -15.50)
      .check();

    categorization.selectTableRows(0, 1);
    transactionDetails.checkLabel("BURGER KING [2 operations]");
    transactionDetails.checkLabelIsNotEditable();
    categorization.edit(0)
      .checkOriginalLabel("(several operations selected)")
      .setLabelAndPressEnter("Burger Queen");

    categorization.initContent()
      .add("16/06/2008", "", "BURGER QUEEN", -27.50)
      .add("18/06/2008", "", "BURGER QUEEN", -15.10)
      .add("15/06/2008", "", "MAC DONALDS", -15.50)
      .check();
  }

  public void testCannotSetEmptyNamesOrEditPlannedTransactions() throws Exception {
    
    operations.openPreferences().setFutureMonthsCount(2).validate();
    
    OfxBuilder.init(this)
      .addTransaction("2008/07/18", -30.00, "Burger King")
      .addTransaction("2008/08/18", -30.00, "Burger King")
      .load();

    categorization.selectAllTransactions();

    categorization.selectVariable().selectNewSeries("Food", -30.00);

    timeline.selectAll();
    transactions.showPlannedTransactions();
    transactions.initContent()
      .add("19/10/2008", TransactionType.PLANNED, "Planned: Food", "", -30.00, "Food")
      .add("19/09/2008", TransactionType.PLANNED, "Planned: Food", "", -30.00, "Food")
      .add("18/08/2008", TransactionType.PRELEVEMENT, "BURGER KING", "", -30.00, "Food")
      .add("18/07/2008", TransactionType.PRELEVEMENT, "BURGER KING", "", -30.00, "Food")
      .check();

    transactions.edit(2)
      .checkOriginalLabel("BURGER KING")
      .setLabel("")
      .checkLabelError("You must enter a label")
      .setLabel("Burger Queen")
      .checkNoTipShown()
      .validate();

    transactions.initContent()
      .add("19/10/2008", TransactionType.PLANNED, "Planned: Food", "", -30.00, "Food")
      .add("19/09/2008", TransactionType.PLANNED, "Planned: Food", "", -30.00, "Food")
      .add("18/08/2008", TransactionType.PRELEVEMENT, "BURGER QUEEN", "", -30.00, "Food")
      .add("18/07/2008", TransactionType.PRELEVEMENT, "BURGER KING", "", -30.00, "Food")
      .check();

    transactions.checkEditionRejected(new int[]{2, 1},
                                      "You cannot change the label of planned transactions.");
  }

  public void testRenamingTransactionsPreservesAutoCategorization() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/18", -30.00, "Burger King")
      .load();

    categorization.setNewVariable("Burger King", "Food", -30.00);

    categorization.edit(0)
      .setLabelAndPressEnter("Mac Do");

    OfxBuilder.init(this)
      .addTransaction("2008/08/18", -30.00, "Burger King")
      .load();

    categorization.initContent()
      .add("18/08/2008", "Food", "BURGER KING", -30.00)
      .add("18/07/2008", "Food", "MAC DO", -30.00)
      .check();
  }

  public void testDeletionWithAndWithoutUpdatingTheAccountPosition() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("1234", 1000.00, "2008/06/30")
      .addTransaction("2008/06/16", -30.00, "Burger King 1")
      .addTransaction("2008/06/18", -50.00, "Burger King 2")
      .addTransaction("2008/06/20", -60.00, "Burger King 3")
      .addTransaction("2008/06/15", -20.00, "McDo")
      .load();

    transactions.initContent()
      .add("20/06/2008", TransactionType.PRELEVEMENT, "BURGER KING 3", "", -60.00)
      .add("18/06/2008", TransactionType.PRELEVEMENT, "BURGER KING 2", "", -50.00)
      .add("16/06/2008", TransactionType.PRELEVEMENT, "BURGER KING 1", "", -30.00)
      .add("15/06/2008", TransactionType.PRELEVEMENT, "MCDO", "", -20.00)
      .check();

    transactions.deleteAndUpdatePosition("BURGER KING 2");
    transactions.initContent()
      .add("20/06/2008", TransactionType.PRELEVEMENT, "BURGER KING 3", "", -60.00)
      .add("16/06/2008", TransactionType.PRELEVEMENT, "BURGER KING 1", "", -30.00)
      .add("15/06/2008", TransactionType.PRELEVEMENT, "MCDO", "", -20.00)
      .check();
    mainAccounts.checkAccount("Account n. 1234", 1050.00, "2008/06/20");

    transactions.deleteWithoutUpdatingThePosition("BURGER KING 3");
    transactions.initContent()
      .add("16/06/2008", TransactionType.PRELEVEMENT, "BURGER KING 1", "", -30.00)
      .add("15/06/2008", TransactionType.PRELEVEMENT, "MCDO", "", -20.00)
      .check();
    mainAccounts.checkAccount("Account n. 1234", 1050.00, "2008/06/20");
  }

  public void testEditAmountAndDateOfManualTransaction() throws Exception {
    accounts.createNewAccount()
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

    categorization.edit(0)
      .setLabel("Transaction 1 renamed")
      .checkDateAndAmountShown()
      .setDay("12")
      .setMonth(200807)
      .setAmount(-20.00)
      .validate();

    categorization.checkTable(new Object[][]{
      {"12/07/2008", "", "TRANSACTION 1 RENAMED", -20.00},
    });

    categorization.edit(0)
      .checkDateAndAmountShown()
      .setDay("")
      .validateAndCheckDayError("You must enter a value between 1 and 31")
      .setDay("15")
      .checkNoTipShown()
      .clearAmount()
      .validateAndCheckAmountError("You must enter an amount")
      .setAmount(-15)
      .validate();

    categorization.checkTable(new Object[][]{
      {"15/07/2008", "", "TRANSACTION 1 RENAMED", -15.00},
    });
  }

  public void testCannotEditImportedTransactions() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/18", -30.00, "Burger King")
      .addTransaction("2008/08/19", -35.00, "MacDo")
      .load();

    accounts.createNewAccount()
      .setName("Cash")
      .setAccountNumber("012345")
      .selectBank("CIC")
      .validate();

    categorization.selectAllTransactions();
    categorization.edit(0)
      .checkImportedTransactionsMessage()
      .cancel();

    categorization.initContent()
      .add("18/08/2008", "", "BURGER KING", -30.00)
      .add("19/08/2008", "", "MACDO", -35.00)
      .check();
  }

  public void testCannotEditDateAndAmountsForMultiSelection() throws Exception {
    accounts.createNewAccount()
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

    transactionCreation
      .enterAmountWithoutValidating(10.00)
      .enterDayWithoutValidating(3)
      .enterLabelWithoutValidating("Transaction 2")
      .create()
      .checkNoErrorMessage();

    categorization.initContent()
      .add("03/08/2008", "", "TRANSACTION 1", -10.00)
      .add("03/08/2008", "", "TRANSACTION 2", -10.00)
      .check();

    categorization
      .selectAllTransactions()
      .edit(0)
      .checkMultiselectionMessage()
      .setLabel("New name")
      .validate();

    categorization.initContent()
      .add("03/08/2008", "", "NEW NAME", -10.00)
      .add("03/08/2008", "", "NEW NAME", -10.00)
      .check();
  }

  public void testAllAmountsAreProperlyUpdatedWhenATransactionIsEdited() throws Exception {
    accounts.createNewAccount()
      .setName("Cash")
      .setAccountNumber("012345")
      .selectBank("CIC")
      .validate();

    timeline.selectMonth(200808);
    transactionCreation
      .show()
      .create(10, "Auchan", -100.00)
      .create(15, "MacDo", -50.00)
      .selectMonth(200807)
      .create(12, "Auchan", -200.00)
      .create(17, "MacDo", -10.00);

    timeline.selectMonth(200808);
    categorization.setNewVariable("AUCHAN", "Groceries", -300.00);
    categorization.setNewVariable("MACDO", "Food", -100.00);

    mainAccounts.editPosition("Cash").setAmount(1000.00).validate();

    timeline.selectMonths(200807, 200808);
    transactions.initAmountContent()
      .add("15/08/2008", "MACDO", -50.00, "Food", 1000.00, 1000.00, "Cash")
      .add("10/08/2008", "AUCHAN", -100.00, "Groceries", 1050.00, 1050.00, "Cash")
      .add("17/07/2008", "MACDO", -10.00, "Food", 1150.00, 1150.00, "Cash")
      .add("12/07/2008", "AUCHAN", -200.00, "Groceries", 1160.00, 1160.00, "Cash")
      .check();

    categorization.initContent()
      .add("12/07/2008", "Groceries", "AUCHAN", -200.00)
      .add("10/08/2008", "Groceries", "AUCHAN", -100.00)
      .add("17/07/2008", "Food", "MACDO", -10.00)
      .add("15/08/2008", "Food", "MACDO", -50.00)
      .check();

    categorization.edit(3)
      .checkDay("15")
      .setDay("13")
      .setMonth(200807)
      .checkAmount(-50.00)
      .setAmount(-20.00)
      .validate();

    transactions.initAmountContent()
      .add("10/08/2008", "AUCHAN", -100.00, "Groceries", 1030.00, 1030.00, "Cash")
      .add("17/07/2008", "MACDO", -10.00, "Food", 1130.00, 1130.00, "Cash")
      .add("13/07/2008", "MACDO", -20.00, "Food", 1140.00, 1140.00, "Cash")
      .add("12/07/2008", "AUCHAN", -200.00, "Groceries", 1160.00, 1160.00, "Cash")
      .check();

    timeline.selectMonth(200807);
    budgetView.variable.checkSeries("Groceries", -200.00, -300.00);
    budgetView.variable.checkSeries("Food", -30.00, -100.00);

    timeline.selectMonth(200808);
    budgetView.variable.checkSeries("Groceries", -100.00, -300.00);
    budgetView.variable.checkSeries("Food", 0.00, -100.00);
  }
}
