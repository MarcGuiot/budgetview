package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class TimeManagementTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2006/03/10");
    super.setUp();
  }

  public void testInitialState() throws Exception {
    timeline.checkSelection("2006/03");
    timeline.checkDisplays("2006/03");
  }

  public void testSingleMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/03/10", -10, "rent")
      .addTransaction("2006/03/20", +5, "income")
      .load();

    timeline.checkDisplays("2006/03");
    timeline.checkSelection("2006/03");
  }

  public void testTwoMonths() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2006/04/10", -10, "rent")
      .addTransaction("2006/03/20", +5, "income")
      .load();

    timeline.checkSelection("2006/04");

    timeline.checkDisplays("2006/03", "2006/04");

    timeline.selectAll();
    timeline.checkSelection("2006/03", "2006/04");
    timeline.selectMonth("2006/03");

    transactions.initContent()
      .add("20/03/2006", TransactionType.VIREMENT, "income", "", 5.0)
      .check();

    timeline.selectMonth("2006/04");
    transactions.initContent()
      .add("10/04/2006", TransactionType.PRELEVEMENT, "rent", "", -10)
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

  public void testMenuSelection() throws Exception {
    operations.openPreferences().setFutureMonthsCount(12).validate();
    OfxBuilder
      .init(this)
      .addTransaction("2006/03/01", 100, "income")
      .addTransaction("2006/02/01", 100, "income")
      .addTransaction("2006/01/01", 100, "income")
      .addTransaction("2005/12/01", 1200, "income")
      .addTransaction("2005/11/01", 1100, "income")
      .addTransaction("2005/10/01", 1000, "income")
      .addTransaction("2005/09/01", 900, "income")
      .addTransaction("2005/08/01", 800, "income")
      .addTransaction("2005/07/01", 700, "income")
      .addTransaction("2005/06/01", 600, "income")
      .addTransaction("2005/05/01", 500, "income")
      .addTransaction("2005/04/01", 400, "income")
      .addTransaction("2005/03/01", 300, "income")
      .addTransaction("2005/02/01", 200, "income")
      .addTransaction("2005/01/01", 100, "income")
      .addTransaction("2004/12/01", 50, "income")
      .load();

    timeline.selectMonth("2005/08");
    transactions.initContent()
      .add("01/08/2005", TransactionType.VIREMENT, "INCOME", "", 800.00)
      .check();

    operations.selectCurrentMonth();
    timeline.checkSelection("2006/03");
    transactions.initContent()
      .add("01/03/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .check();

    timeline.selectMonth("2004/12");
    operations.selectCurrentYear();
    timeline.checkSelection("2006/01", "2006/02", "2006/03", "2006/04", "2006/05", "2006/06",
                            "2006/07", "2006/08", "2006/09", "2006/10", "2006/11", "2006/12");
    transactions.initContent()
      .add("01/03/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .add("01/02/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .add("01/01/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .check();

    timeline.selectMonth("2004/12");
    operations.selectLast12Months();
    timeline.checkSelection("2005/04", "2005/05", "2005/06", "2005/07",
                            "2005/08", "2005/09", "2005/10", "2005/11",
                            "2005/12", "2006/01", "2006/02", "2006/03");

    transactions.initContent()
      .add("01/03/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .add("01/02/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .add("01/01/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .add("01/12/2005", TransactionType.VIREMENT, "INCOME", "", 1200.00)
      .add("01/11/2005", TransactionType.VIREMENT, "INCOME", "", 1100.00)
      .add("01/10/2005", TransactionType.VIREMENT, "INCOME", "", 1000.00)
      .add("01/09/2005", TransactionType.VIREMENT, "INCOME", "", 900.00)
      .add("01/08/2005", TransactionType.VIREMENT, "INCOME", "", 800.00)
      .add("01/07/2005", TransactionType.VIREMENT, "INCOME", "", 700.00)
      .add("01/06/2005", TransactionType.VIREMENT, "INCOME", "", 600.00)
      .add("01/05/2005", TransactionType.VIREMENT, "INCOME", "", 500.00)
      .add("01/04/2005", TransactionType.VIREMENT, "INCOME", "", 400.00)
      .check();

    timeline.selectMonth("2005/12");
    operations.selectLast12Months();
    transactions.initContent()
      .add("01/03/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .add("01/02/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .add("01/01/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .add("01/12/2005", TransactionType.VIREMENT, "INCOME", "", 1200.00)
      .add("01/11/2005", TransactionType.VIREMENT, "INCOME", "", 1100.00)
      .add("01/10/2005", TransactionType.VIREMENT, "INCOME", "", 1000.00)
      .add("01/09/2005", TransactionType.VIREMENT, "INCOME", "", 900.00)
      .add("01/08/2005", TransactionType.VIREMENT, "INCOME", "", 800.00)
      .add("01/07/2005", TransactionType.VIREMENT, "INCOME", "", 700.00)
      .add("01/06/2005", TransactionType.VIREMENT, "INCOME", "", 600.00)
      .add("01/05/2005", TransactionType.VIREMENT, "INCOME", "", 500.00)
      .add("01/04/2005", TransactionType.VIREMENT, "INCOME", "", 400.00)
      .check();
  }

  public void testMenuSelectionWithIncompleteYear() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();
    OfxBuilder
      .init(this)
      .addTransaction("2006/03/01", 100, "income")
      .addTransaction("2005/12/01", 1200, "income")
      .addTransaction("2005/11/01", 1100, "income")
      .addTransaction("2005/10/01", 1000, "income")
      .load();

    timeline.selectMonth("2005/10");
    transactions.initContent()
      .add("01/10/2005", TransactionType.VIREMENT, "INCOME", "", 1000.00)
      .check();

    operations.selectCurrentMonth();
    timeline.checkSelection("2006/03");
    transactions.initContent()
      .add("01/03/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .check();

    timeline.selectMonth("2005/12");
    operations.selectCurrentYear();
    timeline.checkSelection("2006/01", "2006/02", "2006/03",
                            "2006/04", "2006/05", "2006/06",
                            "2006/07", "2006/08", "2006/09");
    transactions.initContent()
      .add("01/03/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .check();

    timeline.selectMonth("2005/12");
    operations.selectLast12Months();
    timeline.checkSelection("2005/10", "2005/11", "2005/12", "2006/01", "2006/02", "2006/03");
    transactions.initContent()
      .add("01/03/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .add("01/12/2005", TransactionType.VIREMENT, "INCOME", "", 1200.00)
      .add("01/11/2005", TransactionType.VIREMENT, "INCOME", "", 1100.00)
      .add("01/10/2005", TransactionType.VIREMENT, "INCOME", "", 1000.00)
      .check();

    timeline.selectMonth("2005/12");
    operations.selectAllMonthsSinceJanuary();
    timeline.checkSelection("2006/01", "2006/02", "2006/03");
    transactions.initContent()
      .add("01/03/2006", TransactionType.VIREMENT, "INCOME", "", 100.00)
      .check();
  }
}
