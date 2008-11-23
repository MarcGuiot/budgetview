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

  public void testCreatingAnAccount() throws Exception {
    views.selectHome();

    accounts.createMain()
      .checkAccountName("")
      .setAccountName("Main CIC account")
      .checkTypes("Main", "Card", "Savings")
      .selectBank("CIC")
      .checkIsMain()
      .validate();

    accounts.checkAccountNames("Main CIC account");
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

  public void testMainAccountTypeIsTheDefault() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "0000123", 100.00, "15/10/2008")
      .addTransaction("2008/10/01", 15.00, "MacDo")
      .load();

    views.selectHome();

    accounts.edit("Account n. 0000123")
      .checkAccountName("Account n. 0000123")
      .checkIsMain()
      .setAsSavings()
      .validate();

    accounts.edit("Account n. 0000123")
      .checkIsSavings()
      .setAsCard()
      .validate();

    accounts.edit("Account n. 0000123")
      .checkIsCard()
      .cancel();
  }
}
