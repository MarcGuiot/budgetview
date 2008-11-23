package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class SeriesWizardTest extends LoggedInFunctionalTestCase {

  public void testStandardUsage() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/1", 3000.00, "WorldCo")
      .addTransaction("2008/06/5", -1000.00, "Rent for june")
      .addTransaction("2008/07/10", -50.00, "SFR")
      .addTransaction("2008/07/15", -40.00, "Auchan")
      .addTransaction("2008/07/20", -200.00, "ING")
      .load();

    views.selectHome();
    monthSummary.openSeriesWizard()
      .select("Income 1")
      .select("Exceptional income")
      .select("Rent")
      .select("Cell phone 1")
      .checkSelected("Groceries")
      .checkSelected("Health")
      .select("Regular savings")
      .select("Irregular savings")
      .validate();

    views.checkCategorizationSelected();
    timeline.selectAll();

    views.selectHome();
    monthSummary.checkSeriesWizardButtonVisible(false);

    views.selectBudget();

    budgetView.income.editSeriesList()
      .checkSeriesListEquals("Exceptional Income", "Income 1")
      .selectSeries("Income 1")
      .checkSelectedProfile("Every month")
      .checkCategory(MasterCategory.INCOME)
      .selectSeries("Exceptional Income")
      .checkSelectedProfile("Irregular")
      .checkCategory(MasterCategory.INCOME)
      .cancel();

    budgetView.recurring.editSeriesList()
      .checkSeriesListEquals("Cell phone 1", "Rent")
      .selectSeries("Rent")
      .checkSelectedProfile("Every month")
      .checkCategory("Rent")
      .selectSeries("Cell phone 1")
      .checkSelectedProfile("Every month")
      .checkCategory(MasterCategory.TELECOMS)
      .cancel();

    budgetView.envelopes.editSeriesList()
      .checkSeriesListContains("Groceries", "Health")
      .selectSeries("Groceries")
      .checkSelectedProfile("Every month")
      .checkCategory(MasterCategory.FOOD)
      .selectSeries("Health")
      .checkSelectedProfile("Every month")
      .checkCategory("Doctor", "Health", "Mutuelle", "Pharmacy", "Reimboursements")
      .cancel();

    budgetView.savings.editSeriesList()
      .checkSeriesListEquals("Irregular savings", "Regular savings")
      .selectSeries("Regular savings")
      .checkSelectedProfile("Every month")
      .checkCategory(MasterCategory.SAVINGS)
      .selectSeries("Irregular savings")
      .checkSelectedProfile("Irregular")
      .checkCategory(MasterCategory.SAVINGS)
      .cancel();

    views.selectCategorization();

    categorization.setIncome("WorldCo", "Income 1", false);
    categorization.setRecurring("Rent for june", "Rent", MasterCategory.HOUSE, false);
    categorization.setRecurring("SFR", "Cell phone 1", MasterCategory.TELECOMS, false);
    categorization.setEnvelope("Auchan", "Groceries", MasterCategory.FOOD, false);
    categorization.setSavings("ING", "Regular savings", MasterCategory.SAVINGS, false);

    views.selectData();
    timeline.selectMonths("2008/06", "2008/07", "2008/08");
    transactions.initContent()
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Regular savings", "", -200.00, "Regular savings", MasterCategory.SAVINGS)
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Groceries", "", -40.00, "Groceries", MasterCategory.FOOD)
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Cell phone 1", "", -50.00, "Cell phone 1", MasterCategory.TELECOMS)
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Rent", "", -1000.00, "Rent", "Rent")
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Income 1", "", 3000.00, "Income 1", MasterCategory.INCOME)
      .add("20/07/2008", TransactionType.PLANNED, "Planned: Rent", "", -1000.00, "Rent", "Rent")
      .add("20/07/2008", TransactionType.PLANNED, "Planned: Income 1", "", 3000.00, "Income 1", MasterCategory.INCOME)
      .add("20/07/2008", TransactionType.PRELEVEMENT, "ING", "", -200.00, "Regular savings", MasterCategory.SAVINGS)
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -40.00, "Groceries", MasterCategory.FOOD)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "SFR", "", -50.00, "Cell phone 1", MasterCategory.TELECOMS)
      .add("05/06/2008", TransactionType.PRELEVEMENT, "Rent for june", "", -1000.00, "Rent", "Rent")
      .add("01/06/2008", TransactionType.VIREMENT, "WorldCo", "", 3000.00, "Income 1", MasterCategory.INCOME)
      .check();
  }

  public void testCancel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/15", -40.00, "Auchan")
      .load();

    views.selectHome();
    monthSummary.openSeriesWizard()
      .select("Income 1")
      .cancel();

    views.checkHomeSelected();
    monthSummary.checkSeriesWizardButtonVisible(true);
  }
}
