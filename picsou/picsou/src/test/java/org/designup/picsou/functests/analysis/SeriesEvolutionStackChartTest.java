package org.designup.picsou.functests.analysis;

import org.designup.picsou.functests.checkers.SavingsSetup;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class SeriesEvolutionStackChartTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2009/07/30");
    super.setUp();
  }

  public void testStandardCase() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.0, "2009/07/30")
      .addTransaction("2009/06/10", -250.00, "Auchan")
      .addTransaction("2009/06/15", -200.00, "Auchan")
      .addTransaction("2009/06/18", -100.00, "Virt Epargne")
      .addTransaction("2009/06/20", -30.00, "Free")
      .addTransaction("2009/06/20", -50.00, "Orange")
      .addTransaction("2009/06/01", 300.00, "WorldCo")
      .addTransaction("2009/06/15", 350.00, "Big Inc.")
      .addTransaction("2009/06/20", 50.00, "Unknown")
      .addTransaction("2009/07/10", -200.00, "Auchan")
      .addTransaction("2009/07/15", -140.00, "Auchan")
      .addTransaction("2009/07/01", 320.00, "WorldCo")
      .addTransaction("2009/07/15", 350.00, "Big Inc.")
      .addTransaction("2009/07/20", -20.00, "Unknown")
      .addTransaction("2009/07/20", -30.00, "Free")
      .addTransaction("2009/07/20", -60.00, "Orange")
      .load();

    views.selectHome();
    accounts.createNewAccount()
      .setAsSavings()
      .setName("Livret")
      .selectBank("ING Direct")
      .setPosition(0)
      .validate();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewRecurring("Orange", "Mobile");
    categorization.setNewVariable("Auchan", "Groceries", -450.);
    categorization.setNewSavings("Virt Epargne", "Virt Livret", "Account n. 00001123", "Livret");

    timeline.selectMonth("2009/06");
    views.selectBudget();
    budgetView.savings.alignAndPropagate("Virt Livret");

    views.selectAnalysis();

    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 650.00);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 80.00)
      .checkValue("Variable", 450.00)
      .checkValue("Savings", 100.00);
    seriesAnalysis.checkBalanceChartLabel("Main accounts balance");
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 450.00)
//      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 50.00)
      .checkValue("Internet", 30.00);
    seriesAnalysis.checkSeriesChartLabel("Main series");

    timeline.selectMonth("2009/07");
    checkStandardCaseMainBalance();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 450.00)
//      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);
    seriesAnalysis.checkSeriesChartLabel("Main series");

    seriesAnalysis.select("Balance");
    checkStandardCaseMainBalance();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 450.00)
//      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);
    seriesAnalysis.checkSeriesChartLabel("Main series");

    seriesAnalysis.select("Main accounts");
    checkStandardCaseMainBalance();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 450.00)
