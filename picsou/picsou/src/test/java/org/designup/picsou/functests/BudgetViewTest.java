package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.MasterCategory;

public class BudgetViewTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/10", -50.00, "Monoprix")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/04", -55.00, "EDF")
      .addTransaction("2008/07/02", 200.00, "WorldCo - Bonus")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    views.selectData();
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .add("04/07/2008", TransactionType.PRELEVEMENT, "EDF", "", -55.00)
      .add("02/07/2008", TransactionType.VIREMENT, "WorldCo - Bonus", "", 200.00)
      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00)
      .check();

    transactions.setEnvelope(0, "Groceries", MasterCategory.FOOD);
    transactions.setEnvelope(1, "Groceries", MasterCategory.FOOD);
    transactions.setRecurring(2, "Internet");
    transactions.setRecurring(3, "Electricity");
    transactions.setIncome(4, "Exceptional Income");
    transactions.setIncome(5, "Salary");

    views.selectBudget();

    budgetView.recurring.checkTitle("Recurring expenses");
    budgetView.recurring.checkTotalAmounts(-84.0, 0);
    budgetView.recurring.checkSeries("Internet", -29.0, 0.0);
    budgetView.recurring.checkSeries("Electricity", -55.0, 0.0);

    budgetView.envelopes.checkTitle("Envelope expenses");
    budgetView.envelopes.checkTotalAmounts(-145.0, 0);
    budgetView.envelopes.checkSeries("Groceries", -145.0, 0.0);

    budgetView.income.checkTitle("Income");
    budgetView.income.checkTotalAmounts(3740.0, 0);
    budgetView.income.checkSeries("Salary", 3540.0, 0.0);
    budgetView.income.checkSeries("Exceptional Income", 200.0, 0.0);
  }

  public void DISABLED_testUnusedSeriesAreHidden() throws Exception {

  }

  public void DISABLED_testSeveralMonths() throws Exception {

  }
}
