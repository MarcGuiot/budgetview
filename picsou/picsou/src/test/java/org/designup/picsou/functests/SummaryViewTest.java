package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SummaryViewTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2011/01");
    super.setUp();
  }

  public void test() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 2800.0, "2011/01/10")
      .addTransaction("2010/12/28", 3000.00, "WorldCo")
      .addTransaction("2011/01/05", -1000.00, "Foncia")
      .addTransaction("2011/01/09", -200.00, "FNAC")
      .load();

    categorization.setNewIncome("WorldCo", "Income");
    categorization.setNewRecurring("Foncia", "Loyer");

    summary.getMainChart()
      .checkEndOfMonthValue(4000.00)
      .checkRange(201012, 201107)
      .checkSelected(201101);
  }

  public void testScrolling() throws Exception {
    operations.openPreferences().setFutureMonthsCount(18).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 2800.0, "2011/01/10")
      .addTransaction("2010/12/28", 3000.00, "WorldCo")
      .addTransaction("2010/09/28", 3000.00, "WorldCo")
      .load();

    checkRange(201010, 201110);

    summary.getMainChart().scroll(-8);
    checkRange(201009, 201109);

    summary.getMainChart().scroll(+1);
    checkRange(201010, 201110);

    timeline.selectMonth("2011/12");
    checkRange(201012, 201112);

    summary.getMainChart().scroll(-1);
    checkRange(201011, 201111);

    summary.getMainChart().scroll(+1);
    checkRange(201012, 201112);

    summary.getMainChart().scroll(+20);
    checkRange(201107, 201207);

    summary.getMainChart().scroll(-1);
    checkRange(201106, 201206);

    summary.getMainChart().scroll(-20);
    checkRange(201009, 201109);

    summary.getMainChart().scroll(+1);
    checkRange(201010, 201110);

    summary.getMainChart().clickColumnId(201109);
    checkRange(201010,201110);
    timeline.checkSelection("2011/09");
  }

  public void testDoubleClick() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "00000123", 2800.00, "2011/01/10")
      .addTransaction("2010/12/28", 3000.00, "WorldCo")
      .addTransaction("2010/09/28", 3000.00, "WorldCo")
      .load();

    views.selectHome();
    summary.getMainChart().doubleClick();

    views.checkBudgetSelected();
    timeline.checkSelection("2010/12");
  }

  private void checkRange(int start, int end) {
    summary.getMainChart().checkRange(start, end);
    summary.getSavingsBalanceChart().checkRange(start, end);
    summary.getSavingsChart().checkRange(start, end);
  }
}
