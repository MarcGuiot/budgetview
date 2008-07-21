package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.transactions.TransactionView;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.uispec4j.Table;
import org.uispec4j.Trigger;
import org.uispec4j.interception.PopupMenuInterceptor;

import java.awt.event.InputEvent;

public class TransactionViewTest extends LoggedInFunctionalTestCase {
  private Table table;

  protected void setUp() throws Exception {
    super.setUp();
    OfxBuilder
      .init(this)
      .addTransactionWithNote("2006/05/01", -70.00, "essence", "frais pro", MasterCategory.TRANSPORTS)
      .addTransactionWithNote("2006/05/03", -30.00, "peage", "", MasterCategory.TRANSPORTS)
      .addTransactionWithNote("2006/05/02", -200.00, "cic", "", MasterCategory.BANK)
      .addTransactionWithNote("2006/05/06", -100.00, "nounou", "nourrice", MasterCategory.HOUSE, MasterCategory.EDUCATION)
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
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "interets", -200.00, MasterCategory.BANK)
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
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
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
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "frais pro", -200.00, MasterCategory.BANK)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testEditingWhileAColumnIsSorted() throws Exception {
    table.getHeader().click(1);

    transactions.initContent()
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    enterNote(2, "garagiste");

    transactions.initContent()
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "garagiste", -30.00, MasterCategory.TRANSPORTS)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    enterNote(3, "essence");

    transactions.initContent()
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "garagiste", -30.00, MasterCategory.TRANSPORTS)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "essence", -70.00, MasterCategory.TRANSPORTS)
      .check();

    enterNote(1, "travaux");

    transactions.initContent()
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "travaux", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "garagiste", -30.00, MasterCategory.TRANSPORTS)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "essence", -70.00, MasterCategory.TRANSPORTS)
      .check();
  }

  public void testCategorizeWithKeyboard() throws Exception {
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    transactions.assignCategoryViaKeyboard(MasterCategory.BANK, InputEvent.ALT_MASK, 1);

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.BANK)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    transactions.assignCategoryViaKeyboard(MasterCategory.TRANSPORTS, InputEvent.ALT_MASK, 1);

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    transactions.assignCategoryViaKeyboard(MasterCategory.TAXES, InputEvent.ALT_MASK, 3);

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TAXES)
      .check();
  }

  public void testMultiCategorization() throws Exception {
    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.TRANSPORTS)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.TRANSPORTS)
      .check();

    transactions.assignCategory(MasterCategory.BEAUTY, 1, 3);

    transactions.initContent()
      .add("06/05/2006", TransactionType.PRELEVEMENT, "nounou", "nourrice", -100.00, MasterCategory.EDUCATION, MasterCategory.HOUSE)
      .add("03/05/2006", TransactionType.PRELEVEMENT, "peage", "", -30.00, MasterCategory.BEAUTY)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "cic", "", -200.00, MasterCategory.BANK)
      .add("01/05/2006", TransactionType.PRELEVEMENT, "essence", "frais pro", -70.00, MasterCategory.BEAUTY)
      .check();
  }

  private void enterNote(int row, String note) {
    table.editCell(row, TransactionView.NOTE_COLUMN_INDEX, note, true);
  }
}