package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class MultiAccountSeriesTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2014/11");
    super.setUp();
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(2).validate();
  }

  public void testForecastWithoutExistingTransactions() throws Exception {
    accounts.createMainAccount("Main1", 1000.00);
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

    // 2. When adding a second account, the envelope distributes 33% on each
    accounts.createMainAccount("Main2", 2000.00);
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 100.00, 100.00, "Main1")
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 250.00, 250.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 400.00, 400.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 550.00, 550.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -150.00, "Leisures", 700.00, 700.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -150.00, "Leisures", 850.00, 850.00, "Main2")
      .check();

    // 3. When adding a third account, the envelope distributes 33% on each
    accounts.createNewAccount()
      .setName("Main3")
      .selectBank("CIC")
      .setPosition(3000)
      .validate();
    transactions.initAmountContent()  // TODO: revoir les soldes des comptes
      .add("11/01/2015", "Planned: Leisures", -100.00, "Leisures", 700.00, 5100.00, "Main1")
      .add("11/01/2015", "Planned: Leisures", -100.00, "Leisures", 1700.00, 5200.00, "Main2")
      .add("11/01/2015", "Planned: Leisures", -100.00, "Leisures", 2700.00, 5300.00, "Main3")
      .add("11/12/2014", "Planned: Leisures", -100.00, "Leisures", 800.00, 5400.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -100.00, "Leisures", 1800.00, 55000.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -100.00, "Leisures", 28000.00, 5600.00, "Main3")
      .add("11/11/2014", "Planned: Leisures", -100.00, "Leisures", 9000.00, 5700.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -100.00, "Leisures", 19000.00, 5850.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -100.00, "Leisures", 2900.00, 5900.00, "Main3")
      .check();
  }

  public void testUsesProportionsFromTheCurrentMonthAndPastTwoMonthsOperationsOnly() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("000111", 1000.00, "2014/11/10")
      .addTransaction("2014/11/05", -10.00, "LEISURE1")
      .addTransaction("2014/10/06", -20.00, "LEISURE1")
      .addTransaction("2014/09/06", -70.00, "LEISURE1")
      .addTransaction("2014/08/06", -50.00, "LEISURE1") // Ignored
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000222", 2000.00, "2014/11/10") // Half the amounts of the other account
      .addTransaction("2014/11/06", -5.00, "LEISURE2")
      .addTransaction("2014/10/07", -10.00, "LEISURE2")
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
    transactions.initAmountContent() // TODO: revoir les soldes des comptes
      .add("11/01/2015", "Planned: Leisures", -200.00, "Leisures", 100.00, 100.00, "Main1")
      .add("11/01/2015", "Planned: Leisures", -100.00, "Leisures", 250.00, 250.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -200.00, "Leisures", 400.00, 400.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -100.00, "Leisures", 550.00, 550.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -195.00, "Leisures", 700.00, 700.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -90.00, "Leisures", 850.00, 850.00, "Main2")
      .add("06/11/2014", "LEISURE2", -5.00, "Leisures", 2000.00, 3000.00, "Account n. 000222")
      .add("05/11/2014", "LEISURE1", -10.00, "Leisures", 1000.00, 3005.00, "Account n. 000111")
      .check();
  }

  public void testSwitchingFromMultiToMonoAccountAndBackWithoutAssignedTransactions() throws Exception {
    accounts.createMainAccount("Main1", 1000.00);
    accounts.createMainAccount("Main2", 2000.00);
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
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 100.00, 100.00, "Main1")
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 250.00, 250.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 400.00, 400.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 550.00, 550.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -150.00, "Leisures", 700.00, 700.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -150.00, "Leisures", 850.00, 850.00, "Main2")
      .check();

    // 2. Switch to mono-account
    budgetView.variable.editSeries("Leisures")
      .setTargetAccount("Main2")
      .validate();
    budgetView.variable.checkContent("| Leisures | 0.00 | 300.00 |");
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -300.00, "Leisures", 100.00, 100.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -300.00, "Leisures", 400.00, 400.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -300.00, "Leisures", 700.00, 700.00, "Main2")
      .check();

    // 3. Switch back to multi-account
    budgetView.variable.editSeries("Leisures")
      .setTargetAccount("Main accounts")
      .validate();
    budgetView.variable.checkContent("| Leisures | 0.00 | 300.00 |");
    transactions.initAmountContent()
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 100.00, 100.00, "Main1")
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 250.00, 250.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 400.00, 400.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 550.00, 550.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -150.00, "Leisures", 700.00, 700.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -150.00, "Leisures", 850.00, 850.00, "Main2")
      .check();
  }

  public void testSwitchingFromMultiToMonoAccountWithAssignedTransactions() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("000111", 1000.00, "2014/11/10")
      .addTransaction("2014/11/05", -10.00, "LEISURE1")
      .addTransaction("2014/10/06", -20.00, "LEISURE1")
      .addTransaction("2014/09/06", -70.00, "LEISURE1")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount("000222", 2000.00, "2014/11/10")
      .addTransaction("2014/11/06", -5.00, "LEISURE2")
      .addTransaction("2014/10/07", -10.00, "LEISURE2")
      .addTransaction("2014/09/03", -35.00, "LEISURE2")
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
    budgetView.variable.checkContent("| Leisures | 15.00 | 300.00 |");

    // 1. Check multi
    transactions.showPlannedTransactions();
    timeline.selectMonths(201410, 201411, 201412, 201501);
    transactions.initAmountContent().dumpCode();
    transactions.initAmountContent() // TODO revoir les soldes des comptes
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 100.00, 100.00, "Main1")
      .add("11/01/2015", "Planned: Leisures", -150.00, "Leisures", 250.00, 250.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 400.00, 400.00, "Main1")
      .add("11/12/2014", "Planned: Leisures", -150.00, "Leisures", 550.00, 550.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -140.00, "Leisures", 700.00, 700.00, "Main1")
      .add("11/11/2014", "Planned: Leisures", -145.00, "Leisures", 850.00, 850.00, "Main2")
      .add("06/11/2014", "LEISURE2", -5.00, "Leisures", 2000.00, 3000.00, "Main2")
      .add("05/11/2014", "LEISURE1", -10.00, "Leisures", 1000.00, 3005.00, "Main1")
      .add("07/10/2014", "LEISURE2", -10.00, "Leisures", 2005.00, 3015.00, "Main2")
      .add("06/10/2014", "LEISURE1", -20.00, "Leisures", 1010.00, 3025.00, "Main1")
      .check();

    // 2. Switch to mono
    budgetView.variable.editSeries("Leisures")
      .setTargetAccount("Main2")
      .validate();
    transactions.initAmountContent() // TODO revoir les soldes des comptes
      .add("11/01/2015", "Planned: Leisures", -300.00, "Leisures", 100.00, 100.00, "Main2")
      .add("11/12/2014", "Planned: Leisures", -300.00, "Leisures", 400.00, 400.00, "Main2")
      .add("11/11/2014", "Planned: Leisures", -285.00, "Leisures", 700.00, 700.00, "Main2")
      .add("06/11/2014", "LEISURE2", -5.00, "Leisures", 2000.00, 3000.00, "Main2")
      .add("05/11/2014", "LEISURE1", -10.00, "Leisures", 1000.00, 3005.00, "Main1")
      .add("07/10/2014", "LEISURE2", -10.00, "Leisures", 2005.00, 3015.00, "Main2")
      .add("06/10/2014", "LEISURE1", -20.00, "Leisures", 1010.00, 3025.00, "Main1")
      .check();

    // 3. Switch back to multi
    budgetView.variable.editSeries("Leisures")
      .setTargetAccount("Main accounts")
      .validate();
    transactions.initAmountContent() // TODO remettre le tableau de l'étape 1
      .dumpCode();
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
}
