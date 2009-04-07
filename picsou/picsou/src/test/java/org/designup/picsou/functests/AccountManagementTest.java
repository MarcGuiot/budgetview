package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.MasterCategory;
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
    mainAccounts.checkSummary(1.23, "10/01/2006");
    views.selectData();
    transactions.initAmountContent()
      .add("BLAH", -1, 1.23, 1.23)
      .check();
  }

  public void testAccountsAreUpdatedDuringSubsequentImports() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "123123123", 10, "2006/01/30")
      .addTransaction("2006/01/10", -1, "EDF")
      .addCardAccount("1000200030004000", 300, "2006/01/28")
      .addTransaction("2006/01/17", -3, "Foo")
      .load();

    OfxBuilder
      .init(this)
      .addBankAccount(30003, 12345, "123123123", 10, "2006/01/31")
      .addTransaction("2006/01/15", -10, "GDF")
      .addCardAccount("1000200030004000", 10, "2006/01/29")
      .addTransaction("2006/01/20", -6, "Bar")
      .load();

    views.selectHome();
    mainAccounts.checkSummary(20.0, "20/01/2006");
    mainAccounts.checkAccount("Account n. 123123123", 10, "2006/01/15");
    mainAccounts.checkAccount("Card n. 1000-2000-3000-4000", 10, "2006/01/20");
    views.selectData();
    transactions.initAmountContent()
      .add("BAR", -6.00, 10.00, 20.00)
      .add("FOO", -3.00, 16.00, 26.00)
      .add("GDF", -10.00, 10.00, 10.00)
      .add("EDF", -1.00, 20.00, 20.00)
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
    mainAccounts.checkSummary(12345.60, "01/05/2006");
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

  public void testEditingTheAccountBalanceLimit() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "000123", 100, "2008/08/26")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", -800, "FNAC")
      .load();

    views.selectCategorization();
    categorization.setIncome("WorldCo", "Salary", true);

    views.selectHome();
    timeline.selectMonth("2008/08");

    mainAccounts
      .checkEstimatedPosition(100)
      .checkEstimatedPositionColor("darkGray")
      .checkIsEstimatedPosition()
      .checkLimit(0);

    mainAccounts
      .setThreshold(1000, true)
      .checkLimit(1000)
      .checkEstimatedPositionColor("red");

    mainAccounts.setThreshold(-2000, false)
      .checkLimit(-2000)
      .checkEstimatedPositionColor("darkGray");

    mainAccounts.setThreshold(0, false)
      .checkEstimatedPositionColor("darkGray");

    timeline.selectMonth("2008/07");
    mainAccounts
      .checkEstimatedPosition(-100)
      .checkEstimatedPositionColor("darkRed")
      .checkIsRealPosition()
      ;
  }

  public void testChangeAccountTypeUncategorizeTransactionIfAssociatedSeriesIsNotSavings() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "000123", 100, "2008/08/26")
      .addTransaction("2008/07/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", 1000, "WorldCo")
      .addTransaction("2008/08/26", -800, "Epargne")
      .load();

    views.selectCategorization();
    timeline.selectAll();
    categorization.setIncome("WorldCo", "income", true);
    categorization.setSpecial("Epargne", "Epargne", MasterCategory.SAVINGS, true);
    views.selectHome();
    mainAccounts.edit("Account n. 000123")
      .setAsSavings()
      .checkSavingsWarning()
      .setAsCard()
      .checkNoSavingsWarning()
      .setAsSavings()
      .checkSavingsWarning()
      .validate();
    views.selectData();
    transactions.initContent()
      .add("26/08/2008", TransactionType.PLANNED, "Planned: Epargne", "", -800.00, "Epargne", MasterCategory.SAVINGS)
      .add("26/08/2008", TransactionType.PRELEVEMENT, "Epargne", "", -800.00)
      .add("26/08/2008", TransactionType.VIREMENT, "WorldCo", "", 1000.00)
      .add("26/07/2008", TransactionType.VIREMENT, "WorldCo", "", 1000.00)
      .check();
  }
}
