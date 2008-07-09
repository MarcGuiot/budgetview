package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.checkers.CategorizationDialogChecker;
import org.designup.picsou.functests.checkers.SeriesCreationDialogChecker;
import org.designup.picsou.model.MasterCategory;

public class SeriesCreationTest extends LoggedInFunctionalTestCase {
  public void testNewIncomeSeries() throws Exception {
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

  public void testNewRecurringSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Telefoot+")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("Telefoot+");

    dialog.selectRecurring();
    SeriesCreationDialogChecker creationDialog = dialog.createSeries();
    creationDialog.setName("Culture");
    creationDialog.checkType("Recurring");
    creationDialog.setCategory(MasterCategory.EDUCATION);
    creationDialog.validate();

    dialog.checkContainsRecurringSeries("Internet", "Culture");
    dialog.selectRecurringSeries("Culture");
    dialog.validate();

    transactionDetails.checkSeries("Culture");
    transactionDetails.checkCategory(MasterCategory.EDUCATION);
  }
  
  public void testCancel() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "JaimeLeFoot.com")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("JaimeLeFoot.com");

    dialog.selectRecurring();
    SeriesCreationDialogChecker creationDialog = dialog.createSeries();
    creationDialog.setName("Culture");
    creationDialog.checkType("Recurring");
    creationDialog.setCategory(MasterCategory.EDUCATION);
    creationDialog.validate();

    dialog.cancel();

    transactionDetails.checkNoSeries();

    CategorizationDialogChecker newDialog = transactions.categorize(0);
    newDialog.checkLabel("JaimeLeFoot.com");
    newDialog.selectRecurring();
    newDialog.checkRecurringSeriesNotFound("Culture");
  }  
}
