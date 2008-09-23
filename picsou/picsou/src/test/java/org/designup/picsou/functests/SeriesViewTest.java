package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class SeriesViewTest extends LoggedInFunctionalTestCase {

  public void testStandardUsage() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .addTransaction("2008/07/10", -50.00, "Monoprix")
      .addTransaction("2008/07/05", -29.00, "Free Telecom")
      .addTransaction("2008/07/04", -55.00, "EDF")
      .addTransaction("2008/07/03", -15.00, "McDo")
      .addTransaction("2008/07/02", 200.00, "WorldCo - Bonus")
      .addTransaction("2008/07/01", 3540.00, "WorldCo")
      .load();

    views.selectData();
    series.checkContains("All",
                         "To categorize",
                         "Income",
                         "Recurring expenses",
                         "Envelope expenses",
                         "Occasional expenses",
                         "Projects",
                         "Savings");

    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, true);
    categorization.setEnvelope("Monoprix", "Groceries", MasterCategory.FOOD, false);
    categorization.setRecurring("Free Telecom", "Internet", MasterCategory.TELECOMS, true);
    categorization.setRecurring("EDF", "Electricity", MasterCategory.HOUSE, true);
    categorization.setExceptionalIncome("WorldCo - Bonus", "Exceptional Income", true);
    categorization.setIncome("WorldCo", "Salary", true);

    views.selectData();
    series.checkContains("All",
                         "To categorize",
                         "Income", "Exceptional Income", "Salary",
                         "Recurring expenses", "Electricity", "Internet",
                         "Envelope expenses", "Groceries",
                         "Occasional expenses",
                         "Projects",
                         "Savings");

  }

  public void testCreatingAndDeletingSeries() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("New envelope")
      .setCategory(MasterCategory.FOOD)
      .validate();

    views.selectData();
    series.checkContains("All",
                         "To categorize",
                         "Income",
                         "Recurring expenses",
                         "Envelope expenses", "New envelope",
                         "Occasional expenses",
                         "Projects",
                         "Savings");

    views.selectBudget();
    budgetView.envelopes.editSeriesList()
      .selectSeries("New envelope")
      .deleteSeries()
      .validate();

    views.selectData();
    series.checkContains("All",
                         "To categorize",
                         "Income",
                         "Recurring expenses",
                         "Envelope expenses",
                         "Occasional expenses",
                         "Projects",
                         "Savings");
  }

  public void testExpandingAndCollapsingBudgetAreas() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/12", -95.00, "Auchan")
      .load();

    views.selectData();
    series.checkContains("All",
                         "To categorize",
                         "Income",
                         "Recurring expenses",
                         "Envelope expenses",
                         "Occasional expenses",
                         "Projects",
                         "Savings");

    views.selectBudget();
    budgetView.envelopes.createSeries()
      .setName("envelope1")
      .setCategory(MasterCategory.FOOD)
      .validate();

    budgetView.envelopes.createSeries()
      .setName("envelope2")
      .setCategory(MasterCategory.LEISURES)
      .validate();

    views.selectData();
    series.checkContains("All",
                         "To categorize",
                         "Income",
                         "Recurring expenses",
                         "Envelope expenses", "envelope1", "envelope2",
                         "Occasional expenses",
                         "Projects",
                         "Savings");

    series.checkExpansionEnabled("All", false);
    series.checkExpansionEnabled("To categorize", false);
    series.checkExpansionEnabled("Income", false);

    series.checkExpansionEnabled("Envelope expenses", true);
    series.toggle("Envelope expenses");

    series.checkContains("All",
                         "To categorize",
                         "Income",
                         "Recurring expenses",
                         "Envelope expenses",
                         "Occasional expenses",
                         "Projects",
                         "Savings");


  }
}
