package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class StatTest extends LoggedInFunctionalTestCase {

  public void testCategorizationWithPositiveTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/07/15", -90.00, "Auchan")
      .addTransaction("2008/07/14", -80.00, "Carouf")
      .load();

    categorization.setNewRecurring("Auchan", "Courant");
    categorization.setRecurring("Carouf", "Courant");

    budgetView.recurring.createSeries().setName("Secu")
      .selectAllMonths()
      .setAmount("0")
      .validate();

    OfxBuilder
      .init(this)
      .addTransaction("2008/08/26", -49.90, "ED")
      .addTransaction("2008/08/25", 60.90, "Auchan")
      .addTransaction("2008/08/13", 10.00, "Pharma")
      .load();

    categorization.setRecurring("Pharma", "Secu");
    categorization.setRecurring("ED", "Courant");
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Courant", "", (-(80 + 90) - 60.90 + 49.9) /*181*/, "Courant")
      .add("26/08/2008", TransactionType.PRELEVEMENT, "ED", "", -49.90, "Courant")
      .add("25/08/2008", TransactionType.VIREMENT, "Auchan", "", 60.90, "Courant")
      .add("13/08/2008", TransactionType.VIREMENT, "Pharma", "", 10.00, "Secu")
      .check();

    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeries("Courant", -90 - 80, -90 - 80);
    budgetView.recurring.checkTotalAmounts(-90 - 80, -90 - 80);

    timeline.selectMonth("2008/08");
    budgetView.recurring.checkSeries("Courant", (60.90 - 49.9), -90 - 80);// 11,  -170 == > 181
    budgetView.recurring.checkSeries("Secu", 10, 0);

    budgetView.recurring.checkTotalAmounts(10 + 60.90 - 49.9, -90 - 80);
  }

  public void testChangingSeriesBudgetCanCreatePlannedTransaction() throws Exception {

    budgetView.variable.createSeries().setName("Secu")
      .selectAllMonths()
      .selectPositiveAmounts()
      .setAmount("10").validate();

    OfxBuilder
      .init(this)
      .addTransaction("2008/08/13", 10.00, "Pharma")
      .load();

    categorization.setVariable("Pharma", "Secu");
    transactions.initContent()
      .add("13/08/2008", TransactionType.VIREMENT, "Pharma", "", 10.00, "Secu")
      .check();

    // je ne comprends pas pourquoi il faut faire un selectPositiveAmounts
    //alors que la series est deja en Positif.
    // non reproductible en vrai.
    budgetView.variable.editSeries("Secu").selectAllMonths()
      .selectPositiveAmounts().setAmount("20").validate();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("13/08/2008", TransactionType.PLANNED, "Planned: Secu", "", 10.00, "Secu")
      .add("13/08/2008", TransactionType.VIREMENT, "Pharma", "", 10.00, "Secu")
      .check();
  }

  public void testOverrunIsTakenIntoAccountInMonthSummaryView() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/07/15", -90.00, "Auchan")
      .addTransaction("2008/07/14", -80.00, "Carouf")
      .load();

    categorization.setNewRecurring("Auchan", "Auchan");
    categorization.setNewRecurring("Carouf", "Carouf");

    OfxBuilder
      .init(this)
      .addTransaction("2008/08/15", -110.00, "Auchan")
      .addTransaction("2008/08/14", -0.00, "Carouf")
      .load();

    timeline.selectAll();
    timeline.selectMonth("2008/08");

    budgetView.recurring.checkTotalAmounts(-110, -170);

    transactions
      .showPlannedTransactions()
      .initContent()
      .add("15/08/2008", TransactionType.PLANNED, "Planned: Carouf", "", -80.00, "Carouf")
      .add("15/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -110.00, "Auchan")
      .add("14/08/2008", TransactionType.VIREMENT, "Carouf", "", -0.00, "Carouf")
      .check();
  }

  public void testBalanceIsBasedOnUserDates() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/01", 200.00, "Salaire")
      .addTransaction("2008/06/15", -90.00, "Auchan")
      .addTransaction("2008/06/30", "2008/07/01", -80.00, "Carouf")
      .load();

    categorization.setNewIncome("Salaire", "Salaire");
    categorization.setNewRecurring("Auchan", "courses");
    categorization.setRecurring("Carouf", "courses");

    timeline.selectMonth("2008/06");
    budgetView.getSummary().checkEndPosition(80);

    timeline.selectMonth("2008/07");
    budgetView.getSummary().checkEndPosition(200 - 170);
  }

  public void testWithIncomeReimbursement() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/01", 200.00, "Salaire")
      .addTransaction("2008/06/15", -90.00, "Auchan")
      .load();
    categorization.setNewIncome("Salaire", "Salaire");
    categorization.setNewVariable("Auchan", "courses")
      .editSeries("courses").alignPlannedAndActual().setPropagationEnabled().validate();

    budgetView.getSummary().checkEndPosition(0);

    mainAccounts.checkEstimatedPosition(0);
    mainAccounts.checkAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME, 0., "2008/06/15");

    OfxBuilder
      .init(this)
      .addTransaction("2008/07/01", -200.00, "Salaire")
      .addTransaction("2008/07/15", -90.00, "Auchan")
      .load();

    mainAccounts.checkAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME, -290., "2008/07/15");
    timeline.selectMonth("2008/07");
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("15/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 400.00, "Salaire")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -90.00, "courses")
      .add("01/07/2008", TransactionType.PRELEVEMENT, "Salaire", "", -200.00, "Salaire")
      .check();
    mainAccounts.checkEstimatedPosition(110);

    budgetView.getSummary().checkEndPosition(110);

    budgetView.income.editSeries("Salaire")
      .selectMonth(200807)
      .selectNegativeAmounts()
      .setAmount("200")
      .validate();

    transactions
      .checkShowsPlannedTransactions()
      .initContent()
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -90.00, "courses")
      .add("01/07/2008", TransactionType.PRELEVEMENT, "Salaire", "", -200.00, "Salaire")
      .check();

    mainAccounts.checkEstimatedPosition(-290);

    budgetView.getSummary().checkEndPosition(-290);
  }
}
