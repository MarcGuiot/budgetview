package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class BudgetSummaryViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/07");
    super.setUp();
  }

  public void test() throws Exception {

    views.selectBudget();

    OfxBuilder
      .init(this)
      .addBankAccount(30006, 10674, "0001212", 1500.00, "2008/07/10")
      .addTransaction("2008/06/05", 1000.00, "WorldCo")
      .addTransaction("2008/06/10", -200.00, "Auchan")
      .addTransaction("2008/07/05", -50.00, "FNAC")
      .load();

    views.selectBudget();
    timeline.checkSelection("2008/07");
    timeline.selectAll();
    budgetView.getSummary().checkUncategorized(1000.00 + 200.00 + 50.00);

    timeline.selectMonth("2008/07");

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewEnvelope("Auchan", "Groceries");

    timeline.checkSelection("2008/07");
    views.selectBudget();
    budgetView.getSummary()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+750.00)
      .checkEndPosition(2300.00)
      .checkUncategorized(50.00);

    timeline.selectAll();

    budgetView.getSummary()
      .checkMultiSelection(2)
      .checkMonthBalance(+1550.00)
      .checkEndPosition(2300.00)
      .checkUncategorized(50.00);

    timeline.selectMonth("2008/06");
    budgetView.getSummary()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+800.00)
      .checkEndPosition(1550.00)
      .checkUncategorizedNotShown();

    views.selectCategorization();
    categorization.setNewEnvelope("FNAC", "Leisures");

    timeline.selectMonth("2008/07");
    views.selectBudget();
    budgetView.getSummary()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+750.00)
      .checkEndPosition(2300.00)
      .checkUncategorizedNotShown();
  }
}
