package org.designup.picsou.functests;

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

    views.selectCategorization();

    categorization.setNewVariable("Auchan", "Courant");
    categorization.setVariable("Carouf", "Courant");
    views.selectBudget();

    budgetView.variable.createSeries().setName("Secu")
      .selectAllMonths()
      .setAmount("0")
      .validate();

    views.selectCategorization();
    OfxBuilder
      .init(this)
      .addTransaction("2008/08/26", -49.90, "ED")
      .addTransaction("2008/08/25", 60.90, "Auchan")
      .addTransaction("2008/08/13", 10.00, "Pharma")
      .load();

    categorization.setVariable("Pharma", "Secu");
    categorization.setVariable("ED", "Courant");
    views.selectData();
    transactions.initContent()
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Courant", "", (-(80 + 90) - 60.90 + 49.9) /*181*/, "Courant")
      .add("26/08/2008", TransactionType.PRELEVEMENT, "ED", "", -49.90, "Courant")
      .add("25/08/2008", TransactionType.VIREMENT, "Auchan", "", 60.90, "Courant")
      .add("13/08/2008", TransactionType.VIREMENT, "Pharma", "", 10.00, "Secu")
      .check();

    views.selectBudget();
    timeline.selectMonth("2008/07");
    budgetView.variable.checkSeries("Courant", -90 - 80, -90 - 80);
    budgetView.variable.checkTotalAmounts(-90 - 80, -90 - 80);

    timeline.selectMonth("2008/08");
    budgetView.variable.checkSeries("Courant", (60.90 - 49.9), -90 - 80);// 11,  -170 == > 181
    budgetView.variable.checkSeries("Secu", 10, 0);

    budgetView.variable.checkTotalAmounts(10 + 60.90 - 49.9, -90 - 80);
  }

  public void testChangingSeriesBudgetCanCreatePlannedTransaction() throws Exception {

    views.selectBudget();
    budgetView.variable.createSeries().setName("Secu")
      .selectAllMonths()
      .selectPositiveAmounts()
      .setAmount("10").validate();

    views.selectCategorization();
    OfxBuilder
      .init(this)
      .addTransaction("2008/08/13", 10.00, "Pharma")
      .load();

    categorization.setVariable("Pharma", "Secu");
    views.selectData();
    transactions.initContent()
      .add("13/08/2008", TransactionType.VIREMENT, "Pharma", "", 10.00, "Secu")
      .check();

    views.selectBudget();

    // je ne comprends pas pourquoi il faut faire un selectPositiveAmounts
    //alors que la series est deja en Positif.
    // non reproductible en vrai.
    budgetView.variable.editSeriesList().selectSeries("Secu").selectAllMonths()
      .selectPositiveAmounts().setAmount("20").validate();
    transactions.initContent()
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

    views.selectCategorization();

    categorization.setNewVariable("Auchan", "Auchan");
    categorization.setNewVariable("Carouf", "Carouf");

    OfxBuilder
      .init(this)
      .addTransaction("2008/08/15", -110.00, "Auchan")
      .addTransaction("2008/08/14", -0.00, "Carouf")
      .load();

    timeline.selectAll();
    timeline.selectMonth("2008/08");

    views.selectBudget();
    budgetView.variable.checkTotalAmounts(-110, -170);

    views.selectData();
    transactions.initContent()
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

    views.selectCategorization();
    categorization.setNewIncome("Salaire", "Salaire");
    categorization.setNewVariable("Auchan", "courses");
    categorization.setVariable("Carouf", "courses");

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.getSummary()
      .checkMonthBalance(200 - 90 - 80)
      .checkEndPosition(80);

    timeline.selectMonth("2008/07");
    budgetView.getSummary()
      .checkMonthBalance(200 - 90 - 80)
      .checkEndPosition(200 - 170);
  }

  public void testWithIncomeReimbursement() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/01", 200.00, "Salaire")
      .addTransaction("2008/06/15", -90.00, "Auchan")
      .load();
    views.selectCategorization();
    categorization.setNewIncome("Salaire", "Salaire");
    categorization.setNewVariable("Auchan", "courses");

    views.selectBudget();
    budgetView.getSummary()
      .checkMonthBalance(110)
      .checkEndPosition(0);

    views.selectHome();
    mainAccounts.checkEstimatedPosition(0);
    mainAccounts.checkAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME, 0., "2008/06/15");

    OfxBuilder
      .init(this)
      .addTransaction("2008/07/01", -200.00, "Salaire")
      .addTransaction("2008/07/15", -90.00, "Auchan")
      .load();

    mainAccounts.checkAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME, 0., "2008/07/15");
    timeline.selectMonth("2008/07");
    views.selectData();
    transactions.initContent()
      .add("15/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 400.00, "Salaire")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -90.00, "courses")
      .add("01/07/2008", TransactionType.PRELEVEMENT, "Salaire", "", -200.00, "Salaire")
      .check();
    views.selectHome();
    mainAccounts.checkEstimatedPosition(400);

    views.selectBudget();
    budgetView.getSummary()
      .checkMonthBalance(110)
      .checkEndPosition(400);
    budgetView.getSummary().openPositionDialog()
      .checkInitialPosition(0)
      .close();

    budgetView.income.editSeriesList()
      .selectMonth(200807)
      .selectNegativeAmounts()
      .setAmount("200")
      .validate();

    views.selectData();
    transactions.initContent()
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -90.00, "courses")
      .add("01/07/2008", TransactionType.PRELEVEMENT, "Salaire", "", -200.00, "Salaire")
      .check();

    views.selectHome();
    mainAccounts.checkEstimatedPosition(0);

    views.selectBudget();
    budgetView.getSummary()
      .checkMonthBalance(-290)
      .checkEndPosition(0);
    budgetView.getSummary().openPositionDialog()
      .checkInitialPosition(0)
      .close();
  }
}
