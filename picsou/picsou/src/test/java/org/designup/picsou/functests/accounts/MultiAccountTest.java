package org.designup.picsou.functests.accounts;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.BankEntity;

public class MultiAccountTest extends LoggedInFunctionalTestCase {

  public void testChangeAccountTypeUncategorizeOperations() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000., "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "Virement in")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement out")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectCategorization();
    categorization.setNewVariable("Virement in", "epargne");
    views.selectHome();
    transactions.initContent()
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement out", "", -100.00)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement in", "", 100.00, "epargne")
      .check();
    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectData();
    transactions.initContent()
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement out", "", -100.00)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement in", "", 100.00)
      .check();
  }

}
