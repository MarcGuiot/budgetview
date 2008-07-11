package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public abstract class UndoRedoTest extends LoggedInFunctionalTestCase {
  public void testUndoRedoSequence() throws Exception {

    operations.checkUndoNotAvailable();
    operations.checkRedoNotAvailable();

    OfxBuilder.init(this)
      .addTransaction("2008/07/11", 95.00, "Fouquet's", MasterCategory.FOOD)
      .load();

    transactions.initContent()
      .add("11/07/2008", TransactionType.VIREMENT, "Fouquet's", "", 95.00, MasterCategory.FOOD)
      .check();

    operations.checkUndoAvailable();
    operations.checkRedoNotAvailable();

    OfxBuilder.init(this)
      .addTransaction("2008/07/12", 15.00, "McDo", MasterCategory.FOOD)
      .load();

    operations.checkUndoAvailable();
    operations.checkRedoNotAvailable();

    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "McDo", "", 15.00, MasterCategory.FOOD)
      .add("11/07/2008", TransactionType.VIREMENT, "Fouquet's", "", 95.00, MasterCategory.FOOD)
      .check();

    operations.undo();

    operations.checkUndoAvailable();
    operations.checkRedoAvailable();

    transactions.initContent()
      .add("11/07/2008", TransactionType.VIREMENT, "Fouquet's", "", 95.00, MasterCategory.FOOD)
      .check();

    operations.redo();

    operations.checkUndoAvailable();
    operations.checkRedoNotAvailable();

    transactions.initContent()
      .add("12/07/2008", TransactionType.VIREMENT, "McDo", "", 15.00, MasterCategory.FOOD)
      .add("11/07/2008", TransactionType.VIREMENT, "Fouquet's", "", 95.00, MasterCategory.FOOD)
      .check();

    operations.checkUndoAvailable();
    operations.checkRedoNotAvailable();

    operations.undo();
    operations.undo();

    transactions.assertVisible(false);
  }

  public void DISABLED_testUndoRedoMaintainsSelection() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/08/15", 15.00, "McDo", MasterCategory.FOOD)
      .addTransaction("2008/07/15", 95.00, "Orange")
      .load();

    periods.selectCell(0);
    transactions.initContent()
      .add("15/07/2008", TransactionType.VIREMENT, "Orange", "", 95.00)
      .check();

    transactions.assignCategory(MasterCategory.TELECOMS, 0);

    periods.selectCell(1);
    transactions.initContent()
      .add("15/08/2008", TransactionType.VIREMENT, "McDo", "", 15.00, MasterCategory.FOOD)
      .check();

    operations.undo();

    periods.assertCellSelected(0);
    transactions.initContent()
      .add("15/07/2008", TransactionType.VIREMENT, "Orange", "", 95.00)
      .check();
  }
}
