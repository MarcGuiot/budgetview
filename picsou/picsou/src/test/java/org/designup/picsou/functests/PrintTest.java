package org.designup.picsou.functests;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.printing.BudgetReportChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.description.Formatting;

public class PrintTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    this.mainWindow = null;
    setCurrentMonth("2012/05");
    super.setUp();
  }

  public void testStandardTablePrint() throws Exception {

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
      .checkTitle("Variable")
      .add("Series", "Total", "Nov 11", "Dec 11", "Jan 12", "Feb 12", "Mar 12", "Apr 12", "May 12", "June 12", "Jul 12")
      .add("Groceries", "1070.00", "", "250.00", "220.00", "", "", "", "200.00", "200.00", "200.00")
      .add("Leisures", "350.00", "", "", "", "", "", "50.00", "100.00", "100.00", "100.00")
      .check();

    monthReport.getChartPage().getHistoChart()
      .checkColumnCount(9)
      .checkDiffColumn(0, "Nov", "2011", 0.00, 0.00)
      .checkDiffColumn(1, "Dec", "2011", 0.00, 250.00, true)
      .checkDiffColumn(2, "Jan", "2012", 0.00, 220.00)
      .checkDiffColumn(3, "Feb", "2012", 0.00, 0.00)
      .checkDiffColumn(4, "Mar", "2012", 0.00, 0.00)
      .checkDiffColumn(5, "Apr", "2012", 0.00, 50.00)
      .checkDiffColumn(6, "May", "2012", 0.00, 300.00)
      .checkDiffColumn(7, "June", "2012", 0.00, 300.00)
      .checkDiffColumn(8, "Jul", "2012", 0.00, 300.00);

    // -- Year 2011 --

    operations.print()
      .checkOptions("December 2011", "Year 2011")
      .checkCurrentMonthSelected()
      .selectCurrentYear()
      .print();

    BudgetReportChecker year2011Report = printer.getBudgetReport();

    year2011Report.initTablePage(1)
      .checkTitle("Variable")
      .add("Series", "Total", "Apr 11", "May 11", "June 11", "Jul 11", "Aug 11", "Sep 11", "Oct 11", "Nov 11", "Dec 11")
      .add("Groceries", "700.00", "100.00", "", "150.00", "", "", "200.00", "", "", "250.00")
      .check();

    year2011Report.getChartPage().getHistoChart()
      .checkColumnCount(9)
      .checkDiffColumn(0, "Apr", "2011", 0.00, 100.00)
      .checkDiffColumn(1, "May", "2011", 0.00, 0.00)
      .checkDiffColumn(2, "June", "2011", 0.00, 150.00)
      .checkDiffColumn(3, "Jul", "2011", 0.00, 0.00)
      .checkDiffColumn(4, "Aug", "2011", 0.00, 0.00)
      .checkDiffColumn(5, "Sep", "2011", 0.00, 200.00)
      .checkDiffColumn(6, "Oct", "2011", 0.00, 0.00)
      .checkDiffColumn(7, "Nov", "2011", 0.00, 0.00)
      .checkDiffColumn(8, "Dec", "2011", 0.00, 250.00, true);

    // -- Year 2011 --

    timeline.selectMonth(201202);
    operations.print()
      .checkOptions("February 2012", "Year 2012")
      .checkCurrentYearSelected()
      .print();

    BudgetReportChecker year2012Report = printer.getBudgetReport();
    year2012Report.initTablePage(1)
      .checkTitle("Variable")
      .add("Series", "Total", "Jan 12", "Feb 12", "Mar 12", "Apr 12", "May 12", "June 12", "Jul 12")
      .add("Groceries", "820.00", "220.00", "", "", "", "200.00", "200.00", "200.00")
      .add("Leisures", "350.00", "", "", "", "50.00", "100.00", "100.00", "100.00")
      .check();

    year2012Report.getChartPage().getHistoChart()
      .checkColumnCount(7)
      .checkDiffColumn(0, "Jan", "2012", 0.00, 220.00)
      .checkDiffColumn(1, "Feb", "2012", 0.00, 0.00, true)
      .checkDiffColumn(2, "Mar", "2012", 0.00, 0.00)
      .checkDiffColumn(3, "Apr", "2012", 0.00, 50.00)
      .checkDiffColumn(4, "May", "2012", 0.00, 300.00)
      .checkDiffColumn(5, "June", "2012", 0.00, 300.00)
      .checkDiffColumn(6, "Jul", "2012", 0.00, 300.00);
  }

  public void testCharts() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", -125.0, "2008/07/12")
      .addTransaction("2012/03/10", 1000.00, "WorldCo")
      .addTransaction("2012/04/10", 1500.00, "WorldCo")
      .addTransaction("2012/05/10", 1500.00, "WorldCo")
      .addTransaction("2012/03/10", -50.00, "EDF")
      .addTransaction("2012/04/10", -50.00, "EDF")
      .addTransaction("2012/05/10", -50.00, "EDF")
      .addTransaction("2012/03/10", -400.00, "Mortgage")
      .addTransaction("2012/04/10", -400.00, "Mortgage")
      .addTransaction("2012/05/10", -400.00, "Mortgage")
      .addTransaction("2012/03/10", -200.00, "Auchan")
      .addTransaction("2012/04/10", -200.00, "Auchan")
      .addTransaction("2012/05/10", -250.00, "Auchan")
      .addTransaction("2012/03/10", -50.00, "FNAC")
      .addTransaction("2012/04/10", -50.00, "FNAC")
      .addTransaction("2012/05/10", -100.00, "FNAC")
      .load();

    categorization.setNewIncome("WorldCo", "Salary");
    categorization.setNewRecurring("EDF", "Electricity");
    categorization.setNewRecurring("Mortgage", "House");
    categorization.setNewVariable("Auchan", "Groceries", -100.00);
    categorization.setNewVariable("FNAC", "Leisures", -100.00);

    timeline.selectMonth(201204);
    operations.print()
      .checkOptions("April 2012", "Year 2012")
      .selectCurrentYear()
      .print();

    BudgetReportChecker year2012Report = printer.getBudgetReport();
    year2012Report.getChartPage().getOverviewStack()
      .getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 4000.00);
    year2012Report.getChartPage().getOverviewStack()
      .getRightDataset()
      .checkSize(2)
      .checkValue("Recurring", 1350.00)
      .checkValue("Variable", 850.00);

    year2012Report.getChartPage().getExpensesStack()
      .getSingleDataset()
      .checkValue("House", 1200.00)
      .checkValue("Groceries", 650.00)
      .checkValue("Leisures", 200.00)
      .checkValue("Electricity", 150.00);

    year2012Report.getChartPage().getHistoChart()
      .checkColumnCount(3)
      .checkDiffColumn(0, "Mar", "2012", 1000.00, 700.00)
      .checkDiffColumn(1, "Apr", "2012", 1500.00, 700.00, true)
      .checkDiffColumn(2, "May", "2012", 1500.00, 800.00);
  }

  public void testTablePagination() throws Exception {
    OfxBuilder builder = OfxBuilder.init(this);
    for (int i = 1; i <= 40; i++) {
      builder
        .addTransaction("2012/03/10", -200.00 - i, getLabel("Auchan", i))
        .addTransaction("2012/04/10", -200.00, getLabel("Auchan", i))
        .addTransaction("2012/05/10", -250.00, getLabel("Auchan", i));
    }
    builder.load();

    for (int i = 1; i <= 40; i++) {
      categorization.setNewVariable(getLabel("Auchan", i), getLabel("Series", i), -100.00);
    }

    timeline.selectMonth(201204);
    operations.print()
      .checkOptions("April 2012", "Year 2012")
      .checkCurrentMonthSelected()
      .print();

    printer.getBudgetReport().checkPageCount(3);
    printer.getBudgetReport().initTablePage(1)
      .checkTitle("Variable")
      .checkRowCount(31)
      .checkRow(0, "Series", "Total", "Mar 12", "Apr 12", "May 12")
      .checkRow(1, "Series40", "690.00", "240.00", "200.00", "250.00")
      .checkRow(30, "Series11", "661.00", "211.00", "200.00", "250.00");
    printer.getBudgetReport().initTablePage(2)
      .checkTitle("Variable")
      .checkRowCount(11)
      .checkRow(0, "Series", "Total", "Mar 12", "Apr 12", "May 12")
      .checkRow(1, "Series10", "660.00", "210.00", "200.00", "250.00")
      .checkRow(10, "Series01", "651.00", "201.00", "200.00", "250.00");
  }

  private String getLabel(String label, int i) {
    return label + Formatting.TWO_DIGIT_INTEGER_FORMAT.format(i);
  }

  public void __testExceptionRaisedDuringPrint() throws Exception {
    Assert.fail("tbd");
  }
}
