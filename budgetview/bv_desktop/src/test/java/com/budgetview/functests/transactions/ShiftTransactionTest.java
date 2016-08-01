package com.budgetview.functests.transactions;

import com.budgetview.functests.checkers.SeriesEditionDialogChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.TransactionType;
import org.junit.Test;

public class ShiftTransactionTest extends LoggedInFunctionalTestCase {

  @Test
  public void testShift() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/05/01", -10.00, "shiftable - First month")
      .addTransaction("2008/06/09", -13.00, "Shiftable to previous")
      .addTransaction("2008/06/10", -15.10, "Non shiftable - middle of month 1")
      .addTransaction("2008/06/15", -17.10, "Non shiftable - middle of month 2")
      .addTransaction("2008/06/20", -11.10, "Non shiftable - middle of month 3")
      .addTransaction("2008/06/21", -77.50, "Shiftable to next")
      .addTransaction("2008/07/25", -27.50, "shiftable - Last month")
      .load();

    transactionDetails.checkActionsHidden();

    categorization.selectAllTransactions();
    categorization.selectVariable();
    categorization.selectVariable().selectNewSeries("An enveloppe");
    transactionDetails.checkBudgetDateNotVisible("SHIFTABLE TO NEXT");
    transactionDetails.checkBudgetDateNotVisible("SHIFTABLE TO PREVIOUS");

    transactionDetails.checkShiftDisabled();

    categorization.selectTransaction("SHIFTABLE TO NEXT");
    transactionDetails.checkShiftEnabled();

    categorization.selectNoTableRow();
    transactionDetails.checkActionsHidden();

    categorization.selectTransaction("NON SHIFTABLE - MIDDLE OF MONTH 1");
    transactionDetails.checkShiftDisabled();

    categorization.selectTransaction("NON SHIFTABLE - MIDDLE OF MONTH 2");
    transactionDetails.checkShiftDisabled();

    categorization.selectTransaction("NON SHIFTABLE - MIDDLE OF MONTH 3");
    transactionDetails.checkShiftDisabled();

    categorization.selectTransaction("SHIFTABLE - LAST MONTH");
    transactionDetails.checkShiftEnabled();
    transactionDetails.shift();
    timeline.checkDisplays("2008/05", "2008/06", "2008/07", "2008/08");

    categorization.selectTransaction("SHIFTABLE - FIRST MONTH");
    transactionDetails.checkShiftEnabled();
    transactionDetails.shift();
    timeline.checkDisplays("2008/04", "2008/05", "2008/06", "2008/07", "2008/08");

    categorization.selectTransaction("SHIFTABLE TO NEXT");
    transactionDetails.openShiftDialog()
      .checkMessageContains("next month")
      .validate();
    transactionDetails.checkShiftInverted();

    categorization.selectTransaction("SHIFTABLE TO PREVIOUS");
    transactionDetails.openShiftDialog()
      .checkMessageContains("previous month")
      .validate();
    transactionDetails.checkShiftInverted();

    categorization.checkUserDate(transactionDetails, "2008/07/01", "SHIFTABLE TO NEXT");
    categorization.checkUserDate(transactionDetails, "2008/05/31", "SHIFTABLE TO PREVIOUS");

    categorization.selectTransaction("SHIFTABLE TO NEXT");
    transactionDetails.checkShiftInverted();
    transactionDetails.unshift();
    transactionDetails.checkShiftEnabled();

    categorization.selectTransaction("SHIFTABLE TO PREVIOUS");
    transactionDetails.checkShiftInverted();
    transactionDetails.unshift();
    transactionDetails.checkShiftEnabled();

