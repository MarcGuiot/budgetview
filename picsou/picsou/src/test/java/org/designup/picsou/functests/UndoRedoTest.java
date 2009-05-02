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
      .addTransaction("2008/07/11", 95.00, "Fouquet's", MasterCategory.FOOD)
      .load();

    transactions.initContent()
      .addOccasional("11/07/2008", TransactionType.VIREMENT, "Fouquet's", "", 95.00, MasterCategory.FOOD)
      .check();

    operations.checkUndoAvailable();
    operations.checkRedoNotAvailable();

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", 15.00, "McDo", MasterCategory.FOOD)
      .load();

    operations.checkUndoAvailable();
    operations.checkRedoNotAvailable();

    transactions.initContent()
      .addOccasional("12/07/2008", TransactionType.VIREMENT, "McDo", "", 15.00, MasterCategory.FOOD)
      .addOccasional("11/07/2008", TransactionType.VIREMENT, "Fouquet's", "", 95.00, MasterCategory.FOOD)
      .check();

    operations.undo();

    operations.checkUndoAvailable();
    operations.checkRedoAvailable();

    transactions.initContent()
      .addOccasional("11/07/2008", TransactionType.VIREMENT, "Fouquet's", "", 95.00, MasterCategory.FOOD)
      .check();

    operations.redo();

    operations.checkUndoAvailable();
    operations.checkRedoNotAvailable();

    transactions.initContent()
      .addOccasional("12/07/2008", TransactionType.VIREMENT, "McDo", "", 15.00, MasterCategory.FOOD)
      .addOccasional("11/07/2008", TransactionType.VIREMENT, "Fouquet's", "", 95.00, MasterCategory.FOOD)
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
    categorization.selectTableRows("Auchan");
    categorization.selectEnvelopes();
    categorization.selectEnvelopeSeries("Courant", MasterCategory.FOOD, true);
    categorization.checkEnvelopeSeriesIsSelected("Courant", MasterCategory.FOOD);
    views.selectData();
    transactions.checkSeries("Auchan", "Courant");
    operations.undo();
    transactions.checkCategory("Auchan", MasterCategory.NONE);
    transactions.checkSeries("Auchan", "To categorize");
    operations.redo();
    transactions.checkSeries("Auchan", "Courant");
    views.selectCategorization();
    categorization.checkEnvelopeSeriesIsSelected("Courant", MasterCategory.FOOD);
    operations.undo();
    transactions.checkCategory("Auchan", MasterCategory.NONE);
    transactions.checkSeries("Auchan", "To categorize");
  }

  public void DISABLED_testUndoRedoMaintainsSelection() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/08/15", 15.00, "McDo", MasterCategory.FOOD)
      .addTransaction("2008/07/15", 95.00, "Orange")
      .load();

    timeline.selectMonth("2008/07");
    transactions.initContent()
      .add("15/07/2008", TransactionType.VIREMENT, "Orange", "", 95.00)
      .check();

//    transactions.assignOccasionalSeries(MasterCategory.TELECOMS, 0);

    timeline.selectMonth("2008/08");
    transactions.initContent()
      .add("15/08/2008", TransactionType.VIREMENT, "McDo", "", 15.00, MasterCategory.FOOD)
      .check();

    operations.undo();

    timeline.checkSelection("2008/07");
    transactions.initContent()
      .add("15/07/2008", TransactionType.VIREMENT, "Orange", "", 95.00)
      .check();
  }

  public void testUndoImportWithManyMonth() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/07/15", -45.00, "Free")
      .addTransaction("2008/07/15", -50.00, "Orange")
      .addTransaction("2008/08/15", 15.00, "McDo", MasterCategory.FOOD)
      .load();

    views.selectCategorization();
    categorization.setRecurring("Orange", "FT", MasterCategory.TELECOMS, true);
    categorization.setRecurring("Free", "Free Telecom", MasterCategory.TELECOMS, true);
    categorization.setEnvelope("McDo", "Resto", MasterCategory.FOOD, true);

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
