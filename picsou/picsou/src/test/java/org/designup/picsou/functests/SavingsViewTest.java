package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SavingsViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2009/07");
    super.setUp();
  }

  public void testEditingSavingsSeries() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/07/30")
      .addTransaction("2009/07/10", -200.00, "Virt")
      .load();

    savingsView
      .createAccount()
      .checkIsSavings()
      .setName("ING")
      .selectBank("ING Direct")
      .setPosition(200)
      .validate();

    categorization.setNewSavings("Virt", "Epargne", "Main accounts", "ING");
    views.selectBudget();
    budgetView.savings.alignAndPropagate("Epargne");

    savingsView.checkContainsSeries("ING", "Epargne");
    savingsView.checkSeriesAmounts("ING", "Epargne", 200, 200);
    savingsView.editPlannedAmount("ING", "Epargne")
      .checkAmountTogglesAreNotVisible()
      .setAmount(300)
      .validate();
    savingsView.checkSeriesAmounts("ING", "Epargne", 200, 300);
  }

  public void testSavingsAccountsEvolution() throws Exception {

    operations.openPreferences().setFutureMonthsCount(12).validate();
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/07/30")
      .addTransaction("2009/07/10", -200.00, "Virt")
      .load();

    savingsAccounts
      .createNewAccount()
      .setName("ING")
      .selectBank("ING Direct")
      .setPosition(200)
      .validate();

    categorization.setNewSavings("Virt", "Epargne", "Main accounts", "ING");

    views.selectBudget();
    budgetView.savings.alignAndPropagate("Epargne");

    views.selectHome();
    summary.getSavingsChart()
      .checkColumnCount(13)
      .checkDailyColumn(0, "J", "2009", 200.00, true)
      .checkDailyColumn(1, "A", "2009", 400.00)
      .checkDailyColumn(2, "S", "2009", 600.00)
      .checkDailyColumn(3, "O", "2009", 800.00)
      .checkDailyColumn(6, "J", "2010", 1400.00);

    savingsAccounts.editPosition("ING").setAmount(300.00).validate();

    views.selectHome();
    summary.getSavingsChart()
      .checkColumnCount(13)
      .checkDailyColumn(0, "J", "2009", 300.00, true)
      .checkDailyColumn(1, "A", "2009", 500.00)
      .checkDailyColumn(2, "S", "2009", 700.00)
      .checkDailyColumn(3, "O", "2009", 900.00)
      .checkDailyColumn(6, "J", "2010", 1500.00);

    operations.openPreferences().setFutureMonthsCount(6).validate();

    views.selectHome();
    summary.getSavingsChart()
      .checkColumnCount(7)
      .checkDailyColumn(0, "J", "2009", 300.00, true)
      .checkDailyColumn(1, "A", "2009", 500.00)
      .checkDailyColumn(2, "S", "2009", 700.00)
      .checkDailyColumn(3, "O", "2009", 900.00)
      .checkDailyColumn(6, "J", "2010", 1500.00);
  }

  public void testWithBeginOfAccount() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/07/30")
      .addTransaction("2009/06/04", -10.00, "McDo")
      .addTransaction("2009/07/10", -200.00, "Virt")
      .load();

    savingsAccounts
      .createNewAccount()
      .setName("ING")
      .selectBank("ING Direct")
      .setPosition(0)
      .setStartDate("2009/07/02")
      .validate();
    categorization.setNewSavings("Virt", "Epargne", "Main accounts", "ING");

    timeline.selectMonth("2009/06");
    savingsView.checkTotalEstimatedPosition("-", "2009/06/30");

    timeline.selectMonth("2009/10");
    savingsAccounts.edit("ING").setEndDate("2009/09/02").validate();
    savingsView.checkTotalEstimatedPosition("-", "2009/10/31");
  }

  public void testAddMonth() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/07/30")
      .addTransaction("2009/05/04", -10.00, "McDo")
      .addTransaction("2009/06/04", -10.00, "McDo")
      .addTransaction("2009/07/10", -200.00, "Virt")
      .load();

    savingsAccounts
      .createNewAccount()
      .setName("ING")
      .selectBank("ING Direct")
      .setPosition(0)
      .setStartDate("2009/06/02")
      .validate();

    views.selectCategorization();
    categorization.setNewSavings("Virt", "Epargne", "Main accounts", "ING");
    views.selectBudget();
    budgetView.savings.alignAndPropagate("Epargne");

    timeline.selectMonth("2009/05");
    savingsView.checkTotalEstimatedPosition("-", "2009/05/31");

    views.selectHome();
    timeline.selectMonth("2009/07");
    savingsAccounts
      .edit("ING")
      .setEndDate("2009/11/02")
      .validate();

    savingsView.checkTotalEstimatedPosition("0.00", "2009/07/31");

    operations.openPreferences()
      .setFutureMonthsCount(6)
      .validate();

    timeline.selectMonth("2009/11");
    savingsView.checkTotalEstimatedPosition("800.00", "2009/11/30");

    timeline.selectMonth("2009/12");
    savingsView.checkTotalEstimatedPosition("-", "2009/12/31");

    timeline.selectMonth("2009/10");
    savingsAccounts.edit("ING")
      .setEndDate("2009/10/02")
      .validate();

    timeline.selectMonth("2009/10");
    savingsView.checkTotalEstimatedPosition("600.00", "2009/10/31");

    timeline.selectMonth("2009/12");
    savingsView.checkTotalEstimatedPosition("-", "2009/12/31");

    timeline.selectMonth("2009/10");
    savingsAccounts.edit("ING")
      .setStartDate("2009/07/02")
      .validate();

    timeline.selectMonth("2009/08");
    savingsView.checkTotalEstimatedPosition("200.00", "2009/08/31");

    timeline.selectMonth("2009/10");
    savingsView.checkTotalEstimatedPosition("600.00", "2009/10/31");

    timeline.selectMonth("2009/12");
    savingsView.checkTotalEstimatedPosition("-", "2009/12/31");
  }

  public void testAutomaticCreation() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/07/30")
      .addTransaction("2009/05/04", -10.00, "McDo")
      .addTransaction("2009/06/04", -10.00, "McDo")
      .addTransaction("2009/07/10", 200.00, "Virt")
      .load();

    savingsAccounts
      .createNewAccount()
      .setName("ING")
      .selectBank("ING Direct")
      .setPosition(0)
      .validate();

    views.selectCategorization();
    categorization.setNewSavings("Virt", "Epargne", "ING", "Main accounts");

    views.selectBudget();
    budgetView.savings.alignAndPropagate("Epargne");

    savingsView.createSeries()
      .setName("External")
      .setFromAccount("External account")
      .setToAccount("ING")
      .selectAllMonths()
      .setAmount(210)
      .setDay("15")
      .validate();

    operations.nextMonth();
    operations.nextSixDays();
    timeline.selectAll();
    transactions.showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2009", "Planned: Epargne", -200.00, "Epargne", 30.00, 30.00, "ING")
      .add("11/10/2009", "Planned: External", 210.00, "External", 230.00, 230.00, "ING")
      .add("11/10/2009", "Planned: Epargne", 200.00, "Epargne", 1600.00, "Main accounts")
      .add("11/09/2009", "Planned: Epargne", -200.00, "Epargne", 20.00, 20.00, "ING")
      .add("11/09/2009", "Planned: External", 210.00, "External", 220.00, 220.00, "ING")
      .add("11/09/2009", "Planned: Epargne", 200.00, "Epargne", 1400.00, "Main accounts")
      .add("11/08/2009", "Planned: Epargne", -200.00, "Epargne", 10.00, 10.00, "ING")
      .add("11/08/2009", "Planned: External", 210.00, "External", 210.00, 210.00, "ING")
      .add("11/08/2009", "Planned: Epargne", 200.00, "Epargne", 1200.00, "Main accounts")
      .add("15/07/2009", "EXTERNAL", 210.00, "External", 0.00, 0.00, "ING")
      .add("10/07/2009", "VIRT", -200.00, "Epargne", -210.00, -210.00, "ING")
      .add("10/07/2009", "VIRT", 200.00, "Epargne", 1000.00, 1000.00, "Account n. 00000123")
      .add("15/06/2009", "EXTERNAL", 210.00, "External", -10.00, -10.00, "ING")
      .add("04/06/2009", "MCDO", -10.00, "To categorize", 800.00, 800.00, "Account n. 00000123")
      .add("15/05/2009", "EXTERNAL", 210.00, "External", -220.00, -220.00, "ING")
      .add("04/05/2009", "MCDO", -10.00, "To categorize", 810.00, 810.00, "Account n. 00000123")
      .check();


    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/08/25")
      .addTransaction("2009/08/25", -10.00, "ed")
      .load();

    timeline.selectAll();
    transactions.showPlannedTransactions()
      .initAmountContent()
      .add("11/10/2009", "Planned: Epargne", -200.00, "Epargne", 30.00, 30.00, "ING")
      .add("11/10/2009", "Planned: External", 210.00, "External", 230.00, 230.00, "ING")
      .add("11/10/2009", "Planned: Epargne", 200.00, "Epargne", 1600.00, "Main accounts")
      .add("11/09/2009", "Planned: Epargne", -200.00, "Epargne", 20.00, 20.00, "ING")
      .add("11/09/2009", "Planned: External", 210.00, "External", 220.00, 220.00, "ING")
      .add("11/09/2009", "Planned: Epargne", 200.00, "Epargne", 1400.00, "Main accounts")
      .add("25/08/2009", "Planned: Epargne", -200.00, "Epargne", 10.00, 10.00, "ING")
      .add("25/08/2009", "Planned: Epargne", 200.00, "Epargne", 1200.00, "Main accounts")
      .add("25/08/2009", "EXTERNAL", 210.00, "External", 210.00, 210.00, "ING")
      .add("25/08/2009", "ED", -10.00, "To categorize", 1000.00, 1000.00, "Account n. 00000123")
      .add("15/07/2009", "EXTERNAL", 210.00, "External", 0.00, 0.00, "ING")
      .add("10/07/2009", "VIRT", -200.00, "Epargne", -210.00, -210.00, "ING")
      .add("10/07/2009", "VIRT", 200.00, "Epargne", 1010.00, 1010.00, "Account n. 00000123")
      .add("15/06/2009", "EXTERNAL", 210.00, "External", -10.00, -10.00, "ING")
      .add("04/06/2009", "MCDO", -10.00, "To categorize", 810.00, 810.00, "Account n. 00000123")
      .add("15/05/2009", "EXTERNAL", 210.00, "External", -220.00, -220.00, "ING")
      .add("04/05/2009", "MCDO", -10.00, "To categorize", 820.00, 820.00, "Account n. 00000123")
      .check();
  }
}
