package org.designup.picsou.functests.analysis;

import org.designup.picsou.functests.checkers.components.HistoDailyChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class SeriesEvolutionHistoChartTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2009/07");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(12).validate();
  }

  public void testStandardCase() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.00, "2009/07/30")
      .addTransaction("2009/06/10", -250.00, "Auchan")
      .addTransaction("2009/06/15", -200.00, "Auchan")
      .addTransaction("2009/06/01", 300.00, "WorldCo")
      .addTransaction("2009/06/15", 350.00, "Big Inc.")
      .addTransaction("2009/06/20", 50.00, "Unknown")
      .addTransaction("2009/07/10", -200.00, "Auchan")
      .addTransaction("2009/07/15", -140.00, "Auchan")
      .addTransaction("2009/07/01", 320.00, "WorldCo")
      .addTransaction("2009/07/15", 350.00, "Big Inc.")
      .addTransaction("2009/07/20", 20.00, "Unknown")
      .load();

    categorization.setNewRecurring("Auchan", "Groceries");
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    timeline.selectMonth("2009/07");
    seriesAnalysis.select("To categorize");
    seriesAnalysis.histoChart
      .checkLineColumn(0, "J", "2009", 50.00)
      .checkLineColumn(1, "J", "2009", 20.00, true);
    seriesAnalysis
      .checkHistoChartLabel("Operations to categorize")
      .checkLegendHidden();
    seriesAnalysis.histoChart.checkTooltip(1, "Amount to categorize for July 2009: 20.00");
    seriesAnalysis.histoChart.checkTooltip(-1, "");
    seriesAnalysis.histoChart.checkTooltip(10, "Amount to categorize for April 2010: 0.00");

    timeline.selectMonth("2009/06");
    seriesAnalysis.select("Balance");
    seriesAnalysis.histoChart
      .checkColumnCount(14)
      .checkDiffColumn(0, "June", "2009", 650.00, 450.00, true);
    seriesAnalysis
      .checkHistoChartLabel("Global balance evolution")
      .checkLegendShown("Expenses", "Income");
    seriesAnalysis.histoChart.checkTooltip(1, "July 2009: Income: 670.00 - Expenses: 450.00");
    seriesAnalysis.histoChart.checkTooltip(-1, "");
    seriesAnalysis.histoChart.checkTooltip(10, "April 2010: Income: 670.00 - Expenses: 450.00");

    timeline.selectMonth("2009/07");
    seriesAnalysis.histoChart
      .checkColumnCount(14)
      .checkDiffColumn(0, "June", "2009", 650.00, 450.00)
      .checkDiffColumn(1, "Jul", "2009", 670.00, 450.00, true)
      .checkDiffColumn(2, "Aug", "2009", 670.00, 450.00)
      .checkDiffColumn(3, "Sep", "2009", 670.00, 450.00)
      .checkDiffColumn(4, "Oct", "2009", 670.00, 450.00)
      .checkDiffColumn(5, "Nov", "2009", 670.00, 450.00);
    seriesAnalysis
      .checkHistoChartLabel("Global balance evolution")
      .checkLegendShown("Expenses", "Income");
    seriesAnalysis.histoChart.checkTooltip(3, "September 2009: Income: 670.00 - Expenses: 450.00");

    timeline.selectMonth("2009/06");
    seriesAnalysis.select("Income");
    seriesAnalysis.histoChart
      .checkDiffColumn(0, "June", "2009", 650.00, 650.00, true)
      .checkDiffColumn(1, "Jul", "2009", 650.00, 670.00);
    seriesAnalysis
      .checkHistoChartLabel("Budget area Income evolution")
      .checkLegendShown("Actual", "Planned");
    seriesAnalysis.histoChart.checkTooltip(0, "June 2009: Planned: 650.00 - Actual: 650.00");
    seriesAnalysis.histoChart.checkTooltip(1, "July 2009: Planned: 650.00 - Actual: 670.00");

    seriesAnalysis.select("John's");
    seriesAnalysis.histoChart
      .checkDiffColumn(0, "June", "2009", 300.00, 300.00, true)
      .checkDiffColumn(1, "Jul", "2009", 300.00, 320.00);
    seriesAnalysis
      .checkHistoChartLabel("Evolution of 'John's'")
      .checkLegendShown("Actual", "Planned");
    seriesAnalysis.histoChart.checkTooltip(1, "July 2009: Planned: 300.00 - Actual: 320.00");

    timeline.selectMonth("2009/07");
    seriesAnalysis.select("Income");
    seriesAnalysis.histoChart
      .checkDiffColumn(0, "June", "2009", 650.00, 650.00)
      .checkDiffColumn(1, "Jul", "2009", 650.00, 670.00, true);
    seriesAnalysis.select("John's");
    seriesAnalysis.histoChart
      .checkDiffColumn(0, "June", "2009", 300.00, 300.00)
      .checkDiffColumn(1, "Jul", "2009", 300.00, 320.00, true);
    seriesAnalysis
      .checkHistoChartLabel("Evolution of 'John's'")
      .checkLegendShown("Actual", "Planned");

    seriesAnalysis.editSeries("John's", "June 2009")
      .setAmount(500)
      .setPropagationEnabled()
      .validate();
    seriesAnalysis.histoChart
      .checkDiffColumn(0, "June", "2009", 500.00, 300.00)
      .checkDiffColumn(1, "Jul", "2009", 500.00, 320.00, true);
  }

  public void testDisplaysUpToTwelveMonthsInThePast() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.00, "2009/07/30")
      .addTransaction("2007/06/10", -250.00, "Auchan")
      .addTransaction("2008/06/10", -200.00, "Auchan")
      .addTransaction("2009/06/10", -150.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries");

    timeline.selectMonth("2009/07");

    views.selectAnalysis();
    seriesAnalysis.histoChart
      .checkColumnCount(19)
      .checkDiffColumn(0, "Jul", "2008", 0.00, 0.00)
      .checkDiffColumn(10, "May", "2009", 0.00, 0.00)
      .checkDiffColumn(11, "June", "2009", 0.00, 150.00);
  }

  public void testDisplayingSeveralSeries() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.00, "2009/07/30")
      .addTransaction("2009/06/10", +2000.00, "WorldCo")
      .addTransaction("2009/07/10", +2000.00, "WorldCo")
      .addTransaction("2009/06/10", -250.00, "Auchan")
      .addTransaction("2009/06/10", -250.00, "Auchan")
      .addTransaction("2009/07/10", -200.00, "Auchan")
      .addTransaction("2009/07/15", -1000.00, "FNAC")
      .load();

    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewVariable("Auchan", "Groceries", -400.00);
    categorization.setNewExtra("FNAC", "TV");

    timeline.selectMonth("2009/07");

    seriesAnalysis.select("Groceries");
    seriesAnalysis.histoChart
      .checkDiffColumn(0, "June", "2009", 400.00, 500.00)
      .checkDiffColumn(1, "Jul", "2009", 400.00, 200.00, true);

    seriesAnalysis.select("Groceries", "TV");
    seriesAnalysis.histoChart
      .checkDiffColumn(0, "June", "2009", 400.00, 500.00)
      .checkDiffColumn(1, "Jul", "2009", 1400.00, 1200.00, true);

    seriesAnalysis.select("Salary", "Groceries", "TV");
    seriesAnalysis.histoChart
      .checkDiffColumn(0, "June", "2009", 1600.00, 1500.00)
      .checkDiffColumn(1, "Jul", "2009", 600.00, 800.00, true);
  }

  public void testAccounts() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.00, "2009/07/30")
      .addTransaction("2009/07/10", -200.00, "Virt")
      .load();

    savingsAccounts
      .createNewAccount()
      .setName("ING")
      .selectBank("ING Direct")
      .setPosition(200)
      .validate();

    categorization.setNewSavings("Virt", "Epargne", "Account n. 00000123", "ING");

    budgetView.savings.alignAndPropagate("Epargne");

    views.selectAnalysis();
    seriesAnalysis.select("Main accounts");
    seriesAnalysis.histoChart
      .checkColumnCount(13)
      .checkLineColumn(0, "J", "2009", 1000.00, true)
      .checkLineColumn(1, "A", "2009", 800.00)
      .checkLineColumn(2, "S", "2009", 600.00)
      .checkLineColumn(3, "O", "2009", 400.00)
      .checkLineColumn(6, "J", "2010", -200.00);
    seriesAnalysis
      .checkHistoChartLabel("End of month position evolution")
      .checkLegendHidden();
    seriesAnalysis.histoChart.checkTooltip(2, "End of September 2009 position: 600.00");

    seriesAnalysis.select("Savings accounts");
    seriesAnalysis.histoChart
      .checkColumnCount(13)
      .checkLineColumn(0, "J", "2009", 400.00, true)
      .checkLineColumn(1, "A", "2009", 600.00)
      .checkLineColumn(2, "S", "2009", 800.00)
      .checkLineColumn(3, "O", "2009", 1000.00)
      .checkLineColumn(6, "J", "2010", 1600.00);
    seriesAnalysis
      .checkHistoChartLabel("End of month position evolution")
      .checkLegendHidden();
    seriesAnalysis.histoChart.checkTooltip(2, "End of September 2009 position: 800.00");

    views.selectHome();

    savingsAccounts.editPosition("ING").setAmount(300.00).validate();

    views.selectAnalysis();
    seriesAnalysis.histoChart
      .checkColumnCount(13)
      .checkLineColumn(0, "J", "2009", 300.00, true)
      .checkLineColumn(1, "A", "2009", 500.00)
      .checkLineColumn(2, "S", "2009", 700.00)
      .checkLineColumn(3, "O", "2009", 900.00)
      .checkLineColumn(6, "J", "2010", 1500.00);

    seriesAnalysis.select("Main accounts");
    seriesAnalysis.histoChart
      .checkColumnCount(13)
      .checkLineColumn(0, "J", "2009", 1000.00, true)
      .checkLineColumn(1, "A", "2009", 800.00)
      .checkLineColumn(2, "S", "2009", 600.00)
      .checkLineColumn(3, "O", "2009", 400.00)
      .checkLineColumn(6, "J", "2010", -200.00);

    views.selectHome();
    mainAccounts.editPosition("Account n. 00000123")
      .setAmount(500.00)
      .validate();

    views.selectAnalysis();
    seriesAnalysis.histoChart
      .checkColumnCount(13)
      .checkLineColumn(0, "J", "2009", 500.00, true)
      .checkLineColumn(1, "A", "2009", 300.00)
      .checkLineColumn(2, "S", "2009", 100.00)
      .checkLineColumn(3, "O", "2009", -100.00)
      .checkLineColumn(6, "J", "2010", -700.00);
  }

  public void testScrolling() throws Exception {
    operations.openPreferences().setFutureMonthsCount(24).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 2800.0, "2009/07/10")
      .addTransaction("2009/07/28", 3000.00, "WorldCo")
      .addTransaction("2008/01/28", 3000.00, "WorldCo")
      .load();

    views.selectHome();
    seriesAnalysis.select("Main accounts");
    seriesAnalysis.histoChart
      .checkRange(200807,201001);

    seriesAnalysis.histoChart
      .scroll(-8)
      .checkRange(200801,200907);

    seriesAnalysis.histoChart
      .scroll(+1)
      .checkRange(200802,200908);

    timeline.selectMonth("2009/12");
    seriesAnalysis.histoChart
      .checkRange(200807,201001);

    seriesAnalysis.histoChart
      .scroll(-1)
      .checkRange(200806, 200912);

    seriesAnalysis.histoChart
      .scroll(+1)
      .checkRange(200807,201001);

    seriesAnalysis.histoChart
      .scroll(+20)
      .checkRange(201001,201107);

    seriesAnalysis.histoChart
      .scroll(-1)
      .checkRange(200912,201106);

    seriesAnalysis.histoChart
      .scroll(-20)
      .checkRange(200804,200910);

    seriesAnalysis.histoChart
      .scroll(+1)
      .checkRange(200805,200911);

    seriesAnalysis.histoChart
      .clickColumnId(200911)
      .checkRange(200807,201001);
    timeline.checkSelection("2009/11");
  }

  public void testClickingInColumnsNavigatesToCorrespondingMonth() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.00, "2009/07/30")
      .addTransaction("2009/06/10", -200.00, "Auchan")
      .addTransaction("2009/06/01", 320.00, "WorldCo")
      .addTransaction("2009/07/10", -200.00, "Auchan")
      .addTransaction("2009/07/01", 320.00, "WorldCo")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Groceries");
    categorization.setNewIncome("WorldCo", "John's");

    views.selectAnalysis();

    timeline.selectMonth("2009/07");
    seriesAnalysis.showChartsAndTable();
    seriesAnalysis.select("Income");
    seriesAnalysis.histoChart
      .checkColumnCount(14)
      .checkDiffColumn(0, "June", "2009", 320.00, 320.00)
      .checkDiffColumn(1, "Jul", "2009", 320.00, 320.00, true);

    seriesAnalysis.histoChart.clickColumn(6);

    timeline.checkSelection("2009/12");
    seriesAnalysis.checkSelected("Income");
    seriesAnalysis.expandAll();

    seriesAnalysis.select("John's");
    seriesAnalysis.histoChart.clickColumn(0);
    timeline.checkSelection("2009/06");

    seriesAnalysis.histoChart.clickColumn(2);
    timeline.checkSelection("2009/08");
  }

  public void testPopupMenus() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.00, "2009/07/30")
      .addTransaction("2009/06/10", -250.00, "Auchan")
      .addTransaction("2009/06/15", -200.00, "Auchan")
      .addTransaction("2009/06/01", 300.00, "WorldCo")
      .addTransaction("2009/07/15", -150.00, "Auchan")
      .addTransaction("2009/07/15", -150.00, "Decathlon")
      .addTransaction("2009/07/15", -100.00, "FNAC")
      .addTransaction("2009/07/15", -150.00, "GoSport")
      .load();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000234", 2000.00, "2009/07/30")
      .addTransaction("2009/06/15", 350.00, "Big Inc.")
      .addTransaction("2009/06/20", 50.00, "Unknown")
      .load();

    categorization.setNewVariable("Auchan", "Groceries", -500.00);
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000456", 5000.00, "2009/07/30")
      .addTransaction("2009/06/10", -200.00, "Virt")
      .load();
    mainAccounts
      .edit("Account n. 00000456")
      .setAsSavings()
      .setName("ING")
      .selectBank("ING Direct")
      .validate();
    categorization.setNewSavings("Virt", "Epargne", "ING", "Account n. 00000123");

    // ---- Balance ----

    views.selectAnalysis();
    seriesAnalysis.histoChart.checkRightClickOptions(0,
                                                     "Show transactions in Categorization view",
                                                     "Show transactions in Accounts view");
    seriesAnalysis.histoChart.rightClickAndSelect(0, "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.initContent()
      .add("10/06/2009", "Groceries", "AUCHAN", -250.00)
      .add("15/06/2009", "Groceries", "AUCHAN", -200.00)
      .add("15/06/2009", "Mary's", "BIG INC.", 350.00)
      .add("20/06/2009", "", "UNKNOWN", 50.00)
      .add("10/06/2009", "Epargne", "VIRT", -200.00)
      .add("01/06/2009", "John's", "WORLDCO", 300.00)
      .check();

    // ---- Main accounts ----

    views.selectAnalysis();
    seriesAnalysis.showChartsAndTable();
    seriesAnalysis.select("Main accounts");
    seriesAnalysis.histoChart.checkRightClickOptions(0,
                                                     "Show transactions in Categorization view",
                                                     "Show transactions in Accounts view");
    seriesAnalysis.histoChart.rightClickAndSelect(0, "Show transactions in Accounts view");
    mainAccounts.checkSelectedAccounts("Account n. 00000234", "Account n. 00000123");
    savingsAccounts.checkNoAccountsSelected();
    transactions.initContent()
      .add("20/06/2009", TransactionType.VIREMENT, "UNKNOWN", "", 50.00)
      .add("15/06/2009", TransactionType.VIREMENT, "BIG INC.", "", 350.00, "Mary's")
      .add("15/06/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -200.00, "Groceries")
      .add("10/06/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -250.00, "Groceries")
      .add("01/06/2009", TransactionType.VIREMENT, "WORLDCO", "", 300.00, "John's")
      .check();

    // ---- Savings accounts ----

    views.selectAnalysis();
    seriesAnalysis.select("Savings accounts");
    seriesAnalysis.histoChart.checkRightClickOptions(0,
                                                     "Show transactions in Categorization view",
                                                     "Show transactions in Accounts view");
    seriesAnalysis.histoChart.rightClickAndSelect(0, "Show transactions in Accounts view");
    savingsAccounts.checkSelectedAccounts("ING");
    mainAccounts.checkNoAccountsSelected();
    transactions.initContent()
      .add("10/06/2009", TransactionType.PRELEVEMENT, "VIRT", "", -200.00, "Epargne")
      .check();

    // ---- Uncategorized ----

    views.selectAnalysis();
    seriesAnalysis.select("To categorize");
    seriesAnalysis.histoChart.checkRightClickOptions(1,
                                                     "Show transactions in Categorization view",
                                                     "Show transactions in Accounts view");
    seriesAnalysis.histoChart.rightClickAndSelect(1, "Show transactions in Categorization view");
    timeline.checkSelection("2009/07");
    categorization.checkShowsUncategorizedTransactionsForSelectedMonths();
    categorization.initContent()
      .add("15/07/2009", "", "DECATHLON", -150.00)
      .add("15/07/2009", "", "FNAC", -100.00)
      .add("15/07/2009", "", "GOSPORT", -150.00)
      .check();

    // ---- BudgetArea ----

    views.selectAnalysis();
    seriesAnalysis.select("Income");
    seriesAnalysis.histoChart.checkRightClickOptions(0,
                                                     "Show transactions in Categorization view",
                                                     "Show transactions in Accounts view");
    seriesAnalysis.histoChart.rightClickAndSelect(0, "Show transactions in Categorization view");
    timeline.checkSelection("2009/06");
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("15/06/2009", "Mary's", "BIG INC.", 350.00)
      .add("01/06/2009", "John's", "WORLDCO", 300.00)
      .check();

    views.selectAnalysis();
    seriesAnalysis.select("Income", "Variable");
    seriesAnalysis.histoChart.checkRightClickOptions(0,
                                                     "Show transactions in Categorization view",
                                                     "Show transactions in Accounts view");
    seriesAnalysis.histoChart.rightClickAndSelect(0, "Show transactions in Categorization view");
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("10/06/2009", "Groceries", "AUCHAN", -250.00)
      .add("15/06/2009", "Groceries", "AUCHAN", -200.00)
      .add("15/06/2009", "Mary's", "BIG INC.", 350.00)
      .add("01/06/2009", "John's", "WORLDCO", 300.00)
      .check();

    // ---- Series ----

    views.selectAnalysis();
    seriesAnalysis.select("Groceries");
    seriesAnalysis.histoChart.checkRightClickOptions(0,
                                                     "Show transactions in Categorization view",
                                                     "Show transactions in Accounts view",
                                                     "Edit");
    seriesAnalysis.histoChart.rightClickAndSelect(0, "Show transactions in Accounts view");
    timeline.checkSelection("2009/06");
    categorization.checkShowsSelectedMonthsOnly();
    views.checkDataSelected();
    transactions.initContent()
      .add("15/06/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -200.00, "Groceries")
      .add("10/06/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -250.00, "Groceries")
      .check();
    transactions.checkClearFilterButtonShown();
    transactions.clearCurrentFilter();

    views.selectAnalysis();
    seriesAnalysis.histoChart.rightClickAndEditSeries(1, "Edit")
      .checkName("Groceries")
      .checkMonthSelected(200907)
      .checkAmount(500.00)
      .validate();
  }
}
