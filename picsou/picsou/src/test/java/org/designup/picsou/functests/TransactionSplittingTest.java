package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SplitDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public abstract class TransactionSplittingTest extends LoggedInFunctionalTestCase {

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
      .ok();

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
      .ok();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -5.00, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "DVD", -12.50, MasterCategory.LEISURES)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "Youth Elixir", -2.50, MasterCategory.BEAUTY)
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

  public void testTransactionTableIsUpdatedOnlyWhenClickingOnOk() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    categorization.getTable().selectRow(0);
    SplitDialogChecker dialog = transactionDetails.openSplitDialog()
      .enterAmount("12.50")
      .enterNote("DVD")
      .selectOccasional(MasterCategory.LEISURES)
      .ok();

    categorization
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0)
      .check();

    dialog.close();

    categorization.initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "DVD", -12.50, MasterCategory.LEISURES)
      .check();
  }

  public void testCancelReallyCancels() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .enterAmount("12.50")
      .selectOccasional(MasterCategory.LEISURES)
      .ok()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, ""},
      })
      .close();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0, MasterCategory.FOOD)
      .check();
  }

  public void testSplittingASplitPart() throws Exception {
    openDialogWithSampleTransaction()
      .addOccasional("12.50", MasterCategory.LEISURES, "")
      .close();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, MasterCategory.LEISURES)
      .check();

    categorization.selectTableRow(1);
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, ""},
      })
      .addOccasional("2.50", MasterCategory.EQUIPMENT, "")
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -5.00, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, ""},
        {MasterCategory.EQUIPMENT, "Auchan", -2.50, ""},
      })
      .close();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -5.00, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, MasterCategory.LEISURES)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -2.50, MasterCategory.EQUIPMENT)
      .check();
  }

  public void testAddButtonUnavailableIfNoAmountIsEntered() throws Exception {
    openDialogWithSampleTransaction()
      .enterAmount("")
      .assertAddDisabled();
  }

  public void testNoCategorySelected() throws Exception {
    openDialogWithSampleTransaction()
      .enterAmount("12.50")
      .ok()
      .close();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, MasterCategory.NONE)
      .check();
  }

  public void testAmountIsSubtractedWhateverTheSign() throws Exception {
    openDialogWithSampleTransaction()
      .enterAmount("12.50")
      .ok()
      .close();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, MasterCategory.NONE)
      .check();
  }

  public void testEnteringSeveralAmounts() throws Exception {
    openDialogWithSampleTransaction()
      .enterAmount(" 2.50 1 4. 5.000 ")
      .selectOccasional(MasterCategory.LEISURES)
      .ok()
      .close();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, MasterCategory.LEISURES)
      .check();
  }

  public void testCommasAreConvertedIntoDots() throws Exception {
    openDialogWithSampleTransaction()
      .enterAmount(" 12,50 ")
      .selectOccasional(MasterCategory.LEISURES)
      .ok()
      .close();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, MasterCategory.LEISURES)
      .check();
  }

  public void testAmountFieldFiltersChars() throws Exception {
    openDialogWithSampleTransaction()
      .enterAmount("- a 1à2$.+5-0= 3 ")
      .checkAmount("  12.50 3 ");
  }

  public void testInvalidValueInAmountField() throws Exception {
    openDialogWithSampleTransaction()
      .enterAmount(" . . ")
      .checkErrorMessage("Invalid amount");
  }

  public void testAmountGreaterThanInitialTransactionAmount() throws Exception {
    openDialogWithSampleTransaction()
      .enterAmount("100")
      .checkErrorMessage("Amount must be less than 20")
      .close();
    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0, MasterCategory.FOOD)
      .check();
  }

  public void testTotalAmountGreaterThanInitialTransactionAmount() throws Exception {
    openDialogWithSampleTransaction()
      .enterAmount("12")
      .ok()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -8.0, ""},
        {MasterCategory.NONE, "Auchan", -12.0, ""},
      })
      .enterAmount("15")
      .checkErrorMessage("Amount must be less than 8")
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -8.0, ""},
        {MasterCategory.NONE, "Auchan", -12.0, ""},
      })
      .close();
  }

  public void testSplittingANonCategorizedTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan")
      .load();

    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .checkTable(new Object[][]{
        {MasterCategory.NONE, "Auchan", -20.0, ""},
      })
      .addOccasional("12", MasterCategory.LEISURES, "DVD")
      .checkTable(new Object[][]{
        {MasterCategory.NONE, "Auchan", -8.0, ""},
        {MasterCategory.LEISURES, "Auchan", -12.0, "DVD"},
      })
      .close();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -8.0, MasterCategory.NONE)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "DVD", -12.0, MasterCategory.LEISURES)
      .check();
  }

  public void testFieldsAreResetAndCreatedTransactionSelectedAfterAdd() throws Exception {
    openDialogWithSampleTransaction()
      .selectOccasional(MasterCategory.LEISURES)
      .enterAmount("12.50")
      .enterNote("DVD")
      .ok()
      .checkAmount("")
      .checkNote("")
      .close();
  }

  public void testFieldsAreResetWhenAddAmountPanelIsHidden() throws Exception {
    openDialogWithSampleTransaction()
      .selectOccasional(MasterCategory.LEISURES)
      .enterAmount("12.50")
      .enterNote("DVD")
      .checkAmount("")
      .checkNote("")
      .close();
  }

  public void testRemovingSplitParts() throws Exception {
    openDialogWithSampleTransaction()
      .addOccasional("5", MasterCategory.LEISURES, "DVD")
      .addOccasional("8", MasterCategory.BEAUTY, "Youth Elixir")
      .addOccasional("3", MasterCategory.EQUIPMENT, "Cool Sticker")
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -4.0, ""},
        {MasterCategory.LEISURES, "Auchan", -5.0, "DVD"},
        {MasterCategory.BEAUTY, "Auchan", -8.0, "Youth Elixir"},
        {MasterCategory.EQUIPMENT, "Auchan", -3.0, "Cool Sticker"},
      })

      .deleteRow(1)
      .deleteRow(2)
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -12.0, ""},
        {MasterCategory.BEAUTY, "Auchan", -8.0, "Youth Elixir"},
      })

      .deleteRow(1)
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -20.0, ""}
      })

      .addOccasional("7", MasterCategory.LEISURES, "Another DVD")
      .close();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -13.0, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "Another DVD", -7.0, MasterCategory.LEISURES)
      .check();
  }

  public void testCannotRemoveSplitSource() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    categorization.selectTableRow(0);
    transactionDetails.openSplitDialog()
      .checkDeleteEnabled(false, 0)
      .addOccasional("12.50", MasterCategory.LEISURES, "DVD")
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, "DVD"},
      })
      .checkDeleteEnabled(false, 0)
      .close();
  }

  private SplitDialogChecker openDialogWithSampleTransaction() {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    categorization.selectTableRow(0);
    return transactionDetails.openSplitDialog();
  }
}
