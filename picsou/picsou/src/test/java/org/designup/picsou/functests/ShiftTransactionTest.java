package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class ShiftTransactionTest extends LoggedInFunctionalTestCase {

  public void testShift() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/05/01", -10.00, "Non shiftable - First month")
      .addTransaction("2008/06/09", -13.00, "Shiftable to previous")
      .addTransaction("2008/06/10", -15.10, "Non shiftable - middle of month 1")
      .addTransaction("2008/06/15", -17.10, "Non shiftable - middle of month 2")
      .addTransaction("2008/06/20", -11.10, "Non shiftable - middle of month 3")
      .addTransaction("2008/06/21", -77.50, "Shiftable to next")
      .addTransaction("2008/07/25", -27.50, "Non shiftable - Last month")
      .load();

    views.selectCategorization();

    transactionDetails.checkShiftDisabled();

    categorization.selectAllTransactions();
    categorization.selectEnvelopes();
    categorization.selectEnvelopes().selectNewSeries("An enveloppe");
    categorization.checkTable(new Object[][]{
      {"01/05/2008", "An enveloppe", "NON SHIFTABLE - FIRST MONTH", -10.00},
      {"25/07/2008", "An enveloppe", "NON SHIFTABLE - LAST MONTH", -27.50},
      {"10/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 1", -15.10},
      {"15/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 2", -17.10},
      {"20/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 3", -11.10},
      {"21/06/2008", "An enveloppe", "SHIFTABLE TO NEXT", -77.50},
      {"09/06/2008", "An enveloppe", "SHIFTABLE TO PREVIOUS", -13.00}
    });

    transactionDetails.checkShiftDisabled();

    categorization.selectTransaction("SHIFTABLE TO NEXT");
    transactionDetails.checkShiftEnabled();

    categorization.selectNoTableRow();
    transactionDetails.checkShiftDisabled();

    categorization.selectTransaction("NON SHIFTABLE - MIDDLE OF MONTH 1");
    transactionDetails.checkShiftDisabled();

    categorization.selectTransaction("NON SHIFTABLE - MIDDLE OF MONTH 2");
    transactionDetails.checkShiftDisabled();

    categorization.selectTransaction("NON SHIFTABLE - MIDDLE OF MONTH 3");
    transactionDetails.checkShiftDisabled();

    categorization.selectTransaction("NON SHIFTABLE - LAST MONTH");
    transactionDetails.checkShiftDisabled();

    categorization.selectTransaction("NON SHIFTABLE - FIRST MONTH");
    transactionDetails.checkShiftDisabled();

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

    categorization.checkTable(new Object[][]{
      {"01/05/2008", "An enveloppe", "NON SHIFTABLE - FIRST MONTH", -10.00},
      {"25/07/2008", "An enveloppe", "NON SHIFTABLE - LAST MONTH", -27.50},
      {"10/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 1", -15.10},
      {"15/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 2", -17.10},
      {"20/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 3", -11.10},
      {"01/07/2008", "An enveloppe", "SHIFTABLE TO NEXT", -77.50},
      {"31/05/2008", "An enveloppe", "SHIFTABLE TO PREVIOUS", -13.00}
    });

    categorization.selectTransaction("SHIFTABLE TO NEXT");
    transactionDetails.checkShiftInverted();
    transactionDetails.unshift();
    transactionDetails.checkShiftEnabled();

    categorization.selectTransaction("SHIFTABLE TO PREVIOUS");
    transactionDetails.checkShiftInverted();
    transactionDetails.unshift();
    transactionDetails.checkShiftEnabled();

    categorization.checkTable(new Object[][]{
      {"01/05/2008", "An enveloppe", "NON SHIFTABLE - FIRST MONTH", -10.00},
      {"25/07/2008", "An enveloppe", "NON SHIFTABLE - LAST MONTH", -27.50},
      {"10/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 1", -15.10},
      {"15/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 2", -17.10},
      {"20/06/2008", "An enveloppe", "NON SHIFTABLE - MIDDLE OF MONTH 3", -11.10},
      {"21/06/2008", "An enveloppe", "SHIFTABLE TO NEXT", -77.50},
      {"09/06/2008", "An enveloppe", "SHIFTABLE TO PREVIOUS", -13.00}
    });
  }

  public void testShiftingToNextOrPreviousYear() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/12/15", -10.00, "Non shiftable 1")
      .addTransaction("2008/12/25", -77.50, "Shiftable to next")
      .addTransaction("2009/01/01", -13.00, "Shiftable to previous")
      .addTransaction("2008/01/15", -15.10, "Non shiftable 2")
      .load();

    views.selectCategorization();

    transactionDetails.checkShiftDisabled();

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

    categorization.checkTable(new Object[][]{
      {"15/12/2008", "", "NON SHIFTABLE 1", -10.0},
      {"15/01/2008", "", "NON SHIFTABLE 2", -15.1},
      {"01/01/2009", "", "SHIFTABLE TO NEXT", -77.5},
      {"31/12/2008", "", "SHIFTABLE TO PREVIOUS", -13.0}
    });
  }

  public void testAmountsAreProperlyUpdatedDuringAShiftAndAnUnshift() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 12345, "00001234", 100.00, "2008/07/15")
      .addTransaction("2008/06/15", -15.00, "Monoprix / June ")
      .addTransaction("2008/06/25", -10.00, "Monoprix / End of june")
      .addTransaction("2008/07/15", -12.00, "Monoprix / July")
      .load();

    views.selectCategorization();
    categorization.selectAllTransactions();
    categorization.selectEnvelopes().selectNewSeries("Groceries");

    views.selectHome();
    mainAccounts.checkAccount("Account n. 00001234", 100.00, "2008/07/15");
    timeline.selectMonth("2008/06");
    mainAccounts.checkEstimatedPosition(112.00);
    timeline.selectMonth("2008/07");
    mainAccounts.checkEstimatedPosition(87.00);

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkTotalAmounts(-25.00, -25.00);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkTotalAmounts(-12.00, -25.00);

    views.selectCategorization();
    categorization.selectTransaction("Monoprix / End of june");
    transactionDetails.shift();
    categorization.checkTable(new Object[][]{
      {"01/07/2008", "Groceries", "MONOPRIX / END OF JUNE", -10.0},
      {"15/07/2008", "Groceries", "MONOPRIX / JULY", -12.0},
      {"15/06/2008", "Groceries", "MONOPRIX / JUNE", -15.0},
    });

    // Account positions are unchanged
    views.selectHome();
    timeline.selectMonth("2008/06");
    mainAccounts.checkEstimatedPosition(112.00);
    timeline.selectMonth("2008/07");
    mainAccounts.checkEstimatedPosition(100.00);

    // Series are updated
    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkTotalAmounts(-15.00, -15.00);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkTotalAmounts(-22.00, -15.00);
  }

  public void testShiftingASplittedTransaction() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 12345, "00001234", 100.00, "2008/07/15")
      .addTransaction("2008/06/25", -25.00, "Monoprix / June ")
      .addTransaction("2008/07/15", -12.00, "Monoprix / July")
      .load();

    views.selectCategorization();
    categorization.selectAllTransactions();
    categorization.selectEnvelopes().selectNewSeries("Groceries");

    views.selectHome();
    mainAccounts.checkAccount("Account n. 00001234", 100.00, "2008/07/15");
    timeline.selectMonth("2008/06");
    mainAccounts.checkEstimatedPosition(112.00);
    timeline.selectMonth("2008/07");
    mainAccounts.checkEstimatedPosition(87.00);

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkTotalAmounts(-25.00, -25.00);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkTotalAmounts(-12.00, -25.00);

    views.selectCategorization();
    categorization.selectTransaction("Monoprix / June");
    transactionDetails.split("10.00", "dvd");
    categorization.checkTable(new Object[][]{
      {"15/07/2008", "Groceries", "MONOPRIX / JULY", -12.0},
      {"25/06/2008", "Groceries", "MONOPRIX / JUNE", -15.0},
      {"25/06/2008", "", "MONOPRIX / JUNE", -10.0},
    });

    categorization.selectTableRow(2);
    categorization.selectEnvelopes().selectNewSeries("Leisures");
    transactionDetails.shift();
    categorization.checkTable(new Object[][]{
      {"15/07/2008", "Groceries", "MONOPRIX / JULY", -12.0},
      {"25/06/2008", "Groceries", "MONOPRIX / JUNE", -15.0},
      {"01/07/2008", "Leisures", "MONOPRIX / JUNE", -10.0},
    });

    // Account positions are unchanged
    views.selectHome();
    timeline.selectMonth("2008/06");
    mainAccounts.checkEstimatedPosition(112.00);
    timeline.selectMonth("2008/07");
    mainAccounts.checkEstimatedPosition(97.00);

    // Series are updated
    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkTotalAmounts(-15.00, -15.00);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkTotalAmounts(-22.00, -15.00);

    views.selectCategorization();
    transactionDetails.openSplitDialog()
      .deleteRow(1)
      .validate();
    categorization.checkTable(new Object[][]{
      {"15/07/2008", "Groceries", "MONOPRIX / JULY", -12.0},
      {"25/06/2008", "Groceries", "MONOPRIX / JUNE", -25.0},
    });

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.envelopes.checkTotalAmounts(-25.00, -25.00);
    timeline.selectMonth("2008/07");
    budgetView.envelopes.checkTotalAmounts(-12.00, -25.00);
  }

  public void testSeriesNotValid() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/25", -25.00, "Monoprix / June")
      .addTransaction("2008/07/05", -30.00, "Monoprix / July")
      .load();

    views.selectCategorization();
    categorization.selectTransaction("Monoprix / June");
    categorization.selectEnvelopes().createSeries("Groceries");

    transactionDetails.checkShiftEnabled();
    categorization.editSeries("Groceries")
      .setEndDate(200806)
      .validate();
    transactionDetails.checkShiftEnabled();
    SeriesEditionDialogChecker.open(
      transactionDetails.openShiftDialog()
        .checkTitle("Shift impossible")
        .checkMessageContains("The operation is assigned to a series which is not active for July 2008. " +
                              "Do you want to edit the series?")
        .getOkTrigger())
      .checkName("Groceries")
      .removeEndDate()
      .validate();
    transactionDetails.checkShiftEnabled();
    transactionDetails.shift();
    categorization.checkTable(new Object[][]{
      {"05/07/2008", "", "MONOPRIX / JULY", -30.0},
      {"01/07/2008", "Groceries", "MONOPRIX / JUNE", -25.0},
    });
    categorization.setUncategorized();
    transactionDetails.unshift();

    categorization.setEnvelope("Monoprix / July", "Groceries");
    transactionDetails.checkShiftEnabled();
    categorization.editSeries("Groceries")
      .setStartDate(200807)
      .validate();
    transactionDetails.checkShiftEnabled();
    SeriesEditionDialogChecker.open(
      transactionDetails.openShiftDialog()
        .checkTitle("Shift impossible")
        .checkMessageContains("The operation is assigned to a series which is not active for June 2008. " +
                              "Do you want to edit the series?")
        .getOkTrigger())
      .checkName("Groceries")
      .removeStartDate()
      .validate();
    transactionDetails.shift();

    categorization.checkTable(new Object[][]{
      {"30/06/2008", "Groceries", "MONOPRIX / JULY", -30.0},
      {"25/06/2008", "", "MONOPRIX / JUNE", -25.0},
    });
  }

  public void testShiftingAMirroredTransaction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/25", -25.00, "Epargne / June ")
      .addTransaction("2008/07/05", -30.00, "Epargne / July")
      .load();

    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne", 0);

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Epargne")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne")
      .validate();

    views.selectCategorization();
    categorization.selectAllTransactions().selectSavings().selectSeries("Epargne");

    categorization.selectTransaction("Epargne / July");
    transactionDetails.shift();
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Epargne", "", 55.00, "Epargne")
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Epargne", "", -55.00, "Epargne")
      .add("05/07/2008", TransactionType.PLANNED, "Planned: Epargne", "", 55.00, "Epargne")
      .add("05/07/2008", TransactionType.PLANNED, "Planned: Epargne", "", -55.00, "Epargne")
      .add("30/06/2008", "05/07/2008", TransactionType.VIREMENT, "EPARGNE / JULY", "", 30.00, "Epargne")
      .add("30/06/2008", "05/07/2008", TransactionType.PRELEVEMENT, "EPARGNE / JULY", "", -30.00, "Epargne")
      .add("25/06/2008", TransactionType.VIREMENT, "EPARGNE / JUNE", "", 25.00, "Epargne")
      .add("25/06/2008", TransactionType.PRELEVEMENT, "EPARGNE / JUNE", "", -25.00, "Epargne")
      .check();
  }
}