    transactionDetails.checkBudgetDateNotVisible("SHIFTABLE TO NEXT");
    transactionDetails.checkBudgetDateNotVisible("SHIFTABLE TO PREVIOUS");
  }

  @Test
  public void testShiftingToNextOrPreviousYear() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/12/15", -10.00, "Non shiftable 1")
      .addTransaction("2008/12/25", -77.50, "Shiftable to next")
      .addTransaction("2009/01/01", -13.00, "Shiftable to previous")
      .addTransaction("2008/01/15", -15.10, "Non shiftable 2")
      .load();

    categorization.selectTransaction("SHIFTABLE TO NEXT");
    transactionDetails.openShiftDialog()
      .checkMessageContains("next month")
      .validate();
    transactionDetails.checkShiftInverted();

    categorization.selectTransaction("SHIFTABLE TO PREVIOUS");
    transactionDetails.openShiftDialog()
      .checkMessageContains("previous month")
      .validate();
    transactionDetails.checkShiftInverted();

    categorization.checkUserDate(transactionDetails, "2009/01/01", "SHIFTABLE TO NEXT");
    categorization.checkUserDate(transactionDetails, "2008/12/31", "SHIFTABLE TO PREVIOUS");
  }

  @Test
  public void testAmountsAreProperlyUpdatedDuringAShiftAndAnUnshift() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 12345, "00001234", 100.00, "2008/07/15")
      .addTransaction("2008/06/15", -15.00, "Monoprix / June ")
      .addTransaction("2008/06/25", -10.00, "Monoprix / End of june")
      .addTransaction("2008/07/15", -12.00, "Monoprix / July")
      .load();

    categorization.selectAllTransactions();
    categorization.selectRecurring().selectNewSeries("Groceries");

    mainAccounts.checkAccount("Account n. 00001234", 100.00, "2008/07/15");
    timeline.selectMonth("2008/06");

    mainAccounts.checkEndOfMonthPosition("Account n. 00001234", 112.00);

    timeline.selectMonth("2008/07");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001234", 87.00);

    timeline.selectMonth("2008/06");
    budgetView.recurring.checkTotalAmounts(-25.00, -25.00);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkTotalAmounts(-12.00, -25.00);

    categorization.selectTransaction("Monoprix / End of june");
    transactionDetails.shift();
    categorization.checkUserDate(transactionDetails, "2008/07/01", "MONOPRIX / END OF JUNE");

    // Account positions are unchanged
    timeline.selectMonth("2008/06");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001234", 112.00);
    timeline.selectMonth("2008/07");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001234", 100.00);

    // Balances are updated
    timeline.selectMonth("2008/06");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001234", 112.00);
    timeline.selectMonth("2008/07");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001234", 100.00);

    // Series are updated
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkTotalAmounts(-15.00, -15.00);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkTotalAmounts(-22.00, -15.00);
  }

  @Test
  public void testShiftingASplittedTransaction() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 12345, "00001234", 100.00, "2008/07/15")
      .addTransaction("2008/06/25", -25.00, "Monoprix / June ")
      .addTransaction("2008/07/15", -12.00, "Monoprix / July")
      .load();

    categorization.selectAllTransactions();
    categorization.selectRecurring().selectNewSeries("Groceries");

    mainAccounts.checkAccount("Account n. 00001234", 100.00, "2008/07/15");
    timeline.selectMonth("2008/06");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001234", 112.00);
    timeline.selectMonth("2008/07");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001234", 87.00);

    timeline.selectMonth("2008/06");
    budgetView.recurring.checkTotalAmounts(-25.00, -25.00);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkTotalAmounts(-12.00, -25.00);

    categorization.selectTransaction("Monoprix / June");
    transactionDetails.split("10.00", "dvd");
    categorization.checkTable(new Object[][]{
      {"15/07/2008", "Groceries", "MONOPRIX / JULY", -12.0},
      {"25/06/2008", "Groceries", "MONOPRIX / JUNE", -15.0},
      {"25/06/2008", "", "MONOPRIX / JUNE", -10.0},
    });

    categorization.selectTableRow(2);
    categorization.selectRecurring().selectNewSeries("Leisures");
    transactionDetails.shift();
    transactionDetails.checkBudgetDate("2008/07/01");

    // Account positions are unchanged
    timeline.selectMonth("2008/06");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001234", 112.00);
    timeline.selectMonth("2008/07");
    mainAccounts.checkEndOfMonthPosition("Account n. 00001234", 97.00);

    // Series are updated
    timeline.selectMonth("2008/06");
    budgetView.recurring.checkTotalAmounts(-15.00, -15.00);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkTotalAmounts(-22.00, -25.00);

    views.selectCategorization();
    transactionDetails.openSplitDialog()
      .deleteRow(1)
      .validateAndClose();
    categorization.checkTable(new Object[][]{
      {"15/07/2008", "Groceries", "MONOPRIX / JULY", -12.0},
      {"25/06/2008", "Groceries", "MONOPRIX / JUNE", -25.0},
    });

    timeline.selectMonth("2008/06");
    budgetView.recurring.checkTotalAmounts(-25.00, -25.00);
    timeline.selectMonth("2008/07");
    budgetView.recurring.checkTotalAmounts(-12.00, -25.00);
  }

  @Test
  public void testSeriesNotValid() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/25", -25.00, "Monoprix / June")
      .addTransaction("2008/07/05", -30.00, "Monoprix / July")
      .load();

    categorization.selectTransaction("Monoprix / June");
    categorization.selectVariable().createSeries("Groceries");

    transactionDetails.checkShiftEnabled();
    categorization.editSeries("Groceries")
      .setEndDate(200806)
      .validate();
    transactionDetails.checkShiftEnabled();
    SeriesEditionDialogChecker.open(
      transactionDetails.openShiftDialog()
        .checkTitle("Shift denied")
        .checkMessageContains("The operation is assigned to an envelope which is not active for July 2008. " +
                              "Do you want to edit the envelope anyway?")
        .getOkTrigger())
      .checkName("Groceries")
      .clearEndDate()
      .validate();
    transactionDetails.checkShiftEnabled();
    transactionDetails.shift();
    categorization.checkUserDate(transactionDetails, "2008/07/01", "MONOPRIX / JUNE");

    transactionDetails.unshift();
    categorization.setUncategorized();

    categorization.setVariable("Monoprix / July", "Groceries");
    transactionDetails.checkShiftEnabled();
    categorization.editSeries("Groceries")
      .setStartDate(200807)
      .validate();
    transactionDetails.checkShiftEnabled();
    SeriesEditionDialogChecker.open(
      transactionDetails.openShiftDialog()
        .checkTitle("Shift denied")
        .checkMessageContains("The operation is assigned to an envelope which is not active for June 2008. " +
                              "Do you want to edit the envelope anyway?")
        .getOkTrigger())
      .checkName("Groceries")
      .clearStartDate()
      .validate();
    transactionDetails.shift();

    categorization.checkUserDate(transactionDetails, "2008/06/30", "MONOPRIX / JULY");
  }

  @Test
  public void testMonthUnShiftInDecember() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2007/12/25", -50.00, "Orange")
      .addTransaction("2008/01/3", 15.00, "McDo")
      .load();

    categorization.selectTransaction("Orange");

    transactionDetails.shift();

    categorization.checkUserDate(transactionDetails, "2008/01/01", "ORANGE");
    transactions
      .initContent()
      .add("03/01/2008", TransactionType.VIREMENT, "MCDO", "", 15.00)
      .add("25/12/2007", TransactionType.PRELEVEMENT, "ORANGE", "", -50.00)
      .check();

    categorization.selectTransaction("Orange");
    transactionDetails.unshift();
    timeline.selectAll();
    transactions
      .initContent()
      .add("03/01/2008", TransactionType.VIREMENT, "MCDO", "", 15.00)
      .add("25/12/2007", TransactionType.PRELEVEMENT, "ORANGE", "", -50.00)
      .check();
    transactionDetails.checkBudgetDateNotVisible("ORANGE");
  }

  @Test
  public void testMonthUndoRedo() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2007/12/25", -50.00, "Orange")
      .addTransaction("2008/01/3", 15.00, "McDo")
      .load();

    categorization.selectTransaction("Orange");

    transactionDetails.shift();

    timeline.selectAll();

    transactions
      .initContent()
      .add("03/01/2008", TransactionType.VIREMENT, "MCDO", "", 15.00)
      .add("25/12/2007", TransactionType.PRELEVEMENT, "ORANGE", "", -50.00)
      .check();
    categorization.checkUserDate(transactionDetails, "2008/01/01", "ORANGE");

    operations.undo();
    transactions
      .initContent()
      .add("03/01/2008", TransactionType.VIREMENT, "MCDO", "", 15.00)
      .add("25/12/2007", TransactionType.PRELEVEMENT, "ORANGE", "", -50.00)
      .check();
    operations.redo();
    transactions
      .initContent()
      .add("03/01/2008", TransactionType.VIREMENT, "MCDO", "", 15.00)
      .add("25/12/2007", TransactionType.PRELEVEMENT, "ORANGE", "", -50.00)
      .check();
    categorization.checkUserDate(transactionDetails, "2008/01/01", "ORANGE");
  }
}
