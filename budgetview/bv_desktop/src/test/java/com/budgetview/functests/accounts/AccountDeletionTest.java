package com.budgetview.functests.accounts;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.TransactionType;
import org.junit.Test;

public class AccountDeletionTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/10/15");
    super.setUp();
  }

  @Test
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

    categorization.setNewIncome("WorldCo", "Salaire");
    categorization.setNewVariable("MacDo", "Gastronomie");
    categorization.setNewVariable("Quick", "Sante");

    budgetView.variable.editSeries("Gastronomie")
      .setTargetAccount("Account n. 0000123")
      .validate();

    budgetView.income.checkTotalObserved(1000);
    budgetView.variable.checkTotalObserved(-30);

    transactions.initContent()
      .add("10/10/2008", TransactionType.PRELEVEMENT, "Quick", "", -15.00, "Sante")
      .add("05/10/2008", TransactionType.PRELEVEMENT, "MacDo", "", -15.00, "Gastronomie")
      .add("01/10/2008", TransactionType.VIREMENT, "WorldCo", "", 1000.00, "Salaire")
      .check();

    mainAccounts.edit("Account n. 0000123").openDelete()
      .checkMessageContains("All the operations and series associated to this account will be deleted")
      .validate();
    mainAccounts.checkNotPresent("Account n. 0000123");

    transactions.initContent()
      .add("10/10/2008", TransactionType.PRELEVEMENT, "Quick", "", -15.00, "Sante")
      .check();

    budgetView.income.checkTotalObserved(0);
    budgetView.variable.checkTotalObserved(-15);

    budgetView.variable.checkContent("| Sante | 15.00 | To define |");
  }

  @Test
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

    categorization.setNewIncome("WorldCo", "Salaire");
    categorization.setNewVariable("MacDo", "Gastronomie");
    categorization.setNewVariable("Quick", "Sante");

    budgetView.variable.editSeries("Gastronomie")
      .setTargetAccount("Account n. 0000123")
      .validate();

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

  @Test
  public void testDeletingAnEmptyAccount() throws Exception {
    views.selectHome();
    accounts.createNewAccount()
      .setName("Main")
      .selectBank("CIC")
      .validate();

    mainAccounts.edit("Main").openDelete()
      .checkMessageContains("No operations are related to this account")
      .validate();

    mainAccounts.checkNotPresent("Main");
  }

  @Test
  public void testDeletingASavingsAccountWithSeries() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000100", 900.00, "2008/10/15")
      .addTransaction("2008/10/01", 1000.00, "Salaire/oct")
      .load();

    OfxBuilder.init(this)
      .addBankAccount(-1, 10674, "0000123", 200000.00, "2008/10/15")
      .addTransaction("2008/10/05", 200.00, "Virement octobre")
      .load();

    categorization.setNewIncome("Salaire/oct", "Salaire");
    categorization.setNewVariable("Virement octobre", "Savings");

    timeline.selectMonth(200810);
    budgetView.income.checkTotalObserved(1000);

    mainAccounts.edit("Account n. 0000123")
      .setName("Livret")
      .selectBank("ING Direct")
      .setAsSavings()
      .validate();
    accounts.createNewAccount()
      .setName("Codevi")
      .setAsSavings()
      .selectBank("ING Direct")
      .validate();

    budgetView.transfer.createSeries()
      .setName("Series 1 for Livret")
      .setFromAccount("Account n. 0000100")
      .setToAccount("Livret")
      .validate();
    budgetView.transfer.createSeries()
      .setName("Series 2 for Livret")
      .setFromAccount("Livret")
      .setToAccount("Account n. 0000100")
      .validate();
    budgetView.transfer.createSeries()
      .setName("Series 3 for Codevi")
      .setFromAccount("Account n. 0000100")
      .setToAccount("Codevi")
      .validate();

    savingsAccounts.edit("Livret")
      .openDelete()
      .checkMessageContains("All the operations and series associated to this account will be deleted")
      .validate();
    savingsAccounts.checkNotPresent("Livret");

    budgetView.transfer.checkSeriesNotPresent("Series 1 for Livret", "Series 2 for Livret");
    budgetView.transfer.checkSeriesPresent("Series 3 for Codevi");

    transactions.initContent()
      .add("01/10/2008", TransactionType.VIREMENT, "Salaire/oct", "", 1000.00, "Salaire")
      .check();

    savingsAccounts.edit("Codevi").openDelete()
      .checkMessageContains("All the series associated to this account will be deleted")
      .validate();
    savingsAccounts.checkNotPresent("Codevi");

    operations.checkDataIsOk();
  }
}
