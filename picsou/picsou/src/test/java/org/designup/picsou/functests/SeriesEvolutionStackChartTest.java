package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SeriesEvolutionStackChartTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2009/07");
    super.setUp();
  }

  public void testStandardCase() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, OfxBuilder.DEFAULT_ACCOUNT_ID, 1000.0, "2009/07/30")
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
      .setBalance(0)
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
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 450.00)
      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 50.00)
      .checkValue("Internet", 30.00);

    timeline.selectMonth("2009/07");
    checkStandardCaseMainBalance();
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 450.00)
      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);

    seriesEvolution.select("Balance");
    checkStandardCaseMainBalance();
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 450.00)
      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);

    seriesEvolution.select("Main accounts");
    checkStandardCaseMainBalance();
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 450.00)
      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);

    seriesEvolution.select("Main accounts");
    checkStandardCaseMainBalance();
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 450.00)
      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);

    seriesEvolution.select("Main accounts");
    checkStandardCaseMainBalance();
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(4)
      .checkValue("Groceries", 450.00)
      .checkValue("Virt Livret", 100.00)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);

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

    seriesEvolution.select("Mary's");
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
      .checkValue("Mary's", 350.00, true)
      .checkValue("John's", 320.00);

    seriesEvolution.select("Recurring");
    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00);
    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 90.00, true)
      .checkValue("Envelopes", 450.00)
      .checkValue("Savings", 100.00);
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00);

    seriesEvolution.select("Internet");
    seriesEvolution.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 670.00);
    seriesEvolution.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 90.00, true)
      .checkValue("Envelopes", 450.00)
      .checkValue("Savings", 100.00);
    seriesEvolution.seriesChart.getSingleDataset()
      .checkSize(2)
      .checkValue("Mobile", 60.00)
      .checkValue("Internet", 30.00, true);
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

  public void testSavings() throws Exception {
    fail("tbd");
  }

  public void testDifferentSignsInBudgetAreaSeries() throws Exception {
    fail("tbd");
  }
}
