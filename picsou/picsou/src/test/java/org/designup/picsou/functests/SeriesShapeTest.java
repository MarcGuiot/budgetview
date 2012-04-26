package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Utils;

public class SeriesShapeTest extends LoggedInFunctionalTestCase {

  public void test3Months() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/10", -50.00, "ED1")
      .addTransaction("2008/06/14", -100.00, "Auchan")
      .addTransaction("2008/06/01", 1000.00, "Salaire")
      .addTransaction("2008/05/29", -10.00, "ED2")
      .addTransaction("2008/05/10", -10.00, "ED2")
      .addTransaction("2008/05/10", 1000.00, "Salaire")
      .addTransaction("2008/04/05", -50.00, "ED1")
      .addTransaction("2008/04/10", 1000.00, "Salaire")
      .addTransaction("2008/04/20", 100.00, "Remboursement")
      .load();

    categorization.setNewIncome("Salaire", "Salaire", 1000.)
      .setNewVariable("Auchan", "Courses", -100.)
      .setVariable("ED1", "Courses")
      .setVariable("ED2", "Courses")
      .setIncome("Remboursement", "Salaire");

    transactions.showPlannedTransactions()
      .initContent()
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -50.00, "Courses")
      .add("10/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "ED1", "", -50.00, "Courses")
      .check();

    operations.openDevOptions().setMonthBack(2).validate();

    transactions
      .initContent()
      .add("27/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -25., "Courses")
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -25.00, "Courses")
      .add("10/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "ED1", "", -50.00, "Courses")
      .check();

    operations.openDevOptions().setMonthBack(3).validate();

    transactions
      .initContent()
      .add("27/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -17.00, "Courses")
      .add("27/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 10.00, "Salaire")
      .add("19/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 30.00, "Salaire")
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -33.00, "Courses")
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 630.00, "Salaire")
      .add("10/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 330.00, "Salaire")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "ED1", "", -50.00, "Courses")
      .check();

    categorization.setNewIncome("Remboursement", "frais");

    transactions
      .initContent()
      .add("27/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -17.00, "Courses")
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -33.00, "Courses")
      .add("10/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "ED1", "", -50.00, "Courses")
      .check();
  }

  public void testObservedNearPlannnedInCurrentMonth() throws Exception {

    operations.openPreferences()
      .setFutureMonthsCount(1)
      .validate();

    operations.openDevOptions()
      .setPeriodInMonth(7)
      .setMonthBack(1)
      .validate();

    OfxBuilder.init(this)
      .addTransaction("2008/08/26", -40.00, "ED2")
      .addTransaction("2008/08/24", -30.00, "ED1")
      .addTransaction("2008/08/14", -20.00, "ED")
      .addTransaction("2008/08/03", -50.00, "Auchan")
      .addTransaction("2008/08/09", -50.00, "ED")
      .addTransaction("2008/08/01", 1000.00, "Salaire")
      .load();

    categorization.setNewIncome("Salaire", "Salaire", 1000.)
      .setNewVariable("Auchan", "Courses", -200.)
      .setVariable("ED", "Courses");

    timeline.selectAll();
    transactions.showPlannedTransactions()
      .initContent()
      .add("18/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -200.00, "Courses")
      .add("02/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Courses", "", -80.00, "Courses")
      .add("26/08/2008", TransactionType.PRELEVEMENT, "ED2", "", -40.00)
      .add("24/08/2008", TransactionType.PRELEVEMENT, "ED1", "", -30.00)
      .add("14/08/2008", TransactionType.PRELEVEMENT, "ED", "", -20.00, "Courses")
      .add("09/08/2008", TransactionType.PRELEVEMENT, "ED", "", -50.00, "Courses")
      .add("03/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -50.00, "Courses")
      .add("01/08/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .check();

    categorization.setVariable("ED1", "Courses");

    transactions.showPlannedTransactions()
      .initContent()
      .add("18/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -200.00, "Courses")
      .add("02/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Courses", "", -50.00, "Courses")
      .add("26/08/2008", TransactionType.PRELEVEMENT, "ED2", "", -40.00)
      .add("24/08/2008", TransactionType.PRELEVEMENT, "ED1", "", -30.00, "Courses")
      .add("14/08/2008", TransactionType.PRELEVEMENT, "ED", "", -20.00, "Courses")
      .add("09/08/2008", TransactionType.PRELEVEMENT, "ED", "", -50.00, "Courses")
      .add("03/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -50.00, "Courses")
      .add("01/08/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .check();

    categorization.setVariable("ED2", "Courses");
    transactions
      .initContent()
      .add("26/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -46.00, "Courses")
      .add("22/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -30.00, "Courses")
      .add("14/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -20.00, "Courses")
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -52.00, "Courses")
      .add("02/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -52.00, "Courses")
      .add("02/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Courses", "", -10.00, "Courses")
      .add("26/08/2008", TransactionType.PRELEVEMENT, "ED2", "", -40.00, "Courses")
      .add("24/08/2008", TransactionType.PRELEVEMENT, "ED1", "", -30.00, "Courses")
      .add("14/08/2008", TransactionType.PRELEVEMENT, "ED", "", -20.00, "Courses")
      .add("09/08/2008", TransactionType.PRELEVEMENT, "ED", "", -50.00, "Courses")
      .add("03/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -50.00, "Courses")
      .add("01/08/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .check();

    operations.openDevOptions()
      .setMonthBack(2)
      .validate();

    transactions
      .initContent()
      .add("26/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -46.00, "Courses")
      .add("22/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -30.00, "Courses")
      .add("14/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -20.00, "Courses")
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -52.00, "Courses")
      .add("02/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -52.00, "Courses")
      .add("02/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Courses", "", -10.00, "Courses")
      .add("26/08/2008", TransactionType.PRELEVEMENT, "ED2", "", -40.00, "Courses")
      .add("24/08/2008", TransactionType.PRELEVEMENT, "ED1", "", -30.00, "Courses")
      .add("14/08/2008", TransactionType.PRELEVEMENT, "ED", "", -20.00, "Courses")
      .add("09/08/2008", TransactionType.PRELEVEMENT, "ED", "", -50.00, "Courses")
      .add("03/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -50.00, "Courses")
      .add("01/08/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .check();

    categorization.selectTransactions("ED2", "ED1")
      .setUncategorized();

    transactions
      .initContent()
      .add("18/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -200.00, "Courses")
      .add("02/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Courses", "", -80.00, "Courses")
      .add("26/08/2008", TransactionType.PRELEVEMENT, "ED2", "", -40.00)
      .add("24/08/2008", TransactionType.PRELEVEMENT, "ED1", "", -30.00)
      .add("14/08/2008", TransactionType.PRELEVEMENT, "ED", "", -20.00, "Courses")
      .add("09/08/2008", TransactionType.PRELEVEMENT, "ED", "", -50.00, "Courses")
      .add("03/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -50.00, "Courses")
      .add("01/08/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .check();

    timeline.selectMonth(200808);
    budgetView.variable.editSeries("Courses").setAmount("130").validate();

    timeline.selectAll();
    transactions
      .initContent()
      .add("14/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -36.00, "Courses")
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -82.00, "Courses")
      .add("02/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -82.00, "Courses")
      .add("02/09/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Courses", "", -10.00, "Courses")
      .add("26/08/2008", TransactionType.PRELEVEMENT, "ED2", "", -40.00)
      .add("24/08/2008", TransactionType.PRELEVEMENT, "ED1", "", -30.00)
      .add("14/08/2008", TransactionType.PRELEVEMENT, "ED", "", -20.00, "Courses")
      .add("09/08/2008", TransactionType.PRELEVEMENT, "ED", "", -50.00, "Courses")
      .add("03/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -50.00, "Courses")
      .add("01/08/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .check();
  }

  public void testUserCanForceSingleOperationModeWithPredefinedDay() throws Exception {

    operations.openPreferences()
      .setFutureMonthsCount(2)
      .validate();

    operations.openDevOptions()
      .setPeriodInMonth(4)
      .setMonthBack(3)
      .validate();

    OfxBuilder.init(this)
      .addTransaction("2008/08/05", -30.00, "Auchan")
      .addTransaction("2008/08/10", -60.00, "Auchan")
      .addTransaction("2008/07/15", -20.00, "Auchan")
      .addTransaction("2008/07/20", -70.00, "Auchan")
      .addTransaction("2008/06/25", -90.00, "Auchan")
      .load();

    categorization.setNewVariable("Auchan", "Courses", -90.00);

    timeline.selectAll();
    transactions.showPlannedTransactions()
      .initContent()
      .add("27/10/2008", TransactionType.PLANNED, "Planned: Courses", "", -33.00, "Courses")
      .add("19/10/2008", TransactionType.PLANNED, "Planned: Courses", "", -22.00, "Courses")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Courses", "", -26.00, "Courses")
      .add("04/10/2008", TransactionType.PLANNED, "Planned: Courses", "", -9.00, "Courses")
      .add("27/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -33.00, "Courses")
      .add("19/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -22.00, "Courses")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -26.00, "Courses")
      .add("04/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -9.00, "Courses")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -60.00, "Courses")
      .add("05/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -30.00, "Courses")
      .add("20/07/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -70.00, "Courses")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -20.00, "Courses")
      .add("25/06/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -90.00, "Courses")
      .check();

    budgetView.variable.editSeries("Courses")
      .checkForceSingleOperationSelected(false)
      .checkForceSingleOperationDayEnabled(false)
      .forceSingleOperationForecast()
      .checkForceSingleOperationDayEnabled(true)
      .checkForceSingleOperationDayList(Utils.range(1, 31))
      .checkForceSingleOperationDay(15)
      .setForceSingleOperationDay(20)
      .validate();

    transactions.showPlannedTransactions()
      .initContent()
      .add("20/10/2008", TransactionType.PLANNED, "Planned: Courses", "", -90.00, "Courses")
      .add("20/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -90.00, "Courses")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -60.00, "Courses")
      .add("05/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -30.00, "Courses")
      .add("20/07/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -70.00, "Courses")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -20.00, "Courses")
      .add("25/06/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -90.00, "Courses")
      .check();

    budgetView.variable.editSeries("Courses")
      .checkForceSingleOperationSelected(true)
      .checkForceSingleOperationDayEnabled(true)
      .checkForceSingleOperationDay(20)
      .unselectedForceSingleOperationForecast()
      .checkForceSingleOperationDayEnabled(false)
      .validate();

    transactions.showPlannedTransactions()
      .initContent()
      .add("27/10/2008", TransactionType.PLANNED, "Planned: Courses", "", -33.00, "Courses")
      .add("19/10/2008", TransactionType.PLANNED, "Planned: Courses", "", -22.00, "Courses")
      .add("11/10/2008", TransactionType.PLANNED, "Planned: Courses", "", -26.00, "Courses")
      .add("04/10/2008", TransactionType.PLANNED, "Planned: Courses", "", -9.00, "Courses")
      .add("27/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -33.00, "Courses")
      .add("19/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -22.00, "Courses")
      .add("11/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -26.00, "Courses")
      .add("04/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -9.00, "Courses")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -60.00, "Courses")
      .add("05/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -30.00, "Courses")
      .add("20/07/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -70.00, "Courses")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -20.00, "Courses")
      .add("25/06/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -90.00, "Courses")
      .check();

    budgetView.variable.editSeries("Courses")
      .checkForceSingleOperationSelected(false)
      .checkForceSingleOperationDayEnabled(false)
      .forceSingleOperationForecast()
      .checkForceSingleOperationDayEnabled(true)
      .checkForceSingleOperationDay(20)
      .setForceSingleOperationDay(31)
      .validate();

    // Takes into account actual last day of month in september
    transactions.showPlannedTransactions()
      .initContent()
      .add("31/10/2008", TransactionType.PLANNED, "Planned: Courses", "", -90.00, "Courses")
      .add("30/09/2008", TransactionType.PLANNED, "Planned: Courses", "", -90.00, "Courses")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -60.00, "Courses")
      .add("05/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -30.00, "Courses")
      .add("20/07/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -70.00, "Courses")
      .add("15/07/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -20.00, "Courses")
      .add("25/06/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -90.00, "Courses")
      .check();
  }

  public void testForceDate() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/09", 1000.00, "Salaire")
      .addTransaction("2008/05/11", 1000.00, "Salaire")
      .addTransaction("2008/04/10", 1000.00, "Salaire")
      .load();

    categorization.setNewIncome("Salaire", "Salaire", 1000.);

    operations.openDevOptions().setMonthBack(3).validate();
    operations.openPreferences().setFutureMonthsCount(3);

    timeline.selectAll();
    transactions.showPlannedTransactions()
      .initContent()
      .add("11/08/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("09/06/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .add("11/05/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .add("10/04/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .check();

    budgetView
      .income
      .editSeries("SALAIRE")
      .forceSingleOperationForecast()
      .setForceSingleOperationDay(9)
      .validate();

    transactions.showPlannedTransactions()
      .initContent()
      .add("09/08/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("09/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("09/06/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .add("11/05/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .add("10/04/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .check();

    budgetView
      .income
      .editSeries("SALAIRE")
      .unselectedForceSingleOperationForecast()
      .validate();

    transactions.showPlannedTransactions()
      .initContent()
      .add("11/08/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("11/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("09/06/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .add("11/05/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .add("10/04/2008", TransactionType.VIREMENT, "SALAIRE", "", 1000.00, "Salaire")
      .check();
  }

  public void testCreateWithFixedDate() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/22", -100.00, "ED")
      .load();

    SeriesEditionDialogChecker courses = categorization.selectTransaction("ED")
      .selectIncome()
      .createSeries();
    courses
      .setName("Courses")
      .forceSingleOperationForecast()
      .validate();
    timeline.selectAll();
    transactions.initAmountContent()
      .dumpCode();
  }
}
