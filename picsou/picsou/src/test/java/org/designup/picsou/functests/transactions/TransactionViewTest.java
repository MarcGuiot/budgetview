package org.designup.picsou.functests.transactions;

import junit.framework.Assert;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.TransactionType;
import org.uispec4j.Clipboard;
import org.uispec4j.Table;
import org.uispec4j.assertion.UISpecAssert;

public class TransactionViewTest extends LoggedInFunctionalTestCase {
  private Table table;

  protected void setUp() throws Exception {
    setCurrentMonth("2006/07");
    super.setUp();
    OfxBuilder
      .init(this)
      .addTransactionWithNote("2006/05/01", -70.00, "essence", "frais pro")
      .addTransactionWithNote("2006/05/03", -30.00, "peage", "")
      .addTransactionWithNote("2006/05/02", -200.00, "sg", "")
      .addTransactionWithNote("2006/05/06", -100.00, "nounou", "nourrice")
      .load();
    table = transactions.getTable();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    table = null;
  }

  public void testUsesColorsToHighlightAmountSign() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/05/11", 1000.0, "WorldCo")
      .load();

    transactions.initContent()
      .add("11/05/2006", TransactionType.VIREMENT, "WORLDCO", "", 1000.00)
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00)
      .check();

    transactions.checkAmountLabelColor("WORLDCO", "green");
    transactions.checkAmountLabelColor("PEAGE", "red");
  }

  public void testEditingANoteByCellSelection() throws Exception {
    transactions.editNote("SG", "interets");
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "interets", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00)
      .check();
  }

  public void testEditingWhileAColumnIsSorted() throws Exception {
    table.getHeader().click(1);

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00)
      .check();

    transactions.editNote(1, "garagiste");

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "garagiste", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00)
      .check();

    transactions.editNote(3, "essence");

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "garagiste", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "essence", -70.00)
      .check();

    transactions.editNote(0, "travaux");

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "travaux", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "garagiste", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "essence", -70.00)
      .check();
  }

  public void testFullLine() throws Exception {

    categorization.selectTransaction("essence")
      .selectVariable()
      .selectNewSeriesWithSubSeries("Voiture", "Carburant");

    Table table = transactions.getTable();
    UISpecAssert.assertTrue(
      table.rowEquals(table.getRowIndex(TransactionView.LABEL_COLUMN_INDEX, "ESSENCE"),
                      new String[]{"01/05/2006", "01/05/2006", "(prelevement)Voiture / Carburant",
                                   "ESSENCE", "-70.00", "frais pro", "330.00", "330.00", OfxBuilder.DEFAULT_ACCOUNT_NAME}));
  }

  public void testNavigatingToCategorizationView() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.0, "Something else")
      .addTransaction("2006/01/10", -1.0, "Menu 14")
      .load();

    timeline.selectMonth("2006/01");

    views.selectData();
    transactions.categorize("SOMETHING ELSE");

    views.checkCategorizationSelected();
    categorization.showSelectedMonthsOnly();
    categorization.checkTable(new Object[][]{
      {"10/01/2006", "", "Menu 14", -1.0},
      {"11/01/2006", "", "Something else", -1.0}
    });
    categorization.checkSelectedTableRows(1);
    categorization.checkCustomFilterVisible(false);
    categorization.showUncategorizedTransactionsOnly();
    categorization.setNewVariable("SOMETHING ELSE", "Clothes");

    views.back();
    views.checkDataSelected();
    transactions.checkSelectedRow(0);
    transactions.checkSeries(0, "Clothes");

    transactions.categorize("SOMETHING ELSE");
    views.checkCategorizationSelected();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.checkSelectedTableRow("SOMETHING ELSE");

    timeline.selectMonth("2006/01");
    categorization.showUncategorizedTransactionsOnly();
    categorization.checkTable(new Object[][]{
      {"01/05/2006", "", "ESSENCE", -70.0},
      {"10/01/2006", "", "MENU 14", -1.0},
      {"06/05/2006", "", "NOUNOU", -100.0},
      {"03/05/2006", "", "PEAGE", -30.0},
      {"02/05/2006", "", "SG", -200.0}
    });

    views.selectData();

    transactions.initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "SOMETHING ELSE", "", -1.00, "Clothes")
      .add("10/01/2006", TransactionType.PRELEVEMENT, "MENU 14", "", -1.00)
      .check();
    transactions.categorizePopup(0);
    views.checkCategorizationSelected();
    categorization.checkShowsSelectedMonthsOnly();
    categorization.checkSelectedTableRow("SOMETHING ELSE");
  }

  public void testNavigatingInCategorizationIsDisabledForMirrorAndCreatedFromSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -100.0, "Virement")
      .load();

    savingsAccounts.createSavingsAccount("Epargne", 1000.);

    transactions.categorize(0);
    categorization
      .selectSavings().createSeries()
      .setName("Epargne")
      .setFromAccount("Main accounts")
      .setToAccount("Epargne")
      .validate();

    transactions
      .initContent()
      .add("11/01/2006", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne")
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne")
      .check();

    transactions.checkCategorizeIsDisabled(0);

    categorization.selectTransaction("Virement")
      .setUncategorized();

    savingsView.editSeries("Epargne", "Epargne")
      .setName("NEW NAME FOR EPARGNE")
      .setFromAccount("External account")
      .selectAllMonths()
      .setAmount("100")
      .validate();

    transactions
      .initContent()
      .add("11/01/2006", TransactionType.VIREMENT, "NEW NAME FOR EPARGNE", "", 100.00, "NEW NAME FOR EPARGNE")
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Virement", "", -100.00)
      .check();

    transactions.checkCategorizeIsDisabled(0);
  }

  public void testMultiCategorization() throws Exception {

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00)
      .check();

    transactions.categorize(1, 3);
    views.checkCategorizationSelected();
    categorization.initContent()
      .add("01/05/2006", "", "essence", -70.00)
      .add("03/05/2006", "", "peage", -30.00)
      .check();
    categorization.checkSelectedTableRows(0, 1);
    categorization.checkCustomFilterVisible(true);
    categorization.selectExtras().selectNewSeries("Leisures");

    categorization.clearCustomFilter();
    categorization.initContent()
      .add("01/05/2006", "Leisures", "essence", -70.00)
      .add("06/05/2006", "", "nounou", -100.00)
      .add("03/05/2006", "Leisures", "peage", -30.00)
      .add("02/05/2006", "", "sg", -200.00)
      .check();
    categorization.checkNoTransactionSelected();

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, "Leisures")
      .add("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, "Leisures")
      .check();
  }

  public void testSeriesTooltip() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder
      .init(this)
      .addTransaction("2006/05/11", -1.0, "Something else")
      .addTransaction("2006/05/10", -1.0, "Menu 14")
      .load();

    timeline.selectMonth("2006/05");

    views.selectData();
    transactions.checkSeriesTooltipContains("SOMETHING ELSE", "Click to categorize this operation");
    transactions.categorize("SOMETHING ELSE");

    views.selectCategorization();
    categorization.setNewVariable("SOMETHING ELSE", "Clothes", -1.);
    categorization.editSeries("Clothes").setDescription("Stuff to dress with").validate();

    views.back();
    views.checkDataSelected();
    transactions.checkSeries("SOMETHING ELSE", "Clothes");
    transactions.checkSeriesTooltipContains("SOMETHING ELSE", "Stuff to dress with");

    timeline.selectMonth("2006/06");
    transactions
      .showPlannedTransactions()
      .checkSeriesTooltipContains("Planned: Clothes", "Stuff to dress with");
  }

  public void testFutureBalance() throws Exception {

    categorization.setNewVariable("essence", "Voiture", -70.);
    categorization.setNewRecurring("nounou", "Nounou");

    mainAccounts.changePosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 500, "nounou");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("04/07/2006", "Planned: Nounou", -100.00, "Nounou", 160.00, "Main accounts")
      .add("04/07/2006", "Planned: Voiture", -70.00, "Voiture", 260.00, "Main accounts")
      .add("04/06/2006", "Planned: Nounou", -100.00, "Nounou", 330.00, "Main accounts")
      .add("04/06/2006", "Planned: Voiture", -70.00, "Voiture", 430.00, "Main accounts")
      .add("06/05/2006", "NOUNOU", -100.00, "Nounou", 500.00, 500.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/05/2006", "PEAGE", -30.00, "To categorize", 600.00, 600.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("02/05/2006", "SG", -200.00, "To categorize", 630.00, 630.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("01/05/2006", "ESSENCE", -70.00, "Voiture", 830.00, 830.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();
  }

  public void testDeleteATransactionASplittedTransationAndALastImportedTransaction() throws Exception {
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00)
      .check();

    transactions.delete("peage", "Removing one operation");
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00)
      .check();

    transactions.delete("nounou");

    categorization.selectTransaction("sg");
    transactionDetails.split("100", "sg2");
    transactions.initContent()
      .add("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -100.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "sg", "sg2", -100.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00)
      .check();

    transactions.delete("sg", "Removing one operation");
    transactions.initContent()
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00)
      .check();

    categorization.selectTransactions("essence");
    transactionDetails.split("30", "essence2");
    transactions.initContent()
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -40.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "essence2", -30.00)
      .check();

    transactions.deleteTransactionWithNote("essence2", "Removing one part");
    transactions.initContent()
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00)
      .check();
  }

  public void testDeleteATransactionWithMirrorSavings() throws Exception {
    savingsAccounts.createSavingsAccount("Epargne LCL", 1000.00);
    categorization
      .selectTransactions("sg")
      .selectSavings().createSeries()
      .setName("Epargne")
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .validate();

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("02/05/2006", TransactionType.VIREMENT, "SG", "", 200.00, "Epargne")
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "", -200.00, "Epargne")
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00)
      .check();

    transactions.checkDeletionForbidden(2, "Operations created by a series cannot be removed");

    transactions.delete(3);
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00)
      .check();
  }

  public void testCannotDeletePlannedTransactions() throws Exception {
    categorization.setNewVariable("essence", "Voiture", -70.00);

    timeline.selectAll();
    transactions.showPlannedTransactions();
    transactions.initContent()
      .add("04/07/2006", TransactionType.PLANNED, "Planned: Voiture", "", -70.00, "Voiture")
      .add("04/06/2006", TransactionType.PLANNED, "Planned: Voiture", "", -70.00, "Voiture")
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00, "Voiture")
      .check();

    transactions.checkDeletionForbidden(new int[]{1, 2}, "Planned operation cannot be deleted");

    transactions.delete(5, "Removing one operation");
    transactions.initContent()
      .add("11/07/2006", TransactionType.PLANNED, "Planned: Voiture", "", -70.00, "Voiture")
      .add("11/06/2006", TransactionType.PLANNED, "Planned: Voiture", "", -70.00, "Voiture")
      .add("11/05/2006", TransactionType.PLANNED, "Planned: Voiture", "", -70.00, "Voiture")
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "", -200.00)
      .check();
  }

  public void testToggleShowPlannedTransactions() throws Exception {
    categorization.setNewVariable("essence", "Voiture", -70.00);
    categorization.setNewRecurring("nounou", "Nounou");

    views.selectData();
    timeline.selectAll();

    transactions.checkShowsPlannedTransaction(false);
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00, "Nounou")
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00, "Voiture")
      .check();

    transactions.showPlannedTransactions();
    transactions.initContent()
      .add("04/07/2006", TransactionType.PLANNED, "Planned: Nounou", "", -100.00, "Nounou")
      .add("04/07/2006", TransactionType.PLANNED, "Planned: Voiture", "", -70.00, "Voiture")
      .add("04/06/2006", TransactionType.PLANNED, "Planned: Nounou", "", -100.00, "Nounou")
      .add("04/06/2006", TransactionType.PLANNED, "Planned: Voiture", "", -70.00, "Voiture")
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00, "Nounou")
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00, "Voiture")
      .check();

    transactions.hidePlannedTransactions();
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00, "Nounou")
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00, "Voiture")
      .check();

    transactions.showPlannedTransactions();
    transactions.initContent()
      .add("04/07/2006", TransactionType.PLANNED, "Planned: Nounou", "", -100.00, "Nounou")
      .add("04/07/2006", TransactionType.PLANNED, "Planned: Voiture", "", -70.00, "Voiture")
      .add("04/06/2006", TransactionType.PLANNED, "Planned: Nounou", "", -100.00, "Nounou")
      .add("04/06/2006", TransactionType.PLANNED, "Planned: Voiture", "", -70.00, "Voiture")
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00, "Nounou")
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00, "Voiture")
      .check();
  }

  public void testCopyToClipboard() throws Exception {

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "", -200.00)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00)
      .check();

    transactions.copy(0, 2);
    
    Assert.assertEquals("Operation date\tBank date\tSeries\tLabel\tAmount\tNote\tAccount position\tTotal position\tAccount\n" +
                        "2006/05/06\t2006/05/06\t\tNOUNOU\t-100.00\tnourrice\t0.00\t0.00\tAccount n. 00001123\n" +
                        "2006/05/02\t2006/05/02\t\tSG\t-200.00\t\t130.00\t130.00\tAccount n. 00001123",
                        Clipboard.getContentAsText().trim());


  }
}