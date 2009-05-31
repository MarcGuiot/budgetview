package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SplitDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class TransactionSplittingTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2006/01");
    super.setUp();
    views.selectCategorization();
  }

  public void testStandardUsage() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan")
      .load();

    categorization.setNewEnvelope("Auchan", "Food");
    transactionDetails.checkSplitButtonAvailable();

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -20.00, ""},
      })
      .enterAmount("12.50")
      .enterNote("DVD")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.5, "Food")
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "DVD", -12.50)
      .check();
    categorization.checkSelectedTableRow(1);
    transactionDetails.checkNote("DVD");
    categorization.selectTableRow(1).selectEnvelopes().selectNewSeries("Leisures");

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -7.50, ""},
        {"Leisures", "Auchan", -12.50, "DVD"},
      })
      .checkSelectedTableRow(1)
      .enterAmount("2.50")
      .enterNote("Youth Elixir")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -5.00, "Food")
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "DVD", -12.50, "Leisures")
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "Youth Elixir", -2.50)
      .check();
    categorization.checkSelectedTableRow(2);
    transactionDetails.checkNote("Youth Elixir");
    categorization.setNewEnvelope(2, "Beauty");

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -5.0, ""},
        {"Leisures", "Auchan", -12.50, "DVD"},
        {"Beauty", "Auchan", -2.50, "Youth Elixir"},
      })
      .checkSelectedTableRow(2)
      .close();
  }

  public void testSplittedTransactionsAreHighlightedInTheCategorizationTable() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -200.0, "Auchan")
      .addTransaction("2006/01/20", -20.0, "Monops")
      .load();

    categorization.selectTableRow(0);
    transactionDetails.split("25", "1st");
    categorization.selectTableRow(0);
    transactionDetails.split("100", "1st");

    categorization.selectTableRow(0);
    categorization.checkTable(new Object[][]{
      {"15/01/2006", "", "Auchan", -75.0},
      {"15/01/2006", "", "Auchan", -25.0},
      {"15/01/2006", "", "Auchan", -100.0},
      {"20/01/2006", "", "Monops", -20.0}
    });

    categorization.selectTableRow(1);
    categorization.checkTableBackground(
      "C3D5F7",
      "3C6CC6",
      "DCE6FA",
      "F5F5F6"
    );
  }

  public void testCancelReallyCancels() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan")
      .load();

    categorization.selectTableRow(0);
    categorization.selectEnvelopes().selectNewSeries("Food");
    transactionDetails.openSplitDialog()
      .enterAmount("12.50")
      .close();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0, "Food")
      .check();
  }

  public void testDialogCanBeReusedAfterACancel() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan")
      .load();

    categorization.setNewEnvelope("Auchan", "Food");
    transactionDetails.openSplitDialog()
      .close();

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -20.00, ""},
      })
      .enterAmount("12.50")
      .enterNote("DVD")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.5, "Food")
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "DVD", -12.50)
      .check();
  }

  public void testSplittingASplitPart() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount("12.50")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, "Food")
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50)
      .check();

    categorization.setNewEnvelope(1, "Leisures");
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -7.50, ""},
        {"Leisures", "Auchan", -12.50, ""},
      })
      .enterAmount("2.50")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -5.00, "Food")
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, "Leisures")
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -2.50)
      .check();
  }

  public void testAddButtonUnavailableIfNoAmountIsEntered() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .assertOkDisabled()
      .close();
  }

  public void testAmountIsSubtractedWhateverTheSign() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount("12.50")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, "Food")
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50)
      .check();
  }

  public void testEnteringSeveralAmounts() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount("  2.50 1 4. 5.000 ")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, "Food")
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50)
      .check();
  }

  public void testCommasAreConvertedIntoDots() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount(" 12,50 ")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, "Food")
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50)
      .check();
  }

  public void testAmountFieldFiltersChars() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount("- a 1à2$.+5-0= 3 ")
      .checkAmount("  12.50 3 ")
      .close();
  }

  public void testInvalidValueInAmountField() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount(" . . ")
      .checkOkFailure("Invalid amount")
      .close();
  }

  public void testAmountGreaterThanInitialTransactionAmount() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount("100")
      .checkOkFailure("Amount must be less than 20")
      .close();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0, "Food")
      .check();
  }

  public void testRemovingSplitParts() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount("5")
      .enterNote("DVD")
      .validate();

    transactionDetails.openSplitDialog()
      .enterAmount("8")
      .enterNote("Youth Elixir")
      .validate();

    transactionDetails.openSplitDialog()
      .enterAmount("3")
      .enterNote("Cool Sticker")
      .validate();

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -4.0, ""},
        {"", "Auchan", -5.0, "DVD"},
        {"", "Auchan", -8.0, "Youth Elixir"},
        {"", "Auchan", -3.0, "Cool Sticker"},
      })
      .deleteRow(1)
      .deleteRow(2)
      .checkTable(new Object[][]{
        {"Food", "Auchan", -12.0, ""},
        {"", "Auchan", -8.0, "Youth Elixir"},
      })
      .deleteRow(1)
      .checkTable(new Object[][]{
        {"Food", "Auchan", -20.0, ""}
      })
      .enterAmount("7")
      .enterNote("Another DVD")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -13.0, "Food")
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "Another DVD", -7.0)
      .check();

    categorization.checkSelectedTableRow(1);
  }

  public void testDeletingCategorizedSplitPartWithUncategorizedSource() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan")
      .load();

    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .enterAmount("12.50")
      .validate();

    categorization.selectTableRow(1).selectEnvelopes().selectNewSeries("Courses");

    transactionDetails.openSplitDialog()
      .deleteRow(1)
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0)
      .check();
  }

  public void testSourceTransactionIsSelectedWhenRemovingAllSplits() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan")
      .addTransaction("2006/01/10", -50.0, "Monoprix")
      .load();

    categorization.selectTableRow(1);
    transactionDetails.openSplitDialog()
      .enterAmount("10")
      .enterNote("CD")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Monoprix", "", -40.0)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Monoprix", "CD", -10.0)
      .check();

    transactionDetails.openSplitDialog()
      .deleteRow(1)
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Monoprix", "", -50.0)
      .check();
    categorization.checkSelectedTableRow(1);

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"",	"MONOPRIX",	-50.00,	"",""}
      })
      .close();
  }

  public void testCannotRemoveSplitSource() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .checkDeleteEnabled(0, false)
      .enterAmount("12.50")
      .enterNote("DVD")
      .checkDeleteEnabled(0, false)
      .close();
  }

  public void testOpeningTheDialogSeveralTimes() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan")
      .addTransaction("2006/01/10", -50.0, "Monoprix")
      .load();

    categorization.setNewEnvelope("Auchan", "Food");
    categorization.setEnvelope("Monoprix", "Food");

    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .enterAmount("12.50")
      .validate();

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -7.50, ""},
        {"", "Auchan", -12.50, ""},
      })
      .enterNoteInTable(0, "note 0")
      .enterNoteInTable(1, "note 1")
      .checkTable(new Object[][]{
        {"Food", "Auchan", -7.50, "note 0"},
        {"", "Auchan", -12.50, "note 1"},
      })
      .validate();
    
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -7.50, "note 0"},
        {"", "Auchan", -12.50, "note 1"},
      })
      .close();

    categorization.selectTableRow(2);
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Monoprix", -50.0, ""},
      })
      .enterAmount("22")
      .validate();
    
    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -7.50, "note 0"},
        {"", "Auchan", -12.50, "note 1"},
      })
      .close();
  }

  private SplitDialogChecker openDialogWith(String date, double amount, String label, String series) {
    OfxBuilder
      .init(this)
      .addTransaction(date, amount, label)
      .load();

    categorization.setNewEnvelope(0, series);
    return transactionDetails.openSplitDialog();
  }
}
