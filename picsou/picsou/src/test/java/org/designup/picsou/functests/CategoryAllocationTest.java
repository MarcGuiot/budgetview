package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CategoryAllocationTest extends LoggedInFunctionalTestCase {
  private static final String DEFAULT_BG = "EBF0F7";
  private static final String ERROR_BG = "red";

  public void testAllocationDialog() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Blah")
      .load();

    List<MasterCategory> proposedCategories = new ArrayList<MasterCategory>();
    proposedCategories.addAll(Arrays.asList(MasterCategory.values()));
    proposedCategories.remove(MasterCategory.ALL);
    String[] names = categories.getSortedCategoryNames(proposedCategories);

    transactions.openCategoryChooserDialog(0).checkContains(names);
  }

  public void testAllocation() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -1.1, "MiamMiam")
      .load();
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "MiamMiam", "", -1.1, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.NONE)
      .check();
    categories
      .initContent()
      .add(MasterCategory.ALL, 0.0, 0.0, -2.2, 1.0)
      .add(MasterCategory.NONE, 0.0, 0.0, 2.2, 1.0)
      .check();

    transactions.assignCategory(MasterCategory.FOOD, 0);
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "MiamMiam", "", -1.1, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.NONE)
      .check();
    categories
      .initContent()
      .add(MasterCategory.ALL, 0.0, 0.0, -2.2, 1.0)
      .add(MasterCategory.NONE, 0.0, 0.0, 1.1, 0.5)
      .add(MasterCategory.FOOD, 0.0, 0.0, -1.1, 0.5)
      .check();
  }

  public void testUserAllocationErasesPreviousMultiAllocations() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K", MasterCategory.FOOD, MasterCategory.LEISURES)
      .load();
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.FOOD, MasterCategory.LEISURES)
      .check();

    transactions.assignCategory(MasterCategory.HEALTH, 0);
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.1, MasterCategory.HEALTH)
      .check();
  }

  public void testAllocationToInternalTransfer() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -3.3, "MiamMiam")
      .load();
    categories
      .initContent()
      .add(MasterCategory.ALL, 0.0, 0.0, -4.4, 1.0)
      .add(MasterCategory.NONE, 0.0, 0.0, 4.4, 1.0)
      .check();

    transactions.assignCategory(MasterCategory.INTERNAL, 0);
    categories
      .initContent()
      .add(MasterCategory.ALL, 0.0, 0.0, -1.1, 1.0)
      .add(MasterCategory.NONE, 0.0, 0.0, 1.1, 1.0)
      .add(MasterCategory.INTERNAL, 0.0, 0.0, -3.3, 0.0)
      .check();
  }

  public void testAllocatingInPreviousMonthsDoesNotChangeTheSelection() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.0, "Menu K")
      .addTransaction("2006/02/10", -2.0, "MiamMiam")
      .load();

    periods.assertContains("2006/01 (0.00/1.00)", "2006/02 (0.00/2.00)");
    periods.selectCell(0);
    categories.select(MasterCategory.NONE);
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.NONE)
      .check();
    categories.assertSelectionEquals(MasterCategory.NONE);

    transactions.assignCategory(MasterCategory.FOOD, 0);
    categories.assertSelectionEquals(MasterCategory.NONE);
    periods.assertCellSelected(0);

    transactions.assertEmpty();
  }

  public void testNonAllocatedTransactionsHaveARedBackground() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .load();

    assertTrue(transactions.getTable().backgroundEquals(new Object[][]{
      {DEFAULT_BG, ERROR_BG, DEFAULT_BG, DEFAULT_BG},
    }));

    transactions.assignCategory(MasterCategory.FOOD, 0);
    transactions.getTable().clearSelection();
    assertTrue(transactions.getTable().backgroundEquals(new Object[][]{
      {DEFAULT_BG, DEFAULT_BG, DEFAULT_BG, DEFAULT_BG, DEFAULT_BG},
    }));
  }

  public void testAllocationOfCategoryIsPropagated() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/13", -1.0, "Menu K")
      .addTransaction("2006/01/12", -1.0, "MacDo")
      .addTransaction("2006/01/11", -1.0, "Menu K")
      .addTransaction("2006/01/10", -1.0, "Quick")
      .load();
    transactions.assignCategory(MasterCategory.FOOD, 0);
    transactions
      .initContent()
      .add("13/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.FOOD)
      .add("12/01/2006", TransactionType.PRELEVEMENT, "MacDo", "", -1.0, MasterCategory.NONE)
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Quick", "", -1.0, MasterCategory.NONE)
      .check();
  }

  public void testPropagationIgnoresDigitsInLabels() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.0, "Menu 14")
      .addTransaction("2006/01/10", -1.0, "Menu 12321")
      .load();
    transactions.assignCategory(MasterCategory.FOOD, 0);
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "Menu 14", "", -1.0, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu 12321", "", -1.0, MasterCategory.FOOD)
      .check();
  }

  public void testNoPropagationForChecks() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/11", -1.0, "Cheque 123")
      .addTransaction("2006/01/10", -1.0, "Cheque 234")
      .load();
    transactions.assignCategory(MasterCategory.FOOD, 0);
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.CHECK, "CHEQUE N. 123", "", -1.0, MasterCategory.FOOD)
      .add("10/01/2006", TransactionType.CHECK, "CHEQUE N. 234", "", -1.0)
      .check();
  }

  public void testAllocationOfNoteOnCheckWithdrawalAndDeposit() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/13", -1.0, "CHEQUE 1")
      .addTransaction("2006/01/13", -2.0, "CHEQUE 1")
      .addTransaction("2006/01/12", -1.0, "RETRAIT 12")
      .addTransaction("2006/01/12", -2.0, "RETRAIT 12")
      .addTransaction("2006/01/11", -1.0, "REM CHQ 4")
      .addTransaction("2006/01/11", -2.0, "REM CHQ 4")
      .addTransactionWithNote("2006/01/10", -1.0, "2", "note 1")
      .load();
    transactions.assignCategory(MasterCategory.PUERICULTURE, 0, 2, 4);
    categories.select(MasterCategory.ALL);
    periods.selectCell(0);
    transactions
      .initContent()
      .add("13/01/2006", TransactionType.CHECK, "CHEQUE N. 1", "", -1.0, MasterCategory.PUERICULTURE)
      .add("13/01/2006", TransactionType.CHECK, "CHEQUE N. 1", "", -2.0, MasterCategory.NONE)
      .add("12/01/2006", TransactionType.WITHDRAWAL, "RETRAIT 12", "", -1.0, MasterCategory.PUERICULTURE)
      .add("12/01/2006", TransactionType.WITHDRAWAL, "RETRAIT 12", "", -2.0, MasterCategory.NONE)
      .add("11/01/2006", TransactionType.DEPOSIT, "REMISE CHEQUES 4", "", -1.0, MasterCategory.PUERICULTURE)
      .add("11/01/2006", TransactionType.DEPOSIT, "REMISE CHEQUES 4", "", -2.0, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "2", "note 1", -1.0, MasterCategory.NONE)
      .check();
  }
}
