package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class BalanceTest extends LoggedInFunctionalTestCase{

  protected void setUp() throws Exception {
    setCurrentDate("2009/05/15");
    super.setUp();
  }

  public void testBalanceWithManualTransactionInTheFuture() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(3)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2009/04/20", -29.90, "Free Telecom")
      .load();

    mainAccounts.createNewAccount()
      .setAccountName("Manual")
      .selectBank("CIC")
      .setUpdateModeToManualInput()
      .setBalance(0)
      .validate();

    views.selectCategorization();
    categorization
      .setNewRecurring("Free Telecom", "Tel");


    timeline.selectMonth("2009/05");
    transactionCreation
      .setAmount(-10)
      .setLabel("prov")
      .setDay(22)
      .create();
    timeline.selectMonth("2009/07");
    transactionCreation
      .setAmount(-10)
      .setLabel("prov")
      .setDay(5)
      .create();

    categorization
      .setNewEnvelope("prov", "Courses");

    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("22/08/2009", "Planned: Courses", -10.00, "Courses", -159.60, "Main accounts")
      .add("20/08/2009", "Planned: Tel", -29.90, "Tel", -149.60, "Main accounts")
      .add("20/07/2009", "Planned: Tel", -29.90, "Tel", -119.70, "Main accounts")
      .add("05/07/2009", "PROV", -10.00, "Courses", -20.00, -89.80, "Manual")
      .add("22/06/2009", "Planned: Courses", -10.00, "Courses", -79.80, "Main accounts")
      .add("20/06/2009", "Planned: Tel", -29.90, "Tel", -69.80, "Main accounts")
      .add("22/05/2009", "PROV", -10.00, "Courses", -10.00, -39.90, "Manual")
      .add("20/05/2009", "Planned: Tel", -29.90, "Tel", -29.90, "Main accounts")
      .add("20/04/2009", "FREE TELECOM", -29.90, "Tel", 0.00, 0.00, "Account n. 00001123")
      .check();
    views.selectHome();
    timeline.selectMonth("2009/05");
    mainAccounts.checkIsEstimatedPosition()
      .checkEstimatedPosition(-39.90)
      .checkAccount("Manual", 0, "2009/05/15");

    timeline.selectMonth("2009/06");
    mainAccounts.checkIsEstimatedPosition()
      .checkEstimatedPosition(-79.80)
      .checkAccount("Manual", 0, "2009/05/15");
  }

  public void testBalanceWithNoTransactionOnAMonth() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(3)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2009/04/20", -29.90, "Free Telecom")
      .load();

    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("NoName")
      .validate();
    views.selectHome();
    timeline.selectMonth("2009/04");
    monthSummary.checkBalance(-29.90);
    timeline.selectMonth("2009/05");
    monthSummary.checkBalance(0);
    timeline.selectMonth("2009/06");
    monthSummary.checkBalance(0);

  }
}
