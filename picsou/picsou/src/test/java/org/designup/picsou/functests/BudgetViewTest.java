package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

public class BudgetViewTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentDate(Dates.parseMonth("2008/08"));
    super.setUp();
  }

  public void test() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/10", -50.00, "Monoprix")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/04", -55.00, "EDF")
      .addTransaction("2008/07/03", -15.00, "McDo")
      .addTransaction("2008/07/02", 200.00, "WorldCo - Bonus")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .add("04/07/2008", TransactionType.PRELEVEMENT, "EDF", "", -55.00)
      .add("03/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -15.00)
      .add("02/07/2008", TransactionType.VIREMENT, "WorldCo - Bonus", "", 200.00)
      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00)
      .check();

    transactions.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    transactions.setEnvelope("Monoprix", "Groceries", MasterCategory.FOOD, false);
    transactions.setRecurring("Free Telecom", "Internet", true);
    transactions.setRecurring("EDF", "Electricity", true);
    transactions.setIncome("WorldCo - Bonus", "Exceptional Income", false);
    transactions.setIncome("WorldCo", "Salary", true);

    views.selectBudget();

    budgetView.recurring.checkTitle("Recurring expenses");
    budgetView.recurring.checkTotalAmounts(84.0, 84.0);
    budgetView.recurring.checkSeries("Internet", 29.0, 29.0);
    budgetView.recurring.checkSeries("Electricity", 55.0, 55.0);

    budgetView.envelopes.checkTitle("Envelope expenses");
    budgetView.envelopes.checkTotalAmounts(145.0, 95);
    budgetView.envelopes.checkSeries("Groceries", 145.0, 95.0);

    budgetView.income.checkTitle("Income");
    budgetView.income.checkTotalAmounts(3740.0, 3540.00);
    budgetView.income.checkSeries("Salary", 3540.0, 3540.0);
    budgetView.income.checkSeries("Exceptional Income", 200.0, 0.0);

    budgetView.occasional.checkTitle("Occasional expenses");
    budgetView.occasional.checkTotalAmounts(0, -3540 + 95 + 84);

    timeline.selectMonths("2008/08");

    transactions.initContent()
      .add("12/08/2008", TransactionType.PLANNED, "Groceries", "", -95.00, MasterCategory.FOOD)
      .add("05/08/2008", TransactionType.PLANNED, "Internet", "", -29.00, "Internet")
      .add("04/08/2008", TransactionType.PLANNED, "Electricity", "", -55.00, "Electricity")
      .add("01/08/2008", TransactionType.PLANNED, "Salary", "", 3540.00, "Salary")
      .check();

    budgetView.recurring.checkTitle("Recurring expenses");
    budgetView.recurring.checkTotalAmounts(0.0, 84.0);
    budgetView.recurring.checkSeries("Internet", 0.0, 29.0);
    budgetView.recurring.checkSeries("Electricity", 0.0, 55.0);

    budgetView.envelopes.checkTitle("Envelope expenses");
    budgetView.envelopes.checkTotalAmounts(0.0, 95);
    budgetView.envelopes.checkSeries("Groceries", 0.0, 95.0);

    budgetView.income.checkTitle("Income");
    budgetView.income.checkTotalAmounts(0.0, 3540.00);
    budgetView.income.checkSeries("Salary", 0.0, 3540.0);
    budgetView.income.checkSeries("Exceptional Income", 0.0, 0.0);
  }

  public void testImportWithUserDateAndBankDateAtNextMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/31", "2008/08/02", -95.00, "Auchan")
      .addTransaction("2008/07/30", "2008/08/01", -50.00, "Monoprix")
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .addTransaction("2008/07/28", "2008/08/01", 3540.00, "WorldCo")
      .load();

    views.selectData();
    transactions.initContent()
      .add("31/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .add("30/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00)
      .add("29/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .add("28/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00)
      .check();

    transactions.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    transactions.setEnvelope("Monoprix", "Groceries", MasterCategory.FOOD, false);
    transactions.setRecurring("Free Telecom", "Internet", true);
    transactions.setIncome("WorldCo", "Salary", true);
    timeline.selectMonth("2008/07");
    views.selectBudget();

    budgetView.recurring.checkTitle("Recurring expenses");
    budgetView.recurring.checkTotalAmounts(29.0, 29.0);
    budgetView.recurring.checkSeries("Internet", 29.0, 29.0);

    budgetView.envelopes.checkTitle("Envelope expenses");
    budgetView.envelopes.checkTotalAmounts(145.0, 95);
    budgetView.envelopes.checkSeries("Groceries", 145.0, 95.0);

    budgetView.income.checkTitle("Income");
    budgetView.income.checkTotalAmounts(3540.0, 3540.00);
    budgetView.income.checkSeries("Salary", 3540.0, 3540.0);
    budgetView.income.checkSeries("Exceptional Income", 0.0, 0.0);

    budgetView.occasional.checkTitle("Occasional expenses");
    budgetView.occasional.checkTotalAmounts(0, -3540 + 95 + 29);
  }

  public void testEditingASeriesWithTransactions() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/29", "2008/08/01", -29.00, "Free Telecom")
      .load();

    timeline.selectMonth("2008/07");
    views.selectData();
    transactions.initContent()
      .add("29/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .check();
    transactions.setRecurring("Free Telecom", "Internet", true);

    views.selectBudget();

    SeriesEditionDialogChecker editionDialog = budgetView.recurring.editSeries("Internet");
    editionDialog.checkName("Internet");
    editionDialog.setName("Free");
    editionDialog.validate();

    budgetView.recurring.checkSeries("Free", 29.00, 29.00);
  }

  public void testSeveralMonths() throws Exception {
    System.out.println("BudgetViewTest.testSeveralMonths: TBD");
  }
}
