package com.budgetview.functests.analysis;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import org.junit.Test;

public class AnalysisEvolutionViewTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2015/07");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(6).validate();
    addOns.activateAnalysis();
  }

  @Test
  public void testStandardDisplay() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", -125.0, "2008/07/12")
      .addTransaction("2015/01/12", -10.00, "Auchan")
      .addTransaction("2015/02/12", -20.00, "Auchan")
      .addTransaction("2015/04/12", -40.00, "Auchan")
      .addTransaction("2015/06/12", -60.00, "Auchan")
      .addTransaction("2015/07/12", -70.00, "Auchan")
      .addTransaction("2015/06/05", -30.00, "Free Telecom")
      .addTransaction("2015/07/05", -30.00, "Free Telecom")
      .addTransaction("2015/07/05", -50.00, "EDF")
      .addTransaction("2015/06/01", 300.00, "WorldCo")
      .addTransaction("2015/07/01", 300.00, "WorldCo")
      .addTransaction("2015/07/11", -40.00, "SomethingElse")
      .load();

    categorization.setNewVariable("Auchan", "Groceries", -100.00);
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Energy");
    categorization.setNewIncome("WorldCo", "Salary");

    budgetView.recurring.editSeries("Energy").setRepeatEveryTwoMonths().validate();

    timeline.selectMonth("2015/07");

    analysis.evolution()
      .selectIncome()
      .checkSeriesList("Salary")
      .selectRecurring()
      .checkSeriesList("Energy", "Internet")
      .selectVariable()
      .checkSeriesList("Groceries");

    analysis.evolution()
      .initSeriesGraph("Groceries")
      .checkColumnCount(13)
      .checkDiffColumn(0, "J", "2015", 100.00, 10.00)
      .checkDiffColumn(1, "F", "2015", 100.00, 20.00)
      .checkDiffColumn(2, "M", "2015", 100.00, 0.00)
      .checkDiffColumn(3, "A", "2015", 100.00, 40.00)
      .checkDiffColumn(4, "M", "2015", 100.00, 0.00)
      .checkDiffColumn(5, "J", "2015", 100.00, 60.00)
      .checkDiffColumn(6, "J", "2015", 100.00, 70.00, true)
      .checkDiffColumn(7, "A", "2015", 100.00, 0.00)
      .checkDiffColumn(8, "S", "2015", 100.00, 0.00)
      .checkDiffColumn(9, "O", "2015", 100.00, 0.00)
      .checkDiffColumn(10, "N", "2015", 100.00, 0.00)
      .checkDiffColumn(11, "D", "2015", 100.00, 0.00)
      .checkDiffColumn(12, "J", "2016", 100.00, 0.00);

    analysis.evolution()
      .selectRecurring()
      .checkSeriesList("Energy", "Internet");

    analysis.evolution()
      .initBudgetAreaGraph()
      .checkColumnCount(13)
      .checkDiffColumn(0, "J", "2015", 0.00, 0.00)
      .checkDiffColumn(1, "F", "2015", 0.00, 0.00)
      .checkDiffColumn(2, "M", "2015", 0.00, 0.00)
      .checkDiffColumn(3, "A", "2015", 0.00, 0.00)
      .checkDiffColumn(4, "M", "2015", 0.00, 0.00)
      .checkDiffColumn(5, "J", "2015", 30.00, 30.00)
      .checkDiffColumn(6, "J", "2015", 80.00, 80.00, true)
      .checkDiffColumn(7, "A", "2015", 30.00, 0.00)
      .checkDiffColumn(8, "S", "2015", 80.00, 0.00)
      .checkDiffColumn(9, "O", "2015", 30.00, 0.00)
      .checkDiffColumn(10, "N", "2015", 80.00, 0.00)
      .checkDiffColumn(11, "D", "2015", 30.00, 0.00)
      .checkDiffColumn(12, "J", "2016", 80.00, 0.00);

    analysis.evolution()
      .initSeriesGraph("Energy")
      .checkColumnCount(13)
      .checkDiffColumn(0, "J", "2015", 0.00, 0.00)
      .checkDiffColumn(1, "F", "2015", 0.00, 0.00)
      .checkDiffColumn(2, "M", "2015", 0.00, 0.00)
      .checkDiffColumn(3, "A", "2015", 0.00, 0.00)
      .checkDiffColumn(4, "M", "2015", 0.00, 0.00)
      .checkDiffColumn(5, "J", "2015", 0.00, 0.00)
      .checkDiffColumn(6, "J", "2015", 50.00, 50.00, true)
      .checkDiffColumn(7, "A", "2015", 0.00, 0.00)
      .checkDiffColumn(8, "S", "2015", 50.00, 0.00)
      .checkDiffColumn(9, "O", "2015", 0.00, 0.00)
      .checkDiffColumn(10, "N", "2015", 50.00, 0.00)
      .checkDiffColumn(11, "D", "2015", 0.00, 0.00)
      .checkDiffColumn(12, "J", "2016", 50.00, 0.00);
  }

  @Test
  public void testEditingAndDeletingSeries() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", -125.0, "2008/07/12")
      .addTransaction("2015/01/12", -10.00, "Auchan")
      .addTransaction("2015/02/12", -20.00, "Auchan")
      .addTransaction("2015/04/12", -40.00, "Auchan")
      .addTransaction("2015/06/12", -60.00, "Auchan")
      .addTransaction("2015/07/12", -70.00, "Auchan")
      .addTransaction("2015/06/05", -30.00, "Free Telecom")
      .addTransaction("2015/07/05", -30.00, "Free Telecom")
      .addTransaction("2015/07/05", -50.00, "EDF")
      .addTransaction("2015/06/01", 300.00, "WorldCo")
      .addTransaction("2015/07/01", 300.00, "WorldCo")
      .addTransaction("2015/07/11", -40.00, "SomethingElse")
      .load();

    categorization.setNewVariable("Auchan", "Groceries", -100.00);
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Energy");
    categorization.setNewIncome("WorldCo", "Salary");

    timeline.selectMonth("2015/07");

    analysis.evolution()
      .selectRecurring()
      .checkSeriesList("Energy", "Internet")
      .editSeries("Internet")
      .setName("AOL")
      .validate();

    analysis.evolution()
      .checkSeriesList("Energy", "AOL")
      .deleteSeriesWithConfirmation("Energy")
      .checkSeriesList("AOL");

    budgetView.recurring.checkContent(
      "| AOL | 30.00 | 30.00 |"
    );
  }
}
