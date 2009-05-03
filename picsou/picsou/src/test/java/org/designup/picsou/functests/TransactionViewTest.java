package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.uispec4j.Table;
import org.uispec4j.assertion.UISpecAssert;

public class TransactionViewTest extends LoggedInFunctionalTestCase {
  private Table table;

  protected void setUp() throws Exception {
    setCurrentMonth("2006/07");
    super.setUp();
    OfxBuilder
      .init(this)
      .addTransactionWithNote("2006/05/01", -70.00, "essence", "frais pro", MasterCategory.TRANSPORTS)
      .addTransactionWithNote("2006/05/03", -30.00, "peage", "", MasterCategory.TRANSPORTS)
      .addTransactionWithNote("2006/05/02", -200.00, "sg", "", MasterCategory.BANK)
      .addTransactionWithNote("2006/05/06", -100.00, "nounou", "nourrice", MasterCategory.EDUCATION)
      .load();
    table = transactions.getTable();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    table = null;
  }

  public void testEditingANoteByCellSelection() throws Exception {
    categories.select(MasterCategory.BANK);
    enterNote(0, "interets");
    transactions.initContent()
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "sg", "interets", -200.00, MasterCategory.BANK)
      .check();
  }

  public void testEditingWhileAColumnIsSorted() throws Exception {
    table.getHeader().click(1);

    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    enterNote(1, "garagiste");

    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "garagiste", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    enterNote(3, "essence");

    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "garagiste", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "essence", -70.00, MasterCategory.TRANSPORTS)
      .check();

    enterNote(0, "travaux");

    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "travaux", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "garagiste", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "essence", -70.00, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testFullLine() throws Exception {
    views.selectCategorization();
    categorization.setEnvelope("essence", "Voiture", MasterCategory.TRANSPORTS, true);

    views.selectData();
    Table table = transactions.getTable();
    UISpecAssert.assertTrue(
      table.rowEquals(table.getRowIndex(TransactionView.LABEL_COLUMN_INDEX, "ESSENCE"),
                      new String[]{"01/05/2006", "01/05/2006", "(prelevement)Voiture", "Transports",
                                   "ESSENCE", "-70.00", "frais pro", "330.00", "330.00"}));
  }

  public void testNavigatingToCategorizationView() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.0, "Something else")
      .addTransaction("2006/01/10", -1.0, "Menu 14")
      .load();

    timeline.selectMonth("2006/01");

    transactions.categorize(0);
    views.checkCategorizationSelected();
    categorization.showSelectedMonthsOnly();
    categorization.checkTable(new Object[][]{
      {"10/01/2006", "", "Menu 14", -1.0},
      {"11/01/2006", "", "Something else", -1.0}
    });
    categorization.checkSelectedTableRows(1);
    categorization.checkCustomFilterVisible(false);

    views.back();
    views.checkDataSelected();
    transactions.checkSelectedRow(0);

    views.forward();
    views.checkCategorizationSelected();
  }

  public void testNavigatingInCategorizationIsDisabledForMirrorAndCreatedFromSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -100.0, "Virement")
      .load();

    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne", 1000);

    transactions.categorize(0);
    categorization
      .selectSavings()
      .createSavingsSeries()
      .setName("Epargne")
      .setCategories(MasterCategory.SAVINGS)
      .setFromAccount("Main accounts")
      .setToAccount("Epargne")
      .validate();

    views.selectData();
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .check();

    transactions.checkCategorizeIsDisabled(0);

    views.selectSavings();
    savingsView.editSavingsSeries("Epargne", "Epargne")
      .setName("NEW NAME FOR EPARGNE")
      .setFromAccount("External account")
      .selectAllMonths()
      .setAmount("100")
      .validate();

    views.selectData();
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.VIREMENT, "NEW NAME FOR EPARGNE", "", 100.00, "NEW NAME FOR EPARGNE", MasterCategory.SAVINGS)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Virement", "", -100.00)
      .check();

    transactions.checkCategorizeIsDisabled(0);
  }

  public void testMultiCategorization() throws Exception {

    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    transactions.categorize(1, 3);
    views.checkCategorizationSelected();
    categorization.initContent()
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .check();
    categorization.checkSelectedTableRows(0, 1);
    categorization.checkCustomFilterVisible(true);
    categorization.selectOccasional();
    categorization.selectOccasionalSeries(MasterCategory.LEISURES);

    categorization.clearCustomFilter();
    categorization.initContent()
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.LEISURES)
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.LEISURES)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00, MasterCategory.BANK)
      .check();
    categorization.checkNoTransactionSelected();

    views.selectData();
    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.LEISURES)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.LEISURES)
      .check();
  }

  public void testFutureBalance() throws Exception {
    views.selectCategorization();
    categorization.setEnvelope("essence", "Voiture", MasterCategory.TRANSPORTS, true);
    categorization.setRecurring("nounou", "Nounou", MasterCategory.EDUCATION, true);
    views.selectHome();
    mainAccounts.changeBalance(OfxBuilder.DEFAULT_ACCOUNT_NAME, 500, "nounou");
    views.selectData();
    timeline.selectAll();
    transactions.initAmountContent()
      .add("Planned: Nounou", -100.00, -300.0)
      .add("Planned: Voiture", -70.00, -200.0)
      .add("Planned: Occasional", -230.00, -130.0)
      .add("Planned: Nounou", -100.00, 100.0)
      .add("Planned: Voiture", -70.00, 200.0)
      .add("Planned: Occasional", -230.00, 270.0)
      .add("NOUNOU", -100.00, 500.00, 500.00)
      .add("PEAGE", -30.00, 600.00, 600.00)
      .add("SG", -200.00, 630.00, 630.00)
      .add("ESSENCE", -70.00, 830.00, 830.00)
      .check();
  }

  public void testDeleteATransactionASplitedTransationAndALastImportedTransaction() throws Exception {
    views.selectData();
    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();
    transactions.delete("peage")
      .validate();
    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();
    transactions.delete("nounou")
      .validate();
    views.selectCategorization();
    categorization.selectTableRows("sg");
    transactionDetails.split("100", "sg2");
    transactions.initContent()
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -100.00, MasterCategory.BANK)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "sg", "sg2", -100.00, MasterCategory.NONE)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();
    views.selectData();
    transactions.delete("sg").validate();
    views.selectCategorization();
    categorization.selectTableRows("essence");
    transactionDetails.split("30", "essence2");
    views.selectData();
    transactions.delete("essence2")
      .checkMessageContains("Use the split function")
      .validate();
    transactions.initContent()
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -40.00, "Occasional", MasterCategory.TRANSPORTS)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "essence2", -30.00)
      .check();
  }

  public void testDeleteATransactionWithMirrorSavings() throws Exception {
    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne LCL", 1000);
    views.selectCategorization();
    categorization
      .selectTableRows("sg")
      .selectSavings()
      .createSavingsSeries()
      .setName("Epargne")
      .setCategories(MasterCategory.SAVINGS)
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .validate();
    views.selectData();
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00, "Occasional", MasterCategory.EDUCATION)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00, "Occasional", MasterCategory.TRANSPORTS)
      .add("02/05/2006", TransactionType.VIREMENT, "SG", "", 200.00, "Epargne", MasterCategory.SAVINGS)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "SG", "", -200.00, "Epargne", MasterCategory.SAVINGS)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00, "Occasional", MasterCategory.TRANSPORTS)
      .check();
    transactions.delete(2)
      .checkMessageContains("Operation created by a series can not be removed").validate();

    transactions.delete(3).validate();
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "NOUNOU", "nourrice", -100.00, "Occasional", MasterCategory.EDUCATION)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "PEAGE", "", -30.00, "Occasional", MasterCategory.TRANSPORTS)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "ESSENCE", "frais pro", -70.00, "Occasional", MasterCategory.TRANSPORTS)
      .check();
  }


  private void enterNote(int row, String note) {
    table.editCell(row, TransactionView.NOTE_COLUMN_INDEX, note, true);
  }
}