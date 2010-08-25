package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.functests.checkers.AccountEditionChecker;
import org.designup.picsou.model.TransactionType;

public class AccountManagementTest extends LoggedInFunctionalTestCase {

  public void testSingleAccount() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 1.23, "2006/01/30")
      .addTransaction("2006/01/10", -1, "Blah")
      .load();

    views.selectHome();
    mainAccounts.checkAccount("Account n. 10101010", 1.23, "2006/01/10");
    mainAccounts.checkSummary(1.23, "2006/01/10");
    views.selectData();
    transactions.initAmountContent()
      .add("10/01/2006", "BLAH", -1.00, "To categorize", 1.23, 1.23, "Account n. 10101010")
      .check();
  }

  public void testAccountsAreUpdatedDuringSubsequentImports() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "123123123", 10, "2006/01/30")
      .addTransaction("2006/01/10", -1, "EDF")
      .addCardAccount("1000200030004000", 300, "2006/01/28")
      .addTransaction("2006/01/17", -3, "Foo")
      .loadDeferredCard("Card n. 1000-2000-3000-4000");

    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "123123123", 10, "2006/01/31")
      .addTransaction("2006/01/15", -10, "GDF")
      .addCardAccount("1000200030004000", 10, "2006/01/29")
      .addTransaction("2006/01/20", -6, "Bar")
      .load();

    views.selectHome();
    mainAccounts.checkSummary(10.0, "2006/01/20");
    mainAccounts.checkAccount("Account n. 123123123", 10, "2006/01/15");
    mainAccounts.checkAccount("Card n. 1000-2000-3000-4000", 10, "2006/01/20");
    views.selectData();
    timeline.selectAll();
    transactions.initAmountContent()
      .add("20/01/2006", "BAR", -6.00, "To categorize", 10.00, 10.00, "Card n. 1000-2000-3000-4000")
      .add("17/01/2006", "FOO", -3.00, "To categorize", 16.00, 10.00, "Card n. 1000-2000-3000-4000")
      .add("15/01/2006", "GDF", -10.00, "To categorize", 10.00, 10.00, "Account n. 123123123")
      .add("10/01/2006", "EDF", -1.00, "To categorize", 20.00, 20.00, "Account n. 123123123")
      .check();
  }

  public void testOnlyTheLatestAccountBalanceIsTakenIntoAccount() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 12345.60, "2006/05/30")
      .addTransaction("2006/05/01", -10, "Foo")
      .load();
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 1111, "2006/01/30")
      .addTransaction("2006/01/10", -20, "Bar")
      .load();

    views.selectHome();
    mainAccounts.checkAccount("Account n. 10101010", 12345.60, "2006/05/01");
    mainAccounts.checkSummary(12345.60, "2006/05/01");
  }

  public void testNothingShownForQifFiles() throws Exception {
    String path = QifBuilder.init(this)
      .addTransaction("2006/01/01", 12.35, "foo")
      .addTransaction("2006/02/01", -7.50, "foo")
      .save();
    operations.importQifFiles(SOCIETE_GENERALE, path);

    views.selectHome();
    mainAccounts.checkDisplayIsEmpty("Main account");
  }

  public void testEditingTheAccountPositionThreshold() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 100, "2008/08/26")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", -800, "FNAC")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "Salary");

    views.selectHome();
    timeline.selectMonth("2008/08");
    mainAccounts
      .checkEstimatedPosition(100.00)
      .checkEstimatedPositionColor("darkGray");

    views.selectBudget();
    budgetView.getSummary().openThresholdDialog()    
      .checkThreshold(0.00)
      .setThreshold(1000.00)
      .validate();

    views.selectHome();
    mainAccounts.checkEstimatedPositionColor("red");

    views.selectBudget();
    budgetView.getSummary().openThresholdDialog()
      .checkThreshold(1000.00)
      .setThreshold(-2000.00)
      .validate();

    views.selectHome();
    mainAccounts.checkEstimatedPositionColor("darkGray");

    views.selectBudget();
    budgetView.getSummary().openThresholdDialog()
      .setThreshold(0.00)
      .validate();

    views.selectHome();
    mainAccounts.checkEstimatedPositionColor("darkGray");

    timeline.selectMonth("2008/07");
    mainAccounts
      .checkEstimatedPosition(-100.00)
      .checkEstimatedPositionColor("darkRed");
  }

  public void testChangeAccountTypeUncategorizeTransactionIfAssociatedSeriesIsNotSavings() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 100, "2008/08/26")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", -800, "Epargne")
      .load();

    views.selectCategorization();
    timeline.selectAll();
    categorization.setNewIncome("WorldCo", "income");
    categorization.setNewExtra("Epargne", "Epargne");
    views.selectHome();
    mainAccounts.edit("Account n. 000123")
      .setAsSavings()
      .checkSavingsWarning()
      .setAsMain()
      .checkNoSavingsWarning()
      .setAsSavings()
      .checkSavingsWarning()
      .validate();
    views.selectData();
    transactions.initContent()
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Epargne", "", -800.00, "Epargne")
      .add("26/08/2008", TransactionType.PRELEVEMENT, "Epargne", "", -800.00)
      .add("26/08/2008", TransactionType.VIREMENT, "WorldCo", "", 1000.00)
      .add("26/07/2008", TransactionType.VIREMENT, "WorldCo", "", 1000.00)
      .check();
  }

  public void testRemoveAllAccount() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 100, "2008/08/26")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", -800, "Epargne")
      .load();

    views.selectHome();
    mainAccounts.edit("Account n. 000123")
      .delete()
      .validate();

    mainAccounts
      .checkNoAccountsDisplayed()
      .checkNoEstimatedPosition();
    views.selectBudget();
    budgetView.getSummary()
      .checkNoEstimatedPosition();
  }

  public void testNavigatingToOperations() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "000111", 12345.60, "2006/01/30")
      .addTransaction("2006/01/01", -10, "Foo")
      .load();
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "000222", 1111, "2006/01/30")
      .addTransaction("2006/01/10", -20, "Bar")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Foo", "Foos");
    categorization.setNewVariable("Bar", "Bars");

    views.selectHome();
    mainAccounts.gotoOperations("Account n. 000111");

    views.checkDataSelected();
    transactions.initContent()
      .add("01/01/2006", TransactionType.PRELEVEMENT, "FOO", "", -10.00, "Foos")
      .check();
    series.select("Foos");

    views.selectHome();
    mainAccounts.gotoOperations("Account n. 000222");
    views.checkDataSelected();
    transactions.initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "BAR", "", -20.00, "Bars")
      .check();
    series.checkSelection("All");
  }

  public void testAccountWithoutPosition() throws Exception {
    views.selectHome();
    AccountEditionChecker accountEditionChecker = mainAccounts.createNewAccount()
      .setAccountName("no position");
    accountEditionChecker
      .openBankSelection()
      .selectBank("CIC")
      .validate();

    accountEditionChecker.validate();
    
    mainAccounts.checkAccountWithoutPosition("no position", "2008/08/31");

  }
}
