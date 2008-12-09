package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class SeriesEvolutionViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2008/07");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(8).validate();
  }

  public void testStandardDisplay() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "00000123", 25.0, "2008/07/12")
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .addTransaction("2008/07/05", -30.00, "Free Telecom")
      .addTransaction("2008/07/05", -50.00, "EDF")
      .addTransaction("2008/07/01", 300.00, "WorldCo")
      .addTransaction("2008/07/11", -40.00, "SomethingElse")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);
    categorization.setRecurring("EDF", "Energy", MasterCategory.HOUSE, true);
    categorization.setIncome("WorldCo", "Salary", true);

    views.selectBudget();
    budgetView.recurring.editSeries("Energy").setTwoMonths().validate();

    timeline.selectMonth("2008/12");
    budgetView.specials.createSeries()
      .setName("Christmas")
      .setCategory(MasterCategory.GIFTS)
      .setAmount(200.00)
      .validate();

    timeline.selectMonth("2008/07");
    views.selectEvolution();
    seriesEvolution.checkColumnNames(
      "", "June 08", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09"
    );
    seriesEvolution.initContent()
      .add("Balance", "", "80.00", "170.00", "120.00", "170.00", "120.00", "-30.00", "120.00")
      .add("Main account", "", "25.00", "195.00", "315.00", "485.00", "605.00", "575.00", "695.00")
      .add("Savings account", "", "", "", "", "", "", "", "")
      .add("To categorize", "", "-40.00", "", "", "", "", "", "")
      .add("Income", "", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00")
      .add("Salary", "", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00", "300.00")
      .add("Recurring", "", "-80.00", "-30.00", "-80.00", "-30.00", "-80.00", "-30.00", "-80.00")
      .add("Energy", "", "-50.00", "", "-50.00", "", "-50.00", "", "-50.00")
      .add("Internet", "", "-30.00", "-30.00", "-30.00", "-30.00", "-30.00", "-30.00", "-30.00")
      .add("Envelopes", "", "-100.00", "-100.00", "-100.00", "-100.00", "-100.00", "-100.00", "-100.00")
      .add("Groceries", "", "-100.00", "-100.00", "-100.00", "-100.00", "-100.00", "-100.00", "-100.00")
      .add("Occasional", "", "", "", "", "", "", "", "")
      .add("Special", "", "", "", "", "", "", "-200.00", "")
      .add("Christmas", "", "", "", "", "", "", "-200.00", "")
      .add("Savings", "", "", "", "", "", "", "", "")
      .check();
  }

  public void testNoData() throws Exception {
    timeline.checkSelection("2008/07");
    views.selectEvolution();
    seriesEvolution.checkColumnNames(
      "", "June 08", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09"
    );
    seriesEvolution.checkTableIsEmpty(
      "Balance", "Main account", "Savings account", "To categorize",
      "Income", "Recurring", "Envelopes", "Occasional", "Special", "Savings");
  }

  public void testColumnNamesAreUpdatedOnMonthSelection() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .load();

    views.selectBudget();
    timeline.selectMonth("2008/12");
    budgetView.specials.createSeries()
      .setName("Christmas")
      .setCategory(MasterCategory.GIFTS)
      .setAmount(200.00)
      .validate();

    views.selectEvolution();

    timeline.selectMonth("2008/07");
    seriesEvolution.checkColumnNames(
      "", "June 08", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09"
    );
    seriesEvolution.checkRow(
      "Christmas", "", "", "", "", "", "", "-200.00", ""
    );

    timeline.selectMonth("2008/10");
    seriesEvolution.checkColumnNames(
      "", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09", "Feb 09", "Mar 09", "Apr 09"
    );
    seriesEvolution.checkRow(
      "Christmas", "", "", "", "-200.00", "", "", "", ""
    );

    timeline.selectMonths("2008/09", "2008/10", "2008/11");
    seriesEvolution.checkColumnNames(
      "", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09", "Feb 09", "Mar 09"
    );
    seriesEvolution.checkRow(
      "Christmas", "", "", "", "", "-200.00", "", "", ""
    );
  }

  public void testNothingIsShownAfterTheLastMonth() throws Exception {

    operations.openPreferences().setFutureMonthsCount(3).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);

    views.selectEvolution();
    timeline.selectMonths("2008/08");
    seriesEvolution.checkColumnNames(
      "", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09", "Feb 09"
    );
    seriesEvolution.checkRow(
      "Groceries", "-100.00", "-100.00", "-100.00", "-100.00", "", "", "", ""
    );
  }

  public void testExpandAndCollapse() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/02", 200.00, "GlobalCorp")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);
    categorization.setIncome("WorldCo", "Salary", true);
    categorization.setIncome("GlobalCorp", "Salary 2", true);

    views.selectEvolution();
    String[] expanded = {"Balance", "Main account", "Savings account", "To categorize",
                         "Income", "Salary", "Salary 2",
                         "Recurring", "Internet",
                         "Envelopes", "Groceries",
                         "Occasional",
                         "Special",
                         "Savings"};

    String[] collapsed = {"Balance", "Main account", "Savings account", "To categorize",
                          "Income", "Recurring", "Envelopes", "Occasional", "Special", "Savings"};

    seriesEvolution.checkRowLabels(expanded);

    seriesEvolution.collapse();
    seriesEvolution.checkRowLabels(collapsed);

    seriesEvolution.expand();
    seriesEvolution.checkRowLabels(expanded);

    seriesEvolution.doubleClickOnRow("Income");
    seriesEvolution.checkRowLabels("Balance", "Main account", "Savings account", "To categorize",
                                   "Income",
                                   "Recurring", "Internet",
                                   "Envelopes", "Groceries",
                                   "Occasional",
                                   "Special",
                                   "Savings");

    seriesEvolution.expand();
    seriesEvolution.checkRowLabels(expanded);
  }

  public void testEditingASeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -100.00, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);

    views.selectEvolution();
    timeline.selectMonth("2008/07");
    seriesEvolution.checkColumnNames(
      "", "June 08", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09"
    );
    seriesEvolution.checkRow(
      "Groceries", "", "-100.00", "-100.00", "-100.00", "-100.00", "-100.00", "-100.00", "-100.00"
    );

    seriesEvolution.editSeries("Groceries", "Sep 08")
      .switchToManual()
      .selectMonth(200809)
      .setAmount("150")
      .setEndDate(200810)
      .validate();

    seriesEvolution.checkColumnNames(
      "", "June 08", "Jul 08", "Aug 08", "Sep 08", "Oct 08", "Nov 08", "Dec 08", "Jan 09"
    );
    seriesEvolution.checkRow(
      "Groceries", "", "-100.00", "-100.00", "-150.00", "-100.00", "", "", ""
    );
  }

  public void testOnlySeriesWithDataForTheDisplayedPeriodAreShown() throws Exception {
    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("Taxes")
      .setCategory(MasterCategory.TAXES)
      .validate();

    views.selectEvolution();
    seriesEvolution.checkSeriesNotShown("Taxes");

    views.selectBudget();
    budgetView.recurring.editSeries("Taxes")
      .switchToManual()
      .selectAllMonths()
      .setAmount(200)
      .validate();

    views.selectEvolution();
    seriesEvolution.checkRow(
      "Taxes", "", "-200.00", "-200.00", "-200.00", "-200.00", "-200.00", "-200.00", "-200.00"
    );

    views.selectBudget();
    budgetView.recurring.editSeries("Taxes")
      .setEndDate(200808)
      .validate();

    timeline.selectMonth("2008/10");
    views.selectEvolution();
    seriesEvolution.checkSeriesNotShown("Taxes");

    timeline.selectMonth("2008/08");
    seriesEvolution.checkRow(
      "Taxes", "-200.00", "-200.00", "", "", "", "", "", ""
    );

  }
}
