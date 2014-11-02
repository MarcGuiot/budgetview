package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class ProjectWithAccountDeletionTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentMonth("2010/12");
    super.setUp();
    addOns.activateProjects();
  }

  public void testDeletingAccounts() throws Exception {
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(6).validate();
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/30")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/10", -100.00, "First 1111")
      .load();

    OfxBuilder.init(this)
      .addBankAccount("002222", 00.00, "2010/12/30")
      .addTransaction("2010/12/01", 500.00, "Income")
      .addTransaction("2010/12/10", -400.00, "Second 2222")
      .load();

    projects.createFirst();
    currentProject
      .setNameAndDefaultAccount("Project A", "Account n. 001111")
      .addExpenseItem(0, "First 1111", 201012, -1000.00, "Account n. 001111")
      .addExpenseItem(1, "Second 2222", 201012, -100.00);
    currentProject
      .toggleAndEditExpense(0)
      .checkTargetAccountCombo("Account n. 001111")
      .validate();
    currentProject
      .toggleAndEditExpense(1)
      .setTargetAccount("Account n. 002222")
      .validate();

    categorization.selectTransaction("FIRST 1111")
      .selectExtras()
      .checkGroupDoesNotContainSeries("Project A", "Second 2222")
      .selectSeries("First 1111");

    categorization.selectTransaction("SECOND 2222")
      .selectExtras()
      .checkGroupDoesNotContainSeries("Project A", "First 1111")
      .selectSeries("Second 2222");

    projects.create();
    currentProject
      .setNameAndDefaultAccount("Project B", "Account n. 001111")
      .addExpenseItem(0, "Item 1", 201012, -500.00);

    budgetView.extras
      .showInactiveSeries()
      .checkContent("| Project A | 500.00 | 1100.00 |\n" +
                    "| Project B | 0.00   | 500.00  |\n");

    mainAccounts.openDelete("Account n. 002222")
      .checkMessageContains("All the operations and series associated to this account will be deleted.")
      .validate();

    currentProject.backToList();
    projectList.checkCurrentProjects("| Project B | Dec | 500.00  | on |\n" +
                                  "| Project A | Dec | 1100.00 | on |");

    budgetView.extras
      .checkContent("| Project A | 100.00 | 1000.00 |\n" +
                    "| Project B | 0.00   | 500.00  |\n");
    transactions.showPlannedTransactions();
    transactions.initContent()
      .add("11/12/2010", TransactionType.PLANNED, "Planned: First 1111", "", -900.00, "First 1111")
      .add("11/12/2010", TransactionType.PLANNED, "Planned: Item 1", "", -500.00, "Item 1")
      .add("10/12/2010", TransactionType.PRELEVEMENT, "FIRST 1111", "", -100.00, "First 1111")
      .add("01/12/2010", TransactionType.VIREMENT, "INCOME", "", 1000.00)
      .check();

    mainAccounts.openDelete("Account n. 001111")
      .checkMessageContains("All the operations and series associated to this account will be deleted.")
      .validate();

    projectList.checkNoCurrentProjects();
    budgetView.extras
      .checkNoSeriesShown()
      .hideInactiveEnveloppes();
    transactions
      .checkShowsPlannedTransactions()
      .checkEmpty();
  }

  public void testDeletingASavingsAccount() throws Exception {
    operations.hideSignposts();
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

    views.selectProjects();
    projects.createFirst();
    currentProject
      .setNameAndValidate("Trip")
      .addTransferItem()
      .editTransfer(0)
      .checkLabel("Transfer")
      .checkPositiveAmountsOnly()
      .setAmount(200.00)
      .checkMonth("Dec 2010")
      .setFromAccount("Savings account")
      .setToAccount("Main account")
      .validate();
    currentProject.checkProjectGauge(0.00, 0.00);
    currentProject.checkPeriod("December 2010");

    currentProject.addExpenseItem(1, "Trip", 201012, -50.00);
    currentProject.checkProjectGauge(0.00, -50.00);

    currentProject.checkItems("| Transfer | Dec | 0.00 | +200.00 |\n" +
                              "| Trip     | Dec | 0.00 | 50.00   |");
    budgetView.transfer.checkContent("| Transfer | 0.00 | +200.00 |");
    budgetView.extras.checkContent("| Trip | 0.00 | 50.00 |");

    savingsAccounts.openDelete("Savings account").validate();

    currentProject.checkItems("| Trip | Dec | 0.00 | 50.00 |");
    budgetView.transfer.checkNoSeriesShown();
    budgetView.extras.checkContent("| Trip | 0.00 | 50.00 |");
  }

  public void testDefaultAccountUpdated() throws Exception {
    operations.hideSignposts();
    operations.openPreferences().setFutureMonthsCount(6).validate();
    OfxBuilder.init(this)
      .addBankAccount("001111", 1000.00, "2010/12/30")
      .addTransaction("2010/12/01", 1000.00, "Income")
      .addTransaction("2010/12/10", -100.00, "First 1111")
      .load();
    OfxBuilder.init(this)
      .addBankAccount("002222", 00.00, "2010/12/30")
      .addTransaction("2010/12/01", 500.00, "Income")
      .addTransaction("2010/12/10", -400.00, "Second 2222")
      .load();

    projects.createFirst();
    currentProject
      .setNameAndDefaultAccount("Project A", "Account n. 001111")
      .addExpenseItem(0, "First 1111", 201012, -1000.00, "Account n. 001111")
      .addExpenseItem(1, "Second 2222", 201012, -100.00, "Account n. 002222");

    categorization.setExtra("FIRST 1111", "First 1111");
    categorization.setExtra("SECOND 2222", "Second 2222");

    mainAccounts.openDelete("Account n. 001111")
      .checkMessageContains("All the operations and series associated to this account will be deleted.")
      .validate();

    views.selectHome();
    currentProject.checkDefaultAccountLabel("Account n. 002222");
    currentProject.checkItems("| Second 2222 | Dec | 400.00 | 100.00 |");

    budgetView.extras.checkContent("| Project A | 400.00 | 100.00 |");
  }
}
