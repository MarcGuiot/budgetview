package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
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
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Courant", MasterCategory.FOOD, true);
    categorization.getEnvelopes().checkSeriesIsSelected("Courant");
    views.selectData();
    transactions.checkSeries("Auchan", "Courant");
    operations.undo();
    transactions.checkCategory("Auchan", MasterCategory.NONE);
    transactions.checkSeries("Auchan", "To categorize");
    operations.redo();
    transactions.checkSeries("Auchan", "Courant");
    views.selectCategorization();
    categorization.getEnvelopes().checkSeriesIsSelected("Courant");
    operations.undo();
    transactions.checkCategory("Auchan", MasterCategory.NONE);
    transactions.checkSeries("Auchan", "To categorize");
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
    categorization.setNewEnvelope("McDo", "Resto");

    OfxBuilder.init(this)
      .addTransaction("2008/05/10", -95.00, "Orange")
      .addTransaction("2008/05/11", -10.00, "Free")
      .load();

    timeline.selectAll();
    categorization.selectTableRows(0, 1, 2, 3, 4);
    views.selectCategorization();
    operations.undo();
    operations.undo();
    operations.undo();
    operations.undo();
    operations.undo();
  }
}
