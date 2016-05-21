package com.budgetview.functests.budget;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;

public class MultiAccountSeriesTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2014/11");
    super.setUp();
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(2).validate();
  }

  public void testForecastWithoutExistingTransactions() throws Exception {
    accounts.createMainAccount("Main1", "4321", 1000.00);
    accounts.createSavingsAccount("Savings1", 10000.00); // Ignored in envelope

    budgetView.variable.createSeries()
      .setName("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main accounts")
      .setTargetAccount("Main accounts")
      .selectNegativeAmounts()
      .setAmount(300.00)
      .validate();

    budgetView.variable.checkContent("| Leisures | 0.00 | 300.00 |");

    transactions.showPlannedTransactions();
    timeline.selectMonths(201411, 201412, 201501);

    // 1. Single account
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -300.00, "Leisures", 100.00, 100.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -300.00, "Leisures", 400.00, 400.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -300.00, "Leisures", 700.00, 700.00, "Main1")
      .check();

    // 2. When adding a second account, the envelope distributes 50% on each
    accounts.createMainAccount("Main2", "4321", 2000.00);
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 1550.00, 2100.00, "Main2")
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 550.00, 2250.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 1700.00, 2400.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 700.00, 2550.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -150.00, "Leisures", 1850.00, 2700.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -150.00, "Leisures", 850.00, 2850.00, "Main1")
      .check();

    // 3. When adding a third account, the envelope distributes 33% on each
    accounts.createNewAccount()
      .setName("Main3")
      .selectBank("CIC")
      .setPosition(3000)
      .validate();
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -100.00, "Leisures", 2700.00, 5100.00, "Main3")
      .add("11/01/2015", "Planned: Leisures", -100.00, "Leisures", 1700.00, 5200.00, "Main2")
      .add("11/01/2015", "Planned: Leisures", -100.00, "Leisures", 700.00, 5300.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -100.00, "Leisures", 2800.00, 5400.00, "Main3")
      .add("11/12/2014", "Planned: Leisures", -100.00, "Leisures", 1800.00, 5500.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -100.00, "Leisures", 800.00, 5600.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -100.00, "Leisures", 2900.00, 5700.00, "Main3")
      .add("11/11/2014", "Planned: Leisures", -100.00, "Leisures", 1900.00, 5800.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -100.00, "Leisures", 900.00, 5900.00, "Main1")
      .check();
  }

  public void testUsesProportionsFromTheCurrentMonthAndPastTwoMonthsOperationsOnly() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("000111", 1000.00, "2014/11/10")
      .addTransaction("2014/11/05", -10.00, "LEISURE1")
      .addTransaction("2014/10/06", -30.00, "LEISURE1")
      .addTransaction("2014/09/06", -60.00, "LEISURE1")
      .addTransaction("2014/08/06", -50.00, "LEISURE1") // Ignored
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000222", 2000.00, "2014/11/10") // Half the amounts of the other account
      .addTransaction("2014/11/06", -10.00, "LEISURE2")
      .addTransaction("2014/10/07", -5.00, "LEISURE2")
      .addTransaction("2014/09/03", -35.00, "LEISURE2")
      .addTransaction("2014/08/06", -500.00, "LEISURE2") // Ignored
      .load();

    mainAccounts.edit("Account n. 000111").setName("Main1").validate();
    mainAccounts.edit("Account n. 000222").setName("Main2").validate();

    budgetView.variable.createSeries()
      .setName("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main2", "Main accounts")
      .setTargetAccount("Main accounts")
      .selectNegativeAmounts()
      .setAmount(300.00)
      .validate();

    categorization.setVariable("LEISURE1", "Leisures");
    categorization.setVariable("LEISURE2", "Leisures");

    transactions.showPlannedTransactions();
    timeline.selectMonths(201411, 201412, 201501);
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -200.00, "Leisures", 410.00, 2120.00, "Main1")
      .add("04/01/2015", "Planned: Leisures", -100.00, "Leisures", 1710.00, 2320.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -200.00, "Leisures", 610.00, 2420.00, "Main1")
      .add("04/12/2014", "Planned: Leisures", -100.00, "Leisures", 1810.00, 2620.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -190.00, "Leisures", 810.00, 2720.00, "Main1")
      .add("06/11/2014", "Planned: Leisures", -90.00, "Leisures", 1910.00, 2910.00, "Main2")
      .add("06/11/2014", "LEISURE2", -10.00, "Leisures", 2000.00, 3000.00, "Main2")
      .add("05/11/2014", "LEISURE1", -10.00, "Leisures", 1000.00, 3010.00, "Main1")
      .check();
  }

  public void testPlansTransactionsOnlyForAccountsInWhichTheEnvelopeHasBeenUsed() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("000111", 1000.00, "2014/11/10")
      .addTransaction("2014/11/05", -10.00, "LEISURE1")
      .addTransaction("2014/10/06", -20.00, "LEISURE1")
      .addTransaction("2014/09/06", -70.00, "LEISURE1")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000222", 2000.00, "2014/11/10") // Half the amounts of the other account
      .addTransaction("2014/11/06", -5.00, "LEISURE2")
      .addTransaction("2014/10/07", -10.00, "LEISURE2")
      .addTransaction("2014/09/03", -35.00, "LEISURE2")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000333", 3000.00, "2014/11/10") // Not related with this envelope => no planned for this one
      .addTransaction("2014/11/05", -75.00, "OTHER")
      .addTransaction("2014/10/04", -35.00, "OTHER")
      .addTransaction("2014/09/03", -20.00, "OTHER")
      .addTransaction("2014/08/02", -50.00, "OTHER")
      .load();

    mainAccounts.edit("Account n. 000111").setName("Main1").validate();
    mainAccounts.edit("Account n. 000222").setName("Main2").validate();
    mainAccounts.edit("Account n. 000333").setName("Main3").validate();

    budgetView.variable.createSeries()
      .setName("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main2", "Main3", "Main accounts")
      .setTargetAccount("Main accounts")
      .selectNegativeAmounts()
      .setAmount(300.00)
      .validate();

    categorization.setVariable("LEISURE1", "Leisures");
    categorization.setVariable("LEISURE2", "Leisures");

    transactions.showPlannedTransactions();
    timeline.selectMonths(201411, 201412, 201501);
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -100.00, "Leisures", 1705.00, 5115.00, "Main2")
      .add("11/01/2015", "Planned: Leisures", -200.00, "Leisures", 410.00, 5215.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -100.00, "Leisures", 1805.00, 5415.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -200.00, "Leisures", 610.00, 5515.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -95.00, "Leisures", 1905.00, 5715.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -190.00, "Leisures", 810.00, 5810.00, "Main1")
      .add("06/11/2014", "LEISURE2", -5.00, "Leisures", 2000.00, 6000.00, "Main2")
      .add("05/11/2014", "OTHER", -75.00, "To categorize", 3000.00, 6005.00, "Main3")
      .add("05/11/2014", "LEISURE1", -10.00, "Leisures", 1000.00, 6080.00, "Main1")
      .check();
  }

  public void testLooksInThePastForNonMonthlyPeriodicity() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("000111", 1000.00, "2014/11/10")
      .addTransaction("2014/11/05", -5.00, "LEISURE1")
      .addTransaction("2014/09/06", -25.00, "LEISURE1")
      .addTransaction("2014/05/06", -70.00, "LEISURE1")
      .addTransaction("2014/03/06", -20.00, "LEISURE1")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000222", 2000.00, "2014/11/10") // Half the amounts of the other account
      .addTransaction("2014/11/06", -5.00, "LEISURE2")
      .addTransaction("2014/09/07", -10.00, "LEISURE2")
      .addTransaction("2014/05/03", -35.00, "LEISURE2")
      .addTransaction("2014/02/03", -20.00, "LEISURE2")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000333", 3000.00, "2014/11/10") // Not related with this envelope => no planned for this one
      .addTransaction("2014/11/06", -5.00, "OTHER")
      .addTransaction("2014/10/07", -10.00, "OTHER")
      .addTransaction("2014/09/03", -35.00, "OTHER")
      .addTransaction("2014/08/06", -500.00, "OTHER")
      .load();

    mainAccounts.edit("Account n. 000111").setName("Main1").validate();
    mainAccounts.edit("Account n. 000222").setName("Main2").validate();
    mainAccounts.edit("Account n. 000333").setName("Main3").validate();

    budgetView.variable.createSeries()
      .setName("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main2", "Main3", "Main accounts")
      .setTargetAccount("Main accounts")
      .selectNegativeAmounts()
      .setAmount(300.00)
      .validate();

    categorization.setVariable("LEISURE1", "Leisures");
    categorization.setVariable("LEISURE2", "Leisures");

    transactions.showPlannedTransactions();
    timeline.selectMonths(201411, 201412, 201501);
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -100.00, "Leisures", 1705.00, 5110.00, "Main2")
      .add("11/01/2015", "Planned: Leisures", -200.00, "Leisures", 405.00, 5210.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -100.00, "Leisures", 1805.00, 5410.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -200.00, "Leisures", 605.00, 5510.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -95.00, "Leisures", 1905.00, 5710.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -195.00, "Leisures", 805.00, 5805.00, "Main1")
      .add("06/11/2014", "OTHER", -5.00, "To categorize", 3000.00, 6000.00, "Main3")
      .add("06/11/2014", "LEISURE2", -5.00, "Leisures", 2000.00, 6005.00, "Main2")
      .add("05/11/2014", "LEISURE1", -5.00, "Leisures", 1000.00, 6010.00, "Main1")
      .check();
  }

  public void testIgnoresInactiveAccounts() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("000111", 1000.00, "2014/11/10")
      .addTransaction("2014/11/05", -10.00, "LEISURE1")
      .addTransaction("2014/10/06", -20.00, "LEISURE1")
      .addTransaction("2014/09/06", -70.00, "LEISURE1")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000222", 2000.00, "2014/11/10") // Half the amounts of the other account
      .addTransaction("2014/11/06", -5.00, "LEISURE2")
      .addTransaction("2014/10/07", -10.00, "LEISURE2")
      .addTransaction("2014/09/03", -35.00, "LEISURE2")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000333", 3000.00, "2014/11/10")
      .addTransaction("2014/11/05", -20.00, "LEISURE3")
      .addTransaction("2014/10/04", -20.00, "LEISURE3")
      .addTransaction("2014/09/03", -60.00, "LEISURE3")
      .load();

    mainAccounts.edit("Account n. 000111").setName("Main1").validate();
    mainAccounts.edit("Account n. 000222").setName("Main2").validate();
    mainAccounts.edit("Account n. 000333").setName("Main3")
      .setEndDate("2014/11/30")
      .validate();

    budgetView.variable.createSeries()
      .setName("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main2", "Main3", "Main accounts")
      .setTargetAccount("Main accounts")
      .selectNegativeAmounts()
      .setAmount(300.00)
      .validate();

    categorization.setVariable("LEISURE1", "Leisures");
    categorization.setVariable("LEISURE2", "Leisures");
    categorization.setVariable("LEISURE3", "Leisures");

    transactions.showPlannedTransactions();
    timeline.selectMonths(201411, 201412, 201501);
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -200.00, "Leisures", 490.00, 2235.00, "Main1")
      .add("04/01/2015", "Planned: Leisures", -100.00, "Leisures", 1745.00, 2435.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -200.00, "Leisures", 690.00, 2535.00, "Main1")
      .add("04/12/2014", "Planned: Leisures", -100.00, "Leisures", 1845.00, 2735.00, "Main2")
      .add("06/11/2014", "Planned: Leisures", -100.00, "Leisures", 2900.00, 5735.00, "Main3")
      .add("06/11/2014", "Planned: Leisures", -55.00, "Leisures", 1945.00, 5835.00, "Main2")
      .add("06/11/2014", "Planned: Leisures", -110.00, "Leisures", 890.00, 5890.00, "Main1")
      .add("06/11/2014", "LEISURE2", -5.00, "Leisures", 2000.00, 6000.00, "Main2")
      .add("05/11/2014", "LEISURE3", -20.00, "Leisures", 3000.00, 6005.00, "Main3")
      .add("05/11/2014", "LEISURE1", -10.00, "Leisures", 1000.00, 6025.00, "Main1")
      .check();
  }

  public void testActualForOneAccountExceedsItsPlannedAmountForTheCurrentMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("000111", 1000.00, "2014/11/10")
      .addTransaction("2014/11/05", -25.00, "LEISURE1")
      .addTransaction("2014/10/06", -100.00, "LEISURE1")
      .addTransaction("2014/09/06", -125.00, "LEISURE1")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000222", 2000.00, "2014/11/10") // Double the amounts of the other account
      .addTransaction("2014/11/06", -250.00, "LEISURE2")
      .addTransaction("2014/10/07", -50.00, "LEISURE2")
      .addTransaction("2014/09/03", -200.00, "LEISURE2")
      .load();

    mainAccounts.edit("Account n. 000111").setName("Main1").validate();
    mainAccounts.edit("Account n. 000222").setName("Main2").validate();
    budgetView.variable.createSeries()
      .setName("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main2", "Main accounts")
      .setTargetAccount("Main accounts")
      .selectNegativeAmounts()
      .setAmount(300.00)
      .validate();
    categorization.setVariable("LEISURE1", "Leisures");
    categorization.setVariable("LEISURE2", "Leisures");

    transactions.showPlannedTransactions();
    timeline.selectMonths(201411, 201412, 201501);
    transactions.initAmountContent()
      .add("04/01/2015", "Planned: Leisures", -200.00, "Leisures", 1600.00, 2375.00, "Main2")
      .add("04/01/2015", "Planned: Leisures", -100.00, "Leisures", 775.00, 2575.00, "Main1")
      .add("04/12/2014", "Planned: Leisures", -200.00, "Leisures", 1800.00, 2675.00, "Main2")
      .add("04/12/2014", "Planned: Leisures", -100.00, "Leisures", 875.00, 2875.00, "Main1")
      .add("06/11/2014", "Planned: Leisures", -25.00, "Leisures", 975.00, 2975.00, "Main1") // Completes only on Main1
      .add("06/11/2014", "LEISURE2", -250.00, "Leisures", 2000.00, 3000.00, "Main2")
      .add("05/11/2014", "LEISURE1", -25.00, "Leisures", 1000.00, 3250.00, "Main1")
      .check();
  }

  public void testActualForEachAccountExceedsPlannedAmountForTheCurrentMonth() throws Exception {

    OfxBuilder
      .init(this)
      .addBankAccount("000111", 1000.00, "2014/11/10")
      .addTransaction("2014/11/05", -200.00, "LEISURE1")
      .addTransaction("2014/10/06", -100.00, "LEISURE1")
      .addTransaction("2014/09/06", -100.00, "LEISURE1")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000222", 2000.00, "2014/11/10") // Double the amounts of the other account
      .addTransaction("2014/11/06", -400.00, "LEISURE2")
      .addTransaction("2014/10/07", -200.00, "LEISURE2")
      .addTransaction("2014/09/03", -200.00, "LEISURE2")
      .load();

    mainAccounts.edit("Account n. 000111").setName("Main1").validate();
    mainAccounts.edit("Account n. 000222").setName("Main2").validate();

    budgetView.variable.createSeries()
      .setName("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main2", "Main accounts")
      .setTargetAccount("Main accounts")
      .selectNegativeAmounts()
      .setAmount(300.00)
      .validate();

    categorization.setVariable("LEISURE1", "Leisures");
    categorization.setVariable("LEISURE2", "Leisures");

    transactions.showPlannedTransactions();
    timeline.selectMonths(201411, 201412, 201501);
    transactions.initAmountContent()
      .add("04/01/2015", "Planned: Leisures", -200.00, "Leisures", 1600.00, 2400.00, "Main2")
      .add("04/01/2015", "Planned: Leisures", -100.00, "Leisures", 800.00, 2600.00, "Main1")
      .add("04/12/2014", "Planned: Leisures", -200.00, "Leisures", 1800.00, 2700.00, "Main2")
      .add("04/12/2014", "Planned: Leisures", -100.00, "Leisures", 900.00, 2900.00, "Main1")
      .add("06/11/2014", "LEISURE2", -400.00, "Leisures", 2000.00, 3000.00, "Main2") // No other planned for November
      .add("05/11/2014", "LEISURE1", -200.00, "Leisures", 1000.00, 3400.00, "Main1")
      .check();
  }

  public void testOneOfTheAccountsHasAnActualOfTheOppositeSign() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("000111", 1000.00, "2014/11/10")
      .addTransaction("2014/11/05", 25.00, "LEISURE1")
      .addTransaction("2014/10/06", 100.00, "LEISURE1")
      .addTransaction("2014/09/06", 125.00, "LEISURE1")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000222", 2000.00, "2014/11/10") // Double the amounts of the other account
      .addTransaction("2014/11/06", -250.00, "LEISURE2")
      .addTransaction("2014/10/07", -50.00, "LEISURE2")
      .addTransaction("2014/09/03", -200.00, "LEISURE2")
      .load();

    mainAccounts.edit("Account n. 000111").setName("Main1").validate();
    mainAccounts.edit("Account n. 000222").setName("Main2").validate();

    budgetView.variable.createSeries()
      .setName("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main2", "Main accounts")
      .setTargetAccount("Main accounts")
      .selectNegativeAmounts()
      .setAmount(300.00)
      .validate();

    categorization.setVariable("LEISURE1", "Leisures");
    categorization.setVariable("LEISURE2", "Leisures");

    transactions.showPlannedTransactions();
    timeline.selectMonths(201411, 201412, 201501);
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -600.00, "Leisures", 450.00, 2325.00, "Main2")
      .add("11/01/2015", "Planned: Leisures", 300.00, "Leisures", 1875.00, 2925.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -600.00, "Leisures", 1050.00, 2625.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", 300.00, "Leisures", 1575.00, 3225.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -350.00, "Leisures", 1650.00, 2925.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", 275.00, "Leisures", 1275.00, 3275.00, "Main1")
      .add("06/11/2014", "LEISURE2", -250.00, "Leisures", 2000.00, 3000.00, "Main2")
      .add("05/11/2014", "LEISURE1", 25.00, "Leisures", 1000.00, 3250.00, "Main1")
      .check();
  }

  public void testMultiTransactionsShape() throws Exception {

    OfxBuilder
      .init(this)
      .addBankAccount("000111", 1000.00, "2014/11/20")
      .addTransaction("2014/11/20", -10.00, "LEISURE1")
      .addTransaction("2014/11/05", -40.00, "LEISURE1")
      .addTransaction("2014/10/20", -10.00, "LEISURE1")
      .addTransaction("2014/10/06", -40.00, "LEISURE1")
      .addTransaction("2014/09/20", -10.00, "LEISURE1")
      .addTransaction("2014/09/06", -40.00, "LEISURE1")
      .addTransaction("2014/08/12", -30.00, "LEISURE1") // Ignored
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000222", 2000.00, "2014/11/20")
      .addTransaction("2014/11/20", -30.00, "LEISURE2")
      .addTransaction("2014/11/05", -70.00, "LEISURE2")
      .addTransaction("2014/10/20", -30.00, "LEISURE2")
      .addTransaction("2014/10/06", -70.00, "LEISURE2")
      .addTransaction("2014/09/20", -30.00, "LEISURE2")
      .addTransaction("2014/09/06", -70.00, "LEISURE2")
      .addTransaction("2014/08/12", -35.00, "LEISURE2") // Ignored
      .load();

    mainAccounts.edit("Account n. 000111").setName("Main1").validate();
    mainAccounts.edit("Account n. 000222").setName("Main2").validate();

    budgetView.variable.createSeries()
      .setName("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main2", "Main accounts")
      .setTargetAccount("Main accounts")
      .selectNegativeAmounts()
      .setAmount(300.00)
      .validate();

    categorization.setVariable("LEISURE1", "Leisures");
    categorization.setVariable("LEISURE2", "Leisures");

    transactions.showPlannedTransactions();
    timeline.selectMonths(201411, 201412, 201501);
    transactions.initAmountContent()
      .add("19/01/2015", "Planned: Leisures", -54.00, "Leisures", 1470.00, 2210.00, "Main2")
      .add("19/01/2015", "Planned: Leisures", -27.00, "Leisures", 740.00, 2264.00, "Main1")
      .add("04/01/2015", "Planned: Leisures", -146.00, "Leisures", 1524.00, 2291.00, "Main2")
      .add("04/01/2015", "Planned: Leisures", -73.00, "Leisures", 767.00, 2437.00, "Main1")
      .add("19/12/2014", "Planned: Leisures", -54.00, "Leisures", 1670.00, 2510.00, "Main2")
      .add("19/12/2014", "Planned: Leisures", -27.00, "Leisures", 840.00, 2564.00, "Main1")
      .add("04/12/2014", "Planned: Leisures", -146.00, "Leisures", 1724.00, 2591.00, "Main2")
      .add("04/12/2014", "Planned: Leisures", -73.00, "Leisures", 867.00, 2737.00, "Main1")
      .add("20/11/2014", "LEISURE2", -30.00, "Leisures", 1870.00, 2810.00, "Main2")
      .add("20/11/2014", "LEISURE1", -10.00, "Leisures", 940.00, 2840.00, "Main1")
      .add("19/11/2014", "Planned: Leisures", -54.00, "Leisures", 1900.00, 2850.00, "Main2")
      .add("19/11/2014", "Planned: Leisures", -27.00, "Leisures", 950.00, 2904.00, "Main1")
      .add("05/11/2014", "Planned: Leisures", -46.00, "Leisures", 1954.00, 2931.00, "Main2")
      .add("05/11/2014", "Planned: Leisures", -23.00, "Leisures", 977.00, 2977.00, "Main1")
      .add("05/11/2014", "LEISURE2", -70.00, "Leisures", 2000.00, 3000.00, "Main2")
      .add("05/11/2014", "LEISURE1", -40.00, "Leisures", 1000.00, 3070.00, "Main1")
      .check();
  }

  public void testSwitchingFromMultiToMonoAccountAndBackWithoutAssignedTransactions() throws Exception {
    accounts.createMainAccount("Main1", "4321", 1000.00);
    accounts.createMainAccount("Main2", "4321", 2000.00);
    accounts.createSavingsAccount("Savings1", 10000.00); // Ignored in envelope

    // 1. Begin as multi-account - the envelope is split evenly among accounts
    budgetView.variable.createSeries()
      .setName("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main2", "Main accounts")
      .setTargetAccount("Main accounts")
      .selectNegativeAmounts()
      .setAmount(300.00)
      .validate();
    budgetView.variable.checkContent("| Leisures | 0.00 | 300.00 |");

    transactions.showPlannedTransactions();
    timeline.selectMonths(201411, 201412, 201501);
    budgetView.variable.checkContent("| Leisures | 0.00 | 900.00 |");
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 1550.00, 2100.00, "Main2")
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 550.00, 2250.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 1700.00, 2400.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 700.00, 2550.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -150.00, "Leisures", 1850.00, 2700.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -150.00, "Leisures", 850.00, 2850.00, "Main1")
      .check();

    // 2. Switch to mono-account
    budgetView.variable.editSeries("Leisures")
      .setTargetAccount("Main2")
      .validate();
    budgetView.variable.checkContent("| Leisures | 0.00 | 900.00 |");
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -300.00, "Leisures", 1100.00, 2100.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -300.00, "Leisures", 1400.00, 2400.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -300.00, "Leisures", 1700.00, 2700.00, "Main2")
      .check();

    // 3. Switch back to multi-account
    budgetView.variable.editSeries("Leisures")
      .setTargetAccount("Main accounts")
      .validate();
    budgetView.variable.checkContent("| Leisures | 0.00 | 900.00 |");
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 1550.00, 2100.00, "Main2")
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 550.00, 2250.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 1700.00, 2400.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 700.00, 2550.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -150.00, "Leisures", 1850.00, 2700.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -150.00, "Leisures", 850.00, 2850.00, "Main1")
      .check();
  }

  public void testMultiAccountsNotProposedForTransferSeries() throws Exception {
    operations.hideSignposts();

    accounts.createNewAccount()
      .setName("Main1")
      .selectBank("CIC")
      .setPosition(1000)
      .validate();

    accounts.createNewAccount()
      .setName("Savings1")
      .setAsSavings()
      .selectBank("LCL")
      .setPosition(10000)
      .validate();

    budgetView.transfer.createSeries()
      .setName("Transfer")
      .checkFromContentEquals("Main1", "Savings1", "External account") // User-created + external accounts only
      .checkToContentEquals("Main1", "Savings1", "External account")
      .cancel();
  }

  public void testCanChangeTargetAccountInRelationWithAlreadyAssignedTransactions() throws Exception {

    budgetView.variable.createSeries()
      .setName("Leisures")
      .checkAvailableTargetAccounts("Main accounts")
      .setTargetAccount("Main accounts")
      .selectNegativeAmounts()
      .setAmount(300.00)
      .validate();

    timeline.selectMonth(201412);
    transactions.showPlannedTransactions();
    transactions.checkEmpty();

    accounts.createMainAccount("Main1", "4321", 1000.00);
    transactions.initAmountContent()
      .add("11/12/2014", "Planned: Leisures", -300.00, "Leisures", 400.00, 400.00, "Main1")
      .check();

    accounts.createMainAccount("Main2", "4321", 2000.00);
    transactions.initAmountContent()
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 1700.00, 2400.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 700.00, 2550.00, "Main1")
      .check();

    OfxBuilder
      .init(this)
      .addBankAccount("000111", 1000.00, "2014/11/20")
      .addTransaction("2014/11/20", -10.00, "LEISURE1")
      .addTransaction("2014/11/05", -40.00, "LEISURE1")
      .addTransaction("2014/10/20", -10.00, "LEISURE1")
      .addTransaction("2014/10/06", -40.00, "LEISURE1")
      .loadInAccount("Main1");

    OfxBuilder
      .init(this)
      .addBankAccount("000222", 2000.00, "2014/11/20")
      .addTransaction("2014/11/20", -30.00, "LEISURE2")
      .addTransaction("2014/11/05", -70.00, "LEISURE2")
      .addTransaction("2014/10/20", -30.00, "LEISURE2")
      .addTransaction("2014/10/06", -70.00, "LEISURE2")
      .loadInAccount("Main2");

    budgetView.variable.editSeries("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main2", "Main accounts")
      .setTargetAccount("Main accounts")
      .validate();
    timeline.selectMonth(201412);
    transactions.initAmountContent()
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 1500.00, 2100.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 600.00, 2250.00, "Main1")
      .check();

    categorization.setVariable("LEISURE1", "Leisures");
    budgetView.variable.editSeries("Leisures")
      .checkEditableTargetAccount("Main accounts")
      .checkAvailableTargetAccounts("Main1", "Main accounts")
      .validate();
    timeline.selectMonths(201411, 201412);
    transactions.initAmountContent()
      .add("11/12/2014", "Planned: Leisures", -300.00, "Leisures", 350.00, 2150.00, "Main1")
      .add("20/11/2014", "LEISURE2", -30.00, "To categorize", 1800.00, 2450.00, "Main2")
      .add("20/11/2014", "LEISURE1", -10.00, "Leisures", 650.00, 2480.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -250.00, "Leisures", 660.00, 2490.00, "Main1")
      .add("05/11/2014", "LEISURE2", -70.00, "To categorize", 1830.00, 2740.00, "Main2")
      .add("05/11/2014", "LEISURE1", -40.00, "Leisures", 910.00, 2810.00, "Main1")
      .check();

    budgetView.variable.editSeries("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main accounts")
      .setTargetAccount("Main1")
      .validate();
    timeline.selectMonths(201411, 201412);
    transactions.initAmountContent()
      .add("11/12/2014", "Planned: Leisures", -300.00, "Leisures", 350.00, 2150.00, "Main1")
      .add("20/11/2014", "LEISURE2", -30.00, "To categorize", 1800.00, 2450.00, "Main2")
      .add("20/11/2014", "LEISURE1", -10.00, "Leisures", 650.00, 2480.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -250.00, "Leisures", 660.00, 2490.00, "Main1")
      .add("05/11/2014", "LEISURE2", -70.00, "To categorize", 1830.00, 2740.00, "Main2")
      .add("05/11/2014", "LEISURE1", -40.00, "Leisures", 910.00, 2810.00, "Main1")
      .check();

    budgetView.variable.editSeries("Leisures")
      .checkAvailableTargetAccounts("Main1", "Main accounts")
      .setTargetAccount("Main accounts")
      .validate();
    categorization.setVariable("LEISURE2", "Leisures");
    budgetView.variable.editSeries("Leisures")
      .checkReadOnlyTargetAccount("Main accounts")
      .validate();
    transactions.initAmountContent()
      .add("19/12/2014", "Planned: Leisures", -54.00, "Leisures", 1500.00, 2250.00, "Main2")
      .add("19/12/2014", "Planned: Leisures", -27.00, "Leisures", 750.00, 2304.00, "Main1")
      .add("04/12/2014", "Planned: Leisures", -146.00, "Leisures", 1554.00, 2331.00, "Main2")
      .add("04/12/2014", "Planned: Leisures", -73.00, "Leisures", 777.00, 2477.00, "Main1")
      .add("20/11/2014", "LEISURE2", -30.00, "Leisures", 1700.00, 2550.00, "Main2")
      .add("20/11/2014", "LEISURE1", -10.00, "Leisures", 850.00, 2580.00, "Main1")
      .add("19/11/2014", "Planned: Leisures", -54.00, "Leisures", 1730.00, 2590.00, "Main2")
      .add("19/11/2014", "Planned: Leisures", -27.00, "Leisures", 860.00, 2644.00, "Main1")
      .add("05/11/2014", "Planned: Leisures", -46.00, "Leisures", 1784.00, 2671.00, "Main2")
      .add("05/11/2014", "Planned: Leisures", -23.00, "Leisures", 887.00, 2717.00, "Main1")
      .add("05/11/2014", "LEISURE2", -70.00, "Leisures", 1830.00, 2740.00, "Main2")
      .add("05/11/2014", "LEISURE1", -40.00, "Leisures", 910.00, 2810.00, "Main1")
      .check();

    categorization.selectTransactions("LEISURE1").setUncategorized();
    budgetView.variable.editSeries("Leisures")
      .checkEditableTargetAccount("Main accounts")
      .checkAvailableTargetAccounts("Main2", "Main accounts")
      .setTargetAccount("Main2")
      .validate();
    transactions.initAmountContent()
      .add("19/12/2014", "Planned: Leisures", -90.00, "Leisures", 1300.00, 2200.00, "Main2")
      .add("04/12/2014", "Planned: Leisures", -210.00, "Leisures", 1390.00, 2290.00, "Main2")
      .add("20/11/2014", "LEISURE2", -30.00, "Leisures", 1600.00, 2500.00, "Main2")
      .add("20/11/2014", "LEISURE1", -10.00, "To categorize", 900.00, 2530.00, "Main1")
      .add("19/11/2014", "Planned: Leisures", -90.00, "Leisures", 1630.00, 2540.00, "Main2")
      .add("05/11/2014", "Planned: Leisures", -110.00, "Leisures", 1720.00, 2630.00, "Main2")
      .add("05/11/2014", "LEISURE2", -70.00, "Leisures", 1830.00, 2740.00, "Main2")
      .add("05/11/2014", "LEISURE1", -40.00, "To categorize", 910.00, 2810.00, "Main1")
      .check();
  }
}
