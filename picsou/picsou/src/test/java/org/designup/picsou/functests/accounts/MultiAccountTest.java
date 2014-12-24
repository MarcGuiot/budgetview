package org.designup.picsou.functests.accounts;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.BankEntity;

public class MultiAccountTest extends LoggedInFunctionalTestCase {

  public void testChangingAccountTypeOperations() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "Virement in")
      .load();
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 222, "222", 2000.00, "2008/08/10")
      .addTransaction("2008/08/10", -100.00, "Virement out")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    views.selectCategorization();
    categorization.selectTransaction("Virement in");
    categorization.selectTransfers().createSeries()
      .setName("epargne")
      .setFromAccount("Account n. 222")
      .setToAccount("Account n. 111")
      .setAmount(150)
      .validate();
    categorization.setTransfer("Virement out", "epargne");

    transactions.initAmountContent()
      .add("10/08/2008", "VIREMENT OUT", -100.00, "epargne", 2000.00, 3000.00, "Account n. 222")
      .add("10/08/2008", "VIREMENT IN", 100.00, "epargne", 1000.00, 3100.00, "Account n. 111")
      .check();

    mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .checkNoErrorDisplayed()
      .checkNoMessageDisplayed()
      .validate();

    transactions.initAmountContent()
      .add("10/08/2008", "VIREMENT OUT", -100.00, "epargne", 2000.00, 2000.00, "Account n. 222")
      .add("10/08/2008", "VIREMENT IN", 100.00, "epargne", 1000.00, 1000.00, "Account n. 111")
      .check();
  }
}
