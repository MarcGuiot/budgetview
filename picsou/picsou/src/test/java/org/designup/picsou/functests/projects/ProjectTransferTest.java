package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

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
      .checkMonth("Dec 2010")
      .checkNoFromAccountSelected("Select the source account")
      .checkFromAccounts("Select the source account", "External account", "Main accounts", "Savings account")
      .checkNoToAccountSelected("Select the target account")
      .checkToAccounts("Select the target account", "External account", "Main accounts", "Savings account")
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
    currentProject.toggleAndEditTransfer(0)
      .checkFromAccount("Savings account")
      .checkToAccount("Main accounts")
      .cancel();
    currentProject.backToList();
    projects.checkCurrentProjects("| Trip | Dec | 0.00 | on |");

    projects.select("Trip");
    budgetView.extras.checkSeries("Trip", 0.00, 0.00);
    budgetView.savings.checkSeries("Transfer", 0.00, -200.00);
    categorization.selectTransaction("Transfer 1").selectSavings()
      .checkContainsSeries("Transfer")
      .checkSeriesIsActive("Transfer")
      .checkSeriesContainsNoSubSeries("Transfer");

    timeline.selectMonth(201011);
    budgetView.extras.checkNoSeriesShown();
    budgetView.savings.checkSeriesNotPresent("Transfer");
  }

  public void testNavigatingFromSeriesAndRenaming() throws Exception {

    createMainAccount("Main account");
    createSavingsAccount("Savings account");

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
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
      .addExpenseItem(0, "Item 1", 201012, -100.00)
      .addTransferItem(1, "Transfer", 200.00, "Savings account", "Main accounts");

    views.selectData();
    currentProject.backToList();

    views.selectBudget();
    budgetView.savings.editProjectSeries("Transfer");
    views.checkHomeSelected();
    currentProject
      .checkName("Trip")
      .editTransfer(1)
      .checkLabel("Transfer")
      .cancel();

    currentProject.backToList();

    views.selectBudget();
    budgetView.savings.editPlannedAmountForProject("Transfer");
    views.checkHomeSelected();
    currentProject
      .checkName("Trip")
      .editTransfer(1)
      .checkLabel("Transfer")
      .setLabel("Project transfer")
      .validate();

    views.selectBudget();
    budgetView.savings.checkSeriesList("From account Savings account",
                                       "Project transfer",
                                       "To account Savings account");
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
      .checkMonth("Dec 2010")
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
    budgetView.savings.checkSeries("Transfer", 0.00, -200.00);
    categorization.selectTransaction("Transfer 1").selectSavings()
      .checkContainsSeries("Transfer")
      .checkSeriesIsActive("Transfer")
      .checkSeriesContainsNoSubSeries("Transfer");
  }

  public void testDeletingTheSelectedSavingsAccount() throws Exception {

    createMainAccount("Main account 1");
    createSavingsAccount("Savings account 1");

    projectChart.create();
    currentProject
      .setName("Trip")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main accounts");
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    savingsAccounts.edit("Savings account 1")
      .openDelete()
      .checkMessageContains("All the operations and series associated to this account will be deleted")
      .validate();

    currentProject.checkItemCount(0);
    currentProject.checkProjectGaugeHidden();
    currentProject.checkPeriodHidden();
  }

  public void testChangingTheSavingsAccountToMainDeletesTheProjectItem() throws Exception {

    createMainAccount("Main account 1");
    createSavingsAccount("Savings account 1");

    projectChart.create();
    currentProject
      .setName("Trip")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main accounts");
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    savingsAccounts.edit("Savings account 1")
      .setAsMain()
      .validate();

    currentProject.checkItemCount(0);
    currentProject.checkProjectGaugeHidden();
    currentProject.checkPeriodHidden();
  }

  public void testSwitchingTheFromAnToAccountsInvertsTheSavingsSeriesSign() throws Exception {

    createMainAccount("Main account 1");
    createSavingsAccount("Savings account 1");

    projectChart.create();
    currentProject
      .setName("Trip")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main accounts");
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    budgetView.extras.checkSeries("Trip", 0.00, 0.00);
    budgetView.savings.checkSeries("Transfer", 0.00, -200.00);

    currentProject
      .toggleAndEditTransfer(0)
      .setFromAccount("Main accounts")
      .setToAccount("Savings account 1")
      .validate();

    budgetView.extras.checkSeries("Trip", 0.00, 0.00);
    budgetView.savings.checkSeries("Transfer", 0.00, 200.00);
  }

  public void testChangingProjectItemAccountsWithExistingTransactions() throws Exception {

    createMainAccount("Main account 1");
    createSavingsAccount("Savings account 1");
    createSavingsAccount("Savings account 2");

    projectChart.create();
    currentProject
      .setName("Trip")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main accounts");
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    categorization.setSavings("TRANSFER FROM SAVINGS ACCOUNT 1", "Transfer");

    views.selectHome();
    currentProject
      .toggleAndEditTransfer(0)
      .setFromAccount("Savings account 2")
      .validateAndCheckConfirmation()
      .checkMessageContains("Operations were assigned to one of the accounts")
      .clickOnHyperlink("show")
      .checkHidden();

    views.checkDataSelected();
    transactions.initContent()
      .add("01/12/2010", TransactionType.PRELEVEMENT, "TRANSFER FROM SAVINGS ACCOUNT 1", "", -100.00, "Transfer")
      .check();

    views.selectHome();
    currentProject
      .editTransfer(0)
      .checkFromAccount("Savings account 2")
      .validateAndCheckConfirmation()
      .checkMessageContains("Operations were assigned to one of the accounts")
      .cancel();
    currentProject
      .editTransfer(0)
      .checkFromAccount("Savings account 2");

    views.selectData();
    transactions.initContent()
      .add("01/12/2010", TransactionType.PRELEVEMENT, "TRANSFER FROM SAVINGS ACCOUNT 1", "", -100.00, "Transfer")
      .check();

    views.selectHome();
    currentProject
      .editTransfer(0)
      .checkFromAccount("Savings account 2")
      .validateAndCheckConfirmation()
      .validate();

    transactions.initContent()
      .add("01/12/2010", TransactionType.PRELEVEMENT, "TRANSFER FROM SAVINGS ACCOUNT 1", "", -100.00, "To categorize")
      .check();

    views.selectHome();
    currentProject
      .toggleAndEditTransfer(0)
      .checkFromAccount("Savings account 2")
      .cancel();
  }

  public void testNavigatingToTransactions() throws Exception {
    createMainAccount("Main account 1");
    createSavingsAccount("Savings account 1");
    createSavingsAccount("Savings account 2");

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/15")
      .addTransaction("2010/12/01", 100.00, "Transfer to savings account 1")
      .addTransaction("2010/12/10", 100.00, "Other 1")
      .addTransaction("2010/12/15", 100.00, "Other 2")
      .loadInAccount("Main account 1");

    projectChart.create();
    currentProject.setName("My Project")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main accounts");

    categorization.setSavings("TRANSFER FROM SAVINGS ACCOUNT 1", "Transfer");
    categorization.setSavings("TRANSFER TO SAVINGS ACCOUNT 1", "Transfer");

    views.selectData();
    transactions.initContent()
      .add("15/12/2010", TransactionType.VIREMENT, "OTHER 2", "", 100.00)
      .add("10/12/2010", TransactionType.VIREMENT, "OTHER 1", "", 100.00)
      .add("01/12/2010", TransactionType.PRELEVEMENT, "TRANSFER FROM SAVINGS ACCOUNT 2", "", -100.00)
      .add("01/12/2010", TransactionType.VIREMENT, "AN OPERATION", "", 1000.00)
      .add("01/12/2010", TransactionType.PRELEVEMENT, "TRANSFER FROM SAVINGS ACCOUNT 1", "", -100.00, "Transfer")
      .add("01/12/2010", TransactionType.VIREMENT, "AN OPERATION", "", 1000.00)
      .add("01/12/2010", TransactionType.VIREMENT, "TRANSFER TO SAVINGS ACCOUNT 1", "", 100.00, "Transfer")
      .add("01/12/2010", TransactionType.VIREMENT, "TRANSFER 1", "", 100.00)
      .add("01/12/2010", TransactionType.VIREMENT, "INCOME", "", 1000.00)
      .check();

    views.selectHome();
    currentProject.view(0).showTransactionsThroughActual();

    views.checkDataSelected();
    transactions.initContent()
      .add("01/12/2010", TransactionType.PRELEVEMENT, "TRANSFER FROM SAVINGS ACCOUNT 1", "", -100.00, "Transfer")
      .add("01/12/2010", TransactionType.VIREMENT, "TRANSFER TO SAVINGS ACCOUNT 1", "", 100.00, "Transfer")
      .check();
  }

  public void testUsingMonthAmounts() throws Exception {

    createMainAccount("Main account 1");
    createSavingsAccount("Savings account 1");

    projectChart.create();
    currentProject
      .setName("Trip")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main accounts");
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    budgetView.extras.checkSeries("Trip", 0.00, 0.00);
    budgetView.savings.checkSeries("Transfer", 0.00, -200.00);

    currentProject
      .toggleAndEditTransfer(0)
      .setFromAccount("Main accounts")
      .setToAccount("Savings account 1")
      .switchToSeveralMonths()
      .switchToMonthEditor()
      .setTableMonthCount(3)
      .checkMonthAmounts("| Dec 2010 | 200.00 |\n" +
                         "| Jan 2011 | 0.00   |\n" +
                         "| Feb 2011 | 0.00   |")
      .setMonthAmount(0, 70.00)
      .setMonthAmount(1, 20.00)
      .setMonthAmount(2, 10.00)
      .checkMonthAmounts("| Dec 2010 | 70.00 |\n" +
                         "| Jan 2011 | 20.00 |\n" +
                         "| Feb 2011 | 10.00 |")
      .validate();

    timeline.selectMonth(201012);
    budgetView.savings.checkSeries("Transfer", 0.00, 70.00);
    timeline.selectMonth(201101);
    budgetView.savings.checkSeries("Transfer", 0.00, 20.00);
    timeline.selectMonth(201102);
    budgetView.savings.checkSeries("Transfer", 0.00, 10.00);

    currentProject
      .toggleAndEditTransfer(0)
      .setMonthAmount(0, 80.00)
      .setMonthAmount(1, 30.00)
      .setMonthAmount(2, 20.00)
      .validate();

    timeline.selectMonth(201012);
    budgetView.savings.checkSeries("Transfer", 0.00, 80.00);
    timeline.selectMonth(201101);
    budgetView.savings.checkSeries("Transfer", 0.00, 30.00);
    timeline.selectMonth(201102);
    budgetView.savings.checkSeries("Transfer", 0.00, 20.00);
  }

  private void createMainAccount(String mainAccountName) {
    mainAccounts.createNewAccount()
      .setName(mainAccountName)
      .selectBank("CIC")
      .setAsMain()
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 100.00, "Transfer 1")
      .loadInAccount(mainAccountName);
  }

  private void createSavingsAccount(String savingsAccountName) {
    savingsAccounts.createNewAccount()
      .setName(savingsAccountName)
      .selectBank("CIC")
      .setAsSavings()
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("002222", 10000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "An operation")
      .addTransaction("2010/12/01", -100.00, "Transfer from " + savingsAccountName)
      .loadInAccount(savingsAccountName);
  }
}
