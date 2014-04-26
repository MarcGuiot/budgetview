package org.designup.picsou.functests.general;

import org.designup.picsou.functests.checkers.components.HistoDailyChecker;
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

    summary.getAccountChart("Account n. 00000123")
      .checkEndOfMonthValue(4000.00)
      .checkRange(201012, 201107)
      .checkSelected(201101);
  }

  public void testDoubleClick() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 2800.00, "2011/01/10")
      .addTransaction("2010/12/28", 3000.00, "WorldCo")
      .addTransaction("2010/09/28", 3000.00, "WorldCo")
      .load();

    views.selectHome();
    summary.getAccountChart("Account n. 00000123").doubleClick();

    views.checkBudgetSelected();
    timeline.checkSelection("2010/12");
  }

  public void testSortingAndHiding() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 2800.00, "2011/01/10")
      .addTransaction("2011/01/10@", 3000.00, "WorldCo")
      .load();
    mainAccounts.edit("Account n. 000123").setAsSavings().validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000234", 1000.00, "2011/01/28")
      .addTransaction("2011/01/28", 3000.00, "WorldCo")
      .load();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000345", -1400.00, "2011/01/16")
      .addTransaction("2011/01/16", 3000.00, "WorldCo")
      .load();

    views.selectHome();
    summary.checkAccountPosition("Account n. 000123", "2800.00 on 2011/01/10");
    summary.checkAccountPosition("Account n. 000234", "1000.00 on 2011/01/01");
    summary.checkAccountPosition("Account n. 000345", "-1400.00 on 2011/01/01");

    summary.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000345");

    summary.moveAccountUp("Account n. 000234");
    summary.checkAccounts("Account n. 000234", "Account n. 000123", "Account n. 000345");

    summary.moveAccountDown("Account n. 000123");
    summary.checkAccounts("Account n. 000234", "Account n. 000345", "Account n. 000123");

    summary.checkGraphShown("Account n. 000123");
    summary.checkGraphShown("Account n. 000234");

    summary.hideGraph("Account n. 000123");
    summary.checkGraphHidden("Account n. 000123");
    summary.checkGraphShown("Account n. 000234");

    summary.hideGraph("Account n. 000123");
    summary.checkGraphShown("Account n. 000123");
    summary.checkGraphShown("Account n. 000234");
  }
}
