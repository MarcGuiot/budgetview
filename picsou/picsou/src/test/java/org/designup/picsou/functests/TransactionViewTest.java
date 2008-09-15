package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.uispec4j.Table;
import org.uispec4j.Trigger;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.PopupMenuInterceptor;
import junit.framework.Assert;

public class TransactionViewTest extends LoggedInFunctionalTestCase {
  private Table table;

  protected void setUp() throws Exception {
    super.setUp();
    OfxBuilder
      .init(this)
      .addTransactionWithNote("2006/05/01", -70.00, "essence", "frais pro", MasterCategory.TRANSPORTS)
      .addTransactionWithNote("2006/05/03", -30.00, "peage", "", MasterCategory.TRANSPORTS)
      .addTransactionWithNote("2006/05/02", -200.00, "cic", "", MasterCategory.BANK)
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
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "cic", "interets", -200.00, MasterCategory.BANK)
      .check();
  }

  public void testPopupIsShownIfAMatchingNoteAlreadyExists() throws Exception {
    PopupMenuInterceptor
      .run(new Trigger() {
        public void run() throws Exception {
          table.editCell(2, TransactionView.NOTE_COLUMN_INDEX, "o", false);
        }
      })
      .contentEquals("frais pro", "nourrice")
      .check();
  }

  public void testEditingANoteBySelectingAValueInThePopup() throws Exception {
    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    PopupMenuInterceptor
      .run(new Trigger() {
        public void run() throws Exception {
          table.editCell(2, TransactionView.NOTE_COLUMN_INDEX, "o", false);
        }
      })
      .getSubMenu("frais pro")
      .click();

    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "cic", "frais pro", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testEditingWhileAColumnIsSorted() throws Exception {
    table.getHeader().click(1);

    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    enterNote(1, "garagiste");

    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "garagiste", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    enterNote(3, "essence");

    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "garagiste", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "essence", -70.00, MasterCategory.TRANSPORTS)
      .check();

    enterNote(0, "travaux");

    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "travaux", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "garagiste", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "essence", -70.00, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testFullLine() throws Exception {
    views.selectCategorization();
    categorization.setEnvelope("essence", "Voiture", MasterCategory.TRANSPORTS, true);
    views.selectData();
    Table table = transactions.getTable();
    UISpecAssert.assertTrue(
      table.rowEquals(table.getRowIndex(TransactionView.LABEL_COLUMN_INDEX, "essence"),
                      new String[]{"01/05/2006", "01/05/2006", "(prelevement)Voiture", "Transports",
                                   "essence", "-70.00", "frais pro"}));
  }

  public void testMultiCategorization() throws Exception {

    Assert.fail("TODO : tester la navigation transactionView => Categorization");

    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    transactions.assignOccasionalSeries(MasterCategory.BEAUTY, 1, 3);

    transactions.initContent()
      .addOccasional("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION)
      .addOccasional("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.BEAUTY)
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.BEAUTY)
      .check();
  }

  public void testNavigatingToCategorizationView() throws Exception {

    Assert.fail("TODO navigation transactionView ==> Categorization");

    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.0, "Something else")
      .addTransaction("2006/01/10", -1.0, "Menu 14")
      .load();
    transactions.getTable().selectRow(0);
    transactions.assignCategoryWithoutSelection(MasterCategory.FOOD, 1);
    assertTrue(transactions.getTable().rowIsSelected(1));
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Something else", "", -1.0)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu 14", "", -1.0, MasterCategory.FOOD)
      .check();
  }

  private void enterNote(int row, String note) {
    table.editCell(row, TransactionView.NOTE_COLUMN_INDEX, note, true);
  }
}