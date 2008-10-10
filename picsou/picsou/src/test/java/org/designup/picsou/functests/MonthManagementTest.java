package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class MonthManagementTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2006/01/10");
    super.setUp();
  }

  public void testEmpty() throws Exception {
    timeline.assertEmpty();
  }

  public void testSingleMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -10, "rent")
      .addTransaction("2006/01/20", +5, "income")
      .load();

    timeline.assertDisplays("2006/01 (5.00/10.00)");
    timeline.checkSelection("2006/01");
  }

  public void testTwoMonths() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -10, "rent")
      .addTransaction("2006/02/20", +5, "income")
      .load();

    timeline.checkSelection("2006/02");

    timeline.assertDisplays("2006/01 (0.00/10.00)", "2006/02 (5.00/0.00)");

    timeline.selectAll();
    timeline.checkSelection("2006/01", "2006/02");
    timeline.selectMonth("2006/02");
    transactions.initContent()
      .add("20/02/2006", TransactionType.VIREMENT, "income", "", 5.0, MasterCategory.NONE)
      .check();

    timeline.selectMonth("2006/01");
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

    timeline.selectMonth("2006/03");
    transactions
      .initContent()
      .add("20/03/2006", TransactionType.VIREMENT, "income", "", 5, MasterCategory.NONE)
      .check();

    timeline.selectMonth("2006/01");
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "rent", "", -10, MasterCategory.NONE)
      .check();

    timeline.selectMonth("2006/02");
    transactions.assertEmpty();

    timeline.selectMonth("2006/03");
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
    timeline.assertDisplays("2006/01 (0.00/60.00)");
    transactions
      .initContent()
      .add("11/01/2006", TransactionType.PRELEVEMENT, "internal", "", -50, MasterCategory.NONE)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "misc", "", -10, MasterCategory.NONE)
      .check();

    views.selectCategorization();
    categorization.setOccasional("internal", MasterCategory.INTERNAL);

    views.selectData();
    transactions
      .initContent()
      .addOccasional("11/01/2006", TransactionType.PRELEVEMENT, "internal", "", -50, MasterCategory.INTERNAL)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "misc", "", -10, MasterCategory.NONE)
      .check();

    timeline.assertDisplays("2006/01 (0.00/10.00)");
  }

  public void testMultiselectionCumulatesTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -10, "rent")
      .addTransaction("2006/02/10", +50, "income1")
      .addTransaction("2006/03/20", +5, "income2")
      .load();

    timeline.selectMonths("2006/01", "2006/02", "2006/03");
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

    timeline.selectNone();
    categories
      .initContent()
      .check();
    transactions.assertEmpty();
  }

}
