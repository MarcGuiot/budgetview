package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

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
                         "Recurring",
                         "Envelopes",
                         "Special",
                         "Savings");

    views.selectCategorization();
    categorization.setNewEnvelope("Auchan", "Groceries");
    categorization.setEnvelope("Monoprix", "Groceries");
    categorization.setNewRecurring("Free Telecom", "Internet");
    categorization.setNewRecurring("EDF", "Electricity");
    categorization.setExceptionalIncome("WorldCo - Bonus", "Exceptional Income", true);
    categorization.setNewIncome("WorldCo", "Salary");

    views.selectData();
    series.checkContains("All",
                         "To categorize",
                         "Income", "Exceptional Income", "Salary",
                         "Recurring", "Electricity", "Internet",
                         "Envelopes", "Groceries",
                         "Special",
                         "Savings");

    series.select("Income");
    transactions.initContent()
      .add("02/07/2008", TransactionType.VIREMENT, "WorldCo - Bonus", "", 200.00, "Exceptional Income")
      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00, "Salary")
      .check();

    series.select("Salary");
    transactions.initContent()
      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00, "Salary")
      .check();

    series.select("All");
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "Groceries")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00, "Groceries")
      .add("05/07/2008", TransactionType.PRELEVEMENT, "Free Telecom", "", -29.00, "Internet")
      .add("04/07/2008", TransactionType.PRELEVEMENT, "EDF", "", -55.00, "Electricity")
      .add("03/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -15.00, "To categorize")
      .add("02/07/2008", TransactionType.VIREMENT, "WorldCo - Bonus", "", 200.00, "Exceptional Income")
      .add("01/07/2008", TransactionType.VIREMENT, "WorldCo", "", 3540.00, "Salary")
      .check();

    series.select("Groceries");
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "Groceries")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Monoprix", "", -50.00, "Groceries")
      .check();

    series.select("To categorize");
    transactions.initContent()
      .add("03/07/2008", TransactionType.PRELEVEMENT, "McDo", "", -15.00, "To categorize")
      .check();
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
                         "Recurring",
                         "Envelopes", "New envelope",
                         "Special",
                         "Savings");

    views.selectBudget();
    budgetView.envelopes.editSeriesList()
      .selectSeries("New envelope")
      .deleteSelectedSeries()
      .validate();

    views.selectData();
    series.checkContains("All",
                         "To categorize",
                         "Income",
                         "Recurring",
                         "Envelopes",
                         "Special",
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
                         "Recurring",
                         "Envelopes",
                         "Special",
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
                         "Recurring",
                         "Envelopes", "envelope1", "envelope2",
                         "Special",
                         "Savings");

    series.checkExpansionEnabled("All", false);
    series.checkExpansionEnabled("To categorize", false);
    series.checkExpansionEnabled("Income", false);

    series.checkExpansionEnabled("Envelopes", true);
    series.toggle("Envelopes");

    series.checkContains("All",
                         "To categorize",
                         "Income",
                         "Recurring",
                         "Envelopes",
                         "Special",
                         "Savings");
  }
}
