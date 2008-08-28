package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.CategorizationDialogChecker;
import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

public class SeriesCreationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate(Dates.parseMonth("2008/06"));
    super.setUp();
  }

  public void testNewIncomeSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -1129.90, "WorldCo/june")
      .load();

    CategorizationDialogChecker categorization = transactions.categorize(0);
    categorization.checkLabel("WorldCo/june");
    categorization.selectIncome();

    SeriesEditionDialogChecker creationSeries = categorization.createSeries();
    creationSeries.setName("Prime");
    creationSeries.checkType("Income");
    creationSeries.setCategory(MasterCategory.INCOME);

    creationSeries.validate();

    categorization.checkContainsIncomeSeries("Salary", "Prime");
    categorization.validate();

    transactionDetails.checkSeries("Prime");
    transactionDetails.checkCategory(MasterCategory.INCOME);
    transactions.initContent()
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
    SeriesEditionDialogChecker editionDialog = dialog.createSeries();
    editionDialog.setName("Culture");
    editionDialog.checkType("Recurring");
    editionDialog.setCategory(MasterCategory.EDUCATION);
    editionDialog.validate();

    dialog.checkContainsRecurringSeries("Internet", "Culture");
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
    SeriesEditionDialogChecker editionDialog = dialog.createSeries();
    editionDialog.setName("Regime");
    editionDialog.checkType("Envelope");
    editionDialog.setCategory(MasterCategory.FOOD);
    editionDialog.validate();

    dialog.checkContainsEnvelope("Regime");
    dialog.validate();

    transactionDetails.checkSeries("Regime");
    transactionDetails.checkCategory(MasterCategory.FOOD);
  }

  public void testSeriesUnselectedAfterCategorization() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .addTransaction("2008/06/20", -60, "Forfait Kro")
      .addTransaction("2008/06/10", -60, "Forfait Kro")
      .addTransaction("2008/06/28", -150, "Palette Leffe")
      .load();

    CategorizationDialogChecker dialog = informationPanel.categorize();
    dialog.checkTable(new Object[][]{
      {"30/06/2008", "Forfait Kro", -60.00},
      {"20/06/2008", "Forfait Kro", -60.00},
      {"10/06/2008", "Forfait Kro", -60.00},
      {"28/06/2008", "Palette Leffe", -150.00},
    });
    dialog.checkSelectedTableRows(0,1,2);

    dialog.selectEnvelopes();
    SeriesEditionDialogChecker editionDialog = dialog.createSeries();
    editionDialog.setName("Regime");
    editionDialog.checkType("Envelope");
    editionDialog.setCategory(MasterCategory.FOOD);

    editionDialog.validate();

    dialog.checkTable(new Object[][]{
      {"28/06/2008", "Palette Leffe", -150.00},
    });
    dialog.checkNoTransactionSelected();
    dialog.checkNoBudgetAreaSelected();
    dialog.validate();
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
    SeriesEditionDialogChecker editionDialog = dialog.createSeries();
    editionDialog.setName("Culture");
    editionDialog.checkType("Recurring");
    editionDialog.setCategory(MasterCategory.EDUCATION);
    editionDialog.validate();

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
