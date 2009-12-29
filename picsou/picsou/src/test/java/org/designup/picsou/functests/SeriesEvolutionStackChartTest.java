package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

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
    savingsAccounts.createNewAccount()
      .setAccountName("Livret")
      .selectBank("ING Direct")
      .setPosition(0)
      .validate();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");
    categorization.setNewRecurring("Free", "Internet");
    categorization.setNewRecurring("Orange", "Mobile");
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setNewSavings("Virt Epargne", "Virt Livret", OfxBuilder.DEFAULT_ACCOUNT_NAME, "Livret");

    views.selectEvolution();

    timeline.selectMonth("2009/06");
    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 650.00);
    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 80.00)
      .checkValue("Envelopes", 450.00)
      .checkValue("Savings", 100.00);
    seriesEvolution.checkBalanceChartLabel("Main accounts balance");
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 450.00)
      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 50.00)
      .checkValue("Internet", 30.00);
    seriesEvolution.checkSeriesChartLabel("Main series");

    timeline.selectMonth("2009/07");
    checkStandardCaseMainBalance();
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 450.00)
      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);
    seriesEvolution.checkSeriesChartLabel("Main series");

    seriesEvolution.select("Balance");
    checkStandardCaseMainBalance();
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 450.00)
      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);
    seriesEvolution.checkSeriesChartLabel("Main series");

    seriesEvolution.select("Main accounts");
    checkStandardCaseMainBalance();
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 450.00)
      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);
    seriesEvolution.checkSeriesChartLabel("Main series");

    seriesEvolution.select("Income");
    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00, true);
    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 90.00)
      .checkValue("Envelopes", 450.00)
      .checkValue("Savings", 100.00);
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mary's", 350.00)
      .checkValue("John's", 320.00);
    seriesEvolution.checkSeriesChartLabel("Main income series");

    seriesEvolution.select("Mary's");
    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00, true);
    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 90.00)
      .checkValue("Envelopes", 450.00)
      .checkValue("Savings", 100.00);
    seriesEvolution.checkBalanceChartLabel("Main accounts balance");
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mary's", 350.00, true)
      .checkValue("John's", 320.00);
    seriesEvolution.checkSeriesChartLabel("Main income series");

    seriesEvolution.select("Recurring");
    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00);
    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 90.00, true)
      .checkValue("Envelopes", 450.00)
      .checkValue("Savings", 100.00);
    seriesEvolution.checkBalanceChartLabel("Main accounts balance");
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);
    seriesEvolution.checkSeriesChartLabel("Main recurring series");

    seriesEvolution.select("Internet");
    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00);
    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 90.00, true)
      .checkValue("Envelopes", 450.00)
      .checkValue("Savings", 100.00);
    seriesEvolution.checkBalanceChartLabel("Main accounts balance");
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00, true);
    seriesEvolution.checkSeriesChartLabel("Main recurring series");
  }

  private void checkStandardCaseMainBalance() {
    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00);

    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 90.00)
      .checkValue("Envelopes", 450.00)
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
    categorization.setNewEnvelope("Auchan", "Groceries");

    views.selectEvolution();

    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(3)
      .checkValue("Groceries", 400.00)
      .checkValue("Mobile", 100.00)
      .checkValue("Internet", 50.00);

    seriesEvolution.seriesChart.click(0.2, 0.9);
    seriesEvolution.checkSelected("Groceries");

    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 500.00);
    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Envelopes", 400.00, true)
      .checkValue("Recurring", 150.00);

    seriesEvolution.balanceChart.click(0.8, 0.2);
    seriesEvolution.checkSelected("Recurring");

    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mobile", 100.00)
      .checkValue("Internet", 50.00);

    seriesEvolution.seriesChart.click(0.5, 0.5);
    seriesEvolution.checkSelected("Mobile");
  }

  public void testSavings() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0001", 1000.0, "2009/07/30")
      .addTransaction("2009/06/01", -150.00, "MAIN TO IMPORTED")
      .addTransaction("2009/06/01", 70.00, "MAIN FROM IMPORTED")
      .addTransaction("2009/06/01", -50.00, "MAIN TO NON IMPORTED")
      .addTransaction("2009/06/01", 40.00, "MAIN FROM NON IMPORTED")
      .addTransaction("2009/06/01", 500.00, "WORLDCO")
      .addTransaction("2009/07/01", -200.00, "MAIN TO IMPORTED")
      .addTransaction("2009/07/01", -50.00, "MAIN TO NON IMPORTED")
      .addTransaction("2009/07/01", 500.00, "WORLDCO")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0002", 20000.0, "2009/07/30")
      .addTransaction("2009/06/01", 150.00, "IMPORTED FROM MAIN")
      .addTransaction("2009/06/01", -70.00, "IMPORTED TO MAIN")
      .addTransaction("2009/06/01", 220.00, "IMPORTED FROM NON IMPORTED")
      .addTransaction("2009/06/01", 200.00, "IMPORTED FROM EXTERNAL")
      .addTransaction("2009/06/01", -300.00, "IMPORTED TO EXTERNAL ")
      .load();

    timeline.selectMonth("2009/06");

    views.selectHome();
    mainAccounts.edit("Account n. 0001")
      .setAccountName("Main")
      .validate();
    mainAccounts.edit("Account n. 0002")
      .setAccountName("Imported Savings")
      .setAsSavings()
      .selectBank("ING Direct")
      .validate();
    savingsAccounts.createNewAccount()
      .setAccountName("Non-imported Savings")
      .setAsSavings()
      .selectBank("ING Direct")
      .setPosition(5000.00)
      .validate();

    views.selectCategorization();
    categorization.setNewSavings("MAIN TO IMPORTED", "MainToImported", "Main", "Imported Savings");
    categorization.setSavings("IMPORTED FROM MAIN", "MainToImported");
    categorization.setNewSavings("MAIN FROM IMPORTED", "ImportedToMain", "Imported Savings", "Main");
    categorization.setSavings("IMPORTED TO MAIN", "ImportedToMain");
    categorization.setNewSavings("MAIN TO NON IMPORTED", "MainToNonImported", "Main", "Non-imported Savings");
    categorization.setNewSavings("MAIN FROM NON IMPORTED", "MainFromNonImported", "Non-imported Savings", "Main");
    categorization.setNewSavings("IMPORTED FROM NON IMPORTED", "ImportedFromNonImported", "Non-imported Savings", "Imported Savings");
    categorization.setNewSavings("IMPORTED FROM EXTERNAL", "ImportedFromExternal", "External account", "Imported Savings");
    categorization.setNewSavings("IMPORTED TO EXTERNAL", "ImportedToExternal", "Imported Savings", "External account");

    views.selectEvolution();
    seriesEvolution.select("Savings accounts");

    seriesEvolution.checkBalanceChartLabel("Savings accounts balance");
    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Added to savings", 150 + 50 + 200);
    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(1)
      .checkValue("Substracted from savings", 300 + 70 + 40);

    seriesEvolution.checkSeriesChartLabel("Main savings series");
    seriesEvolution.seriesChart.getLeftDataset()
      .checkSize(3)
      .checkValue("MainToImported", 150)
      .checkValue("MainToNonImported", 50)
      .checkValue("ImportedFromExternal", 200);
    seriesEvolution.seriesChart.getRightDataset()
      .checkSize(3)
      .checkValue("ImportedToExternal", 300)
      .checkValue("ImportedToMain", 70)
      .checkValue("MainFromNonImported", 40);
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
    categorization.setNewEnvelope("Auchan", "Groceries");

    views.selectEvolution();
    seriesEvolution.select("To categorize");

    timeline.selectMonth("2009/06");
    seriesEvolution.balanceChart.getSingleDataset()
      .checkSize(1)
      .checkValue("Categorized", 200.00);
    seriesEvolution.checkBalanceChartLabel("Total amount of uncategorized operations");
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(0);
    seriesEvolution.checkSeriesChartLabel("Main operations to categorize");

    timeline.selectMonth("2009/07");
    seriesEvolution.balanceChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Categorized", 400.00 + 500.00)
      .checkValue("To categorize", 200.00 + 200.00);
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("UNKNOWN 1", 200.00)
      .checkValue("UNKNOWN 2 WITH A ...", 200.00);

    timeline.selectMonth("2009/08");
    seriesEvolution.balanceChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Categorized", 300.00)
      .checkValue("To categorize", 100.00 + 100.00);
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("UNKNOWN 3", 100.00)
      .checkValue("UNKNOWN 4", 100.00);

    seriesEvolution.seriesChart.click(0.5, 0.1);
    views.checkCategorizationSelected();
    categorization.checkShowsUncategorizedTransactionsOnly();
    categorization.checkSelectedTableRow("UNKNOWN 3");

    categorization.setNewEnvelope("UNKNOWN 3", "Misc");
    
    views.selectEvolution();
    seriesEvolution.checkSelected("To categorize");
    timeline.selectMonth("2009/08");
    seriesEvolution.balanceChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Categorized", 300.00 + 100.00)
      .checkValue("To categorize", 100.00);
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(1)
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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setNewExtra("Check n. 12345", "Gift");
    categorization.setNewExtra("Fouquet's", "Dining");

    views.selectEvolution();

    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(2)
      .checkValue("Income", 500.00)
      .checkValue("Extras", 250.00);
    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Envelopes", 400.00)
      .checkValue("Recurring", 150.00);

    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 400.00)
      .checkValue("Dining", 200.00)
      .checkValue("Mobile", 100.00)
      .checkValue("Internet", 50.00);

    seriesEvolution.select("Extras");
    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(2)
      .checkValue("Income", 500.00)
      .checkValue("Extras", 250.00, true);
    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(2)
      .checkValue("Envelopes", 400.00)
      .checkValue("Recurring", 150.00);
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Gift", 450.00)
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
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setNewEnvelope("Elf", "Fuel");

    views.selectEvolution();

    timeline.selectMonth("2009/07");
    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 300.00);
    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(1)
      .checkValue("Envelopes", 550.00);
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Groceries", 450.00)
      .checkValue("Fuel", 100.00);

    seriesEvolution.collapseAll();
    seriesEvolution.seriesChart.click(0.5, 0.8);

    seriesEvolution.checkSelected("Groceries");
  }
}