//      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);
    seriesAnalysis.checkSeriesChartLabel("Main series");

    seriesAnalysis.select("Income");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00, true);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 90.00)
      .checkValue("Variable", 450.00)
      .checkValue("Savings", 100.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mary's", 350.00)
      .checkValue("John's", 320.00);
    seriesAnalysis.checkSeriesChartLabel("Main income series");

    seriesAnalysis.select("Mary's");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00, true);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 90.00)
      .checkValue("Variable", 450.00)
      .checkValue("Savings", 100.00);
    seriesAnalysis.checkBalanceChartLabel("Main accounts balance");
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mary's", 350.00, true)
      .checkValue("John's", 320.00);
    seriesAnalysis.checkSeriesChartLabel("Main income series");

    seriesAnalysis.select("Recurring");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 90.00, true)
      .checkValue("Variable", 450.00)
      .checkValue("Savings", 100.00);
    seriesAnalysis.checkBalanceChartLabel("Main accounts balance");
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);
    seriesAnalysis.checkSeriesChartLabel("Main recurring series");

    seriesAnalysis.select("Internet");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 90.00, true)
      .checkValue("Variable", 450.00)
      .checkValue("Savings", 100.00);
    seriesAnalysis.checkBalanceChartLabel("Main accounts balance");
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00, true);
    seriesAnalysis.checkSeriesChartLabel("Main recurring series");
  }

  public void testMultiSelection() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.0, "2009/07/30")
      .addTransaction("2009/06/15", 500.00, "Big Inc.") // Income - Mary's
      .addTransaction("2009/07/15", 500.00, "Big Inc.")
      .addTransaction("2009/06/01", 600.00, "WorldCo") // Income - John's
      .addTransaction("2009/07/01", 600.00, "WorldCo")
      .addTransaction("2009/06/10", -300.00, "Auchan") // Variable - Groceries
      .addTransaction("2009/06/15", -200.00, "Auchan")
      .addTransaction("2009/07/10", -200.00, "Auchan")
      .addTransaction("2009/07/15", -200.00, "Auchan")
      .addTransaction("2009/06/20", -30.00, "Free") // Recurring - Internet
      .addTransaction("2009/07/20", -30.00, "Free")
      .addTransaction("2009/06/20", -50.00, "Orange") // Recurring - Mobile
      .addTransaction("2009/07/20", -50.00, "Orange")
      .addTransaction("2009/06/20", 50.00, "Unknown") // Uncategorized
      .addTransaction("2009/07/20", -20.00, "Unknown")
      .addTransaction("2009/06/18", -100.00, "Virt Epargne")
      .load();

    accounts.createNewAccount()
      .setAsSavings()
      .setName("Livret")
      .selectBank("ING Direct")
      .setPosition(0)
      .validate();

    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewRecurring("Orange", "Mobile");
    categorization.setNewVariable("Auchan", "Groceries", -600.00);
    categorization.setNewSavings("Virt Epargne", "Virt Livret", "Account n. 00001123", "Livret");

    timeline.selectMonths("2009/06", "2009/07");

    budgetView.savings.alignAndPropagate("Virt Livret");

    seriesAnalysis.balanceChart.select("Income");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 2200.00, true);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Variable", 1100.00)
      .checkValue("Savings", 200.00)
      .checkValue("Recurring", 160.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mary's", 1000.00)
      .checkValue("John's", 1200.00);
    seriesAnalysis.checkSeriesChartLabel("Main income series");
    seriesAnalysis.checkHistoChartLabel("Budget area Income evolution");

    seriesAnalysis.balanceChart.addToSelection("Variable");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 2200.00, true);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Variable", 1100.00, true)
      .checkValue("Savings", 200.00)
      .checkValue("Recurring", 160.00);
    seriesAnalysis.seriesChart.getLeftDataset()
      .checkSize(2)
      .checkValue("Mary's", 1000.00)
      .checkValue("John's", 1200.00);
    seriesAnalysis.seriesChart.getRightDataset()
      .checkSize(1)
      .checkValue("Groceries", 1100.00);
    seriesAnalysis.checkSeriesChartLabel("Series for 2 budget areas");
    seriesAnalysis.checkHistoChartLabel("2 Budget areas evolution");

    seriesAnalysis.seriesChart.addToSelection("Mary's");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 2200.00, true);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Variable", 1100.00, true)
      .checkValue("Savings", 200.00)
      .checkValue("Recurring", 160.00);
    seriesAnalysis.seriesChart.getLeftDataset()
      .checkSize(2)
      .checkValue("Mary's", 1000.00, true)
      .checkValue("John's", 1200.00);
    seriesAnalysis.seriesChart.getRightDataset()
      .checkSize(1)
      .checkValue("Groceries", 1100.00);
    seriesAnalysis.checkSeriesChartLabel("Series for 2 budget areas");
    seriesAnalysis.checkHistoChartLabel("Evolution of 'Mary's'");

    seriesAnalysis.balanceChart.addToSelection("Recurring");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 2200.00, true);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Variable", 1100.00, true)
      .checkValue("Savings", 200.00)
      .checkValue("Recurring", 160.00, true);
    seriesAnalysis.checkBalanceChartLabel("Main accounts balance");
    seriesAnalysis.seriesChart.getLeftDataset()
      .checkSize(2)
      .checkValue("Mary's", 1000.00, true)
      .checkValue("John's", 1200.00);
    seriesAnalysis.seriesChart.getRightDataset()
      .checkSize(3)
      .checkValue("Groceries", 1100.00)
      .checkValue("Mobile", 100.00)
      .checkValue("Internet", 60.00);
    seriesAnalysis.checkSeriesChartLabel("Series for 3 budget areas");
    seriesAnalysis.checkHistoChartLabel("Evolution of 'Mary's'");

    seriesAnalysis.seriesChart.addToSelection("Groceries");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 2200.00, true);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Variable", 1100.00, true)
      .checkValue("Savings", 200.00)
      .checkValue("Recurring", 160.00, true);
    seriesAnalysis.checkBalanceChartLabel("Main accounts balance");
    seriesAnalysis.seriesChart.getLeftDataset()
      .checkSize(2)
      .checkValue("Mary's", 1000.00, true)
      .checkValue("John's", 1200.00);
    seriesAnalysis.seriesChart.getRightDataset()
      .checkSize(3)
      .checkValue("Groceries", 1100.00, true)
      .checkValue("Mobile", 100.00)
      .checkValue("Internet", 60.00);
    seriesAnalysis.checkSeriesChartLabel("Series for 3 budget areas");
    seriesAnalysis.checkHistoChartLabel("Evolution of 2 envelopes");

    seriesAnalysis.checkSelected("Income", "Variable", "Recurring", "Mary's", "Groceries");
  }

  public void testChartsAreUpdatedWhenSeriesAreModified() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.0, "2009/07/30")
      .addTransaction("2009/06/15", 500.00, "Big Inc.") // Income - Mary's
      .addTransaction("2009/07/15", 500.00, "Big Inc.")
      .addTransaction("2009/06/01", 600.00, "WorldCo") // Income - John's
      .addTransaction("2009/07/01", 600.00, "WorldCo")
      .addTransaction("2009/06/10", -300.00, "Auchan") // Variable - Groceries
      .addTransaction("2009/06/15", -200.00, "Auchan")
      .addTransaction("2009/07/10", -200.00, "Auchan")
      .addTransaction("2009/07/15", -200.00, "Auchan")
      .load();

    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");
    categorization.setNewVariable("Auchan", "Groceries", -600.00);

    timeline.selectMonths("2009/06", "2009/07");

    seriesAnalysis.balanceChart.select("Income");
    seriesAnalysis.seriesChart.addToSelection("Mary's");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 2200.00, true);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(1)
      .checkValue("Variable", 1100.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mary's", 1000.00, true)
      .checkValue("John's", 1200.00);
    seriesAnalysis.checkSeriesChartLabel("Main income series");
    seriesAnalysis.checkHistoChartLabel("Evolution of 'Mary's'");

    budgetView.income.editSeries("Mary's").deleteCurrentSeriesWithConfirmation();
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1200.00, true);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(1)
      .checkValue("Variable", 1100.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("John's", 1200.00);
    seriesAnalysis.checkSeriesChartLabel("Main income series");
    seriesAnalysis.checkHistoChartLabel("Budget area Income evolution");

    seriesAnalysis.seriesChart.select("John's");
    budgetView.income.editSeries("John's").setName("Main").validate();
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1200.00, true);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(1)
      .checkValue("Variable", 1100.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Main", 1200.00, true);
    seriesAnalysis.checkSeriesChartLabel("Main income series");
    seriesAnalysis.checkHistoChartLabel("Evolution of 'Main'");

    seriesAnalysis.balanceChart.select("Variable");
    seriesAnalysis.seriesChart.addToSelection("Groceries");
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Groceries", 1100.00, true);
    budgetView.variable.editSeries("Groceries").setAmount(800.00).validate();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Groceries", 1300.00, true);
    seriesAnalysis.checkSeriesChartLabel("Main variable series");
    seriesAnalysis.checkHistoChartLabel("Evolution of 'Groceries'");
  }

  private void checkStandardCaseMainBalance() {
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00);

    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 90.00)
      .checkValue("Variable", 450.00)
      .checkValue("Savings", 100.00);
  }

  public void testClickNavigation() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.0, "2009/07/30")
      .addTransaction("2009/07/10", -400.00, "Auchan")
      .addTransaction("2009/07/01", 100.00, "WorldCo")
      .addTransaction("2009/07/15", 400.00, "Big Inc.")
      .addTransaction("2009/07/20", -50.00, "Free")
      .addTransaction("2009/07/20", -100.00, "Orange")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewRecurring("Orange", "Mobile");
    categorization.setNewVariable("Auchan", "Groceries");

    views.selectAnalysis();

    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 400.00)
      .checkValue("Mobile", 100.00)
      .checkValue("Internet", 50.00);

    seriesAnalysis.seriesChart.select("Groceries");
    seriesAnalysis.checkSelected("Groceries");
    seriesAnalysis.checkBudgetAndSeriesStacksShown();
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 500.00);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 400.00, true)
      .checkValue("Recurring", 150.00);

    seriesAnalysis.balanceChart.select("Recurring");
    seriesAnalysis.checkSelected("Recurring");
    seriesAnalysis.checkBudgetAndSeriesStacksShown();
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mobile", 100.00)
      .checkValue("Internet", 50.00);

    seriesAnalysis.seriesChart.select("Mobile");
    seriesAnalysis.checkSelected("Mobile");

    seriesAnalysis.seriesChart.select("Mobile", "Internet");
    seriesAnalysis.checkSelected("Mobile", "Internet");
  }

  public void testSavings() throws Exception {

    SavingsSetup.run(this, 200906);

    seriesAnalysis.select("Savings accounts");
    seriesAnalysis.checkBalanceChartLabel("Savings accounts balance");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Added to savings", 620);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(1)
      .checkValue("Substracted from savings", 630);
    seriesAnalysis.checkSeriesChartLabel("Main savings series");
    seriesAnalysis.seriesChart.getLeftDataset()
      .checkSize(4)
      .checkValue("MainToImported", 150)
      .checkValue("MainToNonImported", 50)
      .checkValue("ImportedFromExternal", 200)
      .checkValue("ImportedFromNonImported", 220);
    seriesAnalysis.seriesChart.getRightDataset()
      .checkSize(4)
      .checkValue("ImportedToExternal", 300)
      .checkValue("ImportedFromNonImported", 220)
      .checkValue("ImportedToMain", 70)
      .checkValue("MainFromNonImported", 40);

    timeline.selectMonths("2009/06", "2009/07");
    seriesAnalysis.checkBalanceChartLabel("Savings accounts balance");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Added to savings", 670.00);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(1)
      .checkValue("Substracted from savings", 630.00);
    seriesAnalysis.checkSeriesChartLabel("Main savings series");
    seriesAnalysis.seriesChart.getLeftDataset()
      .checkSize(4)
      .checkValue("MainToImported", 150.00)
      .checkValue("MainToNonImported", 100.00)
      .checkValue("ImportedFromExternal", 200.00)
      .checkValue("ImportedFromNonImported", 220.00);
    seriesAnalysis.seriesChart.getRightDataset()
      .checkSize(4)
      .checkValue("ImportedToExternal", 300.00)
      .checkValue("ImportedFromNonImported", 220.00)
      .checkValue("ImportedToMain", 70.00)
      .checkValue("MainFromNonImported", 40.00);
  }

  public void testUncategorized() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.0, "2009/07/30")
      .addTransaction("2009/06/01", -200.00, "Auchan")
      .addTransaction("2009/07/01", -400.00, "Auchan")
      .addTransaction("2009/07/10", 500.00, "WorldCo")
      .addTransaction("2009/07/15", 200.00, "Unknown 1")
      .addTransaction("2009/07/20", -200.00, "Unknown 2 with a very long label")
      .addTransaction("2009/08/10", -300.00, "Auchan")
      .addTransaction("2009/08/15", 100.00, "Unknown 3")
      .addTransaction("2009/08/20", -100.00, "Unknown 4")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewVariable("Auchan", "Groceries");

    views.selectAnalysis();
    seriesAnalysis.select("To categorize");

    timeline.selectMonth("2009/06");
    seriesAnalysis.balanceChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Categorized", 200.00);
    seriesAnalysis.checkBalanceChartLabel("Total amount of uncategorized operations");
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(0);
    seriesAnalysis.checkSeriesChartLabel("Main operations to categorize");

    timeline.selectMonth("2009/07");
    seriesAnalysis.balanceChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Categorized", 400.00 + 500.00)
      .checkValue("To categorize", 200.00 + 200.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("UNKNOWN 1", 200.00)
      .checkValue("UNKNOWN 2 WITH A ...", 200.00);

    timeline.selectMonth("2009/08");
    seriesAnalysis.balanceChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Categorized", 300.00)
      .checkValue("To categorize", 100.00 + 100.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("UNKNOWN 3", 100.00)
      .checkValue("UNKNOWN 4", 100.00);

    seriesAnalysis.seriesChart.select("UNKNOWN 3");
    views.checkCategorizationSelected();
    categorization.checkShowsUncategorizedTransactionsOnly();
    categorization.checkSelectedTableRow("UNKNOWN 3");

    categorization.setNewVariable("UNKNOWN 3", "Misc");

    views.selectAnalysis();
    seriesAnalysis.checkSelected("To categorize");

    timeline.selectMonth("2009/08");
    seriesAnalysis.balanceChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Categorized", 300.00 + 100.00)
      .checkValue("To categorize", 100.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(1)
      .checkValue("UNKNOWN 4", 100.00);

    timeline.selectMonths("2009/07", "2009/08");
    seriesAnalysis.balanceChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Categorized", 400.00 + 500.00 + 300.00 + 100.00)
      .checkValue("To categorize", 200.00 + 200.00 + 100.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("UNKNOWN 2 WITH A ...", 200.00)
      .checkValue("UNKNOWN 1", 200.00)
      .checkValue("UNKNOWN 4", 100.00);
  }

  public void testDifferentSignsInBudgetAreaSeries() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.0, "2009/07/30")
      .addTransaction("2009/07/10", -400.00, "Auchan")
      .addTransaction("2009/07/01", 100.00, "WorldCo")
      .addTransaction("2009/07/15", 400.00, "Big Inc.")
      .addTransaction("2009/07/20", 450.00, "Check n. 12345")
      .addTransaction("2009/07/20", -200.00, "Fouquet's")
      .addTransaction("2009/07/20", -50.00, "Free")
      .addTransaction("2009/07/20", -100.00, "Orange")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewRecurring("Orange", "Mobile");
    categorization.setNewVariable("Auchan", "Groceries");
    categorization.setNewExtra("Check n. 12345", "Gift");
    categorization.setNewExtra("Fouquet's", "Dining");

    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(2)
      .checkValue("Income", 500.00)
      .checkValue("Extras", 250.00);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 400.00)
      .checkValue("Recurring", 150.00);

    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 400.00)
      .checkValue("Dining", 200.00)
      .checkValue("Mobile", 100.00)
      .checkValue("Internet", 50.00);

    seriesAnalysis.select("Extras");
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(2)
      .checkValue("Income", 500.00)
      .checkValue("Extras", 250.00, true);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Variable", 400.00)
      .checkValue("Recurring", 150.00);
    seriesAnalysis.seriesChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Gift", 450.00);
    seriesAnalysis.seriesChart.getRightDataset()
      .checkSize(1)
      .checkValue("Dining", 200.00);
  }

  public void testNavigatingToASeriesExpandsTheBudgetAreaInTheTable() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.0, "2009/07/30")
      .addTransaction("2009/07/10", -250.00, "Auchan")
      .addTransaction("2009/07/20", -200.00, "Auchan")
      .addTransaction("2009/07/05", -50.00, "Elf")
      .addTransaction("2009/07/25", -50.00, "Elf")
      .addTransaction("2009/07/01", 300.00, "WorldCo")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewVariable("Auchan", "Groceries");
    categorization.setNewVariable("Elf", "Fuel");

    views.selectAnalysis();
    seriesAnalysis.showChartsAndTable();

    timeline.selectMonth("2009/07");
    seriesAnalysis.checkBudgetAndSeriesStacksShown();
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 300.00);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(1)
      .checkValue("Variable", 550.00);
    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 450.00)
      .checkValue("Fuel", 100.00);

    seriesAnalysis.collapseAll();
    seriesAnalysis.seriesChart.select("Groceries");
    seriesAnalysis.checkSelected("Groceries");
  }

  public void testPopupMenus() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.0, "2009/07/30")
      .addTransaction("2009/06/10", -250.00, "Auchan")
      .addTransaction("2009/06/15", -200.00, "Auchan")
      .addTransaction("2009/06/18", -100.00, "Virt Epargne")
      .addTransaction("2009/06/20", -30.00, "Free")
      .addTransaction("2009/06/20", -50.00, "Orange")
      .addTransaction("2009/06/01", 300.00, "WorldCo")
      .addTransaction("2009/06/15", 350.00, "Big Inc.")
      .addTransaction("2009/06/20", 50.00, "Unknown1")
      .addTransaction("2009/06/20", -100.00, "Unknown2")
      .addTransaction("2009/07/10", -200.00, "Auchan")
      .addTransaction("2009/07/15", -140.00, "Auchan")
      .addTransaction("2009/07/01", 320.00, "WorldCo")
      .addTransaction("2009/07/15", 350.00, "Big Inc.")
      .addTransaction("2009/07/20", -20.00, "Unknown3")
      .addTransaction("2009/07/20", -30.00, "Free")
      .addTransaction("2009/07/20", -60.00, "Orange")
      .load();

    views.selectHome();
    accounts.createNewAccount()
      .setAsSavings()
      .setName("Livret")
      .selectBank("ING Direct")
      .setPosition(0)
      .validate();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewRecurring("Orange", "Mobile");
    categorization.setNewVariable("Auchan", "Groceries", -450.00);
    categorization.setNewSavings("Virt Epargne", "Virt Livret", "Account n. 00001123", "Livret");
    categorization.showUncategorizedTransactionsOnly();

    timeline.selectMonth(200907);

    // ---- BudgetArea ----


    views.selectAnalysis();
    seriesAnalysis.balanceChart.checkRightClickOptions("Income",
                                                       "Show transactions in Categorization view",
                                                       "Show transactions in Accounts view");
    seriesAnalysis.balanceChart.rightClickAndSelect("Income", "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("15/07/2009", "Mary's", "BIG INC.", 350.00)
      .add("01/07/2009", "John's", "WORLDCO", 320.00)
      .check();

    // ---- Series ----

    views.selectAnalysis();
    seriesAnalysis.balanceChart.select("Variable");
    seriesAnalysis.seriesChart.checkRightClickOptions("Groceries",
                                                      "Show transactions in Categorization view",
                                                      "Show transactions in Accounts view",
                                                      "Edit");
    seriesAnalysis.seriesChart.rightClickAndSelect("Groceries", "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("10/07/2009", "Groceries", "AUCHAN", -200.00)
      .add("15/07/2009", "Groceries", "AUCHAN", -140.00)
      .check();

    views.selectAnalysis();
    seriesAnalysis.seriesChart.rightClickAndEditSeries("Groceries", "Edit")
      .checkName("Groceries")
      .checkAmount(450.00)
      .validate();

    // ---- Multi-series ----

    views.selectAnalysis();
    seriesAnalysis.balanceChart.select("Recurring");
    seriesAnalysis.seriesChart
      .select("Mobile", "Internet")
      .checkRightClickOptions("Internet",
                              "Show transactions in Categorization view",
                              "Show transactions in Accounts view");
    seriesAnalysis.seriesChart.rightClickAndSelect("Internet", "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.initContent()
      .add("20/07/2009", "Internet", "FREE", -30.00)
      .add("20/07/2009", "Mobile", "ORANGE", -60.00)
      .check();

    // ---- Uncategorized budget area ----

    timeline.selectMonths(200906, 200907);
    views.selectAnalysis();
    seriesAnalysis.select("To categorize");
    seriesAnalysis.balanceChart.checkRightClickOptions("To categorize",
                                                       "Show transactions in Categorization view",
                                                       "Show transactions in Accounts view");
    seriesAnalysis.balanceChart.rightClickAndSelect("To categorize", "Show transactions in Categorization view");
    views.checkCategorizationSelected();
    categorization.checkShowsUncategorizedTransactionsForSelectedMonths();
    categorization.initContent()
      .add("20/06/2009", "", "UNKNOWN1", 50.00)
      .add("20/06/2009", "", "UNKNOWN2", -100.00)
      .add("20/07/2009", "", "UNKNOWN3", -20.00)
      .check();

    // ---- Uncategorized transactions ----

    views.selectAnalysis();
    seriesAnalysis.select("To categorize");
    seriesAnalysis.seriesChart.checkRightClickOptions("UNKNOWN2",
                                                      "Show transactions in Categorization view",
                                                      "Show transactions in Accounts view");
    seriesAnalysis.seriesChart.rightClickAndSelect("UNKNOWN2", "Show transactions in Categorization view");
    categorization.checkShowsUncategorizedTransactionsForSelectedMonths();
    categorization.initContent()
      .add("20/06/2009", "", "UNKNOWN1", 50.00)
      .add("20/06/2009", "", "UNKNOWN2", -100.00)
      .add("20/07/2009", "", "UNKNOWN3", -20.00)
      .check();
    categorization.checkSelectedTableRow("UNKNOWN2");
  }

  public void testRightClickChangesSelectionIfClickedOnNewItem() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.0, "2009/06/30")
      .addTransaction("2009/06/10", -250.00, "Auchan")
      .addTransaction("2009/06/15", -200.00, "Auchan")
      .addTransaction("2009/06/18", -100.00, "Virt Epargne")
      .addTransaction("2009/06/20", -30.00, "Free")
      .addTransaction("2009/06/20", -50.00, "Orange")
      .addTransaction("2009/06/01", 300.00, "WorldCo")
      .addTransaction("2009/06/15", 350.00, "Big Inc.")
      .addTransaction("2009/06/20", 50.00, "Unknown1")
      .addTransaction("2009/06/20", -100.00, "Unknown2")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewRecurring("Orange", "Mobile");
    categorization.setNewVariable("Auchan", "Groceries", -450.00);

    timeline.selectMonth(200906);

    // -- Click in existing selection ==> preserve

    views.selectAnalysis();
    seriesAnalysis.balanceChart
      .select("Income");
    seriesAnalysis.seriesChart
      .select("John's")
      .addToSelection("Mary's");
    seriesAnalysis.seriesChart
      .rightClickAndSelect("John's", "Show transactions in Accounts view");
    views.checkDataSelected();
    transactions.initContent()
      .add("15/06/2009", TransactionType.VIREMENT, "BIG INC.", "", 350.00, "Mary's")
      .add("01/06/2009", TransactionType.VIREMENT, "WORLDCO", "", 300.00, "John's")
      .check();
    views.selectAnalysis();
    seriesAnalysis.checkSelected("John's", "Mary's");

    // -- Click in existing selection ==> change

    views.selectAnalysis();
    seriesAnalysis.balanceChart
      .select("Income");
    seriesAnalysis.seriesChart
      .select("John's");

    seriesAnalysis.seriesChart
      .rightClickAndSelect("Mary's", "Show transactions in Accounts view");
    views.checkDataSelected();
    transactions.initContent()
      .add("15/06/2009", TransactionType.VIREMENT, "BIG INC.", "", 350.00, "Mary's")
      .check();
    views.selectAnalysis();
    seriesAnalysis.checkSelected("Mary's");

  }

  public void testMirrorSavingsSeriesAreNotShown() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Virement")
      .load();

    accounts.createNewAccount()
      .setAsSavings()
      .setName("Livret")
      .selectBank("ING Direct")
      .setAccountNumber("333")
      .setPosition(10.00)
      .validate();

    budgetView.savings.createSavingSeries("To account Livret", "Account n. 00001123", "Livret");

    OfxBuilder.init(this)
      .addBankAccount("333", 20, "2008/07/12")
      .addTransaction("2008/07/12", +95.00, "Virt livret")
      .loadInAccount("Livret");

    categorization.setSavings("Virement", "To account Livret");
    categorization.setSavings("Virt livret", "To account Livret");

    seriesAnalysis.seriesChart.getSingleDataset()
      .checkSize(0);
      //.checkValue("To account Livret", 95.00);

//    seriesAnalysis.seriesChart.select("To account Livret");
//    seriesAnalysis.checkSelected("To account Livret");
  }
}
