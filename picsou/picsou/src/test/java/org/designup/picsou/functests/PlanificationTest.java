package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

public class PlanificationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/07/01");
    super.setUp();
  }

  public void testFirstSeriesInitialization() throws Exception {
    operations.openPreferences().setFutureMonthsCount(24).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/07/08", -29.9, "free telecom")
      .load();
    timeline.checkSpanEquals("2008/07", "2010/07");

    views.selectCategorization();
    categorization.setNewRecurring(0, "Internet");

    timeline.selectMonth("2008/07");

    views.selectData();
    transactions.initContent()
      .add("08/07/2008", TransactionType.PRELEVEMENT, "free telecom", "", -29.90, "Internet", "")
      .check();

    timeline.selectMonth("2008/07");
    views.selectBudget();
    budgetView.recurring.checkTotalAmounts(-29.90, -29.90);

    timeline.selectMonth("2008/08");
    views.selectData();
    transactions.initContent()
      .add("08/08/2008", TransactionType.PLANNED, "Planned: Internet", "", -29.90, "Internet")
      .check();

    views.selectBudget();
    budgetView.recurring.checkTotalAmounts(0, -29.90);
  }

  public void testCreationOfMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -100., "Auchan")
      .load();
    views.selectCategorization();
    categorization.setNewEnvelope("Auchan", "Courant");
    operations.openPreferences().setFutureMonthsCount(1).validate();
    timeline.checkSpanEquals("2008/06", "2008/08");
    timeline.selectAll();
    views.selectBudget();
    budgetView.envelopes.editSeriesList().selectSeries("Courant")
      .switchToManual()
      .checkTable(new Object[][]{
        {"2008", "August", "0.00", "100.00"},
        {"2008", "July", "0.00", "100.00"},
        {"2008", "June", "100.00", "100.00"},
      }).validate();

    views.selectData();
    transactions
      .initContent()
      .add("30/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -100.00, "Courant")
      .add("30/07/2008", TransactionType.PLANNED, "Planned: Courant", "", -100.00, "Courant")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant")
      .check();

    TimeService.setCurrentDate(Dates.parse("2008/08/10"));
    OfxBuilder
      .init(this)
      .addTransaction("2008/08/4", -50., "ED")
      .load();

    views.selectCategorization();
    categorization.setEnvelope("ED", "Courant");
    timeline.selectAll();
    transactions
      .initContent()
      .add("30/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -50.00, "Courant")
      .add("04/08/2008", TransactionType.PRELEVEMENT, "ED", "", -50.00, "Courant")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant")
      .check();
  }

  public void testMovePlannedTransactionDayInMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -100., "Auchan")
      .load();
    operations.openPreferences().setFutureMonthsCount(1).validate();
    views.selectCategorization();
    categorization.setNewEnvelope("Auchan", "Courant");
    timeline.selectAll();
    views.selectBudget();
    budgetView.envelopes.editSeriesList()
      .selectSeries("Courant")
      .switchToManual()
      .selectAllMonths()
      .setAmount("200")
      .validate();
    views.selectData();
    transactions
      .initContent()
      .add("20/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -200.00, "Courant")
      .add("20/07/2008", TransactionType.PLANNED, "Planned: Courant", "", -200.00, "Courant")
      .add("20/06/2008", TransactionType.PLANNED, "Planned: Courant", "", -100.00, "Courant")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant")
      .check();

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -80., "ED")
      .load();
    timeline.selectAll();
    transactions
      .initContent()
      .add("20/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -200.00, "Courant")
      .add("20/07/2008", TransactionType.PLANNED, "Planned: Courant", "", -200.00, "Courant")
      .add("30/06/2008", TransactionType.PLANNED, "Planned: Courant", "", -100.00, "Courant")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "ED", "", -80.00)
      .add("20/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant")
      .check();

    views.selectCategorization();
    categorization.setEnvelope("ED", "Courant");
    views.selectData();
    transactions
      .initContent()
      .add("20/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -200.00, "Courant")
      .add("20/07/2008", TransactionType.PLANNED, "Planned: Courant", "", -200.00, "Courant")
      .add("30/06/2008", TransactionType.PLANNED, "Planned: Courant", "", -20.00, "Courant")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "ED", "", -80.00, "Courant")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant")
      .check();
  }

  public void testCreatePlannedTransactionInCurrentMonthAtLastDay() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -100., "Auchan")
      .load();

//    operations.openPreferences().setFutureMonthsCount(1).validate();
    views.selectCategorization();
    categorization.setNewEnvelope("Auchan", "Courant");
    timeline.selectAll();
    views.selectBudget();
    budgetView.envelopes.editSeriesList()
      .selectSeries("Courant")
      .switchToManual()
      .selectAllMonths()
      .setAmount("200")
      .validate();

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/10", -100., "EDF")
      .load();
    views.selectCategorization();
    categorization.setNewRecurring("EDF", "EDF");
    views.selectBudget();
    budgetView.recurring.editSeriesList()
      .selectSeries("EDF")
      .switchToManual()
      .selectAllMonths()
      .setAmount("150")
      .validate();
    
    views.selectData();
    transactions.initContent()
      .add("20/06/2008", TransactionType.PLANNED, "Planned: EDF", "", -50.00, "EDF")
      .add("20/06/2008", TransactionType.PLANNED, "Planned: Courant", "", -100.00, "Courant")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant")
      .add("10/06/2008", TransactionType.PRELEVEMENT, "EDF", "", -100.00, "EDF")
      .check();
  }

  public void testSpecialSeriesWithSingleMonthProfile() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    timeline.selectMonth("2008/09");
    views.selectBudget();
    budgetView.specials.createSeries()
      .setName("Miami trip")
      .setAmount(2000.00)
      .validate();

    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("01/09/2008", TransactionType.PLANNED, "Planned: Miami trip", "", -2000.00, "Miami trip")
      .check();
  }
}
