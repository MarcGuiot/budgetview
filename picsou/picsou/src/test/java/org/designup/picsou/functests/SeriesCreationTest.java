package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.checkers.CategorizationDialogChecker;
import org.designup.picsou.functests.checkers.SeriesCreationDialogChecker;
import org.designup.picsou.model.MasterCategory;

public class SeriesCreationTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -1129.90, "WorldCo/june")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("WorldCo/june");

    dialog.selectIncome();
    SeriesCreationDialogChecker creationDialog = dialog.createSeries();
    creationDialog.setName("Prime");
    creationDialog.checkType("Income");
    creationDialog.setCategory(MasterCategory.INCOME);
    creationDialog.validate();

    dialog.checkContainsIncomeSeries("Salary", "Prime");
    dialog.selectIncomeSeries("Prime");
    dialog.validate();

    transactionDetails.checkSeries("Prime");
    transactionDetails.checkCategory(MasterCategory.INCOME);
  }

  public void testCancel() throws Exception {
//    fail();
  }
  
}
