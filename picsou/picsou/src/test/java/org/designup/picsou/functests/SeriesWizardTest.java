package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
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
    notes.openSeriesWizard()
      .select("Income 1")
      .select("Exceptional income")
      .select("Rent")
      .select("Cell phone 1")
      .checkSelected("Groceries")
      .checkSelected("Health")
      .validate();

    views.checkCategorizationSelected();
    timeline.selectAll();

    views.selectHome();
    notes.checkSeriesWizardButtonVisible(false);

    views.selectBudget();

    budgetView.income.editSeriesList()
      .checkSeriesListEquals("Exceptional Income", "Income 1")
      .selectSeries("Income 1")
      .checkSelectedProfile("Every month")
      .selectSeries("Exceptional Income")
      .checkSelectedProfile("Irregular")
      .cancel();

    budgetView.recurring.editSeriesList()
      .checkSeriesListEquals("Cell phone 1", "Rent")
      .selectSeries("Rent")
      .checkSelectedProfile("Every month")
      .selectSeries("Cell phone 1")
      .checkSelectedProfile("Every month")
      .cancel();

    budgetView.envelopes.editSeriesList()
      .checkSeriesListContains("Groceries", "Health", "Miscellaneous")
      .selectSeries("Groceries")
      .checkSelectedProfile("Every month")
      .selectSeries("Health")
      .gotoSubSeriesTab()
      .checkSubSeriesList("Drugstore", "Physician", "Reimbursements")
      .checkSelectedProfile("Every month")
      .cancel();

    views.selectCategorization();

    categorization.setIncome("WorldCo", "Income 1");
    categorization.setRecurring("Rent for june", "Rent");
    categorization.setRecurring("SFR", "Cell phone 1");
    categorization.setEnvelope("Auchan", "Groceries");

    views.selectData();
    timeline.selectMonths("2008/06", "2008/07", "2008/08");
    transactions.initContent()
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Groceries", "", -40.00, "Groceries")
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Cell phone 1", "", -50.00, "Cell phone 1")
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Rent", "", -1000.00, "Rent")
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Income 1", "", 3000.00, "Income 1")
      .add("20/07/2008", TransactionType.PLANNED, "Planned: Rent", "", -1000.00, "Rent")
      .add("20/07/2008", TransactionType.PLANNED, "Planned: Income 1", "", 3000.00, "Income 1")
      .add("20/07/2008", TransactionType.PRELEVEMENT, "ING", "", -200.00)
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -40.00, "Groceries")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "SFR", "", -50.00, "Cell phone 1")
      .add("05/06/2008", TransactionType.PRELEVEMENT, "Rent for june", "", -1000.00, "Rent")
      .add("01/06/2008", TransactionType.VIREMENT, "WorldCo", "", 3000.00, "Income 1")
      .check();
  }

  public void testCancel() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/15", -40.00, "Auchan")
      .load();

    views.selectHome();
    notes.openSeriesWizard()
      .select("Income 1")
      .cancel();

    views.checkHomeSelected();
    notes.checkSeriesWizardButtonVisible(true);
  }
}
