package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class UndoRedoTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setNotRegistered();
    super.setUp();
  }

  public void testUndoRedoSequence() throws Exception {

    //   operations.checkUndoNotAvailable();
    operations.checkRedoNotAvailable();

    OfxBuilder.init(this)
      .addTransaction("2008/07/11", 95.00, "Fouquet's")
      .load();

    views.selectData();
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
    OfxBuilder.init(this)
      .addTransaction("2008/07/11", 95.00, "Auchan")
      .load();
    views.selectCategorization();
    categorization.selectTransactions("Auchan");
    categorization.selectVariable().selectNewSeries("Courant");
    categorization.getVariable().checkSeriesIsSelected("Courant");

    views.selectData();
    transactions.checkSeries("Auchan", "Courant");
    operations.undo();
    transactions.checkSeries("Auchan", "To categorize");
    operations.redo();
    transactions.checkSeries("Auchan", "Courant");

    views.selectCategorization();
    categorization.getVariable().checkSeriesIsSelected("Courant");
    operations.undo();

    views.selectCategorization();
    categorization.selectAllTransactions();
    categorization.selectVariable().checkContainsSeries("Courant");

    views.selectData();
    transactions.checkSeries("Auchan", "To categorize");
    operations.undo();

    views.selectCategorization();
    categorization.selectAllTransactions();
    categorization.selectVariable().checkDoesNotContainSeries("Courant");

    views.selectData();
    transactions.checkSeries("Auchan", "To categorize");
    operations.undo();
    operations.undo(); // signpost
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
