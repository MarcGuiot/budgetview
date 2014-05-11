package org.designup.picsou.functests.accounts;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class AccountDeletionTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/10/15");
    super.setUp();
  }

  public void testDeletingAnAccountAndRelatedTransactions() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000123", 100.00, "2008/10/15")
      .addTransaction("2008/10/01", 1000.00, "WorldCo")
      .addTransaction("2008/10/05", -15.00, "MacDo")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000666", 100.00, "2008/10/15")
      .addTransaction("2008/10/10", -15.00, "Quick")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "Salaire");
    categorization.setNewVariable("MacDo", "Gastronomie");
    categorization.setNewVariable("Quick", "Sante");

    views.selectBudget();
    budgetView.income.checkTotalObserved(1000);
    budgetView.variable.checkTotalObserved(-30);

    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PRELEVEMENT, "Quick", "", -15.00, "Sante")
      .add("05/10/2008", TransactionType.PRELEVEMENT, "MacDo", "", -15.00, "Gastronomie")
      .add("01/10/2008", TransactionType.VIREMENT, "WorldCo", "", 1000.00, "Salaire")
      .check();

    views.selectHome();
    mainAccounts.edit("Account n. 0000123").openDelete()
      .checkMessageContains("All the operations and series associated to this account will be deleted")
      .validate();
    mainAccounts.checkNotPresent("Account n. 0000123");

    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PRELEVEMENT, "Quick", "", -15.00, "Sante")
      .check();

    views.selectBudget();
    budgetView.income.checkTotalObserved(0);
    budgetView.variable.checkTotalObserved(-15);
  }

  public void testDeletingUsingThePopupMenu() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000123", 100.00, "2008/10/15")
      .addTransaction("2008/10/01", 1000.00, "WorldCo")
      .addTransaction("2008/10/05", -15.00, "MacDo")
      .load();
    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000666", 100.00, "2008/10/15")
      .addTransaction("2008/10/10", -15.00, "Quick")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("WorldCo", "Salaire");
    categorization.setNewVariable("MacDo", "Gastronomie");
    categorization.setNewVariable("Quick", "Sante");

    views.selectHome();
    mainAccounts.openDelete("Account n. 0000123")
      .checkMessageContains("All the operations and series associated to this account will be deleted")
      .validate();
    mainAccounts.checkNotPresent("Account n. 0000123");

    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PRELEVEMENT, "Quick", "", -15.00, "Sante")
      .check();
  }

  public void testDeletingAnEmptyAccount() throws Exception {
    views.selectHome();
    mainAccounts.createNewAccount()
      .setName("Main")
      .selectBank("CIC")
      .validate();

    mainAccounts.edit("Main").openDelete()
      .checkMessageContains("No operations are related to this account")
      .validate();

    mainAccounts.checkNotPresent("Main");
  }

  public void testDeletingASavingsAccountWithSeries() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000100", 900.00, "2008/10/15")
      .addTransaction("2008/10/01", 1000.00, "Salaire/oct")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000123", 200000.00, "2008/10/15")
      .addTransaction("2008/10/05", 200.00, "Virement octobre")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("Salaire/oct", "Salaire");
    categorization.setNewVariable("Virement octobre", "Savings");

    views.selectBudget();
    budgetView.income.checkTotalObserved(1000);

    views.selectHome();
    mainAccounts.edit("Account n. 0000123")
      .setName("Livret")
      .selectBank("ING Direct")
      .setAsSavings()
      .validate();
    savingsAccounts.createNewAccount()
      .setName("Codevi")
      .selectBank("ING Direct")
      .validate();

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Series 1 for Livret")
      .setFromAccount("Account n. 0000100")
      .setToAccount("Livret")
      .validate();
    budgetView.savings.createSeries()
      .setName("Series 2 for Livret")
      .setFromAccount("Livret")
      .setToAccount("Account n. 0000100")
      .validate();
    budgetView.savings.createSeries()
      .setName("Series 3 for Codevi")
      .setFromAccount("Account n. 0000100")
      .setToAccount("Codevi")
      .validate();

    views.selectHome();
    savingsAccounts.edit("Livret")
      .openDelete()
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
    savingsAccounts.edit("Codevi").openDelete()
      .checkMessageContains("All the series associated to this account will be deleted")
      .validate();
    savingsAccounts.checkNotPresent("Codevi");
  }
}
