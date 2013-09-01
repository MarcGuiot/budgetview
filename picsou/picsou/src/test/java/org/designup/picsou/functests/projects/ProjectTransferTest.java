package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class ProjectTransferTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    super.setUp();
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(6).validate();
  }

  public void testWithSavings() throws Exception {

    mainAccounts.createNewAccount()
      .setName("Main account")
      .selectBank("CIC")
      .setAsMain()
      .validate();

    mainAccounts.createNewAccount()
      .setName("Savings account")
      .selectBank("CIC")
      .setAsSavings()
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 100.00, "Transfer 1")
      .loadInAccount("Main account");

    OfxBuilder.init(this)
      .addBankAccount("002222", 10000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Blah")
      .loadInAccount("Savings account");

    projectChart.create();
    currentProject
      .setName("Trip")
      .addTransferItem()
      .editTransfer(0)
      .checkLabel("Transfer")
      .checkPositiveAmountsOnly()
      .setAmount(200.00)
      .checkMonth("December 2010")
      .checkNoFromAccountSelected("Select the source account")
      .checkFromAccounts("Select the source account,External account,Main accounts")
      .checkNoToAccountSelected("Select the target account")
      .checkToAccounts("Select the source account,External account,Main accounts")
      .checkSavingsMessageHidden()
      .setFromAccount("Savings account")
      .checkSavingsMessageShown()
      .setToAccount("Main accounts")
      .checkSavingsMessageShown()
      .validate();
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    timeline.checkSelection("2010/12");
    budgetView.extras.checkSeries("Trip", 0.00, 0.00);
    fail("[Pour Marc] on veut un montant positif sur la série d'épargne ci-dessous");
    budgetView.savings.checkSeries("Transfer", 0.00, -200.00);
    categorization.selectTransaction("Transfer 1").selectSavings()
      .checkContainsSeries("Transfer")
      .checkSeriesIsActive("Transfer")
      .checkSeriesContainsNoSubSeries("Transfer");

    currentProject.view(0).setInactive();
    currentProject.checkProjectGauge(0.00, 0.00);
    budgetView.extras.checkSeries("Trip", 0.00, 0.00);
    budgetView.savings.checkSeriesNotPresent("Transfer");
    categorization.selectTransaction("Transfer 1").selectSavings()
      .checkContainsSeries("Transfer")
      .checkSeriesIsInactive("Transfer")
      .checkSeriesContainsNoSubSeries("Transfer");

    views.selectHome();
    currentProject.view(0).setActive();
    currentProject.backToList();
    projects.checkProjects("| Trip | Dec | 0.00 | on |");

    projects.select("Trip");
    budgetView.extras.checkSeries("Trip", 0.00, 0.00);
    fail("[Pour Marc] on veut un montant positif sur la série d'épargne ci-dessous");
    budgetView.savings.checkSeries("Transfer", 0.00, -200.00);
    categorization.selectTransaction("Transfer 1").selectSavings()
      .checkContainsSeries("Transfer")
      .checkSeriesIsActive("Transfer")
      .checkSeriesContainsNoSubSeries("Transfer");

    timeline.selectMonth(201011);
    budgetView.extras.checkNoSeriesShown();
    budgetView.savings.checkSeriesNotPresent("Transfer");
  }

  public void testMustSelectDifferentFromAndToAccounts() throws Exception {
    mainAccounts.createNewAccount()
      .setName("Main Account 1")
      .selectBank("CIC")
      .setAsMain()
      .validate();

    mainAccounts.createNewAccount()
      .setName("Main Account 2")
      .selectBank("CIC")
      .setAsMain()
      .validate();

    mainAccounts.createNewAccount()
      .setName("Savings Account 1")
      .selectBank("CIC")
      .setAsSavings()
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 100.00, "Transfer 1")
      .loadInAccount("Main Account 1");

    OfxBuilder.init(this)
      .addBankAccount("002222", 10000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Blah")
      .loadInAccount("Main Account 2");

    OfxBuilder.init(this)
      .addBankAccount("00333", 10000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Blah")
      .loadInAccount("Savings Account 1");

    projectChart.create();
    currentProject
      .setName("Trip")
      .addTransferItem()
      .editTransfer(0)
      .setAmount(200.00)
      .checkMonth("December 2010")
      .validateAndCheckFromAccountError("You must select a source account")
      .setFromAccount("Main accounts")
      .validateAndCheckToAccountError("You must select a target account")
      .setToAccount("Main accounts")
      .validateAndCheckFromAccountError("You must select different source and target accounts")
      .setFromAccount("External account")
      .validateAndCheckFromAccountError("You must select at least one savings account")
      .setFromAccount("Savings Account 1")
      .validate();

    timeline.checkSelection("2010/12");
    budgetView.extras.checkSeries("Trip", 0.00, 0.00);
    fail("[Pour Marc] on veut un montant positif sur la série d'épargne ci-dessous");
    budgetView.savings.checkSeries("Transfer", 0.00, -200.00);
    categorization.selectTransaction("Transfer 1").selectSavings()
      .checkContainsSeries("Transfer")
      .checkSeriesIsActive("Transfer")
      .checkSeriesContainsNoSubSeries("Transfer");
  }

  public void testDeletingTheSelectedSavingsAccount() throws Exception {

    fail("[Regis] en cours");

    mainAccounts.createNewAccount()
      .setName("Main account 1")
      .selectBank("CIC")
      .setAsMain()
      .validate();

    mainAccounts.createNewAccount()
      .setName("Savings account 1")
      .selectBank("CIC")
      .setAsSavings()
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 100.00, "Transfer 1")
      .loadInAccount("Main account 1");

    OfxBuilder.init(this)
      .addBankAccount("002222", 10000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Blah")
      .loadInAccount("Savings account 1");

    projectChart.create();
    currentProject
      .setName("Trip")
      .addTransferItem()
      .editTransfer(0)
      .checkLabel("Transfer")
      .setAmount(200.00)
      .checkMonth("December 2010")
      .setFromAccount("Savings account 1")
      .setToAccount("Main accounts")
      .validate();
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    savingsAccounts.edit("Savings account 1")
      .openDelete()
      .checkMessageContains("All the operations and series associated to this account will be deleted")
      .validate();

    currentProject.checkItemCount(0);
    currentProject.checkProjectGaugeHidden();
    currentProject.checkPeriod("December 2010");
  }

  public void testChangingAnAccountFromSavingsToMain() throws Exception {
    fail("[Regis] mise à jour");
  }

  public void testChangingProjectItemAccountsWithExistingTransactions() throws Exception {
    fail("[Regis] confirmation pour décatégorisation des opérations + navigation");
  }

  public void testNavigatingToTransactions() throws Exception {
    fail("[Regis] on navigue vers series, même quand pas de subseries, sans oublier les opérations sur chaque compte du virement");

  }
}
