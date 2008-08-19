package org.designup.picsou.functests;

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
    budgetView.occasional.checkTotalAmounts(0, -3540 - 95 - 84);

//    transactions.initContent()
//      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
//      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00)
//      .add("05/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
//      .add("04/07/2008", TransactionType.PRELEVEMENT, "EDF", "", -55.00)
//      .add("03/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -15.00)
//      .add("02/07/2008", TransactionType.VIREMENT, "WorldCo - Bonus", "", 200.00)
//      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00)
//      .check();
//
//    timeline.selectMonths("2008/08");
//
//    budgetView.recurring.checkTitle("Recurring expenses");
//    budgetView.recurring.checkTotalAmounts(0.0, 84.0);
//    budgetView.recurring.checkSeries("Internet", 0.0, 29.0);
//    budgetView.recurring.checkSeries("Electricity", 0.0, 55.0);
//
//    budgetView.envelopes.checkTitle("Envelope expenses");
//    budgetView.envelopes.checkTotalAmounts(0.0, 95);
//    budgetView.envelopes.checkSeries("Groceries", 0.0, 95.0);
//
//    budgetView.income.checkTitle("Income");
//    budgetView.income.checkTotalAmounts(0.0, -3540.00);
//    budgetView.income.checkSeries("Salary", 0.0, -3540.0);
//    budgetView.income.checkSeries("Exceptional Income", 0.0, 0.0);
  }

  public void testUnusedSeriesAreHidden() throws Exception {
    System.out.println("BudgetViewTest.testUnusedSeriesAreHidden: TBD");
  }

  public void testSeveralMonths() throws Exception {
    System.out.println("BudgetViewTest.testSeveralMonths: TBD");
  }
}
