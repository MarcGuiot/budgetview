package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class TransactionFilteringTest extends LoggedInFunctionalTestCase {

  public void testAccountFiltering() throws Exception {
    OfxBuilder
      .init(this)
      .addBankAccount("1", 0.00, "2006/05/10")
      .addTransaction("2006/05/11", -10.0, "Transaction 1")
      .addBankAccount("2", 0.00, "2006/05/10")
      .addTransaction("2006/05/10", -10.0, "Transaction 2")
      .load();

    transactions.initContent()
      .add("11/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1", "", -10.00)
      .add("10/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 2", "", -10.00)
      .check();

    mainAccounts.select("Account n. 1");
    mainAccounts.checkSelectedAccounts("Account n. 1");
    transactions.checkClearFilterButtonShown();
    transactions.initContent()
      .add("11/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 1", "", -10.00)
      .check();

    transactions.clearFilters();
    mainAccounts.checkNoAccountsSelected();

    mainAccounts.select("Account n. 2");
    mainAccounts.checkSelectedAccounts("Account n. 2");
    transactions.checkClearFilterButtonShown();
    transactions.initContent()
      .add("10/05/2006", TransactionType.PRELEVEMENT, "TRANSACTION 2", "", -10.00)
      .check();

    transactions.clearFilters();
    mainAccounts.checkNoAccountsSelected();
    transactions.checkClearFilterButtonHidden();
  }
}
