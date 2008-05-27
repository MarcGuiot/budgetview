package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.TransactionChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class TransactionSplittingTest extends LoggedInFunctionalTestCase {
  public void testStandardUsage() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    transactions.openSplitDialog(0)
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -20.00, false, ""},
      })
      .assertAddAmountPanelVisible(true)
      .toggleAddAmountPanel()
      .assertAddAmountPanelVisible(false)
      .toggleAddAmountPanel()
      .enterAmount("12.50")
      .checkCurrentCategory(MasterCategory.NONE)
      .selectCategory(MasterCategory.LEISURES)
      .enterNote("DVD")
      .toggleDispensable()
      .add()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, false, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, true, "DVD"},
      })
      .enterAmount("2.50")
      .selectCategory(MasterCategory.BEAUTY)
      .enterNote("Youth Elixir")
      .add()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -5.0, false, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, true, "DVD"},
        {MasterCategory.BEAUTY, "Auchan", -2.50, false, "Youth Elixir"},
      })
      .ok();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", false, "", -5.00, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", true, "DVD", -12.50, MasterCategory.LEISURES)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", false, "Youth Elixir", -2.50, MasterCategory.BEAUTY)
      .check();
  }

  public void testTransactionTableIsUpdatedOnlyWhenClickingOnOk() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    TransactionChecker.SplitDialog dialog = transactions.openSplitDialog(0)
      .enterAmount("12.50")
      .enterNote("DVD")
      .selectCategory(MasterCategory.LEISURES)
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

    transactions.openSplitDialog(0)
      .enterAmount("12.50")
      .selectCategory(MasterCategory.LEISURES)
      .add()
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, false, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, false, ""},
      })
      .cancel();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -20.0, MasterCategory.FOOD)
      .check();
  }

  public void testSplittingASplitPart() throws Exception {
    openDialogWithSampleTransaction()
      .add("12.50", MasterCategory.LEISURES, "")
      .ok();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -7.50, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, MasterCategory.LEISURES)
      .check();

    transactions.openSplitDialog(1)
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, false, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, false, ""},
      })
      .add("2.50", MasterCategory.BANK, "")
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -5.00, false, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, false, ""},
        {MasterCategory.BANK, "Auchan", -2.50, false, ""},
      })
      .ok();

    transactions
      .initContent()
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -5.00, MasterCategory.FOOD)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -12.50, MasterCategory.LEISURES)
      .add("15/01/2006", TransactionType.PRELEVEMENT, "Auchan", "", -2.50, MasterCategory.BANK)
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
      .selectCategory(MasterCategory.LEISURES)
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
      .selectCategory(MasterCategory.LEISURES)
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
      .checkErrorMessage("Le montant est invalide");
  }

  public void testAmountGreaterThanInitialTransactionAmount() throws Exception {
    openDialogWithSampleTransaction()
      .enterAmount("100")
      .checkErrorMessage("Le montant doit être inférieur à 20€")
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
        {MasterCategory.FOOD, "Auchan", -8.0, false, ""},
        {MasterCategory.NONE, "Auchan", -12.0, false, ""},
      })
      .enterAmount("15")
      .checkErrorMessage("Le montant doit être inférieur à 8€")
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -8.0, false, ""},
        {MasterCategory.NONE, "Auchan", -12.0, false, ""},
      })
      .cancel();
  }

  public void testSplittingANonCategorizedTransaction() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan")
      .load();

    transactions.openSplitDialog(0)
      .checkTable(new Object[][]{
        {MasterCategory.NONE, "Auchan", -20.0, false, ""},
      })
      .add("12", MasterCategory.LEISURES, "DVD")
      .checkTable(new Object[][]{
        {MasterCategory.NONE, "Auchan", -8.0, false, ""},
        {MasterCategory.LEISURES, "Auchan", -12.0, false, "DVD"},
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
      .selectCategory(MasterCategory.LEISURES)
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

  public void testFieldsAreResetAfterCancel() throws Exception {
    openDialogWithSampleTransaction()
      .selectCategory(MasterCategory.LEISURES)
      .enterAmount("12.50")
      .enterNote("DVD")
      .toggleDispensable()
      .cancelAddAmount()
      .checkCurrentCategory(MasterCategory.NONE)
      .checkAmount("")
      .checkNote("")
      .checkDispensable(false)
      .cancel();
  }

  public void testFieldsAreResetWhenAddAmountPanelIsHidden() throws Exception {
    openDialogWithSampleTransaction()
      .selectCategory(MasterCategory.LEISURES)
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
      .add("5", MasterCategory.LEISURES, "DVD")
      .add("8", MasterCategory.BEAUTY, "Youth Elixir")
      .add("3", MasterCategory.TRANSPORTS, "Cool Sticker")
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -4.0, false, ""},
        {MasterCategory.LEISURES, "Auchan", -5.0, false, "DVD"},
        {MasterCategory.BEAUTY, "Auchan", -8.0, false, "Youth Elixir"},
        {MasterCategory.TRANSPORTS, "Auchan", -3.0, false, "Cool Sticker"},
      })

      .deleteRow(1)
      .deleteRow(2)
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -12.0, false, ""},
        {MasterCategory.BEAUTY, "Auchan", -8.0, false, "Youth Elixir"},
      })

      .deleteRow(1)
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -20.0, false, ""}
      })

      .add("7", MasterCategory.LEISURES, "Another DVD")
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

    transactions.openSplitDialog(0)
      .checkDeleteEnabled(false, 0)
      .add("12.50", MasterCategory.LEISURES, "DVD")
      .checkTable(new Object[][]{
        {MasterCategory.FOOD, "Auchan", -7.50, false, ""},
        {MasterCategory.LEISURES, "Auchan", -12.50, false, "DVD"},
      })
      .checkDeleteEnabled(false, 0)
      .cancel();
  }

  private TransactionChecker.SplitDialog openDialogWithSampleTransaction() {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/15", -20.0, "Auchan", MasterCategory.FOOD)
      .load();

    return transactions.openSplitDialog(0);
  }
}
