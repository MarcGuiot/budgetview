package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.TransactionType;

public class MultiAccountTest extends LoggedInFunctionalTestCase {

  public void testChangeAccountTypeUncategorizeOperations() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111", 1000., "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectCategorization();
    categorization.setNewEnvelope("Virement", "epargne");
    views.selectHome();
    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectData();
    transactions.initContent()
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "epargne")
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00)
      .check();
  }

  public void testChangeSeriesBudgetPeriodicity() throws Exception {
    fail("Marc, y a-t-il quelque chose à faire dans ce test vide ?");
  }

  public void testChangeSeriesBudgetAmount() throws Exception {
    fail("Marc, y a-t-il quelque chose à faire dans ce test vide ?");
  }
}
