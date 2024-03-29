package com.budgetview.functests.utils;

import com.budgetview.functests.checkers.ImportDialogChecker;
import com.budgetview.functests.checkers.TransactionChecker;
import com.budgetview.model.TransactionType;

public abstract class LangTestCase extends LoggedInFunctionalTestCase {

  public void setUp() throws Exception {
    resetWindow();
    super.setUp();
    categorization.setUseDisplayedDates();
    transactions.setUseDisplayedDates();
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    resetWindow();
  }

  protected void loadSingleTransaction(String transactionDate, String transactionLabel, String accountDate) {
    OfxBuilder.init(this)
      .addBankAccount("007", 1000.00, accountDate)
      .addTransaction(transactionDate, -100.00, transactionLabel)
      .load();
  }

  protected void checkDates(String tableDate, String summaryDate, String tooltipDate) {
    checkDates(tableDate, TransactionChecker.TO_CATEGORIZE, summaryDate, tooltipDate, "Account n. 007");
  }

  protected void checkDates(String tableDate, String series, String summaryDate, String tooltipDate, String accountName) {
    categorization.checkTable(new Object[][]{
      {tableDate, "", "AUCHAN", -100.00},
    });
    transactions.initContent()
      .add(tableDate, TransactionType.PRELEVEMENT, "AUCHAN", "", -100.00, series)
      .check();
    accounts.checkContentContains(summaryDate);
    mainAccounts.getChart(getAccountName()).checkTooltipContains(200808, 12, tooltipDate);
    mainAccounts.checkReferencePositionDateContains(summaryDate);
    mainAccounts.checkAccountUpdateDate(accountName, summaryDate);
  }

  protected abstract String getAccountName();
}
