package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategorizationDialogChecker;
import org.designup.picsou.functests.checkers.SeriesCreationDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

public class SeriesCreationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    TimeService.setCurrentDate(Dates.parseMonth("2008/06"));
    super.setUp();
  }

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
    dialog.selectIncomeSeries("Prime", false);
    dialog.validate();

    transactionDetails.checkSeries("Prime");
    transactionDetails.checkCategory(MasterCategory.INCOME);
    transactions.initContent()
      .add("30/06/2008", TransactionType.PLANNED, "Prime", "", 0.0, "Prime")
      .add("30/06/2008", TransactionType.PRELEVEMENT, "WorldCo/june", "", -1129.90, "Prime")
      .check();
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
    dialog.selectRecurringSeries("Culture", false);
    dialog.validate();

    transactionDetails.checkSeries("Culture");
    transactionDetails.checkCategory(MasterCategory.EDUCATION);
  }

  public void testNewEnvelopeSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    CategorizationDialogChecker dialog = transactions.categorize(0);
    dialog.checkLabel("Forfait Kro");

    dialog.selectEnvelopes();
    SeriesCreationDialogChecker creationDialog = dialog.createSeries();
    creationDialog.setName("Regime");
    creationDialog.checkType("Envelope");
    creationDialog.setCategory(MasterCategory.FOOD);
    creationDialog.validate();

    dialog.checkContainsEnvelope("Regime");
    dialog.selectEnvelopeSeries("Regime", MasterCategory.FOOD, false);
    dialog.validate();

    transactionDetails.checkSeries("Regime");
    transactionDetails.checkCategory(MasterCategory.FOOD);
  }

  public void testCancel() throws Exception {
    checkCancel(new Callback() {
      public void process(CategorizationDialogChecker dialog) {
        dialog.cancel();
      }
    });
  }

  public void testEscape() throws Exception {
    checkCancel(new Callback() {
      public void process(CategorizationDialogChecker dialog) {
        dialog.pressEscapeKey();
      }
    });
  }

  private void checkCancel(Callback callback) {
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

    callback.process(dialog);

    transactionDetails.checkNoSeries();

    CategorizationDialogChecker newDialog = transactions.categorize(0);
    newDialog.checkLabel("JaimeLeFoot.com");
    newDialog.selectRecurring();
    newDialog.checkRecurringSeriesNotFound("Culture");
  }

  private interface Callback {
    void process(CategorizationDialogChecker dialog);
  }
}
