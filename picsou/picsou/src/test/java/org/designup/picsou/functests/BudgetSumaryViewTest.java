package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class BudgetSumaryViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/06");
    super.setUp();
  }

  public void test() throws Exception {

    views.selectBudget();
    budgetView.getLabel()
      .checkEmpty();

    OfxBuilder
      .init(this)
      .addBankAccount(30006, 10674, "0001212", 1500.00, "2008/07/10")
      .addTransaction("2008/06/5", 1000.0, "WorldCo")
      .addTransaction("2008/06/10", -200.0, "Auchan")
      .addTransaction("2008/07/5", -50, "FNAC")
      .load();

    views.selectBudget();
    timeline.checkSelection("2008/07");
    timeline.selectAll();
    budgetView.getLabel()
      .checkUncategorized(1000.00 + 200.00 + 50.00);

    timeline.selectMonth("2008/07");

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);

    timeline.checkSelection("2008/07");
    views.selectBudget();
    budgetView.getLabel()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+750.00)
      .checkEndPosition(2300.00)
      .checkUncategorized(50.00);

    timeline.selectAll();
    budgetView.getLabel()
      .checkMultiSelection(2)
      .checkMonthBalance(+1550.00)
      .checkEndPosition(2300.00)
      .checkUncategorized(50.00);

    timeline.selectMonth("2008/06");
    budgetView.getLabel()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+800.00)
      .checkEndPosition(1550.00)
      .checkUncategorizedNotShown();

    views.selectCategorization();
    categorization.setNewEnvelope("FNAC", "Occasional");

    timeline.selectMonth("2008/07");
    views.selectBudget();
    budgetView.getLabel()
      .checkMultiSelectionNotShown()
      .checkMonthBalance(+750.00)
      .checkEndPosition(2300.00)
      .checkUncategorizedNotShown();

  }
}
