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

  public void testDeletingAnEmptyAccount() throws Exception {
    views.selectHome();
    mainAccounts.createNewAccount()
      .setAccountName("Main")
      .selectBank("CIC")
      .validate();

    mainAccounts.edit("Main").delete()
      .checkContainsText("No operations are related to this account")
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
    categorization.setIncome("WorldCo", "Salaire", true);
    categorization.setEnvelope("MacDo", "Gastronomie", MasterCategory.FOOD, true);
    categorization.setEnvelope("Quick", "Sante", MasterCategory.FOOD, true);

    views.selectHome();
    monthSummary.checkIncome(1000);
    monthSummary.checkEnvelope(30);

    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PRELEVEMENT, "Quick", "", -15.00, "Sante", MasterCategory.FOOD)
      .add("05/10/2008", TransactionType.PRELEVEMENT, "MacDo", "", -15.00, "Gastronomie", MasterCategory.FOOD)
      .add("01/10/2008", TransactionType.VIREMENT, "WorldCo", "", 1000.00, "Salaire", MasterCategory.INCOME)
      .check();

    views.selectHome();
    mainAccounts.edit("Account n. 0000123").delete()
      .checkContainsText("All the operations associated to this account will be deleted")
      .validate();
    mainAccounts.checkNotPresent("Account n. 0000123");

    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PRELEVEMENT, "Quick", "", -15.00, "Sante", MasterCategory.FOOD)
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
    categorization.setIncome("Salaire/oct", "Salaire", true);
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
      .setFromAccount("Main accounts")
      .setToAccount("Livret")
      .validate();
    budgetView.savings.createSeries()
      .setName("Series 2 for Livret")
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Livret")
      .setToAccount("Main accounts")
      .validate();
    budgetView.savings.createSeries()
      .setName("Series 3 for Codevi")
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Main accounts")
      .setToAccount("Codevi")
      .validate();

    views.selectHome();
    savingsAccounts.edit("Livret").delete()
      .checkContainsText("All the operations and series associated to this account will be deleted")
      .validate();
    savingsAccounts.checkNotPresent("Livret");

    views.selectBudget();
    budgetView.savings.checkSeriesNotPresent("Series 1 for Livret", "Series 2 for Livret");
    budgetView.savings.checkSeriesPresent("Series 3 for Codevi");
    
    views.selectData();
    transactions.initContent()
      .add("01/10/2008", TransactionType.VIREMENT, "Salaire/oct", "", 1000.00, "Salaire", MasterCategory.INCOME)
      .check();

    views.selectHome();
    savingsAccounts.edit("Codevi").delete()
      .checkContainsText("All the series associated to this account will be deleted")
      .validate();
    savingsAccounts.checkNotPresent("Codevi");
  }
}
