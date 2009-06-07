package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.Bank;
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
      .selectTransactions("Virement")
      .selectSavings().createSeries()
      .setName("Epargne")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne LCL")
      .validate();
    views.selectBudget();
    budgetView.savings
      .checkSeries("Epargne", 300, 500);
    views.selectData();
    transactions.initAmountContent()
      .add("10/10/2008", "Planned: Epargne", 100.00, "Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("10/10/2008", "Planned: Epargne", -100.00, "Epargne", -200.00, -200.00, "Account n. 00001123")
      .add("10/09/2008", "Planned: Epargne", 100.00, "Epargne", 1100.00, 1100.00, "Epargne LCL")
      .add("10/09/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "Epargne", 1000.00, 1000.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VIREMENT", 100.00, "Epargne", 900.00, 900.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VIREMENT", 100.00, "Epargne", 800.00, 800.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "Epargne", 200.00, 200.00, "Account n. 00001123")
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
      .selectTransactions("Virement")
      .selectSavings().createSeries()
      .setName("Epargne")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne LCL")
      .validate();
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Achat Tele")
      .setFromAccount("Epargne LCL")
      .setToAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .selectMonth(200810)
      .setAmount(300)
      .validate();
    views.selectData();

    transactions.initAmountContent()
      .add("10/10/2008", "Planned: Epargne", 100.00, "Epargne", 900.00, 900.00, "Epargne LCL")
      .add("10/10/2008", "Planned: Epargne", -100.00, "Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("01/10/2008", "Planned: Achat Tele", -300.00, "Achat Tele", 800.00, 800.00, "Epargne LCL")
      .add("01/10/2008", "Planned: Achat Tele", 300.00, "Achat Tele", 200.00, 200.00, "Account n. 00001123")
      .add("10/09/2008", "Planned: Epargne", 100.00, "Epargne", 1100.00, 1100.00, "Epargne LCL")
      .add("10/09/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "Epargne", 1000.00, 1000.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VIREMENT", 100.00, "Epargne", 900.00, 900.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VIREMENT", 100.00, "Epargne", 800.00, 800.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "Epargne", 200.00, 200.00, "Account n. 00001123")
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
      .selectAllMonths()
      .setAmount("200")
      .validate();

    budgetView.savings
      .createSeries()
      .setName("Travaux")
      .setFromAccount("Epargne LCL")
      .selectMonth(200810)
      .setAmount("400")
      .validate();

    timeline.selectAll();
    views.selectData();
    transactions
      .initAmountContent()
      .add("01/10/2008", "Planned: Travaux", -400.00, "Travaux", 1000.00, 1000.00, "Epargne LCL")
      .add("01/10/2008", "Planned: Epargne", 200.00, "Epargne", 1400.00, 1400.00, "Epargne LCL")
      .add("01/09/2008", "Planned: Epargne", 200.00, "Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("01/08/2008", "EPARGNE", 200.00, "Epargne", 1000.00, 1000.00, "Epargne LCL")
      .check();

    timeline.selectMonth("2008/10");
    views.selectBudget();
    budgetView.savings.checkTotalAmounts(0, 0);
    views.selectHome();
    savingsAccounts.checkPosition("Epargne LCL", 1000);

    views.selectSavings();
    savingsView.checkAmount("Epargne LCL", "Epargne", 0, 200);
    savingsView.checkAmount("Epargne LCL", "Travaux", 0, -400);
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
      .selectTransactions("Virement")
      .selectSavings().createSeries()
      .setName("Epargne")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .validate();
    views.selectData();
    transactions.initAmountContent()
      .add("10/10/2008", "Planned: Epargne", -100.00, "Epargne", -200.00, -200.00, "Account n. 00001123")
      .add("10/09/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VIREMENT", -100.00, "Epargne", 200.00, 200.00, "Account n. 00001123")
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
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne LCL")
      .validate();

    views.selectCategorization();
    categorization.selectSavings().selectSeries("Epargne");

    views.selectData();
    transactions.initAmountContent()
      .add("10/10/2008", "Planned: Epargne", 100.00, "Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("10/10/2008", "Planned: Epargne", -100.00, "Epargne", -200.00, -200.00, "Account n. 00001123")
      .add("10/09/2008", "Planned: Epargne", 100.00, "Epargne", 1100.00, 1100.00, "Epargne LCL")
      .add("10/09/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "Epargne", 1000.00, 1000.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VIREMENT", 100.00, "Epargne", 900.00, 900.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VIREMENT", 100.00, "Epargne", 800.00, 800.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "Epargne", 200.00, 200.00, "Account n. 00001123")
      .check();
  }

  public void testCreateSavingsSeriesAndAssociateLaterToAnotherAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    timeline.selectAll();
    views.selectHome();

    savingsAccounts.createNewAccount()
      .setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setBalance(1000)
      .validate();

    views.selectCategorization();
    categorization
      .selectTransactions("Virement")
      .selectSavings().createSeries()
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne LCL")
      .setName("Epargne")
      .validate();

    views.selectData();
    transactions.initAmountContent()
      .add("10/10/2008", "Planned: Epargne", 100.00, "Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("10/10/2008", "Planned: Epargne", -100.00, "Epargne", -200.00, -200.00, "Account n. 00001123")
      .add("10/09/2008", "Planned: Epargne", 100.00, "Epargne", 1100.00, 1100.00, "Epargne LCL")
      .add("10/09/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "Epargne", 1000.00, 1000.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VIREMENT", 100.00, "Epargne", 900.00, 900.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VIREMENT", 100.00, "Epargne", 800.00, 800.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "Epargne", 200.00, 200.00, "Account n. 00001123")
      .check();
    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAsSavings()
      .setAccountName("Epargne CIC")
      .selectBank("CIC")
      .setBalance(100)
      .validate();

    views.selectBudget();
    budgetView.savings.editSeries("Epargne")
      .setToAccount("Epargne CIC")
      .validate();
    views.selectData();
    transactions.initAmountContent()
      .add("10/08/2008", "VIREMENT", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VIREMENT", -100.00, "To categorize", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VIREMENT", -100.00, "To categorize", 200.00, 200.00, "Account n. 00001123")
      .check();
    views.selectCategorization();
    categorization
      .selectTransactions("Virement")
      .selectSavings()
      .selectSeries("Epargne");
    views.selectData();
    transactions.initAmountContent()
      .add("10/10/2008", "Planned: Epargne", 100.00, "Epargne", 300.00, 1300.00, "Epargne CIC")
      .add("10/10/2008", "Planned: Epargne", -100.00, "Epargne", -200.00, -200.00, "Account n. 00001123")
      .add("10/09/2008", "Planned: Epargne", 100.00, "Epargne", 200.00, 1200.00, "Epargne CIC")
      .add("10/09/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "Epargne", 100.00, 1100.00, "Epargne CIC")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/07/2008", "VIREMENT", 100.00, "Epargne", 0.00, 1000.00, "Epargne CIC")
      .add("10/07/2008", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, "Account n. 00001123")
      .add("10/06/2008", "VIREMENT", 100.00, "Epargne", -100.00, 900.00, "Epargne CIC")
      .add("10/06/2008", "VIREMENT", -100.00, "Epargne", 200.00, 200.00, "Account n. 00001123")
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
      .selectTransactions("Virement")
      .selectSavings().createSeries()
      .setName("Epargne")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne")
      .validate();
    views.selectData();
    transactions.initAmountContent()
      .add("10/08/2008", "VIREMENT", 100.00, "Epargne", 1000.00, 1000.00, "Epargne")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, "Account n. 00001123")
      .check();

    views.selectCategorization();
    categorization.selectTransactions("Virement");
    transactionDetails.split("50", "Comportement impossible?");
    categorization.selectEnvelopes().selectNewSeries("Occasional");
    views.selectData();
    transactions.initAmountContent()
      .add("10/08/2008", "VIREMENT", 50.00, "Epargne", 1000.00, 1000.00, "Epargne")
      .add("10/08/2008", "VIREMENT", -50.00, "Epargne", 0.00, 0.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", -50.00, "Occasional", 50.00, 50.00, "Account n. 00001123")
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
    savingsAccounts.checkSummary(1000, "2008/08/05");

    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Epargne", 1000);
    savingsAccounts.checkEstimatedPosition(1000, "31/08/2008");
    savingsAccounts.checkSummary(1000, "2008/08/05");

    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1300);
    savingsAccounts.checkEstimatedPosition(1300, "30/09/2008");

    views.selectData();
    timeline.selectAll();
    transactions.initAmountContent()
      .add("05/10/2008", "Planned: CAF", 300.00, "CAF", 1600.00, 1600.00, "Epargne")
      .add("05/09/2008", "Planned: CAF", 300.00, "CAF", 1300.00, 1300.00, "Epargne")
      .add("05/08/2008", "CAF", 300.00, "CAF", 1000.00, 1000.00, "Epargne")
      .add("05/07/2008", "CAF", 300.00, "CAF", 700.00, 700.00, "Epargne")
      .add("10/06/2008", "FNAC", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("05/06/2008", "CAF", 300.00, "CAF", 400.00, 400.00, "Epargne")
      .check();

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.savings.checkTotalAmounts(0, 0);
    views.selectHome();
    timeline.selectMonth("2008/10");
    views.selectSavings();
    savingsView.checkAmount("Epargne", "CAF", 0, 300);
    timeline.selectMonth("2008/06");
    savingsView.checkAmount("Epargne", "CAF", 300, 300);

    savingsView.editSavingsSeries("Epargne", "CAF")
      .selectMonth(200806)
      .setAmount(0)
      .validate();
    views.selectSavings();

    // back to normal to see if dateChooser is hidden

    savingsView.editSavingsSeries("Epargne", "CAF")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .checkDateChooserIsHidden()
      .validate();

    views.selectData();
    timeline.selectAll();
    transactions.initAmountContent()
      .add("05/10/2008", "Planned: CAF", 300.00, "CAF", 2200.00, 2200.00, "Epargne")
      .add("05/10/2008", "Planned: CAF", -300.00, "CAF", -1200.00, -1200.00, "Account n. 00001123")
      .add("05/09/2008", "Planned: CAF", 300.00, "CAF", 1900.00, 1900.00, "Epargne")
      .add("05/09/2008", "Planned: CAF", -300.00, "CAF", -900.00, -900.00, "Account n. 00001123")
      .add("05/08/2008", "Planned: CAF", 300.00, "CAF", 1600.00, 1600.00, "Epargne")
      .add("05/08/2008", "Planned: CAF", -300.00, "CAF", -600.00, -600.00, "Account n. 00001123")
      .add("05/07/2008", "Planned: CAF", 300.00, "CAF", 1300.00, 1300.00, "Epargne")
      .add("05/07/2008", "Planned: CAF", -300.00, "CAF", -300.00, -300.00, "Account n. 00001123")
      .add("10/06/2008", "FNAC", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

    views.selectBudget();
    budgetView.savings.editSeries("CAF")
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
    transactions.initAmountContent()
      .add("05/10/2008", "Planned: CAF", 100.00, "CAF", 1200.00, 1200.00, "Epargne")
      .add("05/09/2008", "Planned: CAF", 100.00, "CAF", 1100.00, 1100.00, "Epargne")
      .add("10/08/2008", "CAF", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("05/08/2008", "CAF", 100.00, "CAF", 1000.00, 1000.00, "Epargne")
      .check();

    views.selectBudget();
    budgetView.savings
      .editSeriesList()
      .selectSeries("CAF")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .switchToAutomatic()
      .validate();
    timeline.selectAll();
    views.selectData();
    transactions.initContent()
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Caf", "", -100.00)
      .check();
    views.selectCategorization();
    categorization.setSavings("Caf", "CAF");

    views.selectData();
    transactions
      .initAmountContent()
      .add("05/10/2008", "Planned: CAF", 100.00, "CAF", 1300.00, 1300.00, "Epargne")
      .add("05/10/2008", "Planned: CAF", -100.00, "CAF", -200.00, -200.00, "Account n. 00001123")
      .add("05/09/2008", "Planned: CAF", 100.00, "CAF", 1200.00, 1200.00, "Epargne")
      .add("05/09/2008", "Planned: CAF", -100.00, "CAF", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "CAF", 100.00, "CAF", 1100.00, 1100.00, "Epargne")
      .add("10/08/2008", "CAF", -100.00, "CAF", 0.00, 0.00, "Account n. 00001123")
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
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Epargne")
      .validate();
    views.selectCategorization();
    categorization.setSavings("Virement", "CA");
    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("01/10/2008", "Planned: CA", 100.00, "CA", 1200.00, 1200.00, "Epargne")
      .add("01/10/2008", "Planned: CA", -100.00, "CA", -200.00, -200.00, "Account n. 00001123")
      .add("01/09/2008", "Planned: CA", 100.00, "CA", 1100.00, 1100.00, "Epargne")
      .add("01/09/2008", "Planned: CA", -100.00, "CA", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Epargne")
      .add("10/08/2008", "VIREMENT", -100.00, "CA", 0.00, 0.00, "Account n. 00001123")
      .check();

    views.selectBudget();
    budgetView.savings.editSeriesList()
      .selectSeries("CA")
      .switchToManual()
      .switchToAutomatic()
      .validate();
    views.selectData();
    timeline.selectAll();
    transactions.initAmountContent()
      .add("01/10/2008", "Planned: CA", 100.00, "CA", 1200.00, 1200.00, "Epargne")
      .add("01/10/2008", "Planned: CA", -100.00, "CA", -200.00, -200.00, "Account n. 00001123")
      .add("01/09/2008", "Planned: CA", 100.00, "CA", 1100.00, 1100.00, "Epargne")
      .add("01/09/2008", "Planned: CA", -100.00, "CA", -100.00, -100.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Epargne")
      .add("10/08/2008", "VIREMENT", -100.00, "CA", 0.00, 0.00, "Account n. 00001123")
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
      .setToAccount("Account n. 111")
      .checkDateChooserIsHidden()
      .validate();
    views.selectCategorization();
    categorization.setSavings("CAF", "CAF");
    views.selectHome();
    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Account n. 111", 1000);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Account n. 111", 1100);
    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("01/10/2008", "Planned: CAF", 100.00, "CAF", 1200.00, 1200.00, "Account n. 111")
      .add("01/09/2008", "Planned: CAF", 100.00, "CAF", 1100.00, 1100.00, "Account n. 111")
      .add("10/08/2008", "CAF", 100.00, "CAF", 1000.00, 1000.00, "Account n. 111")
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
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Account n. 111")
      .validate();
    views.selectCategorization();
    categorization.showSelectedMonthsOnly();

    timeline.selectMonth("2008/08");
    categorization.setSavings("Prelevement", "CA");
    categorization.setSavings("Virement", "CA");

    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("01/10/2008", "Planned: CA", -100.00, "CA", -200.00, -200.00, "Account n. 00001123")
      .add("01/10/2008", "Planned: CA", 100.00, "CA", 1200.00, 1200.00, "Account n. 111")
      .add("01/09/2008", "Planned: CA", -100.00, "CA", -100.00, -100.00, "Account n. 00001123")
      .add("01/09/2008", "Planned: CA", 100.00, "CA", 1100.00, 1100.00, "Account n. 111")
      .add("10/08/2008", "PRELEVEMENT", -100.00, "CA", 0.00, 0.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Account n. 111")
      .add("05/07/2008", "MCDO", 12.00, "To categorize", 100.00, 100.00, "Account n. 00001123")
      .check();
    views.selectHome();
    timeline.selectMonth("2008/10");
    savingsAccounts.checkPosition("Account n. 111", 1200);
    mainAccounts.checkEstimatedPosition(-200);

    views.selectSavings();
//    savingsView.gotoTransactions("Account n. 111");
//    views.checkDataSelected();

  }

  public void testImportedSavingsAccountWithMainAccount() throws Exception {
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
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Account n. 111")
      .validate();
    budgetView.savings.createSeries()
      .setName("Project")
      .setFromAccount("Account n. 111")
      .setToAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
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
    transactions.initAmountContent()
      .add("01/10/2008", "Planned: CA", -100.00, "CA", -200.00, -200.00, "Account n. 00001123")
      .add("01/10/2008", "Planned: CA", 100.00, "CA", 1200.00, 1200.00, "Account n. 111")
      .add("01/09/2008", "Planned: CA", -100.00, "CA", -100.00, -100.00, "Account n. 00001123")
      .add("01/09/2008", "Planned: CA", 100.00, "CA", 1100.00, 1100.00, "Account n. 111")
      .add("10/08/2008", "PRELEVEMENT", -100.00, "CA", 0.00, 0.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Account n. 111")
      .add("10/07/2008", "VIREMENT", 200.00, "Project", 100.00, 100.00, "Account n. 00001123")
      .add("10/07/2008", "PRELEVEMENT", -200.00, "Project", 900.00, 900.00, "Account n. 111")
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
    budgetView.savings.checkSeries("CA", 100, 0);
    views.selectSavings();
    savingsView.checkAmount("Account n. 111", "CA", 100, 0);
    views.selectBudget();
    budgetView.savings.checkTotalAmounts(100, 0);

    budgetView.savings.editSeries("CA").switchToManual()
      .selectAllMonths()
      .setAmount(200)
      .validate();
    views.selectSavings();
    savingsView.editSavingsSeries("Account n. 111", "CA").checkManualModeSelected()
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

  public void testImportedSavingsAccountWithMainAccountInManual() throws Exception {
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
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Account n. 111")
      .switchToManual()
      .selectAllMonths()
      .setAmount(50)
      .validate();
    views.selectCategorization();
    categorization.setSavings("Virement", "CA");
    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("01/10/2008", "Planned: CA", -50.00, "CA", -100.00, -100.00, "Account n. 00001123")
      .add("01/10/2008", "Planned: CA", 50.00, "CA", 1100.00, 1100.00, "Account n. 111")
      .add("01/09/2008", "Planned: CA", -50.00, "CA", -50.00, -50.00, "Account n. 00001123")
      .add("01/09/2008", "Planned: CA", 50.00, "CA", 1050.00, 1050.00, "Account n. 111")
      .add("10/08/2008", "VIREMENT", -100.00, "CA", 0.00, 0.00, "Account n. 00001123")
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Account n. 111")
      .check();
    views.selectHome();
    timeline.selectMonth("2008/10");
    savingsAccounts.checkPosition("Account n. 111", 1100);
    mainAccounts.checkEstimatedPosition(-100);

    views.selectBudget();

    timeline.selectMonth("2008/08");
    budgetView.savings.editSeries("CA")
      .selectMonth(200808)
      .setAmount(0).validate();

    budgetView.savings.checkTotalAmounts(100, 0);
  }

  public void testMixedTypeOfSavingsSeriesShouldUpdateCorrectlyTheBudgetView() throws Exception {
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
      .setToAccount("Epargne")
      .setFromAccount("External account")
      .selectAllMonths()
      .setAmount("300")
      .setDay("5")
      .validate();

    budgetView.savings.createSeries()
      .setName("Placement")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Account n. 111222")
      .validate();

    views.selectCategorization();
    categorization.setSavings("Virement Epargne", "Placement");

    categorization.setSavings("Virement vers Epargne", "Placement");

    timeline.selectAll();
    views.selectData();
    transactions.initAmountContent()
      .add("05/10/2008", "Planned: Virement CAF", 300.00, "Virement CAF", 1600.00, 4800.00, "Epargne")
      .add("01/10/2008", "Planned: Placement", -100.00, "Placement", -200.00, -200.00, "Account n. 00001123")
      .add("01/10/2008", "Planned: Placement", 100.00, "Placement", 3200.00, 4500.00, "Account n. 111222")
      .add("05/09/2008", "Planned: Virement CAF", 300.00, "Virement CAF", 1300.00, 4400.00, "Epargne")
      .add("01/09/2008", "Planned: Placement", -100.00, "Placement", -100.00, -100.00, "Account n. 00001123")
      .add("01/09/2008", "Planned: Placement", 100.00, "Placement", 3100.00, 4100.00, "Account n. 111222")
      .add("06/08/2008", "VIREMENT VERS EPARGNE", -100.00, "Placement", 0.00, 0.00, "Account n. 00001123")
      .add("06/08/2008", "VIREMENT EPARGNE", 100.00, "Placement", 3000.00, 4000.00, "Account n. 111222")
      .add("05/08/2008", "VIREMENT CAF", 300.00, "Virement CAF", 1000.00, 3900.00, "Epargne")
      .add("06/07/2008", "VIREMENT VERS EPARGNE", -100.00, "Placement", 100.00, 100.00, "Account n. 00001123")
      .add("06/07/2008", "VIREMENT EPARGNE", 100.00, "Placement", 2900.00, 3600.00, "Account n. 111222")
      .add("05/07/2008", "VIREMENT CAF", 300.00, "Virement CAF", 700.00, 3500.00, "Epargne")
      .add("06/06/2008", "VIREMENT VERS EPARGNE", -100.00, "Placement", 200.00, 200.00, "Account n. 00001123")
      .add("06/06/2008", "VIREMENT EPARGNE", 100.00, "Placement", 2800.00, 3200.00, "Account n. 111222")
      .add("05/06/2008", "VIREMENT CAF", 300.00, "Virement CAF", 400.00, 3100.00, "Epargne")
      .check();

    views.selectHome();
    timeline.selectMonth("2008/06");
    savingsAccounts.checkPosition("Epargne", 400);
    savingsAccounts.checkPosition("Account n. 111222", 2800);
    savingsAccounts.checkEstimatedPosition(3200, "30/06/2008");
    savingsAccounts.checkSummary(4000, "2008/08/06");

    timeline.selectMonth("2008/08");
    savingsAccounts.checkPosition("Epargne", 1000);
    savingsAccounts.checkPosition("Account n. 111222", 3000);
    savingsAccounts.checkEstimatedPosition(4000, "31/08/2008");
    savingsAccounts.checkSummary(4000, "2008/08/06");
    views.selectSavings();
//    monthSummary.checkSavingsIn("Epargne", 300, 300);
    savingsView.checkAmount("Epargne", "Virement CAF", 300, 300);
//    monthSummary.checkSavingsIn("Account n. 111222", 100, 100);
    savingsView.checkAmount("Account n. 111222", "Placement", 100, 100);

    views.selectHome();
    timeline.selectMonth("2008/09");
    savingsAccounts.checkPosition("Epargne", 1300);
    savingsAccounts.checkPosition("Account n. 111222", 3100);
    savingsAccounts.checkEstimatedPosition(4400, "30/09/2008");
    views.selectSavings();
    savingsView.checkAmount("Epargne", "Virement CAF", 0, 300);
//    monthSummary.checkSavingsIn("Epargne", 0, 300);
    savingsView.checkAmount("Account n. 111222", "Placement", 0, 100);
//    monthSummary.checkSavingsIn("Account n. 111222", 0, 100);

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.savings.checkTotalGauge(-100, -100);
    timeline.selectMonth("2008/08");
    budgetView.savings.checkTotalGauge(-100, -100);
    timeline.selectMonth("2008/09");
    budgetView.savings.checkTotalGauge(0, -100);

    timeline.selectMonth("2008/06");
    budgetView.savings.checkSeries("Placement", 100, 100);
    budgetView.savings.checkSeriesNotPresent("Account n. 111222");
    views.selectSavings();
    savingsView.checkAmount("Account n. 111222", "Placement", 100, 100);

    timeline.selectMonth("2008/09");
    views.selectBudget();
    budgetView.savings.checkSeries("Placement", 0, 100);
    budgetView.savings.checkSeriesNotPresent("Account n. 111222");
    views.selectSavings();
    savingsView.checkAmount("Account n. 111222", "Placement", 0, 100);
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
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Account n. 111")
      .validate();
    budgetView.savings.createSeries()
      .setName("Project")
      .setFromAccount("Account n. 111")
      .setToAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
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
    budgetView.savings.checkSeries("CA", 120, 100);
    views.selectData();
    transactions.initAmountContent()
      .add("12/08/2008", "P3 CC", -20.00, "CA", 0.00, 0.00, "Account n. 00001123")
      .add("12/08/2008", "V3 CC", 100.00, "Project", 20.00, 20.00, "Account n. 00001123")
      .add("12/08/2008", "V3 CE", 20.00, "CA", 870.00, 870.00, "Account n. 111")
      .add("12/08/2008", "P3 CE", -100.00, "Project", 850.00, 850.00, "Account n. 111")
      .add("11/08/2008", "P2 CC", -50.00, "CA", -80.00, -80.00, "Account n. 00001123")
      .add("11/08/2008", "V2 CC", 100.00, "Project", -30.00, -30.00, "Account n. 00001123")
      .add("11/08/2008", "V2 CE", 50.00, "CA", 950.00, 950.00, "Account n. 111")
      .add("11/08/2008", "P2 CE", -100.00, "Project", 900.00, 900.00, "Account n. 111")
      .add("10/08/2008", "P1 CC", -50.00, "CA", -130.00, -130.00, "Account n. 00001123")
      .add("10/08/2008", "V1 CC", 100.00, "Project", -80.00, -80.00, "Account n. 00001123")
      .add("10/08/2008", "V1 CE", 50.00, "CA", 1000.00, 1000.00, "Account n. 111")
      .add("10/08/2008", "P1 CE", -100.00, "Project", 950.00, 950.00, "Account n. 111")
      .check();
  }

  public void testChangeAccountDirectionDoNotChangeBudgetSign() throws Exception {
    mainAccounts.createMainAccount("Main", 99);
    savingsAccounts.createSavingsAccount("Epargne", 1000);

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("Test")
      .setFromAccount("Main")
      .setToAccount("External")
      .selectAllMonths()
      .setAmount("300")
      .checkTable(new Object[][]{
        {"2008", "August", "", "300.00"}
      })
      .setFromAccount("External")
      .setToAccount("Main")
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
      .selectAllMonths()
      .setAmount("300")
      .validate();
    timeline.selectMonth("2008/06");

    views.selectSavings();
    savingsView.checkAmount("Savings 1", "Test", -300, -300);
    savingsView.checkAmount("Savings 2", "Test", 300, 300);
  }

  public void testSavingAccountWithNoTransactionShouldNotBeIgnored() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectCategorization();
    categorization.setNewSavings("Virement", "Epargne", OfxBuilder.DEFAULT_ACCOUNT_NAME, "External account");
    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAccountName("Livret")
      .selectBank("ING Direct")
      .setBalance(0)
      .validate();
    savingsAccounts.createNewAccount()
      .setAccountName("Livret 2")
      .selectBank("ING Direct")
      .validate();
    views.selectCategorization();
    categorization.selectTransactions("Virement")
      .editSeries(false)
      .setToAccount("Livret")
      .validate();
    categorization.selectSavings().selectSeries("Epargne");
    views.selectHome();

    timeline.selectMonth("2008/07");
    savingsAccounts.checkEstimatedPosition(100, "31/07/2008");
    timeline.selectMonth("2008/08");
    savingsAccounts.checkEstimatedPosition(200, "31/08/2008");
  }

  public void testInverseAccountAfterCategorization() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAccountName("Livret")
      .selectBank("ING Direct")
      .setBalance(100)
      .validate();
    views.selectCategorization();
    categorization.setNewSavings("Virement", "Epargne", OfxBuilder.DEFAULT_ACCOUNT_NAME, "Livret");
    views.selectSavings();
    savingsView.editSavingsSeries("Livret", "Epargne")
      .setToAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setFromAccount("Livret")
      .validate();
  }

  public void testSavingsAccounts() throws Exception {
    views.selectSavings();
    savingsView.checkNoAccounts();
    savingsView.checkTotalPositionHidden();

    views.selectHome();
    savingsAccounts.createSavingsAccount("Epargne", 1000);
    mainAccounts.createNewAccount()
      .setAccountName("Main")
      .setAccountNumber("4321")
      .selectBank("CIC")
      .setAsMain()
      .checkIsMain()
      .setBalance(99.0)
//      .setUpdateModeToManualInput()
      .validate();

    views.selectSavings();
    savingsView.checkTotalPositionHidden();
    savingsView.checkAccountWithNoPosition("Epargne");

    savingsView.createSavingsSeries()
      .setName("Virement CAF")
      .setToAccount("Epargne")
      .setFromAccount("Main")
      .selectAllMonths()
      .setAmount("300")
      .setDay("5")
      .validate();

    savingsView.checkAccount("Epargne", 1000.00, "31/08/2008");

    savingsView.checkTotalPosition(1000.00, "31/08/2008");
  }

  public void testAllInManualTransactionModeFromSavingsToMainWithCreationOfTransactionInMain() throws Exception {
    mainAccounts.createNewAccount().setAccountName("Main")
      .setAsMain()
      .setBalance(1000)
      .selectBank(SOCIETE_GENERALE)
      .setUpdateModeToManualInput()
      .validate();

    savingsAccounts.createNewAccount().setAccountName("Savings")
      .selectBank(SOCIETE_GENERALE)
      .setAsSavings()
      .setBalance(1000)
      .setUpdateModeToManualInput()
      .validate();

    views.selectCategorization();
    transactionCreation.selectAccount("Savings")
      .setAmount(-100)
      .setLabel("Financement")
      .setDay(2)
      .create();

    categorization.selectTransactions("Financement")
      .selectSavings().createSeries()
      .setName("Savings Series")
      .setFromAccount("Savings")
      .setToAccount("Main")
      .validate();
    categorization.setSavings("Financement", "Savings Series");

    views.selectData();
    transactions.selectAccount("Savings");
    transactions
      .initAmountContent()
      .add("02/08/2008", "FINANCEMENT", -100.00, "Savings Series", 1000.00, 1000.00, "Savings")
      .check();
  }

  public void testTakeAccountPositionDateIfNoOperationsInPast() throws Exception {

    operations.openPreferences().setFutureMonthsCount(6).validate();

    mainAccounts.createNewAccount().setAccountName("Main")
      .setAsMain()
      .setBalance(1000)
      .selectBank(SOCIETE_GENERALE)
      .setUpdateModeToManualInput()
      .validate();

    savingsAccounts.createNewAccount().setAccountName("Savings")
      .selectBank(SOCIETE_GENERALE)
      .setAsSavings()
      .setBalance(1000)
      .setUpdateModeToManualInput()
      .validate();

    views.selectHome();
    mainAccounts
      .checkSummary(1000, "2008/08/31");

    savingsAccounts
      .checkSummary(1000, "2008/08/31");

    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("EDF")
      .switchToManual()
      .selectAllMonths()
      .setAmount("50")
      .validate();

    timeline.selectMonth("2008/09");

    views.selectHome();
    monthSummary
      .checkBalance(-50);

  }

}
