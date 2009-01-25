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
      .checkSeries("Main accounts.Epargne", -300, -500);
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
      .setFromAccount("External Account")
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
    // force creation of month in the past
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "FNAC")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne", 1000);
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CAF")
      .setCategory(MasterCategory.INCOME)
      .setFromAccount("External account")
      .setToAccount("Epargne")
      .selectAllMonths()
      .setAmount("300")
      .setDay("5")
      .validate();
    views.selectHome();

    timeline.selectMonth("2008/06");
    savingsAccounts.checkPosition("Epargne", 400);
    savingsAccounts.checkEstimatedPosition(400, "30/06/2008");
    savingsAccounts.checkSummary(1000, "05/08/2008");

    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Epargne", 1000);
    savingsAccounts.checkEstimatedPosition(1000, "31/08/2008");
    savingsAccounts.checkSummary(1000, "05/08/2008");

    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1300);
    savingsAccounts.checkEstimatedPosition(1300, "30/09/2008");

    views.selectData();
    timeline.selectAll();
    transactions.initContent()
      .add("05/10/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF", MasterCategory.INCOME)
      .add("05/09/2008", TransactionType.PLANNED, "Planned: CAF", "", 300.00, "CAF", MasterCategory.INCOME)
      .add("05/08/2008", TransactionType.VIREMENT, "CAF", "", 300.00, "CAF", MasterCategory.INCOME)
      .add("05/07/2008", TransactionType.VIREMENT, "CAF", "", 300.00, "CAF", MasterCategory.INCOME)
      .add("10/06/2008", TransactionType.PRELEVEMENT, "FNAC", "", -100.00)
      .add("05/06/2008", TransactionType.VIREMENT, "CAF", "", 300.00, "CAF", MasterCategory.INCOME)
      .check();

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.savings.checkTotalAmounts(300, 300);
    timeline.selectMonth("2008/08");
    budgetView.savings.checkTotalAmounts(300, 300);

    // back to normal to see if dateChooser is hidden

    budgetView.savings.editSeries("CAF")
      .setFromAccount("Main account")
      .checkDateChooserIsHidden()
      .validate();

    budgetView.savings.editSeries("Epargne.CAF")
      .checkDateChooserIsHidden()
      .cancel();
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
      .setDay("5")
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

  public void testMixeTypeOfSavingsSeriesShouldUpdateCorrectlyTheBudgetView() throws Exception {
    // Un compte courant, un compte d'épargne importé, un compte d'épargne non importé

    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111222", 3000., "2008/08/10")
      .addTransaction("2008/06/06", 100.00, "Virement Epargne")
      .addTransaction("2008/07/06", 100.00, "Virement Epargne")
      .addTransaction("2008/08/06", 100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .addTransaction("2008/07/06", -100.00, "Virement vers Epargne")
      .addTransaction("2008/08/06", -100.00, "Virement vers Epargne")
      .load();

    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    savingsAccounts.createSavingsAccount("Epargne", 1000);
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Virement CAF")
      .setCategory(MasterCategory.SAVINGS)
      .setToAccount("Epargne")
      .setFromAccount("External account")
      .selectAllMonths()
      .setAmount("300")
      .setDay("5")
      .validate();

    budgetView.savings.createSeries()
      .setName("Placement")
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Main Account")
      .setToAccount("Account n. 111222")
      .validate();

    views.selectCategorization();
    categorization
      .selectTableRows("Virement Epargne")
      .selectSavings()
      .selectSavingsSeries("Placement");

    categorization.selectSavings()
      .selectTableRows("Virement vers Epargne")
      .selectSavings()
      .selectSavingsSeries("Placement");

    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("05/10/2008", TransactionType.PLANNED, "Planned: Virement CAF", "", 300.00, "Virement CAF", MasterCategory.SAVINGS)
      .add("01/10/2008", TransactionType.PLANNED, "Planned: Placement", "", -100.00, "Placement", MasterCategory.SAVINGS)
      .add("01/10/2008", TransactionType.PLANNED, "Planned: Placement", "", 100.00, "Placement", MasterCategory.SAVINGS)
      .add("05/09/2008", TransactionType.PLANNED, "Planned: Virement CAF", "", 300.00, "Virement CAF", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: Placement", "", -100.00, "Placement", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: Placement", "", 100.00, "Placement", MasterCategory.SAVINGS)
      .add("06/08/2008", TransactionType.PRELEVEMENT, "Virement vers Epargne", "", -100.00, "Placement", MasterCategory.SAVINGS)
      .add("06/08/2008", TransactionType.VIREMENT, "Virement Epargne", "", 100.00, "Placement", MasterCategory.SAVINGS)
      .add("05/08/2008", TransactionType.VIREMENT, "Virement CAF", "", 300.00, "Virement CAF", MasterCategory.SAVINGS)
      .add("06/07/2008", TransactionType.PRELEVEMENT, "Virement vers Epargne", "", -100.00, "Placement", MasterCategory.SAVINGS)
      .add("06/07/2008", TransactionType.VIREMENT, "Virement Epargne", "", 100.00, "Placement", MasterCategory.SAVINGS)
      .add("05/07/2008", TransactionType.VIREMENT, "Virement CAF", "", 300.00, "Virement CAF", MasterCategory.SAVINGS)
      .add("06/06/2008", TransactionType.PRELEVEMENT, "Virement vers Epargne", "", -100.00, "Placement", MasterCategory.SAVINGS)
      .add("06/06/2008", TransactionType.VIREMENT, "Virement Epargne", "", 100.00, "Placement", MasterCategory.SAVINGS)
      .add("05/06/2008", TransactionType.VIREMENT, "Virement CAF", "", 300.00, "Virement CAF", MasterCategory.SAVINGS)
      .check();

    views.selectHome();
    timeline.selectMonth("2008/06");
    savingsAccounts.checkPosition("Epargne", 400);
    savingsAccounts.checkPosition("Account n. 111222", 2800);
    savingsAccounts.checkEstimatedPosition(3200, "30/06/2008");
    savingsAccounts.checkSummary(4000, "06/08/2008");

    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Epargne", 1000);
    savingsAccounts.checkPosition("Account n. 111222", 3000);
    savingsAccounts.checkEstimatedPosition(4000, "31/08/2008");
    savingsAccounts.checkSummary(4000, "06/08/2008");

    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1300);
    savingsAccounts.checkPosition("Account n. 111222", 3100);
    savingsAccounts.checkEstimatedPosition(4400, "30/09/2008");
  }

  public void testHideAccountIfNoSeries() throws Exception {
    savingsAccounts.createSavingsAccount("Epargne", 1000);

    views.selectBudget();
    budgetView.savings.checkNoAccountsDisplayed();
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Virement CAF")
      .setCategory(MasterCategory.SAVINGS)
      .setToAccount("Epargne")
      .setFromAccount("External account")
      .selectAllMonths()
      .setAmount("300")
      .setDay("5")
      .validate();
    budgetView.savings.checkSeriesPresent("Virement CAF");
  }

//  public void testSavingWithNoTransactionShouldNotBeIgnored() throws Exception {
//    fail("Comme il n'y a pas de transaction sur ce compte il n'est pas vu pour le calcul du solde total");
//  }
//
}
