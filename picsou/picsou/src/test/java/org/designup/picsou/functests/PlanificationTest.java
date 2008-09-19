package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LicenseChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

public class PlanificationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate(Dates.parse("2008/07/01"));
    super.setUp();
  }

  public void testFirstSeriesInitialization() throws Exception {
    LicenseChecker.enterLicense(mainWindow, "admin", "", 24);
    OfxBuilder.init(this)
      .addTransaction("2008/07/08", -29.9, "free telecom")
      .load();
    timeline.assertSpanEquals("2008/07", "2010/07");

    views.selectCategorization();
    categorization.setRecurring(0, "Internet", MasterCategory.TELECOMS, true);

    timeline.selectMonth("2008/07");

    views.selectData();
    transactions.initContent()
      .add("08/07/2008", TransactionType.PRELEVEMENT, "free telecom", "", -29.90, "Internet", "Telecommunications")
      .check();

    views.selectHome();

    timeline.selectMonth("2008/07");
    monthSummary.init()
      .checkRecurring(29.9)
      .checkPlannedRecurring(29.9);

    timeline.selectMonth("2008/08");
    views.selectData();
    transactions.initContent()
      .add("08/08/2008", TransactionType.PLANNED, "Internet", "", -29.90, "Internet", MasterCategory.TELECOMS)
      .check();

    views.selectHome();
    monthSummary.init()
      .checkRecurring(0)
      .checkPlannedRecurring(29.9);
  }

  public void testCreationOfMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -100., "Auchan")
      .load();
    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, true);
    LicenseChecker.enterLicense(mainWindow, "admin", "", 1);
    timeline.assertSpanEquals("2008/06", "2008/08");
    timeline.selectAll();
    views.selectBudget();
    budgetView.envelopes.editSeriesList().selectSeries("Courant")
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "100.00"},
        {"2008", "July", "0.00", "100.00"},
        {"2008", "June", "100.00", "100.00"},
      });

    views.selectData();
    transactions
      .initContent()
      .add("30/08/2008", TransactionType.PLANNED, "Courant", "", -100.00, "Courant", MasterCategory.FOOD)
      .add("30/07/2008", TransactionType.PLANNED, "Courant", "", -100.00, "Courant", MasterCategory.FOOD)
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant", MasterCategory.FOOD)
      .check();

    OfxBuilder
      .init(this)
      .addTransaction("2008/08/4", -50., "ED")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("ED", "Courant", MasterCategory.FOOD, false);
    timeline.selectAll();
    transactions
      .initContent()
      .add("30/08/2008", TransactionType.PLANNED, "Courant", "", -50.00, "Courant", MasterCategory.FOOD)
      .add("04/08/2008", TransactionType.PRELEVEMENT, "ED", "", -50.00, "Courant", MasterCategory.FOOD)
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant", MasterCategory.FOOD)
      .check();
  }

  public void testMovePlannedTransactionDayInMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -100., "Auchan")
      .load();
    LicenseChecker.enterLicense(mainWindow, "admin", "", 1);
    views.selectCategorization();
    categorization.setEnvelope("Auchan", "Courant", MasterCategory.FOOD, true);
    timeline.selectAll();
    views.selectBudget();
    budgetView.envelopes.editSeriesList()
      .selectSeries("Courant")
      .selectAllMonths()
      .setAmount("200")
      .validate();
    views.selectData();
    transactions
      .initContent()
      .add("20/08/2008", TransactionType.PLANNED, "Courant", "", -200.00, "Courant", MasterCategory.FOOD)
      .add("20/07/2008", TransactionType.PLANNED, "Courant", "", -200.00, "Courant", MasterCategory.FOOD)
      .add("20/06/2008", TransactionType.PLANNED, "Courant", "", -100.00, "Courant", MasterCategory.FOOD)
      .add("20/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant", MasterCategory.FOOD)
      .check();

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -80., "ED")
      .load();
    timeline.selectAll();
    transactions
      .initContent()
      .add("20/08/2008", TransactionType.PLANNED, "Courant", "", -200.00, "Courant", MasterCategory.FOOD)
      .add("20/07/2008", TransactionType.PLANNED, "Courant", "", -200.00, "Courant", MasterCategory.FOOD)
      .add("30/06/2008", TransactionType.PLANNED, "Courant", "", -100.00, "Courant", MasterCategory.FOOD)
      .add("30/06/2008", TransactionType.PRELEVEMENT, "ED", "", -80.00)
      .add("20/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant", MasterCategory.FOOD)
      .check();

    views.selectCategorization();
    categorization.setEnvelope("ED", "Courant", MasterCategory.FOOD, false);
    views.selectData();
    transactions
      .initContent()
      .add("20/08/2008", TransactionType.PLANNED, "Courant", "", -200.00, "Courant", MasterCategory.FOOD)
      .add("20/07/2008", TransactionType.PLANNED, "Courant", "", -200.00, "Courant", MasterCategory.FOOD)
      .add("30/06/2008", TransactionType.PLANNED, "Courant", "", -20.00, "Courant", MasterCategory.FOOD)
      .add("30/06/2008", TransactionType.PRELEVEMENT, "ED", "", -80.00, "Courant", MasterCategory.FOOD)
      .add("20/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant", MasterCategory.FOOD)
      .check();

  }
}
