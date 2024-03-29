package com.budgetview.functests.accounts;

import com.budgetview.functests.checkers.AccountEditionChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.functests.utils.QifBuilder;
import com.budgetview.model.TransactionType;
import org.junit.Test;

public class AccountManagementTest extends LoggedInFunctionalTestCase {

  @Test
  public void testSingleAccount() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 1.23, "2006/01/30")
      .addTransaction("2006/01/10", -1, "Blah")
      .load();

    mainAccounts.checkAccount("Account n. 10101010", 1.23, "2006/01/10");
    mainAccounts.checkReferencePosition(1.23, "2006/01/10");
    transactions.initAmountContent()
      .add("10/01/2006", "BLAH", -1.00, "To categorize", 1.23, 1.23, "Account n. 10101010")
      .check();
  }

  @Test
  public void testAccountsAreUpdatedDuringSubsequentImports() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "123123123", 10, "2006/01/30")
      .addTransaction("2006/01/10", -1, "EDF")
      .addCardAccount("1000200030004000", 300, "2006/01/28")
      .addTransaction("2006/01/17", -3, "Foo")
      .loadDeferredCard("Card n. 1000-2000-3000-4000", "Account n. 123123123");

    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "123123123", 10, "2006/01/31")
      .addTransaction("2006/01/15", -10, "GDF")
      .addCardAccount("1000200030004000", 10, "2006/01/29")
      .addTransaction("2006/01/20", -6, "Bar")
      .load();

    mainAccounts.checkAccount("Account n. 123123123", 0, "2006/01/15");
    mainAccounts.checkAccount("Card n. 1000-2000-30", -9, "2006/01/28");
    mainAccounts.checkReferencePosition(0, "2006/01/15");

    timeline.selectAll();
    transactions.initAmountContent()
      .add("20/01/2006", "BAR", -6.00, "To categorize", -9.00, -9.00, "Card n. 1000-2000-30")
      .add("17/01/2006", "FOO", -3.00, "To categorize", -3.00, -3.00, "Card n. 1000-2000-30")
      .add("15/01/2006", "GDF", -10.00, "To categorize", 0.00, 0.00, "Account n. 123123123")
      .add("10/01/2006", "EDF", -1.00, "To categorize", 10.00, 10.00, "Account n. 123123123")
      .check();
  }

  @Test
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

    mainAccounts.checkAccount("Account n. 10101010", 12345.60, "2006/05/01");
    mainAccounts.checkReferencePosition(12345.60, "2006/05/01");
  }

  @Test
  public void testNothingShownForQifFiles() throws Exception {
    String path = QifBuilder.init(this)
      .addTransaction("2006/01/01", 12.35, "foo")
      .addTransaction("2006/02/01", -7.50, "foo")
      .save();
    operations.importQifFiles(SOCIETE_GENERALE, path);

    mainAccounts.checkDisplayIsEmpty("Main account");
  }

  @Test
  public void testChangeAccountTypeDoesNotUncategorizeTransactionsIfAssociatedSeriesIsNotSavings() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 100, "2008/08/26")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", -800, "Epargne")
      .load();

    timeline.selectAll();
    categorization.setNewIncome("WorldCo", "Income");
    categorization.setNewExtra("Epargne", "Epargne");

    mainAccounts.edit("Account n. 000123")
      .setAsSavings()
      .checkNoErrorDisplayed()
      .validate();

    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("26/08/2008", "EPARGNE", -800.00, "Epargne", 100.00, 100.00, "Account n. 000123")
      .add("26/08/2008", "WORLDCO", 1000.00, "Income", 900.00, 900.00, "Account n. 000123")
      .add("26/07/2008", "WORLDCO", 1000.00, "Income", -100.00, -100.00, "Account n. 000123")
      .check();
  }

  @Test
  public void testRemoveAllAccounts() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 100, "2008/08/26")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", -800, "Epargne")
      .load();

    mainAccounts.edit("Account n. 000123")
      .openDelete()
      .validate();

    mainAccounts.checkNoAccountsDisplayed();
  }

  @Test
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

    categorization.setNewVariable("Foo", "Foos");
    categorization.setNewVariable("Bar", "Bars");

    mainAccounts.select("Account n. 000111");
    views.checkDataSelected();
    transactions.initContent()
      .add("01/01/2006", TransactionType.PRELEVEMENT, "FOO", "", -10.00, "Foos")
      .check();

    mainAccounts.select("Account n. 000222");
    views.checkDataSelected();
    transactions.initContent()
      .add("10/01/2006", TransactionType.PRELEVEMENT, "BAR", "", -20.00, "Bars")
      .check();
  }

  @Test
  public void testAccountWithoutPosition() throws Exception {
    views.selectHome();
    AccountEditionChecker accountEditionChecker = accounts.createNewAccount()
      .setName("no position");
    accountEditionChecker
      .openBankSelection()
      .selectBank("CIC")
      .validate();

    accountEditionChecker.validate();
    mainAccounts.checkAccountWithoutPosition("no position", "2008/08/01");
  }

  @Test
  public void testMultipleAccountAtDifferentDate() throws Exception {
    operations.openPreferences().setFutureMonthsCount(4).validate();

    OfxBuilder.init(this)
      .addBankAccount(30006, 13006, "123", 10, "2008/08/10")
      .addTransaction("2008/07/28", -550, "first account")
      .load();
    categorization.setNewVariable("first account", "first serie", 100.);

    OfxBuilder.init(this)
      .addBankAccount(30006, 13006, "321", 10, "2008/07/30")
      .addTransaction("2008/07/29", -550, "second account ")
      .load();

    mainAccounts.checkAccount("123", 10, "2008/07/28");
    mainAccounts.checkAccount("321", 10, "2008/07/29");
    mainAccounts.checkReferencePosition(20, "2008/07/29");

    OfxBuilder.init(this)
      .addBankAccount("123", -10, "2008/08/14")
      .addTransaction("2008/08/14", -20, "first account")
      .load();

    mainAccounts.checkAccount("123", -10, "2008/08/14");
    mainAccounts.checkAccount("321", 10, "2008/07/29");
    mainAccounts.checkReferencePosition(0, "2008/08/14");
    operations.checkDataIsOk();
  }

  @Test
  public void testMultipleAccountAtWithDateOfAccountEqualToLastOperationDate() throws Exception {
    operations.openPreferences().setFutureMonthsCount(4).validate();

    OfxBuilder.init(this)
      .addBankAccount("123", 10, "2008/08/10")
      .addTransaction("2008/07/28", -550, "first account")
      .load();
    categorization.setNewVariable("first account", "first serie", 100.);

    OfxBuilder.init(this)
      .addBankAccount("321", 10, "2008/07/30")
      .addTransaction("2008/07/30", -550, "second account ")
      .load();

    mainAccounts.checkReferencePosition(20, "2008/07/30");
  }

  @Test
  public void testFindAccountWthOneOperation() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("123", 10, "2008/08/10")
      .addTransaction("2008/07/28", -550, "first account")
      .load();

    OfxBuilder.init(this)
      .addBankAccount("321", 10, "2008/07/30")
      .addTransaction("2008/07/30", -550, "second account")
      .load();

    String path = OfxBuilder.init(this)
      .addBankAccount("321", 10, "2008/07/30")
      .addTransaction("2008/08/01", -50, "second account")
      .save();
    operations.openImportDialog()
      .selectFiles(path)
      .importFileAndPreview()
      .checkSelectedAccount("Account n. 321")
      .importAccountAndOpenNext();
  }

  @Test
  public void testCharts() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 13006, "111", 1000, "2008/05/24")
      .addTransaction("2008/05/24", -100, "Tr 1a")
      .addTransaction("2008/05/20", -200, "Tr 1b")
      .addTransaction("2008/05/20", -200, "Tr 1c")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(30006, 13006, "222", 10000, "2008/05/20")
      .addTransaction("2008/05/20", 1000, "Tr 2a")
      .addTransaction("2008/04/20", 1000, "Tr 2b")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(30006, 13006, "333", 100, "2008/04/15")
      .addTransaction("2008/04/10", 50, "Trans 3")
      .load();

    mainAccounts.edit("Account n. 222").setAsSavings().validate();
    mainAccounts.edit("Account n. 333").setAsSavings().validate();

    timeline.selectMonth(200805);

    mainAccounts.checkAccount("Account n. 111", +1000.00, "2008/05/24");
    mainAccounts.checkChartShown("Account n. 111");
    mainAccounts.getChart("Account n. 111")
      .checkValue(200805, 1, 1500.00)
      .checkValue(200805, 20, 1100.00)
      .checkValue(200805, 24, 1000.00);

    savingsAccounts.checkAccount("Account n. 222", +10000.00, "2008/05/20");
    savingsAccounts.getChart("Account n. 222")
      .checkValue(200805, 1, 9000.00)
      .checkValue(200805, 20, 10000.00);

    savingsAccounts.checkAccount("Account n. 333", +100.00, "2008/04/10");
    savingsAccounts.getChart("Account n. 333")
      .checkValue(200805, 1, 100.00);
  }

  @Test
  public void testSavingsChartsAreHiddenByDefault() throws Exception {
    accounts.createMainAccount("Account 1", "4321", 1000.00);
    mainAccounts.checkChartShown("Account 1");
    mainAccounts.toggleChart("Account 1");
    mainAccounts.checkChartHidden("Account 1");

    accounts.createSavingsAccount("Account 2", 10000.00);
    savingsAccounts.checkChartHidden("Account 2");

    mainAccounts.edit("Account 1").setAsSavings().validate();
    savingsAccounts.checkChartHidden("Account 1");

    savingsAccounts.showChart("Account 1");
    savingsAccounts.checkChartShown("Account 1");

    savingsAccounts.hideChart("Account 1");
    savingsAccounts.checkChartHidden("Account 1");

    savingsAccounts.edit("Account 1").setAsMain().validate();
    mainAccounts.checkChartShown("Account 1");
  }
}
