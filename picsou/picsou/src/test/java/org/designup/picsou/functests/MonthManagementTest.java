package org.designup.picsou.functests;

import org.uispec4j.Button;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class MonthManagementTest extends LoggedInFunctionalTestCase {

  public void testEmpty() throws Exception {
    periods.assertEmpty();
  }

  public void testSingleMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -10, "rent")
      .addTransaction("2006/01/20", +5, "income")
      .load();

    periods.assertContains("2006/01 (5.00/10.00)");
    periods.assertCellSelected(0);
  }

  public void testTwoMonths() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -10, "rent")
      .addTransaction("2006/02/20", +5, "income")
      .load();

    periods.assertContains("2006/01 (0.00/10.00)", "2006/02 (5.00/0.00)");

    periods.assertCellSelected(1);
    transactions.initContent()
      .add("20/02/2006", TransactionType.VIREMENT, "income", "", 5.0, MasterCategory.NONE)
      .check();

    periods.selectCell(0);
    transactions.initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "rent", "", -10, MasterCategory.NONE)
      .check();
  }

  public void testMonthsWithNoDataAreShown() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -10, "rent")
      .addTransaction("2006/03/20", +5, "income")
      .load();

    periods.assertContains("2006/01 (0.00/10.00)", "2006/02 (0.00/0.00)", "2006/03 (5.00/0.00)");
    periods.assertCellSelected(2);
    periods.selectCell(0);
    periods.selectCell(2);
    transactions
      .initContent()
      .add("20/03/2006", TransactionType.VIREMENT, "income", "", 5, MasterCategory.NONE)
      .check();

    periods.selectCell(0);
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "rent", "", -10, MasterCategory.NONE)
      .check();

    periods.selectCell(1);
    transactions.assertEmpty();

    periods.selectCell(2);
    transactions
      .initContent()
      .add("20/03/2006", TransactionType.VIREMENT, "income", "", 5, MasterCategory.NONE)
      .check();
  }

  public void testInternalTransfersAreIgnoredInPeriodStats() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -10, "misc")
      .addTransaction("2006/01/11", -50, "internal")
      .load();
    periods.assertContains("2006/01 (0.00/60.00)");
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "internal", "", -50, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "misc", "", -10, MasterCategory.NONE)
      .check();

    transactions.assignCategory(MasterCategory.INTERNAL, 0);
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "internal", "", -50, MasterCategory.INTERNAL)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "misc", "", -10, MasterCategory.NONE)
      .check();

    periods.assertContains("2006/01 (0.00/10.00)");
  }

  public void testMultiselectionCumulatesTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -10, "rent")
      .addTransaction("2006/02/10", +50, "income1")
      .addTransaction("2006/03/20", +5, "income2")
      .load();

    periods.selectCells(0, 1, 2);
    categories
      .initContent()
      .add(MasterCategory.ALL, 55, 1.0, -10, 1.0)
      .add(MasterCategory.NONE, 55, 1.0, 10, 1.0)
      .check();

    transactions
      .initContent()
      .add("20/03/2006", TransactionType.VIREMENT, "income2", "", +5, MasterCategory.NONE)
      .add("10/02/2006", TransactionType.VIREMENT, "income1", "", +50, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "rent", "", -10, MasterCategory.NONE)
      .check();

    periods.selectNone();
    categories
      .initContent()
      .check();
    transactions.assertEmpty();
  }

  public void testNavigationButtons() throws Exception {
    Button prevButton = mainWindow.getButton("prev");
    Button nextButton = mainWindow.getButton("next");
    Button lastButton = mainWindow.getButton("last");
    Button firstButton = mainWindow.getButton("first");

    OfxBuilder builder = OfxBuilder.init(this);
    for (int i = 0; i < 4; i++) {
      builder.addTransaction("2006/" + i + "/10", 1, "blah");
    }
    builder.load();

    periods.assertCellSelected(3);

    firstButton.click();
    periods.assertCellSelected(0);

    nextButton.click();
    periods.assertCellSelected(1);

    lastButton.click();
    periods.assertCellSelected(3);

    nextButton.click();
    periods.assertCellSelected(3);

    prevButton.click();
    periods.assertCellSelected(2);

    prevButton.click();
    periods.assertCellSelected(1);

    prevButton.click();
    periods.assertCellSelected(0);
  }
}
