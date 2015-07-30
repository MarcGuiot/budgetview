package org.designup.picsou.functests.printing;

import org.designup.picsou.functests.checkers.SavingsSetup;
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

  public void testCompleteDocument() throws Exception {
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
    operations.openPrint()
      .checkOptions("April 2012", "Year 2012")
      .selectCurrentYear()
      .print();

    // -- April 2011 showing 2012 --

    BudgetReportChecker year2012Report = printer.getBudgetReport();
    year2012Report.getOverviewPage().checkTitle("Overview for april 2012");
    year2012Report.getOverviewPage().getOverviewStack()
      .getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 1500.00);
    year2012Report.getOverviewPage().getOverviewStack()
      .getRightDataset()
      .checkSize(2)
      .checkValue("Recurring", 450.00)
      .checkValue("Variable", 250.00);

    year2012Report.getOverviewPage().getExpensesStack()
      .getSingleDataset()
      .checkSize(4)
      .checkValue("House", 400.00)
      .checkValue("Groceries", 200.00)
      .checkValue("Leisures", 50.00)
      .checkValue("Electricity", 50.00);

    year2012Report.getOverviewPage().getHistoChart()
      .checkColumnCount(3)
      .checkColumnCount(3)
      .checkDiffColumn(0, "M", "2012", 1000.00, 700.00)
      .checkDiffColumn(1, "A", "2012", 1500.00, 700.00, true)
      .checkDiffColumn(2, "M", "2012", 1500.00, 800.00);

    year2012Report.initGaugesPage(1)
      .checkTitle("Budget for april 2012")
      .checkBlockCount(14)
      .checkBudget(0, "Income", "1500.00", "1000.00")
      .checkSeries(1, "Salary", "1500.00", "1000.00")
      .checkSeparator(2)
      .checkBudget(3, "Recurring", "450.00", "450.00")
      .checkSeries(4, "House", "400.00", "400.00")
      .checkSeries(5, "Electricity", "50.00", "50.00")
      .checkSeparator(6)
      .checkBudget(7, "Variable", "250.00", "200.00")
      .checkSeries(8, "Groceries", "200.00", "100.00")
      .checkSeries(9, "Leisures", "50.00", "100.00")
      .checkSeparator(10)
      .checkBudget(11, "Transfers", "0.00", "0.00")
      .checkSeparator(12)
      .checkBudget(13, "Extras", "0.00", "0.00");

    year2012Report.initTablePage(2)
      .checkTitle("Income")
      .add("Series", "Total", "Mar 12", "Apr 12", "May 12")
      .add("Salary", "4000.00", "1000.00", "1500.00", "1500.00")
      .check();
    year2012Report.initTablePage(3)
      .checkTitle("Recurring")
      .add("Series", "Total", "Mar 12", "Apr 12", "May 12")
      .add("House", "1200.00", "400.00", "400.00", "400.00")
      .add("Electricity", "150.00", "50.00", "50.00", "50.00")
      .check();
    year2012Report.initTablePage(4)
      .checkTitle("Variable")
      .add("Series", "Total", "Mar 12", "Apr 12", "May 12")
      .add("Groceries", "650.00", "200.00", "200.00", "250.00")
      .add("Leisures", "200.00", "50.00", "50.00", "100.00")
      .check();

    // -- April-May 2012 --

    timeline.selectMonths(201204, 201205);
    operations.openPrint()
      .selectCurrentYear()
      .print();

    BudgetReportChecker multiSelectionReport = printer.getBudgetReport();
    multiSelectionReport.getOverviewPage().checkTitle("Overview for april - may 2012");
    multiSelectionReport.getOverviewPage().getOverviewStack()
      .getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 3000.00);
    multiSelectionReport.getOverviewPage().getOverviewStack()
      .getRightDataset()
      .checkSize(2)
      .checkValue("Recurring", 900.00)
      .checkValue("Variable", 600.00);

    multiSelectionReport.getOverviewPage().getExpensesStack()
      .getSingleDataset()
      .checkSize(4)
      .checkValue("House", 800.00)
      .checkValue("Groceries", 450.00)
      .checkValue("Leisures", 150.00)
      .checkValue("Electricity", 100.00);

    multiSelectionReport.getOverviewPage().getHistoChart()
      .checkColumnCount(3)
      .checkDiffColumn(0, "M", "2012", 1000.00, 700.00)
      .checkDiffColumn(1, "A", "2012", 1500.00, 700.00)
      .checkDiffColumn(2, "M", "2012", 1500.00, 800.00, true);

    multiSelectionReport.initGaugesPage(1)
      .checkTitle("Budget for april - may 2012")
      .checkBlockCount(14)
      .checkBudget(0, "Income", "3000.00", "2500.00")
      .checkSeries(1, "Salary", "3000.00", "2500.00")
      .checkSeparator(2)
      .checkBudget(3, "Recurring", "900.00", "900.00")
      .checkSeries(4, "House", "800.00", "800.00")
      .checkSeries(5, "Electricity", "100.00", "100.00")
      .checkSeparator(6)
      .checkBudget(7, "Variable", "600.00", "400.00")
      .checkSeries(8, "Groceries", "450.00", "200.00")
      .checkSeries(9, "Leisures", "150.00", "200.00")
      .checkSeparator(10)
      .checkBudget(11, "Transfers", "0.00", "0.00")
      .checkSeparator(12)
      .checkBudget(13, "Extras", "0.00", "0.00");

    multiSelectionReport.initTablePage(2)
      .checkTitle("Income")
      .add("Series","Total","Mar 12","Apr 12","May 12")
      .add("Salary","4000.00","1000.00","1500.00","1500.00")
      .check();
    multiSelectionReport.initTablePage(3)
      .checkTitle("Recurring")
      .add("Series","Total","Mar 12","Apr 12","May 12")
      .add("House","1200.00","400.00","400.00","400.00")
      .add("Electricity","150.00","50.00","50.00","50.00")
      .check();
    multiSelectionReport.initTablePage(4)
      .checkTitle("Variable")
      .add("Series","Total","Mar 12","Apr 12","May 12")
      .add("Groceries","650.00","200.00","200.00","250.00")
      .add("Leisures","200.00","50.00","50.00","100.00")
      .check();
  }

  public void testRangeSelections() throws Exception {

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
    operations.openPrint()
      .checkOptions("December 2011", "Year 2011")
      .checkCurrentMonthSelected()
      .print();

    BudgetReportChecker monthReport = printer.getBudgetReport();
    monthReport.getOverviewPage().checkTitle("Overview for december 2011");

    monthReport.initGaugesPage(1)
      .checkTitle("Budget for december 2011")
      .checkBlockCount(11)
      .checkBudget(0, "Income", "0.00", "0.00")
      .checkSeparator(1)
      .checkBudget(2, "Recurring", "0.00", "0.00")
      .checkSeparator(3)
      .checkBudget(4, "Variable", "250.00", "300.00")
      .checkSeries(5, "Groceries", "250.00", "200.00")
      .checkSeries(6, "Leisures", "0.00", "100.00")
      .checkSeparator(7)
      .checkBudget(8, "Transfers", "0.00", "0.00")
      .checkSeparator(9)
      .checkBudget(10, "Extras", "0.00", "0.00");

    monthReport.initTablePage(2)
      .checkTitle("Variable")
      .add("Series", "Total", "Nov 11", "Dec 11", "Jan 12", "Feb 12", "Mar 12", "Apr 12", "May 12", "June 12", "Jul 12")
      .add("Groceries", "1070.00", "", "250.00", "220.00", "", "", "", "200.00", "200.00", "200.00")
      .add("Leisures", "350.00", "", "", "", "", "", "50.00", "100.00", "100.00", "100.00")
      .check();

    monthReport.getOverviewPage().getHistoChart()
      .checkColumnCount(9)
      .checkDiffColumn(0, "N", "2011", 0.00, 0.00)
      .checkDiffColumn(1, "D", "2011", 0.00, 250.00, true)
      .checkDiffColumn(2, "J", "2012", 0.00, 220.00)
      .checkDiffColumn(3, "F", "2012", 0.00, 0.00)
      .checkDiffColumn(4, "M", "2012", 0.00, 0.00)
      .checkDiffColumn(5, "A", "2012", 0.00, 50.00)
      .checkDiffColumn(6, "M", "2012", 0.00, 300.00)
      .checkDiffColumn(7, "J", "2012", 0.00, 300.00)
      .checkDiffColumn(8, "J", "2012", 0.00, 300.00);

    // -- Year 2011 --

    operations.openPrint()
      .checkOptions("December 2011", "Year 2011")
      .checkCurrentMonthSelected()
      .selectCurrentYear()
      .print();

    BudgetReportChecker year2011Report = printer.getBudgetReport();
    year2011Report.getOverviewPage().checkTitle("Overview for december 2011");

    year2011Report.initGaugesPage(1)
      .checkTitle("Budget for december 2011")
      .checkBlockCount(11)
      .checkBudget(0, "Income", "0.00", "0.00")
      .checkSeparator(1)
      .checkBudget(2, "Recurring", "0.00", "0.00")
      .checkSeparator(3)
      .checkBudget(4, "Variable", "250.00", "300.00")
      .checkSeries(5, "Groceries", "250.00", "200.00")
      .checkSeries(6, "Leisures", "0.00", "100.00")
      .checkSeparator(7)
      .checkBudget(8, "Transfers", "0.00", "0.00")
      .checkSeparator(9)
      .checkBudget(10, "Extras", "0.00", "0.00");

    year2011Report.initTablePage(2)
      .checkTitle("Variable")
      .add("Series", "Total", "Apr 11", "May 11", "June 11", "Jul 11", "Aug 11", "Sep 11", "Oct 11", "Nov 11", "Dec 11")
      .add("Groceries", "700.00", "100.00", "", "150.00", "", "", "200.00", "", "", "250.00")
      .check();

    year2011Report.getOverviewPage().getHistoChart()
      .checkColumnCount(9)
      .checkDiffColumn(0, "A", "2011", 0.00, 100.00)
      .checkDiffColumn(1, "M", "2011", 0.00, 0.00)
      .checkDiffColumn(2, "J", "2011", 0.00, 150.00)
      .checkDiffColumn(3, "J", "2011", 0.00, 0.00)
      .checkDiffColumn(4, "A", "2011", 0.00, 0.00)
      .checkDiffColumn(5, "S", "2011", 0.00, 200.00)
      .checkDiffColumn(6, "O", "2011", 0.00, 0.00)
      .checkDiffColumn(7, "N", "2011", 0.00, 0.00)
      .checkDiffColumn(8, "D", "2011", 0.00, 250.00, true);

    // -- Year 2011 --

    timeline.selectMonth(201202);
    operations.openPrint()
      .checkOptions("February 2012", "Year 2012")
      .selectCurrentYear()
      .checkCurrentYearSelected()
      .print();

    BudgetReportChecker year2012Report = printer.getBudgetReport();
    year2012Report.getOverviewPage().checkTitle("Overview for february 2012");

    year2012Report.initGaugesPage(1)
      .checkTitle("Budget for february 2012")
      .checkBlockCount(11)
      .checkBudget(0, "Income", "0.00", "0.00")
      .checkSeparator(1)
      .checkBudget(2, "Recurring", "0.00", "0.00")
      .checkSeparator(3)
      .checkBudget(4, "Variable", "0.00", "300.00")
      .checkSeries(5, "Groceries", "0.00", "200.00")
      .checkSeries(6, "Leisures", "0.00", "100.00")
      .checkSeparator(7)
      .checkBudget(8, "Transfers", "0.00", "0.00")
      .checkSeparator(9)
      .checkBudget(10, "Extras", "0.00", "0.00");

    year2012Report.initTablePage(2)
      .checkTitle("Variable")
      .add("Series", "Total", "Jan 12", "Feb 12", "Mar 12", "Apr 12", "May 12", "June 12", "Jul 12")
      .add("Groceries", "820.00", "220.00", "", "", "", "200.00", "200.00", "200.00")
      .add("Leisures", "350.00", "", "", "", "50.00", "100.00", "100.00", "100.00")
      .check();

    year2012Report.getOverviewPage().getHistoChart()
      .checkColumnCount(7)
      .checkDiffColumn(0, "J", "2012", 0.00, 220.00)
      .checkDiffColumn(1, "F", "2012", 0.00, 0.00, true)
      .checkDiffColumn(2, "M", "2012", 0.00, 0.00)
      .checkDiffColumn(3, "A", "2012", 0.00, 50.00)
      .checkDiffColumn(4, "M", "2012", 0.00, 300.00)
      .checkDiffColumn(5, "J", "2012", 0.00, 300.00)
      .checkDiffColumn(6, "J", "2012", 0.00, 300.00);
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
    operations.openPrint()
      .checkOptions("April 2012", "Year 2012")
      .checkCurrentMonthSelected()
      .print();

    printer.getBudgetReport().checkPageCount(4);
    printer.getBudgetReport().initTablePage(2)
      .checkTitle("Variable")
      .checkRowCount(31)
      .checkRow(0, "Series", "Total", "Mar 12", "Apr 12", "May 12")
      .checkRow(1, "Series01","651.00","201.00","200.00","250.00")
      .checkRow(30, "Series30","680.00","230.00","200.00","250.00");
    printer.getBudgetReport().initTablePage(3)
      .checkTitle("Variable")
      .checkRowCount(11)
      .checkRow(0, "Series", "Total", "Mar 12", "Apr 12", "May 12")
      .checkRow(1, "Series31", "681.00", "231.00", "200.00", "250.00")
      .checkRow(10, "Series40", "690.00", "240.00", "200.00", "250.00");
  }

  public void testMirrorSavingsSeriesAreNotShown() throws Exception {
    SavingsSetup.run(this, 201204);

    timeline.selectMonth(201204);
    operations.openPrint().print();

    BudgetReportChecker report = printer.getBudgetReport();
    report.checkPageCount(3);

    report.initGaugesPage(1)
      .checkTitle("Budget for april 2012")
      .checkBlockCount(16)
      .checkBudget(0, "Income", "0.00", "0.00")
      .checkSeparator(1)
      .checkBudget(2, "Recurring", "0.00", "0.00")
      .checkSeparator(3)
      .checkBudget(4, "Variable", "0.00", "0.00")
      .checkSeparator(5)
      .checkBudget(6, "Transfers", "90.00", "0.00")
      .checkSeries(7, "ImportedToExternal", "300.00", "0.00")
      .checkSeries(8, "ImportedFromNonImported", "+220.00", "0.00")
      .checkSeries(9, "ImportedFromExternal", "+200.00", "0.00")
      .checkSeries(10, "MainToImported", "150.00", "0.00")
      .checkSeries(11, "ImportedToMain", "+70.00", "0.00")
      .checkSeries(12, "MainToNonImported", "50.00", "0.00")
      .checkSeries(13, "MainFromNonImported", "+40.00", "0.00")
      .checkSeparator(14)
      .checkBudget(15, "Extras", "0.00", "0.00");
  }

  public void testExceptionRaisedDuringPrint() throws Exception {
    printer.setException("Boom");
    operations.openPrint()
      .printWithErrorMessage("Print failed", "An error occurred: Boom");
  }

  private String getLabel(String label, int i) {
    return label + Formatting.TWO_DIGIT_INTEGER_FORMAT.format(i);
  }
}
