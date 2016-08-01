package com.budgetview.functests.budget;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.BankEntity;
import org.junit.Test;

public class BalanceTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2009/05/15");
    super.setUp();
  }

  @Test
  public void testBalanceWithManualTransactionInTheFuture() throws Exception {
    operations.openPreferences()
      .setFutureMonthsCount(3)
      .validate();
    OfxBuilder
      .init(this)
      .addTransaction("2009/04/20", -29.90, "Free Telecom")
      .load();

    accounts.createNewAccount()
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

    categorization.setNewRecurring("prov", "Courses");
    timeline.selectMonth("2009/05");
    categorization.editSeries("Courses")
      .checkEditableTargetAccount("Main accounts")
      .setTargetAccount("Manual")
      .setPropagationEnabled()
      .setAmount(10)
      .validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("19/08/2009", "Planned: Tel", -29.90, "Tel", -119.60, -159.60, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("11/08/2009", "Planned: Courses", -10.00, "Courses", -40., -129.70, "Manual")
      .add("19/07/2009", "Planned: Tel", -29.90, "Tel", -89.70, -119.70, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/07/2009", "PROV", -10.00, "Courses", -30.00, -89.80, "Manual")
      .add("19/06/2009", "Planned: Tel", -29.90, "Tel", -59.80, -79.80, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("11/06/2009", "Planned: Courses", -10.00, "Courses", -20., -49.90, "Manual")
      .add("22/05/2009", "PROV", -10.00, "Courses", -10.00, -39.90, "Manual")
      .add("19/05/2009", "Planned: Tel", -29.90, "Tel", -29.90, -29.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("20/04/2009", "FREE TELECOM", -29.90, "Tel", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    timeline.selectMonth("2009/05");
    mainAccounts
      .checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, -29.90)
      .checkAccount("Manual", 0, "2009/04/01");

    timeline.selectMonth("2009/06");
    mainAccounts
      .checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, -59.80)
      .checkAccount("Manual", 0, "2009/04/01");
  }

  @Test
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
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 0.00);

    timeline.selectMonth("2009/05");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 0.00);

    timeline.selectMonth("2009/06");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 0.00);
  }

  @Test
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
    mainAccounts.checkEndOfMonthPosition("Account n. 111", 1000);

    timeline.selectMonth("2008/07");
    mainAccounts.checkEndOfMonthPosition("Account n. 111", 900);

    timeline.selectMonth("2008/06");
    mainAccounts.checkEndOfMonthPosition("Account n. 111", 900);

    // sur savings
    timeline.selectMonth("2008/06");
    savingsAccounts
      .checkAccounts("Account n. 00001123")
      .checkEndOfMonthPosition("Account n. 00001123", 300.00);

    timeline.selectMonth("2008/07");
    savingsAccounts
      .checkEndOfMonthPosition("Account n. 00001123", 300.00);

    timeline.selectMonth("2008/08");
    savingsAccounts
      .checkEndOfMonthPosition("Account n. 00001123", 0.00);
  }

  @Test
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
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 300);

    timeline.selectMonth("2008/06");
    mainAccounts.checkEndOfMonthPosition(OfxBuilder.DEFAULT_ACCOUNT_NAME, 300);

    // sur savings
    timeline.selectMonth("2008/06");
    savingsAccounts.checkEndOfMonthPosition("Account n. 111", 900);

    timeline.selectMonth("2008/07");
    savingsAccounts.checkEndOfMonthPosition("Account n. 111", 900);

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEndOfMonthPosition("Account n. 111", 1000);
  }

  @Test
  public void testBalanceWithMissingOperationInFuture() throws Exception {
    operations.openPreferences().setFutureMonthsCount(6).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 111, "000111", 0.00, "2008/08/15")  //compte d'épargne
      .addTransaction("2008/08/12", -100.00, "V3 CC")
      .addTransaction("2008/08/10", -100.00, "Auchan")
      .addTransaction("2008/06/11", -100.00, "V2 CC")
      .load();
    OfxBuilder.init(this)
      .addBankAccount(-1, 222, "000222", 1000.00, "2008/08/15")  //compte d'épargne
      .addTransaction("2008/08/12", 100.00, "P3 CE")
      .addTransaction("2008/06/11", 100.00, "P2 CE")
      .load();
    mainAccounts.edit("Account n. 000222")
      .setAsSavings()
      .validate();

    categorization.setNewRecurring("Auchan", "Courses");
    categorization.selectTransactions("P3 CE", "P2 CE");

    categorization.selectTransfers()
      .selectAndCreateTransferSeries("Epargne", "Account n. 000111", "Account n. 000222");

    categorization.selectTransactions("V3 CC", "V2 CC")
      .selectTransfers().selectSeries("Epargne");

    budgetView.transfer.alignAndPropagate("Epargne");

    budgetView.recurring.editSeries("Courses")
      .setRepeatCustom()
      .setPeriodMonths(1, 5, 9)
      .setAmount(100.00)
      .validate();

    budgetView.transfer.editSeries("Epargne")
      .setRepeatCustom()
      .toggleMonth(1, 3, 6)
      .validate();

    mainAccounts.checkReferencePosition(0.00, "2008/08/12");
    savingsAccounts.checkReferencePosition(1000.00, "2008/08/12");

    timeline.selectMonth("2009/06");
    mainAccounts.checkEndOfMonthPosition("Account n. 000111", -200);
    savingsAccounts.checkEndOfMonthPosition("Account n. 000222", 1200.00);

    mainAccounts.checkReferencePosition(0.00, "2008/08/12");
    savingsAccounts.checkReferencePosition(1000.00, "2008/08/12");

    timeline.selectMonth("2009/05");
    budgetView.recurring.checkSeriesPresent("Courses");
    mainAccounts.checkEndOfMonthPosition("Account n. 000111", -200.00);
    budgetView.transfer.checkSeriesNotPresent("Epargne");
    savingsAccounts.checkEndOfMonthPosition("Account n. 000222", 1200.00);

    timeline.selectMonth("2009/04");
    budgetView.recurring.checkSeriesNotPresent("Courses");
    mainAccounts.checkEndOfMonthPosition("Account n. 000111", -200.00);
    budgetView.transfer.checkSeriesNotPresent("Epargne");
    savingsAccounts.checkEndOfMonthPosition("Account n. 000222", 1200.00);

    timeline.selectMonth("2009/03");
    budgetView.recurring.checkSeriesNotPresent("Courses");
    mainAccounts.checkEndOfMonthPosition("Account n. 000111", -200.00);
    budgetView.transfer.checkSeriesPresent("Epargne");
    savingsAccounts.checkEndOfMonthPosition("Account n. 000222", 1200.00);

    timeline.selectMonth("2009/02");
    budgetView.recurring.checkSeriesNotPresent("Courses");
    mainAccounts.checkEndOfMonthPosition("Account n. 000111", -100.00);
    budgetView.transfer.checkSeriesNotPresent("Epargne");
    savingsAccounts.checkEndOfMonthPosition("Account n. 000222", 1100.00);

    timeline.selectMonth("2009/01");
    budgetView.recurring.checkSeriesPresent("Courses");
    mainAccounts.checkEndOfMonthPosition("Account n. 000111", -100.00);
    budgetView.transfer.checkSeriesPresent("Epargne");
    savingsAccounts.checkEndOfMonthPosition("Account n. 000222", 1100.00);

    timeline.selectMonth("2008/12");
    budgetView.recurring.checkSeriesNotPresent("Courses");
    mainAccounts.checkEndOfMonthPosition("Account n. 000111", 0.00);
    budgetView.transfer.checkSeriesNotPresent("Epargne");
    savingsAccounts.checkEndOfMonthPosition("Account n. 000222", 1000.00);

    timeline.selectMonth("2008/08"); // Series present because there are actual transactions
    budgetView.recurring.checkSeriesPresent("Courses");
    mainAccounts.checkEndOfMonthPosition("Account n. 000111", 0.00);
    budgetView.transfer.checkSeriesPresent("Epargne");
    savingsAccounts.checkEndOfMonthPosition("Account n. 000222", 1000.00);

    timeline.selectMonth("2008/07");
    budgetView.recurring.checkSeriesNotPresent("Courses");
    mainAccounts.checkEndOfMonthPosition("Account n. 000111", 200.00);
    budgetView.transfer.checkSeriesNotPresent("Epargne");
    savingsAccounts.checkEndOfMonthPosition("Account n. 000222", 900.00);

    timeline.selectMonth("2008/06");
    budgetView.recurring.checkSeriesNotPresent("Courses");
    mainAccounts.checkEndOfMonthPosition("Account n. 000111", 200.00);
    budgetView.transfer.checkSeriesPresent("Epargne");
    savingsAccounts.checkEndOfMonthPosition("Account n. 000222", 900.00);
  }
}
