package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class AccountViewTest extends LoggedInFunctionalTestCase {

  public void testCreateAccount() throws Exception {
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
  }
}
