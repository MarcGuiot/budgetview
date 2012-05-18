package org.designup.picsou.functests.accounts;

import org.designup.picsou.functests.checkers.AccountEditionChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.TransactionType;

public class AccountManagementTest extends LoggedInFunctionalTestCase {

  public void testSingleAccount() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "10101010", 1.23, "2006/01/30")
      .addTransaction("2006/01/10", -1, "Blah")
      .load();

    mainAccounts.checkAccount("Account n. 10101010", 1.23, "2006/01/10");
    mainAccounts.checkSummary(1.23, "2006/01/10");
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

    mainAccounts.checkSummary(10.0, "2006/01/15");
    mainAccounts.checkAccount("Account n. 123123123", 10, "2006/01/15");
    mainAccounts.checkAccount("Card n. 1000-2000-3000-4000", 10, "2006/01/31");

    timeline.selectAll();
    transactions.initAmountContent()
      .add("20/01/2006", "BAR", -6.00, "To categorize", 10.00, 1.00, "Card n. 1000-2000-3000-4000")
      .add("17/01/2006", "FOO", -3.00, "To categorize", 16.00, 7.00, "Card n. 1000-2000-3000-4000")
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

    mainAccounts.checkAccount("Account n. 10101010", 12345.60, "2006/05/01");
    mainAccounts.checkSummary(12345.60, "2006/05/01");
  }

  public void testNothingShownForQifFiles() throws Exception {
    String path = QifBuilder.init(this)
      .addTransaction("2006/01/01", 12.35, "foo")
      .addTransaction("2006/02/01", -7.50, "foo")
      .save();
    operations.importQifFiles(SOCIETE_GENERALE, path);

    mainAccounts.checkDisplayIsEmpty("Main account");
  }

  public void testChangeAccountTypeUncategorizeTransactionIfAssociatedSeriesIsNotSavings() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 100, "2008/08/26")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", -800, "Epargne")
      .load();

    timeline.selectAll();
    categorization.setNewIncome("WorldCo", "income");
    categorization.setNewExtra("Epargne", "Epargne");

    mainAccounts.edit("Account n. 000123")
      .setAsSavings()
      .checkSavingsWarning()
      .setAsMain()
      .checkNoSavingsWarning()
      .setAsSavings()
      .checkSavingsWarning()
      .validate();

    transactions
      .showPlannedTransactions()
      .initContent()
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Epargne", "", -800.00, "Epargne")
      .add("26/08/2008", TransactionType.PRELEVEMENT, "Epargne", "", -800.00)
      .add("26/08/2008", TransactionType.VIREMENT, "WorldCo", "", 1000.00)
      .add("26/07/2008", TransactionType.VIREMENT, "WorldCo", "", 1000.00)
      .check();
  }

  public void testRemoveAllAccounts() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "000123", 100, "2008/08/26")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", -800, "Epargne")
      .load();

    mainAccounts.edit("Account n. 000123")
      .delete()
      .validate();

    mainAccounts
      .checkNoAccountsDisplayed()
      .checkNoEstimatedPosition();

    budgetView.getSummary().checkNoEstimatedPosition();
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

  public void testAccountWithoutPosition() throws Exception {
    views.selectHome();
    AccountEditionChecker accountEditionChecker = mainAccounts.createNewAccount()
      .setName("no position");
    accountEditionChecker
      .openBankSelection()
      .selectBank("CIC")
      .validate();

    accountEditionChecker.validate();
    mainAccounts.checkAccountWithoutPosition("no position", "2008/08/31");
  }

  public void testMultipleAccountAtDifferentDate() throws Exception {
    operations.openPreferences().setFutureMonthsCount(4).validate();

    OfxBuilder.init(this)
      .addBankAccount("13006", 13006, "123", 10, "2008/08/10")
      .addTransaction("2008/07/28", -550, "first account")
      .load();
    categorization.setNewVariable("first account", "first serie", 100.);

    OfxBuilder.init(this)
      .addBankAccount("13006", 13006, "321", 10, "2008/07/30")
      .addTransaction("2008/07/29", -550, "second account ")
      .load();

    mainAccounts.checkAccount("123", 10, "2008/07/28");
    mainAccounts.checkAccount("321", 10, "2008/07/29");
    mainAccounts.checkSummary(20, "2008/07/29");

    OfxBuilder.init(this)
      .addBankAccount("123", -10, "2008/08/14")
      .addTransaction("2008/08/14", -20, "first account")
      .load();

    mainAccounts.checkAccount("123", -10, "2008/08/14");
    mainAccounts.checkAccount("321", 10, "2008/07/29");
    mainAccounts.checkSummary(0, "2008/08/14");
    operations.checkDataIsOk();
  }


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

    mainAccounts.checkSummary(20, "2008/07/30");
  }

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
      .acceptFile()
      .checkSelectedAccount("Account n. 321")
      .doImport();
  }
}
