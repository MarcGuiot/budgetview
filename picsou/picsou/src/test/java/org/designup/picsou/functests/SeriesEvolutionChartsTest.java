package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SeriesEvolutionChartsTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2009/07");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(12).validate();
  }

  public void test() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "00000123", 1000.0, "2009/07/30")
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

    views.selectCategorization();
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setNewIncome("WorldCo", "John's");
    categorization.setNewIncome("Big Inc.", "Mary's");

    views.selectEvolution();

    timeline.selectMonth("2009/06");
    seriesEvolution.histoChart
      .checkColumnCount(7)
      .checkDiffColumn(0, "J", 650.00, 450.00);

    timeline.selectMonth("2009/07");
    seriesEvolution.histoChart
      .checkColumnCount(8)
      .checkDiffColumn(0, "J", 650.00, 450.00)
      .checkDiffColumn(1, "J", 670.00, 340.00);

    timeline.selectMonth("2009/06");
    seriesEvolution.select("Income");
    seriesEvolution.histoChart
      .checkDiffColumn(0, "J", 650.00, 650.00)
      .checkDiffColumn(1, "J", 650.00, 670.00);
    seriesEvolution.select("John's");
    seriesEvolution.histoChart
      .checkDiffColumn(0, "J", 300.00, 300.00)
      .checkDiffColumn(1, "J", 300.00, 320.00);

    timeline.selectMonth("2009/07");
    seriesEvolution.select("Income");
    seriesEvolution.histoChart
      .checkDiffColumn(0, "J", 650.00, 650.00)
      .checkDiffColumn(1, "J", 650.00, 670.00);
    seriesEvolution.select("John's");
    seriesEvolution.histoChart
      .checkDiffColumn(0, "J", 300.00, 300.00)
      .checkDiffColumn(1, "J", 300.00, 320.00);

    seriesEvolution.select("To categorize");
    seriesEvolution.histoChart
      .checkLineColumn(0, "J", 50.00)
      .checkLineColumn(1, "J", 20.00);
  }

  public void testDisplaysUpToTwelveMonthsInThePast() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "00000123", 1000.0, "2009/07/30")
      .addTransaction("2007/06/10", -250.00, "Auchan")
      .addTransaction("2008/06/10", -200.00, "Auchan")
      .addTransaction("2009/06/10", -150.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewEnvelope("Auchan", "Groceries");

    timeline.selectMonth("2009/07");

    views.selectEvolution();
    seriesEvolution.histoChart
      .checkColumnCount(19)
      .checkDiffColumn(0, "J", 0.00, 0.00)
      .checkDiffColumn(10, "M", 0.00, 0.00)
      .checkDiffColumn(11, "J", 0.00, 150.00);
  }

  public void testAccounts() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "00000123", 1000.0, "2009/07/30")
      .addTransaction("2009/07/10", -200.00, "Virt")
      .load();

    savingsAccounts
      .createNewAccount()
      .setAccountName("ING")
      .selectBank("ING Direct")
      .setBalance(200)
      .validate();

    views.selectCategorization();
    categorization.setNewSavings("Virt", "Epargne", "00000123", "ING");

    views.selectEvolution();
    seriesEvolution.select("Main accounts");
    seriesEvolution.histoChart
      .checkColumnCount(7)
      .checkLineColumn(0, "J", 1000.00)
      .checkLineColumn(1, "A", 800.00)
      .checkLineColumn(2, "S", 600.00)
      .checkLineColumn(3, "O", 400.00)
      .checkLineColumn(6, "J", -200.00);

    seriesEvolution.select("Savings accounts");
    seriesEvolution.histoChart
      .checkColumnCount(7)
      .checkLineColumn(0, "J", 200.00)
      .checkLineColumn(1, "A", 400.00)
      .checkLineColumn(2, "S", 600.00)
      .checkLineColumn(3, "O", 800.00)
      .checkLineColumn(6, "J", 1400.00);
  }

  public void testUncategorized() throws Exception {
    fail("tbd");
  }

  public void testUpdate() throws Exception {
    fail("tbd");
  }
}
