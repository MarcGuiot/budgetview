package org.designup.picsou.functests.general;

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
      .addTransaction("2011/01/10", 3000.00, "WorldCo")
      .load();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000234", 1000.00, "2011/01/28")
      .addTransaction("2011/01/28", 3000.00, "WorldCo")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000345", -1400.00, "2011/01/16")
      .addTransaction("2011/01/10", 3000.00, "WorldCo")
      .load();
    mainAccounts.edit("Account n. 000345").setAsSavings().validate();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000456", -1500.00, "2011/01/17")
      .addTransaction("2011/01/10", 3000.00, "WorldCo")
      .load();
    mainAccounts.edit("Account n. 000456").setAsSavings().validate();

    views.selectProjects();
    summary.checkAccountPosition("Account n. 000123", "2800.00 on 2011/01/10");
    summary.checkAccountPosition("Account n. 000234", "1000.00 on 2011/01/01");
    summary.checkAccountPosition("Account n. 000345", "-1400.00 on 2011/01/10");
    summary.checkAccountPosition("Account n. 000456", "-1500.00 on 2011/01/10");

    summary.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000345", "Account n. 000456");

    summary.moveAccountUp("Account n. 000234");
    summary.checkAccounts("Account n. 000234", "Account n. 000123", "Account n. 000345", "Account n. 000456");

    summary.moveAccountDown("Account n. 000123");
    summary.checkAccounts("Account n. 000234", "Account n. 000123", "Account n. 000345", "Account n. 000456");

    summary.moveAccountDown("Account n. 000234");
    summary.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000345", "Account n. 000456");

    summary.moveAccountUp("Account n. 000345");
    summary.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000345", "Account n. 000456");

    summary.moveAccountUp("Account n. 000456");
    summary.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000456", "Account n. 000345");

    summary.moveAccountUp("Account n. 000345");
    summary.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000345", "Account n. 000456");

    summary.checkGraphShown("Account n. 000123");
    summary.checkGraphShown("Account n. 000234");
    summary.checkGraphShown("Account n. 000345");
    summary.checkGraphShown("Account n. 000456");

    summary.hideGraph("Account n. 000123");
    summary.checkGraphHidden("Account n. 000123");
    summary.checkGraphShown("Account n. 000234");
    summary.checkGraphShown("Account n. 000345");
    summary.checkGraphShown("Account n. 000456");

    summary.hideGraph("Account n. 000123");
    summary.checkGraphShown("Account n. 000123");
    summary.checkGraphShown("Account n. 000234");
    summary.checkGraphShown("Account n. 000345");
    summary.checkGraphShown("Account n. 000456");
  }

  public void testCanAggregateAccountGraphs() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    views.selectProjects();
    summary.checkNoAccounts();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 2800.00, "2011/01/10")
      .addTransaction("2011/01/10", 3000.00, "WorldCo")
      .load();
    views.selectHome();
    summary.checkAccounts("Account n. 000123");

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000234", 1000.00, "2011/01/28")
      .addTransaction("2011/01/28", 3000.00, "WorldCo")
      .load();
    summary.checkAccounts("Account n. 000123", "Account n. 000234");

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000345", -1400.00, "2011/01/16")
      .addTransaction("2011/01/10", 3000.00, "WorldCo")
      .load();
    mainAccounts.edit("Account n. 000345").setAsSavings().validate();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000456", -1500.00, "2011/01/17")
      .addTransaction("2011/01/10", 3000.00, "WorldCo")
      .load();
    mainAccounts.edit("Account n. 000456").setAsSavings().validate();
    summary.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000345", "Account n. 000456");
    summary.getAccountChart("Account n. 000123")
      .checkValue(201101, 1, -200.00)
      .checkValue(201101, 10, 2800.00);

    views.selectHome();
    summary.toggleMainAccountGraphs("Show only one graph for all accounts");
    summary.checkAccounts("Main accounts", "Account n. 000345", "Account n. 000456");
    summary.getMainSummaryGraph()
      .checkValue(201101, 1, 800.00)
      .checkValue(201101, 10, 3800.00)
      .checkValue(201101, 28, 6800.00);

    summary.toggleSavingsAccountGraphs("Show only one graph for all accounts");
    summary.checkAccounts("Main accounts", "Savings accounts");
    summary.getSavingsSummaryGraph()
      .checkValue(201101, 1, -8900.00)
      .checkValue(201101, 10, -2900.00);

    summary.toggleMainAccountGraphs("Show a graph for each account");
    summary.checkAccounts("Account n. 000123", "Account n. 000234", "Savings accounts");
    summary.getAccountChart("Account n. 000123")
      .checkValue(201101, 1, -200.00)
      .checkValue(201101, 10, 2800.00);
  }
}
