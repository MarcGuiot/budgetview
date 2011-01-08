package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SummaryViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2011/01");
    super.setUp();
  }

  public void test() throws Exception {

    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 2800.0, "2011/01/10")
      .addTransaction("2010/12/28", 3000.00, "WorldCo")
      .addTransaction("2011/01/05", -1000.00, "Foncia")
      .addTransaction("2011/01/09", -200.00, "FNAC")
      .load();

    categorization.setNewIncome("WorldCo", "Income");
    categorization.setNewRecurring("Foncia", "Loyer");

    summary.getMainChart()
      .checkColumnCount(8)
      .checkDiffColumn(0, "D", "2010", 0.00, 4000.00)
      .checkDiffColumn(1, "J", "2011", 0.00, 5800.00, true)
      .checkDiffColumn(2, "F", "2011", 0.00, 7800.00)
      .checkDiffColumn(3, "M", "2011", 0.00, 9800.00)
      .checkDiffColumn(4, "A", "2011", 0.00, 11800.00)
      .checkDiffColumn(5, "M", "2011", 0.00, 13800.00)
      .checkDiffColumn(6, "J", "2011", 0.00, 15800.00)
      .checkDiffColumn(7, "J", "2011", 0.00, 17800.00);
  }

  public void testNavigation() throws Exception {

    operations.hideSignposts();
    
    views.selectHome();

    summary.gotoBudget();
    views.checkBudgetSelected();

    views.selectHome();
    summary.gotoData();
    views.checkDataSelected();

    views.selectHome();
    summary.gotoSavings();
    views.checkSavingsSelected();
  }

}
