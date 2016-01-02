package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.model.*;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Log;

public class ProjectTransferTest extends LoggedInFunctionalTestCase {
  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    super.setUp();
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(12).validate();
    addOns.activateProjects();
  }

  public void testWithSavings() throws Exception {

    accounts.createNewAccount()
      .setName("Main account")
      .selectBank("CIC")
      .setAsMain()
      .validate();

    accounts.createNewAccount()
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

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addTransferItem()
      .editTransfer(0)
      .checkLabel("Transfer")
      .checkPositiveAmountsOnly()
      .setAmount(200.00)
      .checkMonth("Dec 2010")
      .checkNoFromAccountSelected("Select the source account")
      .checkFromAccounts("Select the source account", "Main account", "Savings account", "External account")
      .checkNoToAccountSelected("Select the target account")
      .checkToAccounts("Select the target account", "Main account", "Savings account", "External account")
      .checkSavingsMessageHidden()
      .setFromAccount("Savings account")
      .checkSavingsMessageShown()
      .setToAccount("Main account")
      .checkSavingsMessageShown()
      .validate();
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    timeline.checkSelection("2010/12");
    budgetView.extras.checkSeriesNotPresent("Trip");
    budgetView.transfer.checkSeries("Transfer", "0.00", "+200.00");

    categorization.selectTransaction("Transfer 1").selectTransfers()
      .checkContainsSeries("Transfer")
      .checkSeriesIsActive("Transfer")
      .checkSeriesContainsNoSubSeries("Transfer");

    currentProject.view(0).setInactive();
    currentProject.checkProjectGauge(0.00, 0.00);
    budgetView.extras.checkSeriesNotPresent("Trip");
    budgetView.transfer.checkSeriesNotPresent("Transfer");
    categorization.selectTransaction("Transfer 1").selectTransfers()
      .checkContainsSeries("Transfer")
      .checkSeriesIsInactive("Transfer")
      .checkSeriesContainsNoSubSeries("Transfer");

    views.selectHome();
    currentProject.view(0).setActive();
    currentProject.toggleAndEditTransfer(0)
      .checkFromAccount("Savings account")
      .checkToAccount("Main account")
      .cancel();
    currentProject.backToList();
    projectList.checkCurrentProjects("| Trip | Dec | 0.00 | on |");

    projectList.select("Trip");
    budgetView.extras.checkSeriesNotPresent("Trip");
    budgetView.transfer.checkSeries("Transfer", "0.00", "+200.00");
    categorization.selectTransaction("Transfer 1").selectTransfers()
      .checkContainsSeries("Transfer")
      .checkSeriesIsActive("Transfer")
      .checkSeriesContainsNoSubSeries("Transfer");

    timeline.selectMonth(201011);
    budgetView.extras.checkNoSeriesShown();
    budgetView.transfer.checkSeriesNotPresent("Transfer");
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

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addExpenseItem(0, "Item 1", 201012, -100.00)
      .addTransferItem(1, "Transfer", 200.00, "Savings account", "Main account")
      .addTransferItem(2, "Savings", 200.00, "Main account", "Savings account");

    views.selectData();
    currentProject.backToList();

    views.selectBudget();
    budgetView.transfer.editProjectForSeries("Transfer");
    views.checkProjectsSelected();
    currentProject
      .checkName("Trip")
      .editTransfer(1)
      .checkLabel("Transfer")
      .cancel();

    views.selectBudget();
    budgetView.transfer.editProjectForSeries("Savings");
    views.checkProjectsSelected();
    currentProject
      .checkName("Trip")
      .editTransfer(2)
      .checkLabel("Savings")
      .cancel();

    currentProject.backToList();

    views.selectBudget();
    budgetView.transfer.editPlannedAmountForProject("Transfer");
    views.checkProjectsSelected();
    currentProject
      .checkName("Trip")
      .editTransfer(1)
      .checkLabel("Transfer")
      .setLabel("Project transfer")
      .validate();

    views.selectBudget();
    budgetView.transfer.checkSeriesList("Savings", "Transfer");
  }

  public void testMustSelectDifferentFromAndToAccounts() throws Exception {
    accounts.createNewAccount()
      .setName("Main Account 1")
      .selectBank("CIC")
      .setAccountNumber("001111")
      .setPosition(1000.00)
      .setAsMain()
      .validate();

    accounts.createNewAccount()
      .setName("Main Account 2")
      .selectBank("CIC")
      .setAccountNumber("002222")
      .setAsMain()
      .validate();

    accounts.createNewAccount()
      .setName("Savings Account 1")
      .selectBank("CIC")
      .setAccountNumber("00333")
      .setAsSavings()
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 100.00, "Transfer 1")
      .load();

    OfxBuilder.init(this)
      .addBankAccount("002222", 10000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Blah")
      .load();

    OfxBuilder.init(this)
      .addBankAccount("00333", 10000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "Blah")
      .load();

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addTransferItem()
      .editTransfer(0)
      .setAmount(200.00)
      .checkMonth("Dec 2010")
      .validateAndCheckFromAccountError("You must select a source account")
      .setFromAccount("Main account 1")
      .validateAndCheckToAccountError("You must select a target account")
      .setToAccount("Main account 1")
      .validateAndCheckFromAccountError("You must select different source and target accounts")
      .setFromAccount("External account")
      .validateAndCheckFromAccountError("You must select at least one savings account")
      .setFromAccount("Savings Account 1")
      .validate();

    timeline.checkSelection("2010/12");
    budgetView.extras.checkNoSeriesShown();
    budgetView.transfer.checkSeries("Transfer", "0.00", "+200.00");
    categorization.selectTransaction("Transfer 1").selectTransfers()
      .checkContainsSeries("Transfer")
      .checkSeriesIsActive("Transfer")
      .checkSeriesContainsNoSubSeries("Transfer");
  }

  public void testDeletingTheSelectedSavingsAccount() throws Exception {

    createMainAccount("Main account 1");
    createSavingsAccount("Savings account 1");

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main account 1");
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

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main account 1");
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    savingsAccounts.edit("Savings account 1")
      .setAsMain()
      .validate();

    currentProject.checkItemCount(0);
    currentProject.checkProjectGaugeHidden();
    currentProject.checkPeriodHidden();
  }

  public void testSwitchingTheFromAndToAccountsInvertsTheSavingsSeriesSign() throws Exception {

    createMainAccount("Main account 1");
    createSavingsAccount("Savings account 1");

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main account 1");
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    budgetView.extras.checkNoSeriesShown();
    budgetView.transfer.checkSeries("Transfer", "0.00", "+200.00");

    transactions.showPlannedTransactions()
      .initAmountContent()
      .add("11/12/2010", "Planned: Transfer", -200.00, "Transfer", 700.00, 700.00, "Savings account 1")
      .add("11/12/2010", "Planned: Transfer", 200.00, "Transfer", 2300.00, 2300.00, "Main account 1")
      .add("01/12/2010", "TRANSFER FROM SAVINGS ACCOUNT 1", -100.00, "To categorize", 900.00, 900.00, "Savings account 1")
      .add("01/12/2010", "AN OPERATION", 1000.00, "To categorize", 1000.00, 1000.00, "Savings account 1")
      .add("01/12/2010", "TRANSFER 1", 100.00, "To categorize", 2100.00, 2100.00, "Main account 1")
      .add("01/12/2010", "INCOME", 1000.00, "To categorize", 2000.00, 2000.00, "Main account 1")
      .check();

    currentProject
      .toggleAndEditTransfer(0)
      .setFromAccount("Main account 1")
      .setToAccount("Savings account 1")
      .validate();
    transactions
      .initAmountContent()
      .add("11/12/2010", "Planned: Transfer", 200.00, "Transfer", 1100.00, 1100.00, "Savings account 1")
      .add("11/12/2010", "Planned: Transfer", -200.00, "Transfer", 1900.00, 1900.00, "Main account 1")
      .add("01/12/2010", "TRANSFER FROM SAVINGS ACCOUNT 1", -100.00, "To categorize", 900.00, 900.00, "Savings account 1")
      .add("01/12/2010", "AN OPERATION", 1000.00, "To categorize", 1000.00, 1000.00, "Savings account 1")
      .add("01/12/2010", "TRANSFER 1", 100.00, "To categorize", 2100.00, 2100.00, "Main account 1")
      .add("01/12/2010", "INCOME", 1000.00, "To categorize", 2000.00, 2000.00, "Main account 1")
      .check();

    budgetView.extras.checkNoSeriesShown();
    budgetView.transfer.checkSeries("Transfer", "0.00", "200.00");

    transactions
      .initAmountContent()
      .add("11/12/2010", "Planned: Transfer", 200.00, "Transfer", 1100.00, 1100.00, "Savings account 1")
      .add("11/12/2010", "Planned: Transfer", -200.00, "Transfer", 1900.00, 1900.00, "Main account 1")
      .add("01/12/2010", "TRANSFER FROM SAVINGS ACCOUNT 1", -100.00, "To categorize", 900.00, 900.00, "Savings account 1")
      .add("01/12/2010", "AN OPERATION", 1000.00, "To categorize", 1000.00, 1000.00, "Savings account 1")
      .add("01/12/2010", "TRANSFER 1", 100.00, "To categorize", 2100.00, 2100.00, "Main account 1")
      .add("01/12/2010", "INCOME", 1000.00, "To categorize", 2000.00, 2000.00, "Main account 1")
      .check();

    projects.select("Trip");
    currentProject.setInactive();
    currentProject.setActive();
    budgetView.transfer.checkSeries("Transfer", "0.00", "200.00");
    transactions
      .initAmountContent()
      .add("11/12/2010", "Planned: Transfer", 200.00, "Transfer", 1100.00, 1100.00, "Savings account 1")
      .add("11/12/2010", "Planned: Transfer", -200.00, "Transfer", 1900.00, 1900.00, "Main account 1")
      .add("01/12/2010", "TRANSFER FROM SAVINGS ACCOUNT 1", -100.00, "To categorize", 900.00, 900.00, "Savings account 1")
      .add("01/12/2010", "AN OPERATION", 1000.00, "To categorize", 1000.00, 1000.00, "Savings account 1")
      .add("01/12/2010", "TRANSFER 1", 100.00, "To categorize", 2100.00, 2100.00, "Main account 1")
      .add("01/12/2010", "INCOME", 1000.00, "To categorize", 2000.00, 2000.00, "Main account 1")
      .check();
  }

  public void testChangingProjectItemAccountsWithExistingTransactions() throws Exception {

    createMainAccount("Main account 1");
    createSavingsAccount("Savings account 1");
    createSavingsAccount("Savings account 2");

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main account 1");
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    categorization.setTransfer("TRANSFER FROM SAVINGS ACCOUNT 1", "Transfer");

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

    projects.createFirst();
    currentProject.setNameAndValidate("My Project")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main account 1");

    categorization.setTransfer("TRANSFER FROM SAVINGS ACCOUNT 1", "Transfer");
    categorization.setTransfer("TRANSFER TO SAVINGS ACCOUNT 1", "Transfer");

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

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main account 1");
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    budgetView.extras.checkNoSeriesShown();
    budgetView.transfer.checkSeries("Transfer", "0.00", "+200.00");

    currentProject
      .toggleAndEditTransfer(0)
      .setFromAccount("Main account 1")
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
    budgetView.transfer.checkSeries("Transfer", "0.00", "70.00");
    timeline.selectMonth(201101);
    budgetView.transfer.checkSeries("Transfer", "0.00", "20.00");
    timeline.selectMonth(201102);
    budgetView.transfer.checkSeries("Transfer", "0.00", "10.00");

    currentProject
      .toggleAndEditTransfer(0)
      .setMonthAmount(0, 80.00)
      .setMonthAmount(1, 30.00)
      .setMonthAmount(2, 20.00)
      .validate();

    timeline.selectMonth(201012);
    budgetView.transfer.checkSeries("Transfer", "0.00", "80.00");
    timeline.selectMonth(201101);
    budgetView.transfer.checkSeries("Transfer", "0.00", "30.00");
    timeline.selectMonth(201102);
    budgetView.transfer.checkSeries("Transfer", "0.00", "20.00");

    timeline.selectAll();
    transactions.showPlannedTransactions().initAmountContent()
      .add("11/02/2011", "Planned: Transfer", 20.00, "Transfer", 1030.00, 1030.00, "Savings account 1")
      .add("11/02/2011", "Planned: Transfer", -20.00, "Transfer", 1970.00, 1970.00, "Main account 1")
      .add("11/01/2011", "Planned: Transfer", 30.00, "Transfer", 1010.00, 1010.00, "Savings account 1")
      .add("11/01/2011", "Planned: Transfer", -30.00, "Transfer", 1990.00, 1990.00, "Main account 1")
      .add("11/12/2010", "Planned: Transfer", 80.00, "Transfer", 980.00, 980.00, "Savings account 1")
      .add("11/12/2010", "Planned: Transfer", -80.00, "Transfer", 2020.00, 2020.00, "Main account 1")
      .add("01/12/2010", "TRANSFER FROM SAVINGS ACCOUNT 1", -100.00, "To categorize", 900.00, 900.00, "Savings account 1")
      .add("01/12/2010", "AN OPERATION", 1000.00, "To categorize", 1000.00, 1000.00, "Savings account 1")
      .add("01/12/2010", "TRANSFER 1", 100.00, "To categorize", 2100.00, 2100.00, "Main account 1")
      .add("01/12/2010", "INCOME", 1000.00, "To categorize", 2000.00, 2000.00, "Main account 1")
      .add("01/11/2010", "INCOME", 1000.00, "To categorize", 1000.00, 1000.00, "Main account 1")
      .check();

    currentProject.toggleAndEditTransfer(0)
      .setMonthAmount(0, 100)
      .setMonthAmount(1, 50)
      .validate();
    transactions.initAmountContent()
      .add("11/02/2011", "Planned: Transfer", 20.00, "Transfer", 1070.00, 1070.00, "Savings account 1")
      .add("11/02/2011", "Planned: Transfer", -20.00, "Transfer", 1930.00, 1930.00, "Main account 1")
      .add("11/01/2011", "Planned: Transfer", 50.00, "Transfer", 1050.00, 1050.00, "Savings account 1")
      .add("11/01/2011", "Planned: Transfer", -50.00, "Transfer", 1950.00, 1950.00, "Main account 1")
      .add("11/12/2010", "Planned: Transfer", 100.00, "Transfer", 1000.00, 1000.00, "Savings account 1")
      .add("11/12/2010", "Planned: Transfer", -100.00, "Transfer", 2000.00, 2000.00, "Main account 1")
      .add("01/12/2010", "TRANSFER FROM SAVINGS ACCOUNT 1", -100.00, "To categorize", 900.00, 900.00, "Savings account 1")
      .add("01/12/2010", "AN OPERATION", 1000.00, "To categorize", 1000.00, 1000.00, "Savings account 1")
      .add("01/12/2010", "TRANSFER 1", 100.00, "To categorize", 2100.00, 2100.00, "Main account 1")
      .add("01/12/2010", "INCOME", 1000.00, "To categorize", 2000.00, 2000.00, "Main account 1")
      .add("01/11/2010", "INCOME", 1000.00, "To categorize", 1000.00, 1000.00, "Main account 1")
      .check();

    currentProject.slideToNextMonth();
    transactions.initAmountContent()
      .add("11/03/2011", "Planned: Transfer", 20.00, "Transfer", 1070.00, 1070.00, "Savings account 1")
      .add("11/03/2011", "Planned: Transfer", -20.00, "Transfer", 1930.00, 1930.00, "Main account 1")
      .add("11/02/2011", "Planned: Transfer", 50.00, "Transfer", 1050.00, 1050.00, "Savings account 1")
      .add("11/02/2011", "Planned: Transfer", -50.00, "Transfer", 1950.00, 1950.00, "Main account 1")
      .add("11/01/2011", "Planned: Transfer", 100.00, "Transfer", 1000.00, 1000.00, "Savings account 1")
      .add("11/01/2011", "Planned: Transfer", -100.00, "Transfer", 2000.00, 2000.00, "Main account 1")
      .add("01/12/2010", "TRANSFER FROM SAVINGS ACCOUNT 1", -100.00, "To categorize", 900.00, 900.00, "Savings account 1")
      .add("01/12/2010", "AN OPERATION", 1000.00, "To categorize", 1000.00, 1000.00, "Savings account 1")
      .add("01/12/2010", "TRANSFER 1", 100.00, "To categorize", 2100.00, 2100.00, "Main account 1")
      .add("01/12/2010", "INCOME", 1000.00, "To categorize", 2000.00, 2000.00, "Main account 1")
      .add("01/11/2010", "INCOME", 1000.00, "To categorize", 1000.00, 1000.00, "Main account 1")
      .check();
  }

  public void testDuplicatingAProjectWithMonthTransfers() throws Exception {
    createMainAccount("Main account 1");
    createSavingsAccount("Savings account 1");

    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addTransferItem(0, "Transfer", 200.00, "Savings account 1", "Main account 1");
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    currentProject
      .toggleAndEditTransfer(0)
      .setFromAccount("Main account 1")
      .setToAccount("Savings account 1")
      .switchToSeveralMonths()
      .switchToMonthEditor()
      .setTableMonthCount(3)
      .setMonthAmount(0, 70.00)
      .setMonthAmount(1, 20.00)
      .setMonthAmount(2, 10.00)
      .checkMonthAmounts("| Dec 2010 | 70.00 |\n" +
                         "| Jan 2011 | 20.00 |\n" +
                         "| Feb 2011 | 10.00 |")
      .validate();
    currentProject
      .checkPeriod("December 2010 - February 2011")
      .checkProjectGauge(0.00, 0.00)
      .checkItems("| Transfer | Dec | 0.00 | +100.00 |");

    currentProject.openDuplicate()
      .setName("Copy")
      .checkFirstMonth("December 2010")
      .setFirstMonth(201106)
      .validate();

    currentProject
      .checkName("Copy")
      .checkPeriod("June - August 2011")
      .checkProjectGauge(0, 0.00)
      .checkItems("| Transfer | June | 0.00 | +100.00 |");

    timeline.selectMonth(201012);
    budgetView.transfer.checkSeries("Transfer", "0.00", "70.00");
    timeline.selectMonth(201101);
    budgetView.transfer.checkSeries("Transfer", "0.00", "20.00");
    timeline.selectMonth(201102);
    budgetView.transfer.checkSeries("Transfer", "0.00", "10.00");

    timeline.selectMonth(201106);
    budgetView.transfer.checkSeries("Transfer", "0.00", "70.00");
    timeline.selectMonth(201107);
    budgetView.transfer.checkSeries("Transfer", "0.00", "20.00");
    timeline.selectMonth(201108);
    budgetView.transfer.checkSeries("Transfer", "0.00", "10.00");
  }

  private void createMainAccount(String mainAccountName) throws Exception {
    accounts.createNewAccount()
      .setAsMain()
      .setName(mainAccountName)
      .selectBank("CIC")
      .setAccountNumber("001111")
      .setPosition(1000.00)
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/01")
      .addTransaction("2010/11/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/01", 100.00, "Transfer 1")
      .loadInAccount(mainAccountName);
  }

  private void createSavingsAccount(String savingsAccountName) {
    accounts.createNewAccount()
      .setName(savingsAccountName)
      .selectBank("CIC")
      .setAccountNumber("002222")
      .setPosition(1000.00)
      .setAsSavings()
      .validate();

    OfxBuilder.init(this)
      .addBankAccount("002222", 10000.00, "2010/12/01")
      .addTransaction("2010/12/01", 1000.00, "An operation")
      .addTransaction("2010/12/01", -100.00, "Transfer from " + savingsAccountName)
      .loadInAccount(savingsAccountName);
  }
}
