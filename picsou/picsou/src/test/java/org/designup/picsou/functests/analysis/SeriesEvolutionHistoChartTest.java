package org.designup.picsou.functests.analysis;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SeriesEvolutionHistoChartTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2009/07");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(12).validate();
  }

  public void testStandardCase() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/07/30")
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
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/07/30")
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
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/07/30")
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
      .checkLineColumn(0, "J", "2009", 200.00, true)
      .checkLineColumn(1, "A", "2009", 400.00)
      .checkLineColumn(2, "S", "2009", 600.00)
      .checkLineColumn(3, "O", "2009", 800.00)
      .checkLineColumn(6, "J", "2010", 1400.00);
    seriesAnalysis
      .checkHistoChartLabel("End of month position evolution")
      .checkLegendHidden();
    seriesAnalysis.histoChart.checkTooltip(2, "End of September 2009 position: 600.00");

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

  public void testClickingInColumnsNavigatesToCorrespondingMonth() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 1000.0, "2009/07/30")
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
}
