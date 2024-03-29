package com.budgetview.functests.budget;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.desktop.time.TimeService;
import com.budgetview.model.TransactionType;
import org.globsframework.utils.Dates;
import org.junit.Test;

public class PlanificationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/07/08");
    super.setUp();
  }

  @Test
  public void testFirstSeriesInitialization() throws Exception {
    operations.openPreferences().setFutureMonthsCount(24).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/07/08", -29.9, "free telecom")
      .load();
    timeline.checkSpanEquals("2008/07", "2010/07");

    categorization.setNewRecurring(0, "Internet");

    timeline.selectMonth("2008/07");

    transactions
      .showPlannedTransactions()
      .initContent()
      .add("08/07/2008", TransactionType.PRELEVEMENT, "free telecom", "", -29.90, "Internet")
      .check();

    timeline.selectMonth("2008/07");
    budgetView.recurring.checkTotalAmounts(-29.90, -29.90);

    timeline.selectMonth("2008/08");
    transactions.initContent()
      .add("11/08/2008", TransactionType.PLANNED, "Planned: Internet", "", -29.90, "Internet")
      .check();

    budgetView.recurring.checkTotalAmounts(0, -29.90);
  }

  @Test
  public void testCreationOfMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -100., "Auchan")
      .load();

    categorization.setNewVariable("Auchan", "Courant", -100.);
    operations.openPreferences().setFutureMonthsCount(1).validate();
    timeline.checkSpanEquals("2008/06", "2008/08");

    timeline.selectAll();
    budgetView.variable.editSeries("Courant")
      .checkChart(new Object[][]{
        {"2008", "J", 100.00, 100.00, true},
        {"2008", "J", 0.00, 100.00, true},
        {"2008", "A", 0.00, 100.00, true},
      }).validate();

    transactions
      .showPlannedTransactions()
      .initContent()
      .add("27/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -100.00, "Courant")
      .add("27/07/2008", TransactionType.PLANNED, "Planned: Courant", "", -100.00, "Courant")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant")
      .check();

    TimeService.setCurrentDate(Dates.parse("2008/08/10"));
    operations.changeDate();
    OfxBuilder
      .init(this)
      .addTransaction("2008/08/4", -50., "ED")
      .load();

    views.selectCategorization();
    categorization.setVariable("ED", "Courant");
    timeline.selectAll();
    transactions
      .initContent()
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Courant", "", -100.00, "Courant")
      .add("11/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -50.00, "Courant")
      .add("04/08/2008", TransactionType.PRELEVEMENT, "ED", "", -50.00, "Courant")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant")
      .check();
  }

  @Test
  public void testMovePlannedTransactionDayInMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -100., "Auchan")
      .load();
    operations.openPreferences().setFutureMonthsCount(1).validate();
    views.selectCategorization();
    categorization.setNewVariable("Auchan", "Courant");
    timeline.selectAll();
    views.selectBudget();
    budgetView.variable.editSeries("Courant")
      .selectAllMonths()
      .setAmount("200")
      .validate();
    views.selectData();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("11/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -200.00, "Courant")
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Courant", "", -200.00, "Courant")
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
      .add("11/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -200.00, "Courant")
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Courant", "", -200.00, "Courant")
      .add("30/06/2008", TransactionType.PLANNED, "Planned: Courant", "", -100.00, "Courant")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "ED", "", -80.00)
      .add("20/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant")
      .check();

    views.selectCategorization();
    categorization.setVariable("ED", "Courant");
    views.selectData();
    transactions
      .initContent()
      .add("11/08/2008", TransactionType.PLANNED, "Planned: Courant", "", -200.00, "Courant")
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Courant", "", -200.00, "Courant")
      .add("30/06/2008", TransactionType.PLANNED, "Planned: Courant", "", -20.00, "Courant")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "ED", "", -80.00, "Courant")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant")
      .check();
  }

  @Test
  public void testCreatePlannedTransactionInCurrentMonthAtLastDay() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/20", -100., "Auchan")
      .load();

    categorization.setNewVariable("Auchan", "Courant");

    timeline.selectAll();
    budgetView.variable.editSeries("Courant")
      .selectAllMonths()
      .setAmount("200")
      .validate();

    OfxBuilder
      .init(this)
      .addTransaction("2008/06/10", -100.00, "EDF")
      .load();
    categorization.setNewRecurring("EDF", "EDF");
    budgetView.recurring.editSeries("EDF")
      .selectAllMonths()
      .setAmount("150")
      .validate();

    transactions
      .showPlannedTransactions()
      .initContent()
      .add("20/06/2008", TransactionType.PLANNED, "Planned: Courant", "", -100.00, "Courant")
      .add("20/06/2008", TransactionType.PLANNED, "Planned: EDF", "", -50.00, "EDF")
      .add("20/06/2008", TransactionType.PRELEVEMENT, "Auchan", "", -100.00, "Courant")
      .add("10/06/2008", TransactionType.PRELEVEMENT, "EDF", "", -100.00, "EDF")
      .check();
  }

  @Test
  public void testExtraSeriesWithSingleMonthProfile() throws Exception {
    accounts.createMainAccount("Main", "4321", 0.);
    operations.openPreferences().setFutureMonthsCount(6).validate();

    timeline.selectMonth("2008/09");
    budgetView.extras.createSeries()
      .setName("Miami trip")
      .setTargetAccount("Main")
      .setAmount(2000.00)
      .validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Miami trip", "", -2000.00, "Miami trip")
      .check();
  }
}
