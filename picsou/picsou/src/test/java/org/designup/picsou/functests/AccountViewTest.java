package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class AccountViewTest extends LoggedInFunctionalTestCase {

  public void testCreateAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();
    views.selectHome();
    accounts.create()
      .setAccountName("Saving")
      .setAccountNumber("123")
      .setAsSavings()
      .selectBank("cic")
      .validate();

    accounts.edit("Saving")
      .checkIsSavings()
      .cancel();
    views.selectData();
    transactions
      .initContent()
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00)
      .check();

  }
}
