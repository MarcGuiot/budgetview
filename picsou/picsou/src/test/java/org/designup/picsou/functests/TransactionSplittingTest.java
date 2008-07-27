package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SplitDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;

public class TransactionSplittingTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    TimeService.setCurrentDate(Dates.parseMonth("2006/01"));
    super.setUp();
  }

  public void testStandardUsage() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.checkSplitMessageNotDisplayed();

    transactionDetails.openSplitDialog(0)
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.FOOD, "Auchan", -20.00, ""},
      })
      .assertAddAmountPanelVisible(true)
      .toggleAddAmountPanel()
      .assertAddAmountPanelVisible(false)
      .toggleAddAmountPanel()
      .enterAmount("12.50")
      .checkCurrentCategory(MasterCategory.NONE)
      .selectOccasional(MasterCategory.LEISURES)
      .enterNote("DVD")
      .toggleDispensable()
      .add()
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.FOOD, "Auchan", -7.50, ""},
        {TransactionType.PRELEVEMENT, MasterCategory.LEISURES, "Auchan", -12.50, "DVD"},
      })
      .enterAmount("2.50")
      .selectOccasional(MasterCategory.BEAUTY)
      .enterNote("Youth Elixir")
      .add()
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.FOOD, "Auchan", -5.0, ""},
        {TransactionType.PRELEVEMENT, MasterCategory.LEISURES, "Auchan", -12.50, "DVD"},
        {TransactionType.PRELEVEMENT, MasterCategory.BEAUTY, "Auchan", -2.50, "Youth Elixir"},
      })
      .ok();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -5.00, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "DVD", -12.50, MasterCategory.LEISURES)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "Youth Elixir", -2.50, MasterCategory.BEAUTY)
      .check();

    transactions.getTable().selectRow(0);
    transactionDetails.checkSplitMessage("This operation (initial amount -20.00) has been split " +
                                         "among several categories");

    transactions.getTable().selectRow(1);
    transactionDetails.checkSplitMessage("This operation has been extracted from an original operation " +
                                         "(total amount -20.00)");

    transactions.getTable().selectRow(2);
    transactionDetails.checkSplitMessage("This operation has been extracted from an original operation " +
                                         "(total amount -20.00)");
  }

  public void testTransactionTableIsUpdatedOnlyWhenClickingOnOk() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    transactions.getTable().selectRow(0);
    SplitDialogChecker dialog = transactionDetails.openSplitDialog(0)
      .enterAmount("12.50")
      .enterNote("DVD")
      .selectOccasional(MasterCategory.LEISURES)
      .add();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0, MasterCategory.FOOD)
      .check();

    dialog.ok();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "DVD", -12.50, MasterCategory.LEISURES)
      .check();
  }

  public void testCancelReallyCancels() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.openSplitDialog(0)
      .enterAmount("12.50")
      .selectOccasional(MasterCategory.LEISURES)
      .add()
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.FOOD, "Auchan", -7.50, ""},
        {TransactionType.PRELEVEMENT, MasterCategory.LEISURES, "Auchan", -12.50, ""},
      })
      .cancel();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0, MasterCategory.FOOD)
      .check();
  }

  public void testSplittingASplitPart() throws Exception {
    openDialogWithSampleTransaction()
      .addOccasional("12.50", MasterCategory.LEISURES, "")
      .ok();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, MasterCategory.LEISURES)
      .check();

    transactions.getTable().selectRow(1);
    transactionDetails.openSplitDialog(1)
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.FOOD, "Auchan", -7.50, ""},
        {TransactionType.PRELEVEMENT, MasterCategory.LEISURES, "Auchan", -12.50, ""},
      })
      .addOccasional("2.50", MasterCategory.MULTIMEDIA, "")
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.FOOD, "Auchan", -5.00, ""},
        {TransactionType.PRELEVEMENT, MasterCategory.LEISURES, "Auchan", -12.50, ""},
        {TransactionType.PRELEVEMENT, MasterCategory.MULTIMEDIA, "Auchan", -2.50, ""},
      })
      .ok();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -5.00, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, MasterCategory.LEISURES)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -2.50, MasterCategory.MULTIMEDIA)
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
      .add()
      .ok();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, MasterCategory.NONE)
      .check();
  }

  public void testAmountIsSubtractedWhateverTheSign() throws Exception {
    openDialogWithSampleTransaction()
      .enterAmount("12.50")
      .add()
      .ok();

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
      .add()
      .ok();

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
      .add()
      .ok();

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
      .cancel();
    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0, MasterCategory.FOOD)
      .check();
  }

  public void testTotalAmountGreaterThanInitialTransactionAmount() throws Exception {
    openDialogWithSampleTransaction()
      .enterAmount("12")
      .add()
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.FOOD, "Auchan", -8.0, ""},
        {TransactionType.PRELEVEMENT, MasterCategory.NONE, "Auchan", -12.0, ""},
      })
      .enterAmount("15")
      .checkErrorMessage("Amount must be less than 8")
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.FOOD, "Auchan", -8.0, ""},
        {TransactionType.PRELEVEMENT, MasterCategory.NONE, "Auchan", -12.0, ""},
      })
      .cancel();
  }

  public void testSplittingANonCategorizedTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan")
      .load();

    transactions.getTable().selectRow(0);
    transactionDetails.openSplitDialog(0)
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.NONE, "Auchan", -20.0, ""},
      })
      .addOccasional("12", MasterCategory.LEISURES, "DVD")
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.NONE, "Auchan", -8.0, ""},
        {TransactionType.PRELEVEMENT, MasterCategory.LEISURES, "Auchan", -12.0, "DVD"},
      })
      .ok();

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
      .toggleDispensable()
      .add()
      .checkCurrentCategory(MasterCategory.LEISURES)
      .checkAmount("")
      .checkNote("")
      .checkDispensable(false)
      .cancel();
  }

  public void testFieldsAreResetWhenAddAmountPanelIsHidden() throws Exception {
    openDialogWithSampleTransaction()
      .selectOccasional(MasterCategory.LEISURES)
      .enterAmount("12.50")
      .enterNote("DVD")
      .toggleDispensable()
      .toggleAddAmountPanel()
      .toggleAddAmountPanel()
      .checkCurrentCategory(MasterCategory.NONE)
      .checkAmount("")
      .checkNote("")
      .checkDispensable(false)
      .cancel();
  }

  public void testRemovingSplitParts() throws Exception {
    openDialogWithSampleTransaction()
      .addOccasional("5", MasterCategory.LEISURES, "DVD")
      .addOccasional("8", MasterCategory.BEAUTY, "Youth Elixir")
      .addOccasional("3", MasterCategory.MULTIMEDIA, "Cool Sticker")
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.FOOD, "Auchan", -4.0, ""},
        {TransactionType.PRELEVEMENT, MasterCategory.LEISURES, "Auchan", -5.0, "DVD"},
        {TransactionType.PRELEVEMENT, MasterCategory.BEAUTY, "Auchan", -8.0, "Youth Elixir"},
        {TransactionType.PRELEVEMENT, MasterCategory.MULTIMEDIA, "Auchan", -3.0, "Cool Sticker"},
      })

      .deleteRow(1)
      .deleteRow(2)
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.FOOD, "Auchan", -12.0, ""},
        {TransactionType.PRELEVEMENT, MasterCategory.BEAUTY, "Auchan", -8.0, "Youth Elixir"},
      })

      .deleteRow(1)
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.FOOD, "Auchan", -20.0, ""}
      })

      .addOccasional("7", MasterCategory.LEISURES, "Another DVD")
      .ok();

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

    transactions.getTable().selectRow(0);
    transactionDetails.openSplitDialog(0)
      .checkDeleteEnabled(false, 0)
      .addOccasional("12.50", MasterCategory.LEISURES, "DVD")
      .checkTable(new Object[][]{
        {TransactionType.PRELEVEMENT, MasterCategory.FOOD, "Auchan", -7.50, ""},
        {TransactionType.PRELEVEMENT, MasterCategory.LEISURES, "Auchan", -12.50, "DVD"},
      })
      .checkDeleteEnabled(false, 0)
      .cancel();
  }

  private SplitDialogChecker openDialogWithSampleTransaction() {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    transactions.getTable().selectRow(0);
    return transactionDetails.openSplitDialog(0);
  }
}
