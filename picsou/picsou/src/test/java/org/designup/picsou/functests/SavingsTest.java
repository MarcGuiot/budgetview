package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.Bank;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;

public class SavingsTest extends LoggedInFunctionalTestCase {

  public void testCreateSavingsInMainAccount() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();
    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne LCL", 1000);
    views.selectCategorization();
    categorization
      .selectTableRows("Virement")
      .selectSavings()
      .createSavingsSeries()
      .setName("Epargne")
      .setCategories(MasterCategory.SAVINGS)
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .validate();
    views.selectBudget();
    budgetView.savings
      .checkSeries("Epargne", -300, -500);
    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .check();
    views.selectHome();
    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Epargne", 1000);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1100);
    timeline.selectMonth("2008/10");
    savingsAccounts.checkPosition("Epargne", 1200);
  }

  public void testCreateSavingsSeriesAndPayFromSavings() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();
    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne LCL", 1000);
    views.selectCategorization();
    categorization
      .selectTableRows("Virement")
      .selectSavings()
      .createSavingsSeries()
      .setName("Epargne")
      .setCategories(MasterCategory.SAVINGS)
      .setToAccount("Epargne LCL")
      .setFromAccount("Main accounts")
      .switchToAutomatic()
      .validate();
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Achat Tele")
      .setCategories(MasterCategory.EQUIPMENT)
      .setFromAccount("Epargne LCL")
      .setToAccount("Main accounts")
      .selectMonth(200810)
      .setAmount(300)
      .validate();
    views.selectData();

    transactions.initContent()
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("01/10/2008", TransactionType.PLANNED, "Planned: Achat Tele", "", -300.00, "Achat Tele", MasterCategory.EQUIPMENT)
      .add("01/10/2008", TransactionType.PLANNED, "Planned: Achat Tele", "", 300.00, "Achat Tele", MasterCategory.EQUIPMENT)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .check();

    timeline.selectMonth("2008/10");
    views.selectHome();
    savingsAccounts.checkPosition("Epargne", 900);
  }

  public void testSavingsAccountFillFromExternalAccountBalance() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setBalance(1000)
      .validate();

    views.selectBudget();
    budgetView.savings
      .createSeries()
      .setToAccount("Epargne LCL")
      .setName("Epargne")
      .setCategory(MasterCategory.SAVINGS)
      .selectAllMonths()
      .setAmount("200")
      .validate();

    budgetView.savings
      .createSeries()
      .setCategories(MasterCategory.HOUSE)
      .setName("Travaux")
      .setFromAccount("Epargne LCL")
      .selectMonth(200810)
      .setAmount("400")
      .validate();

    views.selectHome();
    timeline.selectMonth("2008/10");
    savingsAccounts.checkPosition("Epargne LCL", 1000);
  }

  public void testCreateSavingsSeriesAndAssociateLaterToAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    timeline.selectAll();
    views.selectHome();

    views.selectCategorization();
    categorization
      .selectTableRows("Virement")
      .selectSavings()
      .createSavingsSeries()
      .setName("Epargne")
      .setCategories(MasterCategory.SAVINGS)
      .validate();
    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .check();

    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setBalance(1000)
      .validate();

    views.selectBudget();
    budgetView.savings.editSeries("Epargne")
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .validate();

    views.selectCategorization();
    categorization
      .selectSavings()
      .selectSavingsSeries("Epargne");

    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .check();
  }

  public void testCreateSavingsSeriesAndAssociateLaterToAccountFromExternal() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    timeline.selectAll();
    views.selectHome();

    views.selectCategorization();
    categorization
      .selectTableRows("Virement")
      .selectSavings()
      .createSavingsSeries()
      .setName("Epargne")
      .setCategories(MasterCategory.SAVINGS)
      .validate();

    views.selectData();
    transactions.initContent()
      .add("10/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .check();

    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setBalance(1000)
      .validate();

    views.selectBudget();
    budgetView.savings.editSeries("Epargne")
      .setToAccount("Epargne LCL")
      .validate();
    views.selectData();
    transactions.initContent()
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00)
      .check();
  }

  public void testSplitPreviouslyCategorizedInSavings() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();
    views.selectHome();

    savingsAccounts.createSavingsAccount("Epargne", 1000);
    views.selectCategorization();
    categorization
      .selectTableRows("Virement")
      .selectSavings()
      .createSavingsSeries()
      .setName("Epargne")
      .setFromAccount("Main account")
      .setToAccount("Epargne")
      .setCategories(MasterCategory.SAVINGS)
      .validate();
    views.selectData();
    transactions.initContent()
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "Epargne", MasterCategory.SAVINGS)
      .check();

    views.selectCategorization();
    categorization.selectTableRows("Virement");
    transactionDetails.split("50", "Comportement impossible?");
    categorization.selectOccasionalSeries(MasterCategory.LEISURES);
    views.selectData();
    transactions.initContent()
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 50.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -50.00, "Epargne", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "Comportement impossible?", -50.00, "Occasional", MasterCategory.LEISURES)
      .check();
  }


  public void testExternalToNotImportedSavingsWithDate() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne", 1000);
    views.selectBudget();
    budgetView.income.createSeries()
      .setName("CAF")
      .setFromAccount("External account")
      .setToAccount("Epargne")
      .selectAllMonths()
      .setAmount("100")
      .setDate("5")
      .validate();
    views.selectHome();
    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Epargne", 1000);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1100);
  }

  // ==> test de l'effet de suppression de transaction référencé dans account
  public void testUpdateAccountOnSeriesChange() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Caf")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne", 1000);
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CAF")
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("External account")
      .setToAccount("Epargne")
      .selectAllMonths()
      .setAmount("100")
      .setDate("5")
      .validate();
    views.selectHome();
    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Epargne", 1000);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1100);
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("05/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF", MasterCategory.SAVINGS)
      .add("05/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Caf", "", -100.00)
      .add("05/08/2008", TransactionType.VIREMENT, "CAF", "", 100.00, "CAF", MasterCategory.SAVINGS)
      .check();
    views.selectBudget();
    budgetView.savings
      .editSeriesList()
      .selectSeries("CAF")
      .setFromAccount("Main account")
      .switchToAutomatic()
      .validate();
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Caf", "", -100.00)
      .check();
    views.selectCategorization();
    categorization
      .selectTableRows("Caf")
      .selectSavings()
      .selectSavingsSeries("CAF");

    views.selectData();
    transactions
      .initContent()
      .add("05/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF", MasterCategory.SAVINGS)
      .add("05/10/2008", TransactionType.PLANNED, "Planned: CAF", "", -100.00, "CAF", MasterCategory.SAVINGS)
      .add("05/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF", MasterCategory.SAVINGS)
      .add("05/09/2008", TransactionType.PLANNED, "Planned: CAF", "", -100.00, "CAF", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Caf", "", 100.00, "CAF", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Caf", "", -100.00, "CAF", MasterCategory.SAVINGS)
      .check();

    views.selectBudget();
    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Epargne", 1100);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1200);
  }

  public void testAutomaticBudget() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne", 1000);
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CA")
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Main account")
      .setToAccount("Epargne")
      .validate();
    views.selectCategorization();
    categorization.setSavings("Virement", "CA");
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "CA", MasterCategory.SAVINGS)
      .check();
    views.selectBudget();
    budgetView.savings.editSeriesList()
      .selectSeries("CA")
      .switchToManual()
      .switchToAutomatic()
      .validate();
    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "CA", MasterCategory.SAVINGS)
      .check();
  }

  public void testImportedSavingsAccountFromExternal() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111", 1000., "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "CAF")
      .load();
    views.selectHome();
    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CAF")
      .setCategory(MasterCategory.INCOME)
      .setToAccount("Account n. 111")
      .checkDateChooserIsHidden()
      .validate();
    views.selectCategorization();
    categorization.selectTableRows("CAF")
      .selectSavings()
      .selectSavingsSeries("CAF");
    views.selectHome();
    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Account n. 111", 1000);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Account n. 111", 1100);
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF", MasterCategory.INCOME)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 100.00, "CAF", MasterCategory.INCOME)
      .add("10/08/2008", TransactionType.VIREMENT, "CAF", "", 100.00, "CAF", MasterCategory.INCOME)
      .check();
  }

  public void testImportedSavingAccountWithMainAccount() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111", 1000., "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CA")
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Main account")
      .setToAccount("Account n. 111")
      .validate();
    views.selectCategorization();
    categorization.setSavings("Virement", "CA");
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "CA", MasterCategory.SAVINGS)
      .check();
    views.selectHome();
    timeline.selectMonth("2008/10");
    savingsAccounts.checkPosition("Account n. 111", 1200);
    mainAccounts.checkEstimatedPosition(-200);
  }

  public void testImportedSavingAccountWithMainAccountInManual() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111", 1000., "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CA")
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Main account")
      .setToAccount("Account n. 111")
      .switchToManual()
      .selectAllMonths()
      .setAmount(50)
      .validate();
    views.selectCategorization();
    categorization.setSavings("Virement", "CA");
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", 50.00, "CA", MasterCategory.SAVINGS)
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", -50.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", 50.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", -50.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "CA", MasterCategory.SAVINGS)
      .check();
    views.selectHome();
    timeline.selectMonth("2008/10");
    savingsAccounts.checkPosition("Account n. 111", 1100);
    mainAccounts.checkEstimatedPosition(-100);
  }

  public void testMultiple() throws Exception {
  }
}
