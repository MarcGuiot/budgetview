package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.model.MasterCategory;

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
      .checkUpdateModeIsFileImport()
      .checkUpdateModeIsDisabled()
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
      .checkUpdateModeIsFileImport()
      .checkUpdateModeIsEnabled()
      .checkUpdateModes()
      .checkIsMain()
      .validate();

    mainAccounts.checkAccountNames("Main CIC account");
  }

  public void testCreatingASavingsAccount() throws Exception {
    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAccountName("Savings")
      .setAccountNumber("123")
      .checkUpdateModeIsFileImport()
      .checkUpdateModeIsEnabled()
      .checkUpdateModes()
      .checkIsSavings()
      .selectBank("cic")
      .validate();

    savingsAccounts.edit("Savings")
      .checkIsSavings()
      .cancel();
  }

  public void testABankMustBeSelectedWhenCreatingAnAccount() throws Exception {
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

  public void testDeletingAnEmptyAccount() throws Exception {
    views.selectHome();
    mainAccounts.createNewAccount()
      .setAccountName("Main")
      .selectBank("CIC")
      .validate();

    mainAccounts.edit("Main").delete()
      .checkMessageContains("No operations are related to this account")
      .validate();

    mainAccounts.checkNotPresent("Main");
  }

  public void testDeletingAnAccountAndRelatedTransactions() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "0000123", 100.00, "15/10/2008")
      .addTransaction("2008/10/01", 1000.00, "WorldCo")
      .addTransaction("2008/10/05", -15.00, "MacDo")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "0000666", 100.00, "15/10/2008")
      .addTransaction("2008/10/10", -15.00, "Quick")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "Salaire");
    categorization.setEnvelope("MacDo", "Gastronomie", MasterCategory.FOOD, true);
    categorization.setEnvelope("Quick", "Sante", MasterCategory.FOOD, true);

    views.selectHome();
    monthSummary.checkIncome(1000);
    monthSummary.checkEnvelope(30);

    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PRELEVEMENT, "Quick", "", -15.00, "Sante")
      .add("05/10/2008", TransactionType.PRELEVEMENT, "MacDo", "", -15.00, "Gastronomie")
      .add("01/10/2008", TransactionType.VIREMENT, "WorldCo", "", 1000.00, "Salaire")
      .check();

    views.selectHome();
    mainAccounts.edit("Account n. 0000123").delete()
      .checkMessageContains("All the operations associated to this account will be deleted")
      .validate();
    mainAccounts.checkNotPresent("Account n. 0000123");

    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PRELEVEMENT, "Quick", "", -15.00, "Sante")
      .check();

    views.selectHome();
    monthSummary.checkIncome(0);
    monthSummary.checkEnvelope(15);
  }

  public void testDeletingASavingsAccountWithSeries() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "0000100", 900.00, "15/10/2008")
      .addTransaction("2008/10/01", 1000.00, "Salaire/oct")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(30006, 10674, "0000123", 200000.00, "15/10/2008")
      .addTransaction("2008/10/05", 200.00, "Virement octobre")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("Salaire/oct", "Salaire");
    categorization.setEnvelope("Virement octobre", "Savings", MasterCategory.SAVINGS, true);

    views.selectHome();
    monthSummary.checkIncome(1000.0);
    mainAccounts.edit("Account n. 0000123")
      .setAccountName("Livret")
      .selectBank("ING Direct")
      .setAsSavings()
      .validate();
    savingsAccounts.createNewAccount()
      .setAccountName("Codevi")
      .selectBank("ING Direct")
      .validate();

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Series 1 for Livret")
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Account n. 0000100")
      .setToAccount("Livret")
      .validate();
    budgetView.savings.createSeries()
      .setName("Series 2 for Livret")
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Livret")
      .setToAccount("Account n. 0000100")
      .validate();
    budgetView.savings.createSeries()
      .setName("Series 3 for Codevi")
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Account n. 0000100")
      .setToAccount("Codevi")
      .validate();

    views.selectHome();
    savingsAccounts.edit("Livret")
      .delete()
      .checkMessageContains("All the operations and series associated to this account will be deleted")
      .validate();
    savingsAccounts.checkNotPresent("Livret");

    views.selectBudget();
    budgetView.savings.checkSeriesNotPresent("Series 1 for Livret", "Series 2 for Livret");
    budgetView.savings.checkSeriesPresent("Series 3 for Codevi");
    
    views.selectData();
    transactions.initContent()
      .add("01/10/2008", TransactionType.VIREMENT, "Salaire/oct", "", 1000.00, "Salaire")
      .check();

    views.selectHome();
    savingsAccounts.edit("Codevi").delete()
      .checkMessageContains("All the series associated to this account will be deleted")
      .validate();
    savingsAccounts.checkNotPresent("Codevi");
  }
}
