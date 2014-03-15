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
      categorization.getVariable().checkSelectedSeries("Courant");

      transactions.checkSeries("Auchan", "Courant");
      operations.undo();
      transactions.checkSeries("Auchan", "To categorize");
      operations.redo();
      transactions.checkSeries("Auchan", "Courant");

      categorization.getVariable().checkSelectedSeries("Courant");
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
