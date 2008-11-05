package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class AccountEditionTest extends LoggedInFunctionalTestCase {
  public void testEditingAnExistingAccount() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "0000123", 100.00, "15/10/2008")
      .addTransaction("2008/10/01", 15.00, "MacDo")
      .load();

    views.selectHome();

    accounts.edit("Account n. 0000123")
      .checkAccountName("Account n. 0000123")
      .checkAccountNumber("0000123")
      .checkBalanceDisplayed(false)
      .setAccountName("My account")
      .setAccountNumber("12345")
      .validate();

    accounts.checkAccountNames("My account");

    accounts.checkAccountInformation("My account", "12345");
  }

  public void testEmptyAccountNamesAreNotAllowed() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "0000123", 100.00, "15/10/2008")
      .addTransaction("2008/10/01", 15.00, "MacDo")
      .load();

    views.selectHome();

    accounts.edit("Account n. 0000123")
      .checkAccountName("Account n. 0000123")
      .setAccountName("")
      .checkValidationError("You must enter a name for the account")
      .cancel();

    accounts.checkAccountNames("Account n. 0000123");
  }

  public void testDayAccountTypeIsTheDefaultAndIsImported() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "0000123", 100.00, "15/10/2008")
      .addTransaction("2008/10/01", 15.00, "MacDo")
      .load();

    views.selectHome();

    accounts.edit("Account n. 0000123")
      .checkAccountName("Account n. 0000123")
      .checkIsDay()
      .checkIsImported()
      .setAsSaving()
      .clickOnImported()
      .validate();

    accounts.edit("Account n. 0000123")
      .checkIsSaving()
      .setAsCreditCard()
      .checkNotImported()
      .validate();

    accounts.edit("Account n. 0000123")
      .checkIsCard()
      .cancel();
  }
}
