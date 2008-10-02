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
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "sg", "", -200.00, MasterCategory.BANK)
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
      .addOccasional("02/05/2006", TransactionType.PRELEVEMENT, "sg", "frais pro", -200.00, MasterCategory.BANK)
      .addOccasional("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
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
      table.rowEquals(table.getRowIndex(TransactionView.LABEL_COLUMN_INDEX, "essence"),
                      new String[]{"01/05/2006", "01/05/2006", "(prelevement)Voiture", "Transports",
                                   "essence", "-70.00", "frais pro", "330.00", "330.00"}));
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
    categorization.checkTable(new Object[][]{
      {"10/01/2006", "", "Menu 14", -1.0},
      {"11/01/2006", "", "Something else", -1.0}
    });
    categorization.checkSelectedTableRows(1);
    categorization.checkCustomFilterVisible(false);
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

  public void testBalanceOfFuture() throws Exception {
    views.selectCategorization();
    categorization.setEnvelope("essence", "Voiture", MasterCategory.TRANSPORTS, true);
    categorization.setRecurring("nounou", "Nounou", MasterCategory.EDUCATION, true);
    views.selectHome();
    accounts.changeBalance(OfxBuilder.DEFAULT_ACCOUNT_ID, 500, "nounou");
    views.selectData();
    timeline.selectAll();
    transactions.initAmountContent()
      .add("Nounou", -100.00, 160.0)
      .add("Voiture", -70.00, 260.0)
      .add("Nounou", -100.00, 330.0)
      .add("Voiture", -70.00, 430.0)
      .add("nounou", -100.00, 500.00, 500.00)
      .add("peage", -30.00, 600.00, 600.00)
      .add("sg", -200.00, 630.00, 630.00)
      .add("essence", -70.00, 830.00, 830.00)
      .check();
  }

  private void enterNote(int row, String note) {
    table.editCell(row, TransactionView.NOTE_COLUMN_INDEX, note, true);
  }
}