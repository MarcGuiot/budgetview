package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SplitDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class TransactionSplittingTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2006/01");
    super.setUp();
    views.selectCategorization();
    operations.hideSignposts();
  }

  public void testStandardUsage() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan")
      .load();

    categorization.setNewVariable("Auchan", "Food");
    transactionDetails.checkSplitButtonAvailable();

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -20.00, ""},
      })
      .enterAmount("12.50")
      .enterNote("DVD")
      .validateAndClose();

    categorization
      .initContent()
      .add("15/01/2006", "Food", "Auchan", -7.5)
      .add("15/01/2006", "", "Auchan", -12.50)
      .check();
    categorization.checkSelectedTableRow(1);
    transactionDetails.checkNote("DVD");
    categorization.selectTableRow(1).selectVariable().selectNewSeries("Leisures");

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -7.50, ""},
        {"Leisures", "Auchan", -12.50, "DVD"},
      })
      .checkSelectedTableRow(1)
      .enterAmount("2.50")
      .enterNote("Youth Elixir")
      .validateAndClose();

    categorization
      .initContent()
      .add("15/01/2006", "Food", "Auchan", -5.00)
      .add("15/01/2006", "Leisures", "Auchan", -12.50)
      .add("15/01/2006", "", "Auchan", -2.50)
      .check();
    categorization.checkSelectedTableRow(2);
    transactionDetails.checkNote("Youth Elixir");
    categorization.setNewVariable(2, "Beauty");

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
    categorization.selectVariable().selectNewSeries("Food");
    transactionDetails.openSplitDialog()
      .enterAmount("12.50")
      .close();

    categorization
      .initContent()
      .add("15/01/2006", "Food", "Auchan", -20.0)
      .check();
  }

  public void testDialogCanBeReusedAfterACancel() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan")
      .load();

    categorization.setNewVariable("Auchan", "Food");
    transactionDetails.openSplitDialog()
      .close();

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -20.00, ""},
      })
      .enterAmount("12.50")
      .enterNote("DVD")
      .validateAndClose();

    categorization
      .initContent()
      .add("15/01/2006", "Food", "Auchan", -7.5)
      .add("15/01/2006", "", "Auchan", -12.50)
      .check();

    categorization.selectTableRow(1);
    transactionDetails.checkNote("DVD");
  }

  public void testSplittingASplitPart() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount("12.50")
      .validateAndClose();

    categorization
      .initContent()
      .add("15/01/2006", "Food", "Auchan", -7.50)
      .add("15/01/2006", "", "Auchan", -12.50)
      .check();

    categorization.setNewVariable(1, "Leisures");
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {"Food", "Auchan", -7.50, ""},
        {"Leisures", "Auchan", -12.50, ""},
      })
      .enterAmount("2.50")
      .validateAndClose();

    categorization
      .initContent()
      .add("15/01/2006", "Food", "Auchan", -5.00)
      .add("15/01/2006", "Leisures", "Auchan", -12.50)
      .add("15/01/2006", "", "Auchan", -2.50)
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
      .validateAndClose();

    categorization
      .initContent()
      .add("15/01/2006", "Food", "Auchan", -7.50)
      .add("15/01/2006", "", "Auchan", -12.50)
      .check();
  }

  public void testEnteringSeveralAmounts() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount("  2.50 1 4. 5.000 ")
      .validateAndClose();

    categorization
      .initContent()
      .add("15/01/2006", "Food", "Auchan", -7.50)
      .add("15/01/2006", "", "Auchan", -12.50)
      .check();
  }

  public void testCommasAreConvertedIntoDots() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount(" 12,50 ")
      .validateAndClose();

    categorization
      .initContent()
      .add("15/01/2006", "Food", "Auchan", -7.50)
      .add("15/01/2006", "", "Auchan", -12.50)
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
      .checkErrorOnOk("Invalid amount")
      .close();
  }

  public void testAmountGreaterThanInitialTransactionAmount() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount("100")
      .checkErrorOnOk("Amount must be less than 20.00")
      .enterAmount("")
      .checkNoError()
      .enterAmount("21")
      .checkErrorOnAdd("Amount must be less than 20.00")
      .close();

    categorization
      .initContent()
      .add("15/01/2006", "Food", "Auchan", -20.0)
      .check();
  }

  public void testRemovingSplitParts() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", "Food")
      .enterAmount("5")
      .enterNote("DVD")
      .validateAndClose();

    transactionDetails.openSplitDialog()
      .enterAmount("8")
      .enterNote("Youth Elixir")
      .add()
      .enterAmount("3")
      .enterNote("Cool Sticker")
      .validateAndClose();

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
      .validateAndClose();

    categorization
      .initContent()
      .add("15/01/2006", "Food", "Auchan", -13.0)
      .add("15/01/2006", "", "Auchan", -7.0)
      .check();

    categorization.checkSelectedTableRow(1);
    transactionDetails.checkNote("Another DVD");
  }

  public void testDeletingCategorizedSplitPartWithUncategorizedSource() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan")
      .load();

    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .enterAmount("12.50")
      .validateAndClose();

    categorization.selectTableRow(1).selectVariable().selectNewSeries("Courses");

    transactionDetails.openSplitDialog()
      .deleteRow(1)
      .validateAndClose();

    categorization
      .initContent()
      .add("15/01/2006", "", "Auchan", -20.0)
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
      .validateAndClose();

    categorization
      .initContent()
      .add("15/01/2006", "", "Auchan", -20.0)
      .add("10/01/2006", "", "Monoprix", -40.0)
      .add("10/01/2006", "", "Monoprix", -10.0)
      .check();

    transactionDetails.openSplitDialog()
      .deleteRow(1)
      .validateAndClose();

    categorization
      .initContent()
      .add("15/01/2006", "", "Auchan", -20.0)
      .add("10/01/2006", "", "Monoprix", -50.0)
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

    categorization.setNewVariable("Auchan", "Food");
    categorization.setVariable("Monoprix", "Food");

    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .enterAmount("12.50")
      .validateAndClose();

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
      .validateAndClose();
    
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
      .validateAndClose();
    
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

    categorization.setNewVariable(0, series);
    return transactionDetails.openSplitDialog();
  }
}
