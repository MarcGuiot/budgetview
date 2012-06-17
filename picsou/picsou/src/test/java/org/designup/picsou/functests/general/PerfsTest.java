package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Utils;
import org.uispec4j.utils.Chrono;

public abstract class PerfsTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    OfxBuilder builder = OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 1.23, "2006/01/30");
    for (int m = 0; m < 10; m++) {
      for (int i = 0; i < 200; i++) {
        builder.addTransaction("2006/0" + m + "/10", Utils.randomInt(2000) - 1000, "Blah");
      }
    }
    builder.load();

    System.out.println("PerfsTest.test: START");

  }

  // Reference ~= 7400
  public void testPeriodSelections() throws Exception {
    Chrono chrono = Chrono.start();
    for (int i = 0; i < 100; i++) {
      for (int m = 0; m < 10; m++) {
        timeline.selectCell(m);
      }
      System.out.println("Round " + i + " done in " + chrono.getElapsedTime());
    }
    System.out.println("==> " + chrono.getElapsedTime());
  }

  // Reference ~= 13250
  public void testCategoryAllocation() throws Exception {
    Chrono chrono = Chrono.start();
    for (int i = 0; i < 5; i++) {
      for (int row = 0; row < 5; row++) {
//        transactions.assignOccasionalSeries(MasterCategory.FOOD, row);
      }
      for (int row = 0; row < 5; row++) {
//        transactions.assignOccasionalSeries(MasterCategory.NONE, row);
      }
      System.out.println("Round " + i + " done in " + chrono.getElapsedTime());
    }
    System.out.println("==> " + chrono.getElapsedTime());
  }

  public static class TimeManagementTest extends LoggedInFunctionalTestCase {

    protected void setUp() throws Exception {
      setCurrentMonth("2006/01/10");
      super.setUp();
    }

    public void testInitialState() throws Exception {
      timeline.checkSelection("2006/01");
      timeline.checkDisplays("2006/01");
    }

    public void testSingleMonth() throws Exception {
      OfxBuilder
        .init(this)
        .addTransaction("2006/01/10", -10, "rent")
        .addTransaction("2006/01/20", +5, "income")
        .load();

      timeline.checkDisplays("2006/01");
      timeline.checkSelection("2006/01");
    }

    public void testTwoMonths() throws Exception {
      OfxBuilder
        .init(this)
        .addTransaction("2006/01/10", -10, "rent")
        .addTransaction("2006/02/20", +5, "income")
        .load();

      timeline.checkSelection("2006/02");

      timeline.checkDisplays("2006/01", "2006/02");

      timeline.selectAll();
      timeline.checkSelection("2006/01", "2006/02");
      timeline.selectMonth("2006/02");

      transactions.initContent()
        .add("20/02/2006", TransactionType.VIREMENT, "income", "", 5.0)
        .check();

      timeline.selectMonth("2006/01");
      transactions.initContent()
        .add("10/01/2006", TransactionType.PRELEVEMENT, "rent", "", -10)
        .check();
    }

    public void testMonthsWithNoDataAreShown() throws Exception {
      OfxBuilder
        .init(this)
        .addTransaction("2006/01/10", -10, "rent")
        .addTransaction("2006/03/20", +5, "income")
        .load();

      timeline.selectMonth("2006/03");
      transactions
        .initContent()
        .add("20/03/2006", TransactionType.VIREMENT, "income", "", 5)
        .check();

      timeline.selectMonth("2006/01");
      transactions
        .initContent()
        .add("10/01/2006", TransactionType.PRELEVEMENT, "rent", "", -10)
        .check();

      timeline.selectMonth("2006/02");
      transactions.checkTableIsEmpty();

      timeline.selectMonth("2006/03");
      transactions
        .initContent()
        .add("20/03/2006", TransactionType.VIREMENT, "income", "", 5)
        .check();
    }

    public void testMultiselectionCumulatesTransactions() throws Exception {
      OfxBuilder
        .init(this)
        .addTransaction("2006/01/10", -10, "rent")
        .addTransaction("2006/02/10", +50, "income1")
        .addTransaction("2006/03/20", +5, "income2")
        .load();

      timeline.selectMonths("2006/01", "2006/02", "2006/03");

      transactions
        .initContent()
        .add("20/03/2006", TransactionType.VIREMENT, "income2", "", +5)
        .add("10/02/2006", TransactionType.VIREMENT, "income1", "", +50)
        .add("10/01/2006", TransactionType.PRELEVEMENT, "rent", "", -10)
        .check();

      timeline.selectNone();
      transactions.checkTableIsEmpty();
    }

    public void testMenuSelection() throws Exception {
      operations.openPreferences().setFutureMonthsCount(12).validate();
      OfxBuilder
        .init(this)
        .addTransaction("2006/01/01", 100, "income")
        .addTransaction("2005/12/01", 1200, "income")
        .addTransaction("2005/11/01", 1100, "income")
        .addTransaction("2005/10/01", 1000, "income")
        .addTransaction("2005/09/01", 900, "income")
        .addTransaction("2005/08/01", 800, "income")
        .addTransaction("2005/07/01", 700, "income")
        .addTransaction("2005/06/01", 600, "income")
        .addTransaction("2005/05/01", 500, "income")
        .addTransaction("2005/04/01", 400, "income")
        .addTransaction("2005/03/01", 300, "income")
        .addTransaction("2005/02/01", 200, "income")
        .addTransaction("2005/01/01", 100, "income")
        .addTransaction("2004/12/01", 50, "income")
        .load();

      timeline.selectMonth("2005/08");
      transactions.initContent()
        .add("01/08/2005", TransactionType.VIREMENT, "INCOME", "", 800.00)
        .check();

      operations.selectCurrentMonth();
      timeline.checkSelection("2006/01");
      transactions.initContent()
        .add("01/01/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
        .check();

      timeline.selectMonth("2004/12");
      operations.selectCurrentYear();
      timeline.checkSelection("2006/01", "2006/02", "2006/03", "2006/04", "2006/05", "2006/06",
                              "2006/07", "2006/08", "2006/09", "2006/10", "2006/11", "2006/12");
      transactions.initContent()
        .add("01/01/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
        .check();

      timeline.selectMonth("2004/12");
      operations.selectLast12Months();
      timeline.checkSelection("2005/02", "2005/03", "2005/04", "2005/05", "2005/06", "2005/07",
                              "2005/08", "2005/09", "2005/10", "2005/11", "2005/12", "2006/01");
      transactions.initContent()
        .add("01/01/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
        .add("01/12/2005", TransactionType.VIREMENT, "INCOME", "", 1200.00)
        .add("01/11/2005", TransactionType.VIREMENT, "INCOME", "", 1100.00)
        .add("01/10/2005", TransactionType.VIREMENT, "INCOME", "", 1000.00)
        .add("01/09/2005", TransactionType.VIREMENT, "INCOME", "", 900.00)
        .add("01/08/2005", TransactionType.VIREMENT, "INCOME", "", 800.00)
        .add("01/07/2005", TransactionType.VIREMENT, "INCOME", "", 700.00)
        .add("01/06/2005", TransactionType.VIREMENT, "INCOME", "", 600.00)
        .add("01/05/2005", TransactionType.VIREMENT, "INCOME", "", 500.00)
        .add("01/04/2005", TransactionType.VIREMENT, "INCOME", "", 400.00)
        .add("01/03/2005", TransactionType.VIREMENT, "INCOME", "", 300.00)
        .add("01/02/2005", TransactionType.VIREMENT, "INCOME", "", 200.00)
        .check();
    }

    public void testMenuSelectionWithIncompleteYear() throws Exception {
      operations.openPreferences().setFutureMonthsCount(6).validate();
      OfxBuilder
        .init(this)
        .addTransaction("2006/01/01", 100, "income")
        .addTransaction("2005/12/01", 1200, "income")
        .addTransaction("2005/11/01", 1100, "income")
        .addTransaction("2005/10/01", 1000, "income")
        .load();

      timeline.selectMonth("2005/10");
      transactions.initContent()
        .add("01/10/2005", TransactionType.VIREMENT, "INCOME", "", 1000.00)
        .check();

      operations.selectCurrentMonth();
      timeline.checkSelection("2006/01");
      transactions.initContent()
        .add("01/01/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
        .check();

      timeline.selectMonth("2005/12");
      operations.selectCurrentYear();
      timeline.checkSelection("2006/01", "2006/02", "2006/03", "2006/04", "2006/05", "2006/06", "2006/07");
      transactions.initContent()
        .add("01/01/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
        .check();

      timeline.selectMonth("2005/12");
      operations.selectLast12Months();
      timeline.checkSelection("2005/10", "2005/11", "2005/12", "2006/01");
      transactions.initContent()
        .add("01/01/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
        .add("01/12/2005", TransactionType.VIREMENT, "INCOME", "", 1200.00)
        .add("01/11/2005", TransactionType.VIREMENT, "INCOME", "", 1100.00)
        .add("01/10/2005", TransactionType.VIREMENT, "INCOME", "", 1000.00)
        .check();  }
  }

  public static class TitleDisplayTest extends LoggedInFunctionalTestCase {

    public void testViewNames() throws Exception {

      screen.checkContent("Initialization");
      views.selectHome();
      screen.checkContent("Initialization");
      views.selectBudget();
      screen.checkContent("Initialization");
      views.selectHome();

      OfxBuilder
        .init(this)
        .addTransaction("2009/02/10", -10, "rent")
        .addTransaction("2009/01/10", -10, "rent")
        .addTransaction("2008/12/10", -10, "rent")
        .addTransaction("2008/11/10", -10, "rent")
        .addTransaction("2008/10/10", -10, "rent")
        .addTransaction("2008/09/10", -10, "rent")
        .addTransaction("2008/08/10", -10, "rent")
        .load();

      timeline.selectAll();
      timeline.checkSelection("2008/08", "2008/09", "2008/10", "2008/11", "2008/12", "2009/01", "2009/02");

      views.selectHome();
      screen.checkContent("Dashboard", "August 2008 - February 2009");

      timeline.selectMonth("2008/10");
      views.selectBudget();
      screen.checkContent("Budget", "October 2008");

      views.selectData();
      screen.checkContent("Operations", "October 2008");

      views.selectBudget();
      screen.checkContent("Budget", "October 2008");
    }

    public void testSeveralMonths() throws Exception {
      OfxBuilder
        .init(this)
        .addTransaction("2009/02/10", -10, "rent")
        .addTransaction("2008/08/10", -10, "rent")
        .load();

      views.selectData();
      timeline.selectMonths("2008/08", "2008/09", "2008/10");
      screen.checkContent("Operations", "August - October 2008");
    }

    public void testNoMonth() throws Exception {
      OfxBuilder
        .init(this)
        .addTransaction("2009/02/10", -10, "rent")
        .load();

      timeline.selectNone();
      screen.checkContent("Select a period");
    }
  }

  public static class UndoRedoTest extends LoggedInFunctionalTestCase {

    protected void setUp() throws Exception {
      super.setNotRegistered();
      super.setUp();
    }

    public void testUndoRedoSequence() throws Exception {

      operations.hideSignposts();
      operations.checkRedoNotAvailable();

      OfxBuilder.init(this)
        .addTransaction("2008/07/11", 95.00, "Fouquet's")
        .load();

      transactions.initContent()
        .add("11/07/2008", TransactionType.VIREMENT, "Fouquet's", "", 95.00)
        .check();

      operations.checkUndoAvailable();
      operations.checkRedoNotAvailable();

      OfxBuilder.init(this)
        .addTransaction("2008/07/12", 15.00, "McDo")
        .load();

      operations.checkUndoAvailable();
      operations.checkRedoNotAvailable();

      transactions.initContent()
        .add("12/07/2008", TransactionType.VIREMENT, "McDo", "", 15.00)
        .add("11/07/2008", TransactionType.VIREMENT, "Fouquet's", "", 95.00)
        .check();

      operations.undo();

      operations.checkUndoAvailable();
      operations.checkRedoAvailable();

      transactions.initContent()
        .add("11/07/2008", TransactionType.VIREMENT, "Fouquet's", "", 95.00)
        .check();

      operations.redo();

      operations.checkUndoAvailable();
      operations.checkRedoNotAvailable();

      transactions.initContent()
        .add("12/07/2008", TransactionType.VIREMENT, "McDo", "", 15.00)
        .add("11/07/2008", TransactionType.VIREMENT, "Fouquet's", "", 95.00)
        .check();

      operations.checkUndoAvailable();
      operations.checkRedoNotAvailable();

      operations.undo();
      operations.undo();

      transactions.checkTableIsEmpty();
    }


    public void testOnSeriesCreation() throws Exception {

      operations.hideSignposts();

      OfxBuilder.init(this)
        .addTransaction("2008/07/11", 95.00, "Auchan")
        .load();

      categorization.selectTransactions("Auchan");
      categorization.selectVariable().selectNewSeries("Courant");
      categorization.getVariable().checkSeriesIsSelected("Courant");

      transactions.checkSeries("Auchan", "Courant");
      operations.undo();
      transactions.checkSeries("Auchan", "To categorize");
      operations.redo();
      transactions.checkSeries("Auchan", "Courant");

      categorization.getVariable().checkSeriesIsSelected("Courant");
      operations.undo();

      categorization.selectAllTransactions();
      categorization.selectVariable().checkContainsSeries("Courant");

      transactions.checkSeries("Auchan", "To categorize");
      operations.undo();

      categorization.selectAllTransactions();
      categorization.selectVariable().checkDoesNotContainSeries("Courant");

      transactions.checkSeries("Auchan", "To categorize");
      operations.undo();
      transactions.checkTableIsEmpty();
    }

    public void testMaxUndo() throws Exception {
      OfxBuilder.init(this)
        .addTransaction("2008/07/11", 95.00, "Auchan")
        .load();
      views.selectCategorization();
      categorization.selectTransactions("Auchan");
      categorization.selectVariable().selectNewSeries("Courant");
      for (int i = 0; i < 30; i++) {
        categorization
          .selectUncategorized()
          .setUncategorized()
          .selectVariable().selectSeries("Courant");
      }
      int i;
      for (i = 0; i < 30; i++) {
        if (operations.isUndoAvailable()) {
          operations.undo();
        }
        else {
          break;
        }
      }
      assertTrue(i < 25);
    }

    public void DISABLED_testUndoRedoMaintainsSelection() throws Exception {

      OfxBuilder.init(this)
        .addTransaction("2008/08/15", 15.00, "McDo")
        .addTransaction("2008/07/15", 95.00, "Orange")
        .load();

      timeline.selectMonth("2008/07");
      transactions.initContent()
        .add("15/07/2008", TransactionType.VIREMENT, "Orange", "", 95.00)
        .check();

  //    transactions.assignOccasionalSeries(MasterCategory.TELECOMS, 0);

      timeline.selectMonth("2008/08");
      transactions.initContent()
        .add("15/08/2008", TransactionType.VIREMENT, "McDo", "", 15.00)
        .check();

      operations.undo();

      timeline.checkSelection("2008/07");
      transactions.initContent()
        .add("15/07/2008", TransactionType.VIREMENT, "Orange", "", 95.00)
        .check();
    }

    public void testUndoImportWithManyMonths() throws Exception {
      OfxBuilder.init(this)
        .addTransaction("2008/07/15", -45.00, "Free")
        .addTransaction("2008/07/15", -50.00, "Orange")
        .addTransaction("2008/08/15", 15.00, "McDo")
        .load();

      views.selectCategorization();
      categorization.setNewRecurring("Orange", "FT");
      categorization.setNewRecurring("Free", "Free Telecom");
      categorization.setNewVariable("McDo", "Resto");

      OfxBuilder.init(this)
        .addTransaction("2008/05/10", -95.00, "Orange")
        .addTransaction("2008/05/11", -10.00, "Free")
        .load();

      timeline.selectAll();
      categorization.showAllTransactions();
      categorization.selectTableRows(0, 1, 2, 3, 4);
      views.selectCategorization();
      operations.undo();
      operations.undo();
      operations.undo();
      operations.undo();
      operations.undo();
    }
  }

  public static class ViewsManagementTest extends LoggedInFunctionalTestCase {

    public void testHomePage() throws Exception {
      views.selectHome();
      views.checkHomeSelected();
      transactions.checkVisible(false);
    }

    protected void selectInitialView() {
      // default view
    }

    public void testDefaultState() throws Exception {

      views.checkHomeSelected();

      views.selectData();
      transactions.checkVisible(true);
    }

    public void testBackForward() throws Exception {
      views.checkHomeSelected();
      views.checkBackForward(false, false);

      views.selectData();
      views.checkBackForward(true, false);

      views.selectBudget();
      views.checkBackForward(true, false);

      views.back();
      views.checkDataSelected();
      views.checkBackForward(true, true);

      views.forward();
      views.checkBudgetSelected();
      views.checkBackForward(true, false);

      views.back();
      views.checkDataSelected();
      views.checkBackForward(true, true);

      views.back();
      views.checkHomeSelected();
      views.checkBackForward(false, true);

      views.forward();
      views.checkDataSelected();
      views.checkBackForward(true, true);

      views.selectCategorization();
      views.checkBackForward(true, false);

      views.back();
      views.checkDataSelected();
      views.checkBackForward(true, true);

      views.back();
      views.checkHomeSelected();
      views.checkBackForward(false, true);
    }

    public void testTooltips() throws Exception {
      views.checkAllTooltipsPresent();
    }
  }
}
