package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.Bank;

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

  public void testWithoutTransactionsInSavings() throws Exception {
  }

  public void testSavingWithMonthWithoutTransaction() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111", 1000., "2008/08/12")
      .addTransaction("2008/08/12", 100.00, "P3 CE")
      .addTransaction("2008/06/11", 100.00, "P2 CE")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/12", -100.00, "V3 CC")  //compte d'épargne
      .addTransaction("2008/08/11", -100.00, "V2 CC")
      .addTransaction("2008/08/10", -100.00, "Auchan")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    this.mainAccounts.edit(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setAsSavings()
      .validate();
    //sur compte courant
    timeline.selectMonth("2008/08");
    mainAccounts.checkEstimatedPosition(1000);

    timeline.selectMonth("2008/07");
    mainAccounts.checkEstimatedPosition(900);

    timeline.selectMonth("2008/06");
    mainAccounts.checkEstimatedPosition(900);

    // sur savings
    timeline.selectMonth("2008/06");
    savingsAccounts.checkEstimatedPosition(300, "30/06/2008");

    timeline.selectMonth("2008/07");
    savingsAccounts.checkEstimatedPosition(300, "31/07/2008");

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEstimatedPosition(0, "31/08/2008");
  }

  public void testMissingBalanceAtBeginIfNoOperation() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111", 1000., "2008/08/15")  //compte d'épargne
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
    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();

    //sur compte courant
    timeline.selectMonth("2008/07");
    mainAccounts.checkEstimatedPosition(300);

    timeline.selectMonth("2008/06");
    mainAccounts.checkEstimatedPosition(300);

    // sur savings
    timeline.selectMonth("2008/06");
    savingsAccounts.checkEstimatedPosition(900, "30/06/2008");

    timeline.selectMonth("2008/07");
    savingsAccounts.checkEstimatedPosition(900, "31/07/2008");

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEstimatedPosition(1000, "31/08/2008");


  }


  public void testBalanceWithMissingOperationInFuture() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111", 1000., "2008/08/15")  //compte d'épargne
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
    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();


    views.selectCategorization();
    categorization.setNewRecurring("Auchan", "courses");
    categorization.selectTransactions("P3 CE", "P2 CE");
    
    categorization.selectSavings()
      .selectAndCreateSavingsSeries("epargne", OfxBuilder.DEFAULT_ACCOUNT_NAME, "Account n. 111");

    categorization.selectTransactions("V3 CC", "V2 CC")
      .selectSavings().selectSeries("epargne");
    
    views.selectBudget();

    budgetView.recurring.editSeries("courses")
      .setFourMonths()
      .validate();

    budgetView.savings.editSeries("epargne")
      .setCustom()
      .toggleMonth(1, 3, 6)
      .validate();

    //sur compte courant
    timeline.selectMonth("2009/06");
    mainAccounts.checkEstimatedPosition(-1000);
    savingsAccounts.checkEstimatedPosition(1700, "30/06/2009");

    timeline.selectMonth("2009/05");
    mainAccounts.checkEstimatedPosition(-1000);
    savingsAccounts.checkEstimatedPosition(1700, "31/05/2009");

    timeline.selectMonth("2009/04");
    mainAccounts.checkEstimatedPosition(-800);
    savingsAccounts.checkEstimatedPosition(1600, "30/04/2009");

    timeline.selectMonth("2009/03");
    mainAccounts.checkEstimatedPosition(-700);
    savingsAccounts.checkEstimatedPosition(1500, "31/03/2009");

    timeline.selectMonth("2009/02");
    mainAccounts.checkEstimatedPosition(-700);
    savingsAccounts.checkEstimatedPosition(1500, "28/02/2009");

    timeline.selectMonth("2009/01");
    mainAccounts.checkEstimatedPosition(-600);
    savingsAccounts.checkEstimatedPosition(1400, "31/01/2009");

    timeline.selectMonth("2008/12");
    mainAccounts.checkEstimatedPosition(-500);
    savingsAccounts.checkEstimatedPosition(1400, "31/12/2008");

    timeline.selectMonth("2008/08");
    mainAccounts.checkEstimatedPosition(0);
    savingsAccounts.checkEstimatedPosition(1000, "31/08/2008");

    timeline.selectMonth("2008/07");
    mainAccounts.checkEstimatedPosition(200);
    savingsAccounts.checkEstimatedPosition(900, "31/07/2008");

    timeline.selectMonth("2008/06");
    mainAccounts.checkEstimatedPosition(200);
    savingsAccounts.checkEstimatedPosition(900, "30/06/2008");


  }

}