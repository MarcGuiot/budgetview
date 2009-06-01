package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class TimeManagementTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2006/01/10");
    super.setUp();
  }

  public void testInitialState() throws Exception {
    timeline.checkSelection("2006/01");
    timeline.checkDisplays("2006/01");
  }

  public void testSingleMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -10, "rent")
      .addTransaction("2006/01/20", +5, "income")
      .load();

    timeline.checkDisplays("2006/01");
    timeline.checkSelection("2006/01");
  }

  public void testTwoMonths() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -10, "rent")
      .addTransaction("2006/02/20", +5, "income")
      .load();

    timeline.checkSelection("2006/02");

    timeline.checkDisplays("2006/01", "2006/02");

    timeline.selectAll();
    timeline.checkSelection("2006/01", "2006/02");
    timeline.selectMonth("2006/02");
    transactions.initContent()
      .add("20/02/2006", TransactionType.VIREMENT, "income", "", 5.0)
      .check();

    timeline.selectMonth("2006/01");
    transactions.initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "rent", "", -10)
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
      .add("20/03/2006", TransactionType.VIREMENT, "income", "", 5)
      .check();

    timeline.selectMonth("2006/01");
    transactions
      .initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "rent", "", -10)
      .check();

    timeline.selectMonth("2006/02");
    transactions.checkTableIsEmpty();

    timeline.selectMonth("2006/03");
    transactions
      .initContent()
      .add("20/03/2006", TransactionType.VIREMENT, "income", "", 5)
      .check();
  }

  public void testMultiselectionCumulatesTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/01/10", -10, "rent")
      .addTransaction("2006/02/10", +50, "income1")
      .addTransaction("2006/03/20", +5, "income2")
      .load();

    timeline.selectMonths("2006/01", "2006/02", "2006/03");

    transactions
      .initContent()
      .add("20/03/2006", TransactionType.VIREMENT, "income2", "", +5)
      .add("10/02/2006", TransactionType.VIREMENT, "income1", "", +50)
      .add("10/01/2006", TransactionType.PRELEVEMENT, "rent", "", -10)
      .check();

    timeline.selectNone();
    transactions.checkTableIsEmpty();
  }
}
