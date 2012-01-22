package org.designup.picsou.functests;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.BudgetReportChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class PrintTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    this.mainWindow = null;
    setCurrentMonth("2012/05");
    super.setUp();
  }

  public void testStandardPrint() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", -125.0, "2008/07/12")
      .addTransaction("2011/04/10", -100.00, "Auchan")
      .addTransaction("2011/06/10", -150.00, "Auchan")
      .addTransaction("2011/09/10", -200.00, "Auchan")
      .addTransaction("2011/12/10", -250.00, "Auchan")
      .addTransaction("2012/01/10", -220.00, "Auchan")
      .addTransaction("2012/04/10", -50.00, "FNAC")
      .addTransaction("2012/05/10", -100.00, "FNAC")
      .load();

    categorization.setNewVariable("Auchan", "Groceries", -200.00);
    categorization.setNewVariable("FNAC", "Leisures", -100.00);

    // -- December 2011 --

    timeline.selectMonth(201112);
    operations.print()
      .checkOptions("December 2011", "Year 2011")
      .checkCurrentMonthSelected()
      .print();

    BudgetReportChecker monthReport = printer.getBudgetReport();

    monthReport.initTablePage(1)
      .checkTitle("Income")
      .checkEmpty();

    monthReport.initTablePage(2)
      .checkTitle("Recurring")
      .checkEmpty();

    monthReport.initTablePage(3)
      .checkTitle("Variable")
      .add("Series","Total","Nov 11","Dec 11","Jan 12","Feb 12","Mar 12","Apr 12","May 12","June 12","Jul 12")
      .add("Groceries","1070.00","","250.00","220.00","","","","200.00","200.00","200.00")
      .add("Leisures","350.00","","","","","","50.00","100.00","100.00","100.00")
      .check();

    // -- Year 2011 --

    operations.print()
      .checkOptions("December 2011", "Year 2011")
      .checkCurrentMonthSelected()
      .selectCurrentYear()
      .print();

    BudgetReportChecker year2011Report = printer.getBudgetReport();

    year2011Report.initTablePage(1)
      .checkTitle("Income")
      .checkEmpty();

    year2011Report.initTablePage(2)
      .checkTitle("Recurring")
      .checkEmpty();

    year2011Report.initTablePage(3)
      .checkTitle("Variable")
      .add("Series", "Total", "Apr 11", "May 11", "June 11", "Jul 11", "Aug 11", "Sep 11", "Oct 11", "Nov 11", "Dec 11")
      .add("Groceries", "700.00", "100.00", "", "150.00", "", "", "200.00", "", "", "250.00")
      .check();

    // -- Year 2011 --

    timeline.selectMonth(201202);
    operations.print()
      .checkOptions("February 2012", "Year 2012")
      .checkCurrentYearSelected()
      .print();

    BudgetReportChecker year2012Report = printer.getBudgetReport();

    year2012Report.initTablePage(1)
      .checkTitle("Income")
      .checkEmpty();

    year2012Report.initTablePage(2)
      .checkTitle("Recurring")
      .checkEmpty();

    year2012Report.initTablePage(3)
      .checkTitle("Variable")
      .add("Series","Total","Jan 12","Feb 12","Mar 12","Apr 12","May 12","June 12","Jul 12")
      .add("Groceries","820.00","220.00","","","","200.00","200.00","200.00")
      .add("Leisures","350.00","","","","50.00","100.00","100.00","100.00")
      .check();
  }

  public void __testPagination() throws Exception {
    Assert.fail("tbd");
  }

  public void __testExceptionRaisedDuringPrint() throws Exception {
    Assert.fail("tbd");
  }
}
