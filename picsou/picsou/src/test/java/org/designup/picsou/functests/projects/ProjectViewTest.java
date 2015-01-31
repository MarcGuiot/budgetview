package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class ProjectViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2011/01");
    super.setUp();
    addOns.activateProjects();
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

    projects.getAccountChart("Account n. 00000123")
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
    projects.getAccountChart("Account n. 00000123").doubleClick();

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
    projects.checkAccountPosition("Account n. 000123", "2800.00 on 2011/01/10");
    projects.checkAccountPosition("Account n. 000234", "1000.00 on 2011/01/01");
    projects.checkAccountPosition("Account n. 000345", "-1400.00 on 2011/01/10");
    projects.checkAccountPosition("Account n. 000456", "-1500.00 on 2011/01/10");

    projects.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000345", "Account n. 000456");

    projects.moveAccountUp("Account n. 000234");
    projects.checkAccounts("Account n. 000234", "Account n. 000123", "Account n. 000345", "Account n. 000456");

    projects.moveAccountDown("Account n. 000123");
    projects.checkAccounts("Account n. 000234", "Account n. 000123", "Account n. 000345", "Account n. 000456");

    projects.moveAccountDown("Account n. 000234");
    projects.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000345", "Account n. 000456");

    projects.moveAccountUp("Account n. 000345");
    projects.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000345", "Account n. 000456");

    projects.moveAccountUp("Account n. 000456");
    projects.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000456", "Account n. 000345");

    projects.moveAccountUp("Account n. 000345");
    projects.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000345", "Account n. 000456");

    projects.checkGraphShown("Account n. 000123");
    projects.checkGraphShown("Account n. 000234");
    projects.checkGraphShown("Account n. 000345");
    projects.checkGraphShown("Account n. 000456");

    projects.hideGraph("Account n. 000123");
    projects.checkGraphHidden("Account n. 000123");
    projects.checkGraphShown("Account n. 000234");
    projects.checkGraphShown("Account n. 000345");
    projects.checkGraphShown("Account n. 000456");

    projects.hideGraph("Account n. 000123");
    projects.checkGraphShown("Account n. 000123");
    projects.checkGraphShown("Account n. 000234");
    projects.checkGraphShown("Account n. 000345");
    projects.checkGraphShown("Account n. 000456");

    mainAccounts.checkChartHidden("Account n. 000123");
    mainAccounts.checkChartHidden("Account n. 000234");
    savingsAccounts.checkChartHidden("Account n. 000345");
    savingsAccounts.checkChartHidden("Account n. 000456");

    views.selectBudget();
    mainAccounts.showChart("Account n. 000123");
    mainAccounts.checkChartShown("Account n. 000123");
    projects.checkGraphShown("Account n. 000123");

    views.selectBudget();
    mainAccounts.hideChart("Account n. 000123");
    mainAccounts.checkChartHidden("Account n. 000123");
    projects.checkGraphShown("Account n. 000123");
  }

  public void testCanAggregateAccountGraphs() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    views.selectProjects();
    projects.checkNoAccounts();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 2800.00, "2011/01/10")
      .addTransaction("2011/01/10", 3000.00, "WorldCo")
      .load();
    projects.checkAccounts("Account n. 000123");
    projects.getAccountChart("Account n. 000123")
      .checkValue(201101, 1, -200.00)
      .checkValue(201101, 10, 2800.00);

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000234", 1000.00, "2011/01/28")
      .addTransaction("2011/01/28", 3000.00, "WorldCo")
      .load();
    projects.checkAccounts("Account n. 000123", "Account n. 000234");

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
    projects.checkAccounts("Account n. 000123", "Account n. 000234", "Account n. 000345", "Account n. 000456");
    projects.getAccountChart("Account n. 000123")
      .checkValue(201101, 1, -200.00)
      .checkValue(201101, 10, 2800.00);

    projects.toggleMainAccountGraphs("Show only one graph for all accounts");
    projects.checkAccounts("Main accounts", "Account n. 000345", "Account n. 000456");
    projects.getMainSummaryGraph()
      .checkValue(201101, 1, 800.00)
      .checkValue(201101, 10, 3800.00)
      .checkValue(201101, 28, 6800.00);

    projects.toggleSavingsAccountGraphs("Show only one graph for all accounts");
    projects.checkAccounts("Main accounts", "Savings accounts");
    projects.getSavingsSummaryGraph()
      .checkValue(201101, 1, -8900.00)
      .checkValue(201101, 10, -2900.00);

    projects.toggleMainAccountGraphs("Show a graph for each account");
    projects.checkAccounts("Account n. 000123", "Account n. 000234", "Savings accounts");
    projects.getAccountChart("Account n. 000123")
      .checkValue(201101, 1, -200.00)
      .checkValue(201101, 10, 2800.00);
  }
}
