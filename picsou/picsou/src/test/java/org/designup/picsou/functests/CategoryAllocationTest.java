package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Dates;
import org.uispec4j.Button;
import org.uispec4j.Panel;

import java.awt.*;

public class CategoryAllocationTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate(Dates.parse("2006/01/10"));
    super.setUp();
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

    assignCategory("MiamMiam", MasterCategory.FOOD);

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

  public void testAllocationToInternalTransfer() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .addTransaction("2006/01/11", -3.3, "Virt")
      .load();
    categories
      .initContent()
      .add(MasterCategory.ALL, 0.0, 0.0, -4.4, 1.0)
      .add(MasterCategory.NONE, 0.0, 0.0, 4.4, 1.0)
      .check();

    assignCategory("Virt", MasterCategory.INTERNAL);

    categories
      .initContent()
      .add(MasterCategory.ALL, 0.0, 0.0, -1.1, 1.0)
      .add(MasterCategory.NONE, 0.0, 0.0, 1.1, 1.0)
      .add(MasterCategory.INTERNAL, 0.0, 0.0, -3.3, 0.0)
      .check();
  }

  public void testAllocatingInPreviousMonthsDoesNotChangeTheSelectedCategory() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.0, "Menu K")
      .addTransaction("2006/02/10", -2.0, "MiamMiam")
      .load();

    timeline.assertDisplays("2006/01 (0.00/1.00)", "2006/02 (0.00/2.00)");
    timeline.selectMonth("2006/01");
    categories.select(MasterCategory.NONE);
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "Menu K", "", -1.0, MasterCategory.NONE)
      .check();
    categories.assertSelectionEquals(MasterCategory.NONE);

    assignCategory("Menu K", MasterCategory.FOOD);

    categories.assertSelectionEquals(MasterCategory.NONE);
    timeline.checkSelection("2006/01");

    transactions.assertEmpty();
  }

  public void testNonAllocatedTransactionsHaveARedLabel() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -1.1, "Menu K")
      .load();

    Button lintToCategorized = new Panel((Container)transactions.getTable().getSwingRendererComponentAt(0, 1)).getButton();
    assertTrue(lintToCategorized.foregroundEquals("red"));

    assignCategory("Menu K", MasterCategory.FOOD);

    transactions.getTable().clearSelection();
    Button categorizedLink = new Panel((Container)transactions.getTable().getSwingRendererComponentAt(0, 1)).getButton();
    assertTrue(categorizedLink.foregroundEquals("black"));
  }

  public void testCategorisationLinkSelectsCorrespondingRow() throws Exception {
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

  private void assignCategory(String label, MasterCategory category) {
    views.selectCategorization();
    categorization.setOccasional(label, category);
    views.selectData();
  }
}
