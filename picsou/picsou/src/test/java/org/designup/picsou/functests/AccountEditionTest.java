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

    mainAccounts.edit("Account n. 0000123")
      .checkAccountName("Account n. 0000123")
      .checkAccountNumber("0000123")
      .checkBalanceDisplayed(false)
      .setAccountName("My account")
      .setAccountNumber("12345")
      .validate();

    mainAccounts.checkAccountNames("My account");

    mainAccounts.checkAccountInformation("My account", "12345");
  }

  public void testCreatingAMainAccount() throws Exception {
    views.selectHome();

    mainAccounts.createNewAccount()
      .checkAccountName("")
      .setAccountName("Main CIC account")
      .checkTypes("Main", "Card", "Savings")
      .selectBank("CIC")
      .checkIsMain()
      .validate();

    mainAccounts.checkAccountNames("Main CIC account");
  }

  public void testCreatingASavingsAccount() throws Exception {
    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAccountName("Savings")
      .setAccountNumber("123")
      .checkIsSavings()
      .selectBank("cic")
      .validate();

    savingsAccounts.edit("Savings")
      .checkIsSavings()
      .cancel();
  }

  public void testABankMustBeSelected() throws Exception {
    views.selectHome();

    mainAccounts.createNewAccount()
      .setAccountName("Main")
      .checkNoBankSelected()
      .checkValidationError("You must select a bank for this account")
      .selectBank("Autre")
      .validate();

    mainAccounts.edit("Main")
      .checkSelectedBank("Autre")
      .cancel();
  }

  public void testEmptyAccountNamesAreNotAllowed() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "0000123", 100.00, "15/10/2008")
      .addTransaction("2008/10/01", 15.00, "MacDo")
      .load();

    views.selectHome();

    mainAccounts.edit("Account n. 0000123")
      .checkAccountName("Account n. 0000123")
      .setAccountName("")
      .checkValidationError("You must enter a name for the account")
      .cancel();

    mainAccounts.checkAccountNames("Account n. 0000123");
  }

  public void testChangingAMainAccountIntoASavingsAccount() throws Exception {
    views.selectHome();

    mainAccounts.createNewAccount()
      .setAccountName("Main CIC account")
      .selectBank("CIC")
      .checkIsMain()
      .validate();

    savingsAccounts.checkNoAccountsDisplayed();

    mainAccounts.edit("Main CIC account")
      .setAsSavings()
      .validate();

    savingsAccounts.checkAccountNames("Main CIC account");
    mainAccounts.checkNoAccountsDisplayed();
  }

  public void testMainAccountTypeIsTheDefault() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "0000123", 100.00, "15/10/2008")
      .addTransaction("2008/10/01", 15.00, "MacDo")
      .load();

    views.selectHome();

    mainAccounts.edit("Account n. 0000123")
      .checkAccountName("Account n. 0000123")
      .checkIsMain()
      .setAsCard()
      .validate();

    mainAccounts.edit("Account n. 0000123")
      .checkIsCard()
      .setAsMain()
      .validate();

    mainAccounts.edit("Account n. 0000123")
      .checkIsMain()
      .cancel();
  }
}
