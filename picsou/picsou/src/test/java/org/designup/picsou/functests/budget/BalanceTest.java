package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BankEntity;

public class BalanceTest extends LoggedInFunctionalTestCase {

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
      .setName("Manual")
      .selectBank("CIC")
      .setPosition(0)
      .validate();

    views.selectCategorization();
    categorization
      .setNewRecurring("Free Telecom", "Tel");

    timeline.selectMonth("2009/05");
    transactionCreation
      .show()
      .setToBeReconciled()
      .shouldUpdatePosition()
      .selectAccount("Manual")
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
      .setNewRecurring("prov", "Courses");
    timeline.selectMonth("2009/05");
    categorization.editSeries("Courses").setPropagationEnabled().setAmount(10).validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("19/08/2009", "Planned: Tel", -29.90, "Tel", -159.60, "Main accounts")
      .add("11/08/2009", "Planned: Courses", -10.00, "Courses", -129.70, "Main accounts")
      .add("19/07/2009", "Planned: Tel", -29.90, "Tel", -119.70, "Main accounts")
      .add("05/07/2009", "PROV", -10.00, "Courses", -20.00, -89.80, "Manual")
      .add("19/06/2009", "Planned: Tel", -29.90, "Tel", -79.80, "Main accounts")
      .add("11/06/2009", "Planned: Courses", -10.00, "Courses", -49.90, "Main accounts")
      .add("22/05/2009", "PROV", -10.00, "Courses", -10.00, -39.90, "Manual")
      .add("19/05/2009", "Planned: Tel", -29.90, "Tel", -29.90, "Main accounts")
      .add("20/04/2009", "FREE TELECOM", -29.90, "Tel", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    timeline.selectMonth("2009/05");
    mainAccounts
      .checkEstimatedPosition(-39.90)
      .checkAccount("Manual", 0, "2009/04/01");

    timeline.selectMonth("2009/06");
    mainAccounts
      .checkEstimatedPosition(-79.80)
      .checkAccount("Manual", 0, "2009/04/01");
  }

  public void testPositionWithNoOperationsForAMonth() throws Exception {
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

    timeline.selectMonth("2009/04");
    budgetView.getSummary().checkEndPosition(0.00);

    timeline.selectMonth("2009/05");
    budgetView.getSummary().checkEndPosition(0.00);

    timeline.selectMonth("2009/06");
    budgetView.getSummary().checkEndPosition(0.00);
  }

  public void testSavingsWithMonthWithoutTransaction() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000., "2008/08/12")
      .addTransaction("2008/08/12", 100.00, "P3 CE")
      .addTransaction("2008/06/11", 100.00, "P2 CE")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/12", -100.00, "V3 CC")  //compte d'épargne
      .addTransaction("2008/08/11", -100.00, "V2 CC")
      .addTransaction("2008/08/10", -100.00, "Auchan")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    mainAccounts.edit(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setAsSavings()
      .validate();

    // sur compte courant
    timeline.selectMonth("2008/08");
    mainAccounts.checkEstimatedPosition(1000);

    timeline.selectMonth("2008/07");
    mainAccounts.checkEstimatedPosition(900);

    timeline.selectMonth("2008/06");
    mainAccounts.checkEstimatedPosition(900);

    // sur savings
    timeline.selectMonth("2008/06");
    savingsAccounts
      .checkEstimatedPositionDate("2008/06/30")
      .checkEstimatedPosition(300);

    timeline.selectMonth("2008/07");
    savingsAccounts
      .checkEstimatedPositionDate("2008/07/31")
      .checkEstimatedPosition(300);

    timeline.selectMonth("2008/08");
    savingsAccounts
      .checkEstimatedPositionDate("2008/08/31")
      .checkEstimatedPosition(0);
  }

  public void testMissingBalanceAtBeginningIfNoOperation() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000., "2008/08/15")  //compte d'épargne
      .addTransaction("2008/08/12", 100.00, "P3 CE")
      .addTransaction("2008/06/11", 100.00, "P2 CE")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/12", -100.00, "V3 CC")
      .addTransaction("2008/08/11", -100.00, "V2 CC")
      .addTransaction("2008/08/10", -100.00, "Auchan")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();

    // sur compte courant
    timeline.selectMonth("2008/07");
    mainAccounts.checkEstimatedPosition(300);

    timeline.selectMonth("2008/06");
    mainAccounts.checkEstimatedPosition(300);

    // sur savings
    timeline.selectMonth("2008/06");
    savingsAccounts.checkEstimatedPosition(900);

    timeline.selectMonth("2008/07");
    savingsAccounts.checkEstimatedPosition(900);

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEstimatedPosition(1000);
  }

  public void testBalanceWithMissingOperationInFuture() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000., "2008/08/15")  //compte d'épargne
      .addTransaction("2008/08/12", 100.00, "P3 CE")
      .addTransaction("2008/06/11", 100.00, "P2 CE")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/12", -100.00, "V3 CC")
      .addTransaction("2008/08/10", -100.00, "Auchan")
      .addTransaction("2008/06/11", -100.00, "V2 CC")
      .load();
    operations.openPreferences().setFutureMonthsCount(6).validate();
    views.selectHome();
    mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();

    views.selectCategorization();
    categorization.setNewRecurring("Auchan", "Courses");
    categorization.selectTransactions("P3 CE", "P2 CE");

    categorization.selectSavings()
      .selectAndCreateSavingsSeries("Epargne", "Main accounts", "Account n. 111");

    categorization.selectTransactions("V3 CC", "V2 CC")
      .selectSavings().selectSeries("Epargne");

    budgetView.savings.alignAndPropagate("Epargne");

    budgetView.recurring.editSeries("Courses")
      .setRepeatCustom()
      .setPeriodMonths(1, 5, 9)
      .validate();

    budgetView.savings.editSeries("Epargne")
      .setRepeatCustom()
      .toggleMonth(1, 3, 6)
      .validate();

    // sur compte courant
    timeline.selectMonth("2009/06");
    mainAccounts.checkEstimatedPosition(-1000);
    savingsAccounts.checkEstimatedPosition(1700);

    timeline.selectMonth("2009/05");
    mainAccounts.checkEstimatedPosition(-1000);
    savingsAccounts.checkEstimatedPosition(1700);

    timeline.selectMonth("2009/04");
    mainAccounts.checkEstimatedPosition(-800);
    savingsAccounts.checkEstimatedPosition(1600);

    timeline.selectMonth("2009/03");
    mainAccounts.checkEstimatedPosition(-700);
    savingsAccounts.checkEstimatedPosition(1500);

    timeline.selectMonth("2009/02");
    mainAccounts.checkEstimatedPosition(-700);
    savingsAccounts.checkEstimatedPosition(1500);

    timeline.selectMonth("2009/01");
    mainAccounts.checkEstimatedPosition(-600);
    savingsAccounts.checkEstimatedPosition(1400);

    timeline.selectMonth("2008/12");
    mainAccounts.checkEstimatedPosition(-500);
    savingsAccounts.checkEstimatedPosition(1400);

    timeline.selectMonth("2008/08");
    mainAccounts.checkEstimatedPosition(0);
    savingsAccounts.checkEstimatedPosition(1000);

    timeline.selectMonth("2008/07");
    mainAccounts.checkEstimatedPosition(200);
    savingsAccounts.checkEstimatedPosition(900);

    timeline.selectMonth("2008/06");
    mainAccounts.checkEstimatedPosition(200);
    savingsAccounts.checkEstimatedPosition(900);
  }
}
