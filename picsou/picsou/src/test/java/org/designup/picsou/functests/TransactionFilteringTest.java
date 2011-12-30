package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class TransactionFilteringTest extends LoggedInFunctionalTestCase {

  public void testFilteringWithSingleAccount() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("1", 500.00, "2006/05/15")
      .addTransaction("2006/05/08", -400.0, "Transaction 1c")
      .addTransaction("2006/05/02", -300.0, "Transaction 1b")
      .addTransaction("2006/04/15", -200.0, "Transaction 1a")
      .load();

    transactions.initContent()
      .add("08/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1C", "", -400.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1B", "", -300.00)
      .check();
    transactions.checkGraph("Main accounts - may 2006")
      .checkRange(200605, 200605)
      .checkCurrentDay(200605, 8)
      .checkValue(200605, 15, 500.00)
      .checkValue(200605, 6, 900.00)
      .checkValue(200605, 1, 1200.00);

    mainAccounts.select("Account n. 1");
    transactions.initContent()
      .add("08/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1C", "", -400.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1B", "", -300.00)
      .check();
    transactions.checkGraph("Account n. 1 - may 2006")
      .checkRange(200605, 200605)
      .checkCurrentDay(200605, 8)
      .checkValue(200605, 15, 500.00)
      .checkValue(200605, 6, 900.00)
      .checkValue(200605, 1, 1200.00);
    transactions.clearFilters();

    timeline.selectMonth(200604);
    transactions.initContent()
      .add("15/04/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1A", "", -200.00)
      .check();
    transactions.checkGraph("Main accounts - april 2006")
      .checkRange(200604, 200604)
      .checkIsPastOnly(200604)
      .checkValue(200604, 16, 1200.00)
      .checkValue(200604, 14, 1400.00);

    mainAccounts.select("Account n. 1");
    transactions.initContent()
      .add("15/04/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1A", "", -200.00)
      .check();
    transactions.checkGraph("Account n. 1 - april 2006")
      .checkRange(200604, 200604)
      .checkIsPastOnly(200604)
      .checkValue(200604, 16, 1200.00)
      .checkValue(200604, 14, 1400.00);
  }

  public void testFilteringWithMultipleAccounts() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("1", 500.00, "2006/05/15")
      .addTransaction("2006/05/02", -400.0, "Transaction 1a")
      .addTransaction("2006/05/08", -200.0, "Transaction 1b")
      .addTransaction("2006/05/11", -100.0, "Transaction 1c")
      .addBankAccount("2", 1000.00, "2006/05/15")
      .addTransaction("2006/05/05", -300.0, "Transaction 2a")
      .addTransaction("2006/05/10", -200.0, "Transaction 2b")
      .addBankAccount("3", 10000.00, "2006/05/15")
      .addTransaction("2006/05/08", -1000.0, "Transaction 3a")
      .load();

    mainAccounts.edit("Account n. 3").setAsSavings().validate();

    transactions.initAmountContent()
      .add("11/05/2006", "TRANSACTION 1C", -100.00, "To categorize", 500.00, 1500.00, "Account n. 1")
      .add("10/05/2006", "TRANSACTION 2B", -200.00, "To categorize", 1000.00, 1600.00, "Account n. 2")
      .add("08/05/2006", "TRANSACTION 3A", -1000.00, "To categorize", 10000.00, 10000.00, "Account n. 3")
      .add("08/05/2006", "TRANSACTION 1B", -200.00, "To categorize", 600.00, 1800.00, "Account n. 1")
      .add("05/05/2006", "TRANSACTION 2A", -300.00, "To categorize", 1200.00, 2000.00, "Account n. 2")
      .add("02/05/2006", "TRANSACTION 1A", -400.00, "To categorize", 800.00, 2300.00, "Account n. 1")
      .check();
    transactions.checkGraph("Main accounts - may 2006")
      .checkRange(200605, 200605)
      .checkCurrentDay(200605, 11)
      .checkValue(200605, 15, 1500.00)
      .checkValue(200605, 9, 1800.00)
      .checkValue(200605, 6, 2000.00)
      .checkValue(200605, 3, 2300.00);

    mainAccounts.select("Account n. 1");
    mainAccounts.checkSelectedAccounts("Account n. 1");
    transactions.checkClearFilterButtonShown();
    transactions.initAmountContent()
      .add("11/05/2006", "TRANSACTION 1C", -100.00, "To categorize", 500.00, 1500.00, "Account n. 1")
      .add("08/05/2006", "TRANSACTION 1B", -200.00, "To categorize", 600.00, 1800.00, "Account n. 1")
      .add("02/05/2006", "TRANSACTION 1A", -400.00, "To categorize", 800.00, 2300.00, "Account n. 1")
      .check();
    transactions.checkGraph("Account n. 1 - may 2006")
      .checkRange(200605, 200605)
      .checkCurrentDay(200605, 11)
      .checkValue(200605, 15, 500.00)
      .checkValue(200605, 10, 600.00)
      .checkValue(200605, 5, 800.00);

    transactions.clearFilters();
    mainAccounts.checkNoAccountsSelected();
    transactions.checkGraph("Main accounts - may 2006")
      .checkRange(200605, 200605)
      .checkCurrentDay(200605, 11)
      .checkValue(200605, 15, 1500.00)
      .checkValue(200605, 9, 1800.00)
      .checkValue(200605, 6, 2000.00);

    mainAccounts.select("Account n. 2");
    mainAccounts.checkSelectedAccounts("Account n. 2");
    transactions.checkClearFilterButtonShown();
    transactions.initAmountContent()
      .add("10/05/2006", "TRANSACTION 2B", -200.00, "To categorize", 1000.00, 1600.00, "Account n. 2")
      .add("05/05/2006", "TRANSACTION 2A", -300.00, "To categorize", 1200.00, 2000.00, "Account n. 2")
      .check();
    transactions.checkGraph("Account n. 2 - may 2006")
      .checkRange(200605, 200605)
      .checkCurrentDay(200605, 11)
      .checkValue(200605, 15, 1000.00)
      .checkValue(200605, 9, 1200.00)
      .checkValue(200605, 3, 1500.00);

    savingsAccounts.select("Account n. 3");
    transactions.checkClearFilterButtonShown();
    transactions.initContent()
      .add("08/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 3A", "", -1000.00)
      .check();
    transactions.checkGraph("Account n. 3 - may 2006")
      .checkRange(200605, 200605)
      .checkCurrentDay(200605, 11)
      .checkValue(200605, 12, 10000.00)
      .checkValue(200605, 7, 11000.00);

    transactions.clearFilters();
    mainAccounts.checkNoAccountsSelected();
    transactions.checkClearFilterButtonHidden();
    transactions.initAmountContent()
      .add("11/05/2006", "TRANSACTION 1C", -100.00, "To categorize", 500.00, 1500.00, "Account n. 1")
      .add("10/05/2006", "TRANSACTION 2B", -200.00, "To categorize", 1000.00, 1600.00, "Account n. 2")
      .add("08/05/2006", "TRANSACTION 3A", -1000.00, "To categorize", 10000.00, 10000.00, "Account n. 3")
      .add("08/05/2006", "TRANSACTION 1B", -200.00, "To categorize", 600.00, 1800.00, "Account n. 1")
      .add("05/05/2006", "TRANSACTION 2A", -300.00, "To categorize", 1200.00, 2000.00, "Account n. 2")
      .add("02/05/2006", "TRANSACTION 1A", -400.00, "To categorize", 800.00, 2300.00, "Account n. 1")
      .check();
    transactions.checkGraph("Main accounts - may 2006")
      .checkRange(200605, 200605)
      .checkCurrentDay(200605, 11)
      .checkValue(200605, 15, 1500.00)
      .checkValue(200605, 9, 1800.00)
      .checkValue(200605, 6, 2000.00);

    timeline.selectAll();
    mainAccounts.select("Account n. 1");
    transactions.checkGraph("Account n. 1 - may 2006 - august 2008")
      .checkValue(200605, 1, 1200.00)
      .checkValue(200605, 2, 800.00)
      .checkValue(200605, 8, 600.00)
      .checkValue(200605, 11, 500.00)
      .checkValue(200608, 1, 500.00);
    mainAccounts.select("Account n. 1", "Account n. 2");
    transactions.checkGraph("2 accounts - may 2006 - august 2008")
      .checkValue(200605, 1, 2700.00)
      .checkValue(200605, 2, 2300.00)
      .checkValue(200605, 5, 2000.00)
      .checkValue(200605, 8, 1800.00)
      .checkValue(200605, 10, 1600.00)
      .checkValue(200605, 11, 1500.00)
      .checkValue(200605, 31, 1500.00)
      .checkValue(200608, 1, 1500.00);
  }

  public void testAccountFilteringWithMissingPreviousAndNextMonth() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("1", 500.00, "2006/05/15")
      .addTransaction("2006/04/15", -400.0, "Transaction 1a")
      .addTransaction("2006/05/02", -400.0, "Transaction 1b")
      .addTransaction("2006/05/08", -200.0, "Transaction 1c")
      .addBankAccount("2", 1000.00, "2006/05/15")
      .addTransaction("2006/04/20", -300.0, "Transaction 2a")
      .load();

    mainAccounts.createNewAccount()
      .setAccountName("Account n. 3")
      .selectBank("CIC")
      .setPosition(10000.00)
      .validate();

    transactions.initAmountContent()
      .add("08/05/2006", "TRANSACTION 1C", -200.00, "To categorize", 500.00, 11500.00, "Account n. 1")
      .add("02/05/2006", "TRANSACTION 1B", -400.00, "To categorize", 700.00, 11700.00, "Account n. 1")
      .check();
    transactions.checkGraph("Main accounts - may 2006")
      .checkRange(200605, 200605)
      .checkCurrentDay(200605, 8)
      .checkValue(200605, 15, 11500.00)
      .checkValue(200605, 8, 11500.00)
      .checkValue(200605, 7, 11700.00)
      .checkValue(200605, 1, 12100.00);

    timeline.selectMonth(200604);
    transactions.initAmountContent()
      .add("20/04/2006", "TRANSACTION 2A", -300.00, "To categorize", 1000.00, 12100.00, "Account n. 2")
      .add("15/04/2006", "TRANSACTION 1A", -400.00, "To categorize", 1100.00, 12400.00, "Account n. 1")
      .check();
    transactions.checkGraph("Main accounts - april 2006")
      .checkRange(200604, 200604)
      .checkValue(200604, 25, 12100.00)
      .checkValue(200604, 18, 12400.00)
      .checkValue(200604, 10, 12800.00);

    mainAccounts.select("Account n. 1");
    transactions.initContent()
      .add("15/04/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1A", "", -400.00)
      .check();
    transactions.checkGraph("Account n. 1 - april 2006")
      .checkRange(200604, 200604)
      .checkValue(200604, 20, 1100.00)
      .checkValue(200604, 14, 1500.00);

    mainAccounts.select("Account n. 2");
    transactions.initContent()
      .add("20/04/2006", TransactionType.PRELEVEMENT, "TRANSACTION 2A", "", -300.00)
      .check();
    transactions.checkGraph("Account n. 2 - april 2006")
      .checkRange(200604, 200604)
      .checkValue(200604, 25, 1000.00)
      .checkValue(200604, 19, 1300.00);

    mainAccounts.select("Account n. 3");
    mainAccounts.checkSelectedAccounts("Account n. 3");
    transactions.checkClearFilterButtonShown();
    transactions.checkTableIsEmpty();
    transactions.checkGraph("Account n. 3 - april 2006")
      .checkRange(200604, 200604)
      .checkValue(200604, 25, 10000.00)
      .checkValue(200604, 15, 10000.00)
      .checkValue(200604, 5, 10000.00);

    transactions.clearFilters();
    mainAccounts.checkNoAccountsSelected();
    transactions.checkClearFilterButtonHidden();
    timeline.selectMonths(200604, 200605);
    transactions.initContent()
      .add("08/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1C", "", -200.00)
      .add("02/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1B", "", -400.00)
      .add("20/04/2006", TransactionType.PRELEVEMENT, "TRANSACTION 2A", "", -300.00)
      .add("15/04/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1A", "", -400.00)
      .check();
    transactions.checkGraph("Main accounts - april - may 2006")
      .checkRange(200604, 200605)
      .checkValue(200605, 15, 11500.00)
      .checkValue(200605, 9, 11500.0)
      .checkValue(200605, 6, 11700.0);

    timeline.selectAll();
    mainAccounts.select("Account n. 1");
    transactions.checkGraph("Account n. 1 - april 2006 - august 2008")
      .checkValue(200604, 1, 1500.00)
      .checkValue(200604, 15, 1100.00)
      .checkValue(200605, 2, 700.00)
      .checkValue(200605, 8, 500.00);
    mainAccounts.select("Account n. 1", "Account n. 2");
    transactions.initAmountContent()
      .add("08/05/2006", "TRANSACTION 1C", -200.00, "To categorize", 500.00, 11500.00, "Account n. 1")
      .add("02/05/2006", "TRANSACTION 1B", -400.00, "To categorize", 700.00, 11700.00, "Account n. 1")
      .add("20/04/2006", "TRANSACTION 2A", -300.00, "To categorize", 1000.00, 12100.00, "Account n. 2")
      .add("15/04/2006", "TRANSACTION 1A", -400.00, "To categorize", 1100.00, 12400.00, "Account n. 1")
      .check();
    transactions.checkGraph("2 accounts - april 2006 - august 2008")
      .checkValue(200604, 1, 2800.00)
      .checkValue(200604, 15, 2400.00)
      .checkValue(200604, 20, 2100.00)
      .checkValue(200605, 2, 1700.00)
      .checkValue(200605, 8, 1500.00);
  }
}
