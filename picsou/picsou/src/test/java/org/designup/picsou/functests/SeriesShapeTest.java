package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class SeriesShapeTest extends LoggedInFunctionalTestCase {

  public void test3month() throws Exception {
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
      .add("10/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -50.00, "Courses")
      .add("10/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "ED1", "", -50.00, "Courses")
      .check();

    operations.openPreferences().setMonthBack(2).validate();

    transactions
      .initContent()
      .add("24/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -25., "Courses")
      .add("10/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -25.00, "Courses")
      .add("10/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "ED1", "", -50.00, "Courses")
      .check();

    operations.openPreferences().setMonthBack(3).validate();

    transactions
      .initContent()
      .add("31/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 10.00, "Salaire")
      .add("24/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -17.00, "Courses")
      .add("23/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 30.00, "Salaire")
      .add("15/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 630.00, "Salaire")
      .add("10/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -33.00, "Courses")
      .add("10/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 330.00, "Salaire")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "ED1", "", -50.00, "Courses")
      .check();


    categorization.setNewIncome("Remboursement", "frais");

    transactions
      .initContent()
      .add("24/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -17.00, "Courses")
      .add("10/07/2008", TransactionType.PLANNED, "Planned: Courses", "", -33.00, "Courses")
      .add("10/07/2008", TransactionType.PLANNED, "Planned: Salaire", "", 1000.00, "Salaire")
      .add("10/07/2008", TransactionType.PRELEVEMENT, "ED1", "", -50.00, "Courses")
      .check();

  }
}
