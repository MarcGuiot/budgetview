package org.designup.picsou.functests.accounts;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.BankEntity;

public class MultiAccountTest extends LoggedInFunctionalTestCase {

  public void testChangeAccountTypeUncategorizeOperations() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000., "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectCategorization();
    categorization.setNewVariable("Virement", "epargne");
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

}