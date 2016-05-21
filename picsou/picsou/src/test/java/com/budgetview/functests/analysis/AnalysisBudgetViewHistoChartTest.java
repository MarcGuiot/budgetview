package com.budgetview.functests.analysis;

import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.model.TransactionType;

public class AnalysisBudgetViewHistoChartTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2009/07");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(12).validate();
    addOns.activateAnalysis();
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
    analysis.table().select("To categorize");
    analysis.budget().histoChart
      .checkLineColumn(0, "J", "2009", 50.00)
      .checkLineColumn(1, "J", "2009", 20.00, true);
    analysis.budget()
      .checkHistoChartLabel("Operations to categorize")
      .checkLegendHidden();
    analysis.budget().histoChart
      .checkTooltip(1, "Amount to categorize for July 2009: 20.00")
      .checkTooltip(-1, "")
      .checkTooltip(10, "Amount to categorize for April 2010: 0.00");

    timeline.selectMonth("2009/06");
    analysis.table().select("Balance");
    analysis.budget().histoChart
      .checkColumnCount(14)
      .checkDiffColumn(0, "J", "2009", 650.00, 450.00, true);
    analysis.budget()
      .checkHistoChartLabel("Global balance evolution")
      .checkLegendShown("Expenses", "Income");
    analysis.budget().histoChart.checkTooltip(1, "July 2009: Income: 670.00 - Expenses: 450.00");
    analysis.budget().histoChart.checkTooltip(-1, "");
    analysis.budget().histoChart.checkTooltip(10, "April 2010: Income: 670.00 - Expenses: 450.00");

    timeline.selectMonth("2009/07");
    analysis.budget().histoChart
      .checkColumnCount(14)
      .checkDiffColumn(0, "J", "2009", 650.00, 450.00)
      .checkDiffColumn(1, "J", "2009", 670.00, 450.00, true)
      .checkDiffColumn(2, "A", "2009", 670.00, 450.00)
      .checkDiffColumn(3, "S", "2009", 670.00, 450.00)
      .checkDiffColumn(4, "O", "2009", 670.00, 450.00)
      .checkDiffColumn(5, "N", "2009", 670.00, 450.00);
    analysis.budget()
      .checkHistoChartLabel("Global balance evolution")
      .checkLegendShown("Expenses", "Income");
    analysis.budget().histoChart.checkTooltip(3, "September 2009: Income: 670.00 - Expenses: 450.00");

    timeline.selectMonth("2009/06");
    analysis.table().select("Income");
    analysis.budget().histoChart
      .checkDiffColumn(0, "J", "2009", 650.00, 650.00, true)
      .checkDiffColumn(1, "J", "2009", 650.00, 670.00);
    analysis.budget()
      .checkHistoChartLabel("Budget area Income evolution")
      .checkLegendShown("Actual", "Planned");
    analysis.budget().histoChart.checkTooltip(0, "June 2009: Planned: 650.00 - Actual: 650.00");
    analysis.budget().histoChart.checkTooltip(1, "July 2009: Planned: 650.00 - Actual: 670.00");

    analysis.table().select("John's");
    analysis.budget().histoChart
      .checkDiffColumn(0, "J", "2009", 300.00, 300.00, true)
      .checkDiffColumn(1, "J", "2009", 300.00, 320.00);
    analysis.budget()
      .checkHistoChartLabel("Evolution of 'John's'")
      .checkLegendShown("Actual", "Planned");
    analysis.budget().histoChart.checkTooltip(1, "July 2009: Planned: 300.00 - Actual: 320.00");

    timeline.selectMonth("2009/07");
    analysis.table().select("Income");
    analysis.budget().histoChart
      .checkDiffColumn(0, "J", "2009", 650.00, 650.00)
      .checkDiffColumn(1, "J", "2009", 650.00, 670.00, true);
    analysis.table().select("John's");
    analysis.budget().histoChart
      .checkDiffColumn(0, "J", "2009", 300.00, 300.00)
      .checkDiffColumn(1, "J", "2009", 300.00, 320.00, true);
    analysis.budget()
      .checkHistoChartLabel("Evolution of 'John's'")
      .checkLegendShown("Actual", "Planned");

    analysis.table().editSeries("John's", "June 2009")
      .setAmount(500)
      .setPropagationEnabled()
      .validate();
    analysis.budget().histoChart
      .checkDiffColumn(0, "J", "2009", 500.00, 300.00)
      .checkDiffColumn(1, "J", "2009", 500.00, 320.00, true);
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
    analysis.budget().histoChart
      .checkColumnCount(19)
      .checkDiffColumn(0, "J", "2008", 0.00, 0.00)
      .checkDiffColumn(10, "M", "2009", 0.00, 0.00)
      .checkDiffColumn(11, "J", "2009", 0.00, 150.00);
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

    analysis.table().select("Groceries");
    analysis.budget().histoChart
      .checkDiffColumn(0, "J", "2009", 400.00, 500.00)
      .checkDiffColumn(1, "J", "2009", 400.00, 200.00, true);

    analysis.table().select("Groceries", "TV");
    analysis.budget().histoChart
      .checkDiffColumn(0, "J", "2009", 400.00, 500.00)
      .checkDiffColumn(1, "J", "2009", 1400.00, 1200.00, true);

    analysis.table().select("Salary", "Groceries", "TV");
    analysis.budget().histoChart
      .checkDiffColumn(0, "J", "2009", 1600.00, 1500.00)
      .checkDiffColumn(1, "J", "2009", 600.00, 800.00, true);
  }

  public void testAccounts() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.00, "2009/07/30")
      .addTransaction("2009/07/10", -200.00, "Virt / Courant")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000222", 400.00, "2009/07/30")
      .addTransaction("2009/07/10", 200.00, "Virt / Epargne")
      .load();
    mainAccounts.setAsSavings("Account n. 000222", "ING");

    categorization.setNewTransfer("Virt / Courant", "Epargne", "Account n. 00000123", "ING");
    categorization.setTransfer("Virt / Epargne", "Epargne");

    budgetView.transfer.alignAndPropagate("Epargne");

    views.selectAnalysis();
    analysis.table().select("Main accounts");
    analysis.budget().histoChart
      .checkColumnCount(13)
      .checkLineColumn(0, "J", "2009", 1000.00, true)
      .checkLineColumn(1, "A", "2009", 800.00)
      .checkLineColumn(2, "S", "2009", 600.00)
      .checkLineColumn(3, "O", "2009", 400.00)
      .checkLineColumn(6, "J", "2010", -200.00);
    analysis.budget()
      .checkHistoChartLabel("End of month position evolution")
      .checkLegendHidden();
    analysis.budget().histoChart.checkTooltip(2, "End of September 2009 position: 600.00");

    analysis.table().select("Savings accounts");
    analysis.budget().histoChart
      .checkColumnCount(13)
      .checkLineColumn(0, "J", "2009", 400.00, true)
      .checkLineColumn(1, "A", "2009", 600.00)
      .checkLineColumn(2, "S", "2009", 800.00)
      .checkLineColumn(3, "O", "2009", 1000.00)
      .checkLineColumn(6, "J", "2010", 1600.00);
    analysis.budget()
      .checkHistoChartLabel("End of month position evolution")
      .checkLegendHidden();
    analysis.budget().histoChart.checkTooltip(2, "End of September 2009 position: 800.00");

    views.selectHome();

    savingsAccounts.editPosition("ING").setAmount(300.00).validate();

    views.selectAnalysis();
    analysis.budget().histoChart
      .checkColumnCount(13)
      .checkLineColumn(0, "J", "2009", 300.00, true)
      .checkLineColumn(1, "A", "2009", 500.00)
      .checkLineColumn(2, "S", "2009", 700.00)
      .checkLineColumn(3, "O", "2009", 900.00)
      .checkLineColumn(6, "J", "2010", 1500.00);

    analysis.table().select("Main accounts");
    analysis.budget().histoChart
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
    analysis.budget().histoChart
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
    analysis.table().select("Main accounts");
    analysis.budget().histoChart
      .checkRange(200807, 201001);

    analysis.budget().histoChart
      .scroll(-8)
      .checkRange(200801, 200907);

    analysis.budget().histoChart
      .scroll(+1)
      .checkRange(200802, 200908);

    timeline.selectMonth("2009/12");
    analysis.budget().histoChart
      .checkRange(200807, 201001);

    analysis.budget().histoChart
      .scroll(-1)
      .checkRange(200806, 200912);

    analysis.budget().histoChart
      .scroll(+1)
      .checkRange(200807, 201001);

    analysis.budget().histoChart
      .scroll(+20)
      .checkRange(201001, 201107);

    analysis.budget().histoChart
      .scroll(-1)
      .checkRange(200912, 201106);

    analysis.budget().histoChart
      .scroll(-20)
      .checkRange(200804, 200910);

    analysis.budget().histoChart
      .scroll(+1)
      .checkRange(200805, 200911);

    analysis.budget().histoChart
      .clickColumnId(200911)
      .checkRange(200807, 201001);
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
    analysis.budget();
    analysis.table().select("Income");
    analysis.budget().histoChart
      .checkColumnCount(14)
      .checkDiffColumn(0, "J", "2009", 320.00, 320.00)
      .checkDiffColumn(1, "J", "2009", 320.00, 320.00, true);

    analysis.budget().histoChart.clickColumn(6);

    timeline.checkSelection("2009/12");
    analysis.table().checkSelected("Income");
    analysis.table().expandAll();

    analysis.table().select("John's");
    analysis.budget().histoChart.clickColumn(0);
    timeline.checkSelection("2009/06");

    analysis.budget().histoChart.clickColumn(2);
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
    categorization.setNewTransfer("Virt", "Epargne", "ING", "Account n. 00000123");

    // ---- Balance ----

    views.selectAnalysis();
    analysis.budget().histoChart.checkRightClickOptions(0,
                                                              "Show transactions in Categorization view",
                                                              "Show transactions in Accounts view");
    analysis.budget().histoChart.rightClickAndSelect(0, "Show transactions in Categorization view");
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
    analysis.budget();
    analysis.table().select("Main accounts");
    analysis.budget().histoChart.checkRightClickOptions(0,
                                                              "Show transactions in Categorization view",
                                                              "Show transactions in Accounts view");
    analysis.budget().histoChart.rightClickAndSelect(0, "Show transactions in Accounts view");
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
    analysis.table().select("Savings accounts");
    analysis.budget().histoChart.checkRightClickOptions(0,
                                                              "Show transactions in Categorization view",
                                                              "Show transactions in Accounts view");
    analysis.budget().histoChart.rightClickAndSelect(0, "Show transactions in Accounts view");
    savingsAccounts.checkSelectedAccounts("ING");
    mainAccounts.checkNoAccountsSelected();
    transactions.initContent()
      .add("10/06/2009", TransactionType.PRELEVEMENT, "VIRT", "", -200.00, "Epargne")
      .check();

    // ---- Uncategorized ----

    views.selectAnalysis();
    analysis.table().select("To categorize");
    analysis.budget().histoChart.checkRightClickOptions(1,
                                                              "Show transactions in Categorization view",
                                                              "Show transactions in Accounts view");
    analysis.budget().histoChart.rightClickAndSelect(1, "Show transactions in Categorization view");
    timeline.checkSelection("2009/07");
    categorization.checkShowsUncategorizedTransactionsForSelectedMonths();
    categorization.initContent()
      .add("15/07/2009", "", "DECATHLON", -150.00)
      .add("15/07/2009", "", "FNAC", -100.00)
      .add("15/07/2009", "", "GOSPORT", -150.00)
      .check();

    // ---- BudgetArea ----

    views.selectAnalysis();
    analysis.table().select("Income");
    analysis.budget().histoChart.checkRightClickOptions(0,
                                                              "Show transactions in Categorization view",
                                                              "Show transactions in Accounts view");
    analysis.budget().histoChart.rightClickAndSelect(0, "Show transactions in Categorization view");
    timeline.checkSelection("2009/06");
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("15/06/2009", "Mary's", "BIG INC.", 350.00)
      .add("01/06/2009", "John's", "WORLDCO", 300.00)
      .check();

    views.selectAnalysis();
    analysis.table().select("Income", "Variable");
    analysis.budget().histoChart.checkRightClickOptions(0,
                                                              "Show transactions in Categorization view",
                                                              "Show transactions in Accounts view");
    analysis.budget().histoChart.rightClickAndSelect(0, "Show transactions in Categorization view");
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("10/06/2009", "Groceries", "AUCHAN", -250.00)
      .add("15/06/2009", "Groceries", "AUCHAN", -200.00)
      .add("15/06/2009", "Mary's", "BIG INC.", 350.00)
      .add("01/06/2009", "John's", "WORLDCO", 300.00)
      .check();

    // ---- Series ----

    views.selectAnalysis();
    analysis.table().select("Groceries");
    analysis.budget().histoChart.checkRightClickOptions(0,
                                                              "Show transactions in Categorization view",
                                                              "Show transactions in Accounts view",
                                                              "Edit");
    analysis.budget().histoChart.rightClickAndSelect(0, "Show transactions in Accounts view");
    timeline.checkSelection("2009/06");
    categorization.checkShowsSelectedMonthsOnly();
    views.checkDataSelected();
    transactions.initContent()
      .add("15/06/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -200.00, "Groceries")
      .add("10/06/2009", TransactionType.PRELEVEMENT, "AUCHAN", "", -250.00, "Groceries")
      .check();
    transactions.checkFilterMessage("Envelope");
    transactions.clearCurrentFilter();

    views.selectAnalysis();
    analysis.budget().histoChart.rightClickAndEditSeries(1, "Edit")
      .checkName("Groceries")
      .checkMonthSelected(200907)
      .checkAmount(500.00)
      .validate();
  }
}
