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
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    categorization.selectTableRow(0);
    transactionDetails.checkSplitButtonAvailable();

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -20.00, ""},
      })
      .enterAmount("12.50")
      .enterNote("DVD")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.5, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "DVD", -12.50)
      .check();
    categorization.checkSelectedTableRow(1);
    transactionDetails.checkNote("DVD");
    categorization.setOccasional(1, MasterCategory.LEISURES);

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, "DVD"},
      })
      .checkSelectedTableRow(1)
      .enterAmount("2.50")
      .enterNote("Youth Elixir")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -5.00, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "DVD", -12.50, MasterCategory.LEISURES)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "Youth Elixir", -2.50)
      .check();
    categorization.checkSelectedTableRow(2);
    transactionDetails.checkNote("Youth Elixir");
    categorization.setOccasional(2, MasterCategory.BEAUTY);

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -5.0, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, "DVD"},
        {MasterCategory.BEAUTY, "Auchan", -2.50, "Youth Elixir"},
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
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .enterAmount("12.50")
      .close();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0, MasterCategory.FOOD)
      .check();
  }

  public void testDialogCanBeReusedAfterACancel() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    categorization.selectTableRow(0);

    transactionDetails.openSplitDialog()
      .close();

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -20.00, ""},
      })
      .enterAmount("12.50")
      .enterNote("DVD")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.5, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "DVD", -12.50)
      .check();
  }

  public void testSplittingASplitPart() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .enterAmount("12.50")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50)
      .check();

    categorization.setOccasional(1, MasterCategory.LEISURES);
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, ""},
      })
      .enterAmount("2.50")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -5.00, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, MasterCategory.LEISURES)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -2.50)
      .check();
  }

  public void testAddButtonUnavailableIfNoAmountIsEntered() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .assertOkDisabled()
      .close();
  }

  public void testAmountIsSubtractedWhateverTheSign() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .enterAmount("12.50")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50)
      .check();
  }

  public void testEnteringSeveralAmounts() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .enterAmount("  2.50 1 4. 5.000 ")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50)
      .check();
  }

  public void testCommasAreConvertedIntoDots() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .enterAmount(" 12,50 ")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50)
      .check();
  }

  public void testAmountFieldFiltersChars() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .enterAmount("- a 1à2$.+5-0= 3 ")
      .checkAmount("  12.50 3 ")
      .close();
  }

  public void testInvalidValueInAmountField() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .enterAmount(" . . ")
      .checkOkFailure("Invalid amount")
      .close();
  }

  public void testAmountGreaterThanInitialTransactionAmount() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .enterAmount("100")
      .checkOkFailure("Amount must be less than 20")
      .close();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0, MasterCategory.FOOD)
      .check();
  }

  public void testRemovingSplitParts() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
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
        {MasterCategory.FOOD, "Auchan", -4.0, ""},
        {MasterCategory.NONE, "Auchan", -5.0, "DVD"},
        {MasterCategory.NONE, "Auchan", -8.0, "Youth Elixir"},
        {MasterCategory.NONE, "Auchan", -3.0, "Cool Sticker"},
      })
      .deleteRow(1)
      .deleteRow(2)
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -12.0, ""},
        {MasterCategory.NONE, "Auchan", -8.0, "Youth Elixir"},
      })
      .deleteRow(1)
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -20.0, ""}
      })
      .enterAmount("7")
      .enterNote("Another DVD")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -13.0, MasterCategory.FOOD)
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

    categorization.setOccasional(1, MasterCategory.FOOD);

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
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .addTransaction("2006/01/10", -50.0, "Monoprix", MasterCategory.FOOD)
      .load();

    categorization.selectTableRow(1);
    transactionDetails.openSplitDialog()
      .enterAmount("10")
      .enterNote("CD")
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Monoprix", "", -40.0, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Monoprix", "CD", -10.0)
      .check();

    transactionDetails.openSplitDialog()
      .deleteRow(1)
      .validate();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Monoprix", "", -50.0, MasterCategory.FOOD)
      .check();
    categorization.checkSelectedTableRow(1);

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD,	"MONOPRIX",	-50.00,	"",""}	
      })
      .close();
  }

  public void testCannotRemoveSplitSource() throws Exception {
    openDialogWith("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .checkDeleteEnabled(0, false)
      .enterAmount("12.50")
      .enterNote("DVD")
      .checkDeleteEnabled(0, false)
      .close();
  }

  public void testOpeningTheDialogSeveralTimes() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .addTransaction("2006/01/10", -50.0, "Monoprix", MasterCategory.FOOD)
      .load();

    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .enterAmount("12.50")
      .validate();

    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, ""},
        {MasterCategory.NONE, "Auchan", -12.50, ""},
      })
      .enterNoteInTable(0, "note 0")
      .enterNoteInTable(1, "note 1")
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, "note 0"},
        {MasterCategory.NONE, "Auchan", -12.50, "note 1"},
      })
      .validate();
    
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, "note 0"},
        {MasterCategory.NONE, "Auchan", -12.50, "note 1"},
      })
      .close();

    categorization.selectTableRow(2);
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Monoprix", -50.0, ""},
      })
      .enterAmount("22")
      .validate();
    
    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, "note 0"},
        {MasterCategory.NONE, "Auchan", -12.50, "note 1"},
      })
      .close();
  }

  private SplitDialogChecker openDialogWith(String date, double amount,
                                            String label, final MasterCategory category) {
    OfxBuilder
      .init(this)
      .addTransaction(date, amount, label, category)
      .load();

    categorization.selectTableRow(0);
    return transactionDetails.openSplitDialog();
  }
}
