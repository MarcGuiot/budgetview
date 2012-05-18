package org.designup.picsou.functests.transactions;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

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
      .checkValidationError("You must enter a label")
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

  public void testEditingATransactionAlsoChangesItsMirror() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();

    savingsAccounts.createSavingsAccount("Livret", 1000.00);
    budgetView.savings.createSeries()
      .setName("ToSavings")
      .setFromAccount("Main accounts")
      .setToAccount("Livret")
      .validate();

    categorization.setSavings("Virement", "ToSavings");

    timeline.selectAll();
    transactions.initAmountContent()
      .add("10/08/2008", "VIREMENT", 100.00, "ToSavings", 1000.00, 1000.00, "Livret")
      .add("10/08/2008", "VIREMENT", -100.00, "ToSavings", 0.00, 0.00, "Account n. 00001123")
      .check();

    transactions.edit(0)
      .setLabel("Virt epargne")
      .validate();

    transactions.initAmountContent()
      .add("10/08/2008", "VIRT EPARGNE", 100.00, "ToSavings", 1000.00, 1000.00, "Livret")
      .add("10/08/2008", "VIRT EPARGNE", -100.00, "ToSavings", 0.00, 0.00, "Account n. 00001123")
      .check();

    transactions.edit(1)
      .setLabel("Epargne")
      .validate();

    transactions.initAmountContent()
      .add("10/08/2008", "EPARGNE", 100.00, "ToSavings", 1000.00, 1000.00, "Livret")
      .add("10/08/2008", "EPARGNE", -100.00, "ToSavings", 0.00, 0.00, "Account n. 00001123")
      .check();
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
}
