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

    timeline.selectAll();
    views.selectData();
    transactions
      .initContent()
      .add("01/10/2008", TransactionType.PLANNED, "Planned: Travaux", "", -400.00, "Travaux", MasterCategory.HOUSE)
      .add("01/10/2008", TransactionType.PLANNED, "Planned: Epargne", "", 200.00, "Epargne", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: Epargne", "", 200.00, "Epargne", MasterCategory.SAVINGS)
      .add("01/08/2008", TransactionType.VIREMENT, "Epargne", "", 200.00, "Epargne", MasterCategory.SAVINGS)
      .check();

    timeline.selectMonth("2008/10");
    views.selectBudget();
    budgetView.savings.checkTotalAmounts(0, 0);
    views.selectHome();
    savingsAccounts.checkPosition("Epargne LCL", 1000);
    monthSummary.checkSavingsBalance(-200);
    monthSummary.checkSavingsIn("Epargne LCL", 0, 200);
    monthSummary.checkSavingsOut("Epargne LCL", 0, 400);
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
    budgetView.savings.checkTotalAmounts(0, 0);
    views.selectHome();
    timeline.selectMonth("2008/10");
    monthSummary.checkSavingsIn("Epargne", 0, 300);
    timeline.selectMonth("2008/06");
    monthSummary.checkSavingsIn("Epargne", 300, 300);

    views.selectBudget();
    budgetView.savings.editSeries("CAF")
      .selectMonth(200806)
      .setAmount(0)
      .validate();
    budgetView.savings.checkSeries("CAF", 0, 0);
    views.selectHome();
    monthSummary.checkSavingsIn("Epargne", 0, 0);


    // back to normal to see if dateChooser is hidden
    views.selectBudget();
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

  public void testSimpleCase() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111", 1000., "2008/08/10")  //compte d'épargne
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Prelevement")
      .addTransaction("2008/07/05", 12.00, "McDo")
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
    categorization.showSelectedMonthsOnly();

    timeline.selectMonth("2008/08");
    categorization.setSavings("Prelevement", "CA");
    categorization.setSavings("Virement", "CA");

    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Prelevement", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("05/07/2008", TransactionType.VIREMENT, "McDo", "", 12.00)
      .check();
    views.selectHome();
    timeline.selectMonth("2008/10");
    savingsAccounts.checkPosition("Account n. 111", 1200);
    mainAccounts.checkEstimatedPosition(-200);
  }

  public void testImportedSavingAccountWithMainAccount() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111", 1000., "2008/08/10")  //compte d'épargne
      .addTransaction("2008/08/10", 100.00, "Virement")
      .addTransaction("2008/07/10", -200.00, "Prelevement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Prelevement")
      .addTransaction("2008/07/10", 200.00, "Virement")
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
    budgetView.savings.createSeries()
      .setName("Project")
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Account n. 111")
      .setToAccount("Main account")
      .setOnceAYear()
      .toggleMonth(7)
      .validate();
    views.selectCategorization();
    categorization.showSelectedMonthsOnly();

    timeline.selectMonth("2008/07");
    categorization.setSavings("Prelevement", "Project");
    categorization.setSavings("Virement", "Project");
    timeline.selectMonth("2008/08");
    categorization.setSavings("Prelevement", "CA");
    categorization.setSavings("Virement", "CA");
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Prelevement", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "CA", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.VIREMENT, "Virement", "", 200.00, "Project", MasterCategory.SAVINGS)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "Prelevement", "", -200.00, "Project", MasterCategory.SAVINGS)
      .check();
    timeline.selectMonth("2008/10");
    views.selectHome();
    savingsAccounts.checkPosition("Account n. 111", 1200);
    mainAccounts.checkEstimatedPosition(-200);

    monthSummary.checkSavingsIn(0, 100);
    monthSummary.checkSavingsOut(0, 0);

    timeline.selectMonth("2008/08");
    views.selectHome();
    monthSummary.checkSavingsIn(100, 0);

    views.selectBudget();
    budgetView.savings.checkSeries("Main accounts.CA", -100, 0);
    budgetView.savings.checkSeries("Account n. 111.CA", 100, 0);
    budgetView.savings.checkTotalAmounts(100, 0);

    budgetView.savings.editSeries("Main accounts.CA").switchToManual()
      .selectAllMonths()
      .setAmount(200)
      .validate();
    budgetView.savings.editSeries("Account n. 111.CA").checkInManual()
      .selectAllMonths()
      .checkAmount("200.00")
      .checkTable(new Object[][]{
        {"2008", "October", "0.00", "200.00"},
        {"2008", "September", "0.00", "200.00"},
        {"2008", "August", "100.00", "200.00"},
        {"2008", "July", "0.00", "200.00"},
      }
      )
      .validate();

    views.selectHome();
    monthSummary.checkSavingsIn(100, 200);

    views.selectBudget();
    budgetView.savings.checkTotalAmounts(100, 200);

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
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", -50.00, "CA", MasterCategory.SAVINGS)
      .add("01/10/2008", TransactionType.PLANNED, "Planned: CA", "", 50.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", 50.00, "CA", MasterCategory.SAVINGS)
      .add("01/09/2008", TransactionType.PLANNED, "Planned: CA", "", -50.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Virement", "", -100.00, "CA", MasterCategory.SAVINGS)
      .add("10/08/2008", TransactionType.VIREMENT, "Virement", "", 100.00, "CA", MasterCategory.SAVINGS)
      .check();
    views.selectHome();
    timeline.selectMonth("2008/10");
    savingsAccounts.checkPosition("Account n. 111", 1100);
    mainAccounts.checkEstimatedPosition(-100);

    views.selectBudget();
    
    timeline.selectMonth("2008/08");
    budgetView.savings.editSeries("Account n. 111.CA")
     .selectMonth(200808)
      .setAmount(0).validate();

    budgetView.savings.checkTotalAmounts(100, 0);
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
    monthSummary.checkSavingsIn("Epargne", 300, 300);
    monthSummary.checkSavingsIn("Account n. 111222", 100, 100);

    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1300);
    savingsAccounts.checkPosition("Account n. 111222", 3100);
    savingsAccounts.checkEstimatedPosition(4400, "30/09/2008");
    monthSummary.checkSavingsIn("Epargne", 0, 300);
    monthSummary.checkSavingsIn("Account n. 111222", 0, 100);

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.savings.checkTotalGauge(100, 100);
    timeline.selectMonth("2008/08");
    budgetView.savings.checkTotalGauge(100, 100);
    timeline.selectMonth("2008/09");
    budgetView.savings.checkTotalGauge(0, 100);

    timeline.selectMonth("2008/06");
    budgetView.savings.checkSeries("Main Accounts.Placement", -100, -100);
    budgetView.savings.checkSeries("Account n. 111222", 100, 100);

    timeline.selectMonth("2008/09");
    budgetView.savings.checkSeries("Main Accounts.Placement", 0, -100);
    budgetView.savings.checkSeries("Account n. 111222", 0, 100);

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

  public void testSavingsGauge() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(Bank.GENERIC_BANK_ID, 111, "111", 1000., "2008/08/10")  //compte d'épargne
      .addTransaction("2008/08/12", -100.00, "P3 CE")
      .addTransaction("2008/08/11", -100.00, "P2 CE")
      .addTransaction("2008/08/10", -100.00, "P1 CE")
      .addTransaction("2008/08/12", 20.00, "V3 CE")
      .addTransaction("2008/08/11", 50.00, "V2 CE")
      .addTransaction("2008/08/10", 50.00, "V1 CE")
      .addTransaction("2008/07/10", 100.00, "Virement CE")
      .addTransaction("2008/07/10", -200.00, "Prelevement CE")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/12", 100.00, "V3 CC")
      .addTransaction("2008/08/11", 100.00, "V2 CC")
      .addTransaction("2008/08/10", 100.00, "V1 CC")
      .addTransaction("2008/08/12", -20.00, "P3 CC")
      .addTransaction("2008/08/11", -50.00, "P2 CC")
      .addTransaction("2008/08/10", -50.00, "P1 CC")
      .addTransaction("2008/07/10", -100.00, "Prelevement CC")
      .addTransaction("2008/07/10", 200.00, "Virement CC")
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
    budgetView.savings.createSeries()
      .setName("Project")
      .setCategory(MasterCategory.SAVINGS)
      .setFromAccount("Account n. 111")
      .setToAccount("Main account")
      .setCustom()
      .setStartDate(200807)
      .setEndDate(200808)
      .validate();
    views.selectCategorization();
    categorization.showSelectedMonthsOnly();

    timeline.selectMonth("2008/07");
    categorization.setSavings("Prelevement CE", "Project");
    categorization.setSavings("Virement CE", "CA");
    categorization.setSavings("Virement CC", "Project");
    categorization.setSavings("Prelevement CC", "CA");

    timeline.selectMonth("2008/08");
    categorization.setSavings("P1 CC", "CA");
    categorization.setSavings("P2 CC", "CA");
    categorization.setSavings("P3 CC", "CA");
    categorization.setSavings("P1 CE", "Project");
    categorization.setSavings("P2 CE", "Project");
    categorization.setSavings("P3 CE", "Project");
    categorization.setSavings("V1 CC", "Project");
    categorization.setSavings("V2 CC", "Project");
    categorization.setSavings("V3 CC", "Project");
    categorization.setSavings("V1 CE", "CA");
    categorization.setSavings("V2 CE", "CA");
    categorization.setSavings("V3 CE", "CA");

    views.selectBudget();
    budgetView.savings.checkSeries("Main Accounts.CA", -120, -100);
  }

  public void testChangeAccountDirectionDoNotChangeBudgetSign() throws Exception {
    savingsAccounts.createSavingsAccount("Epargne", 1000);

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Test")
      .setFromAccount("Main accounts")
      .setToAccount("External")
      .setCategory(MasterCategory.SAVINGS)
      .switchToManual()
      .selectAllMonths()
      .setAmount("300")
      .checkTable(new Object[][]{
        {"2008", "August", "", "300.00"}
      })
      .setFromAccount("External")
      .setToAccount("Main accounts")
      .checkTable(new Object[][]{
        {"2008", "August", "", "300.00"}
      })
      .validate();
  }

  public void testBothNotImportedAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    views.selectHome();
    savingsAccounts.createSavingsAccount("Savings 1", 1000);
    savingsAccounts.createSavingsAccount("Savings 2", 1000);
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Test")
      .setFromAccount("Savings 1")
      .setToAccount("Savings 2")
      .setCategory(MasterCategory.SAVINGS)
      .selectAllMonths()
      .setAmount("300")
      .validate();
    timeline.selectMonth("2008/06");

    budgetView.savings.checkSeries("Savings 1.Test", -300, -300);
    budgetView.savings.checkSeriesGaugeRemaining("Savings 1.Test", 0);
    budgetView.savings.checkSeries("Savings 2.Test", 300, 300);

    views.selectHome();
    monthSummary.checkSavingsOut("Savings 1", 300, 300);
    monthSummary.checkSavingsIn("Savings 2", 300, 300);
  }

//  public void testSavingWithNoTransactionShouldNotBeIgnored() throws Exception {
//    fail("Comme il n'y a pas de transaction sur ce compte il n'est pas vu pour le calcul du solde total");
//  }
//
}
