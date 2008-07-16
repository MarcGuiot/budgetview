package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.MasterCategory;

public class BudgetViewTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/10", -95.00, "Auchan")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    views.selectData();
    transactions.initContent()
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00)
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00)
      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00)
      .check();

    transactions.setEnvelope(0, "Groceries", MasterCategory.FOOD);
    transactions.setRecurring(1, "Internet");
    transactions.setIncome(2, "Salary");

    views.selectBudget();
    budgetView.envelopes.checkSeries("Groceries", -95.0, 0.0);
    budgetView.recurring.checkSeries("Internet", -29.0, 0.0);
    budgetView.income.checkSeries("Salary", 3540.0, 0.0);
  }
}
