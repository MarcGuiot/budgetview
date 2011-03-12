package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.BankEntity;
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
    savingsAccounts.createSavingsAccount("Epargne LCL", 1000.);

    categorization
      .selectTransactions("Virement")
      .selectSavings().createSeries()
      .setName("Epargne")
      .setFromAccount("Main accounts")
      .checkFromContentEquals("Main accounts")
      .setToAccount("Epargne LCL")
      .checkToContentEquals("External account", "Epargne LCL")
      .validate();

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.savings.alignAndPropagate("Epargne");

    timeline.selectAll();
    budgetView.savings
      .checkSeries("Epargne", 300, 500);

    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("08/10/2008", "Planned: Epargne", 100.00, "Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("08/10/2008", "Planned: Epargne", -100.00, "Epargne", -200.00, "Main accounts")
      .add("08/09/2008", "Planned: Epargne", 100.00, "Epargne", 1100.00, 1100.00, "Epargne LCL")
      .add("08/09/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, "Main accounts")
      .add("10/08/2008", "VIREMENT", 100.00, "Epargne", 1000.00, 1000.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/07/2008", "VIREMENT", 100.00, "Epargne", 900.00, 900.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/06/2008", "VIREMENT", 100.00, "Epargne", 800.00, 800.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "Epargne", 200.00, 200.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEstimatedPosition("Epargne LCL", 1000);

    timeline.selectMonth("2008/09");
    savingsAccounts.checkEstimatedPosition("Epargne LCL", 1100);

    timeline.selectMonth("2008/10");
    savingsAccounts.checkEstimatedPosition("Epargne LCL", 1200);
  }

  public void testCreateSavingsSeriesAndPayFromSavings() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();

    savingsAccounts.createSavingsAccount("Epargne LCL", 1000.);

    categorization
      .selectTransactions("Virement")
      .selectSavings().createSeries()
      .setName("Epargne")
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .validate();
    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.savings.alignAndPropagate("Epargne");

    budgetView.savings.createSeries()
      .setName("Achat Tele")
      .setFromAccount("Epargne LCL")
      .setToAccount("Main accounts")
      .selectMonth(200810)
      .setAmount(300)
      .validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("08/10/2008", "Planned: Achat Tele", -300.00, "Achat Tele", 900.00, 900.00, "Epargne LCL")
      .add("08/10/2008", "Planned: Epargne", 100.00, "Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("08/10/2008", "Planned: Epargne", -100.00, "Epargne", 100.00, "Main accounts")
      .add("08/10/2008", "Planned: Achat Tele", 300.00, "Achat Tele", 200.00, "Main accounts")
      .add("08/09/2008", "Planned: Epargne", 100.00, "Epargne", 1100.00, 1100.00, "Epargne LCL")
      .add("08/09/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, "Main accounts")
      .add("10/08/2008", "VIREMENT", 100.00, "Epargne", 1000.00, 1000.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/07/2008", "VIREMENT", 100.00, "Epargne", 900.00, 900.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/06/2008", "VIREMENT", 100.00, "Epargne", 800.00, 800.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "Epargne", 200.00, 200.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    timeline.selectMonth("2008/10");
    savingsAccounts.checkEstimatedPosition("Epargne LCL", 900);
    budgetView.savings.checkSeries("Achat Tele", 0, -300);
    budgetView.savings.checkTotalAmounts(0, -200);
  }

  public void testSavingsAccountFilledFromExternalAccountBalance() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    savingsAccounts.createNewAccount()
      .setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();

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
      .setToAccount("External Account")
      .selectMonth(200810)
      .setAmount("400")
      .validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("08/10/2008", "Planned: Travaux", -400.00, "Travaux", 1000.00, 1000.00, "Epargne LCL")
      .add("08/10/2008", "Planned: Epargne", 200.00, "Epargne", 1400.00, 1400.00, "Epargne LCL")
      .add("08/09/2008", "Planned: Epargne", 200.00, "Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("01/08/2008", "EPARGNE", 200.00, "Epargne", 1000.00, 1000.00, "Epargne LCL")
      .check();

    timeline.selectMonth("2008/10");
    budgetView.savings.checkTotalAmounts(0, 0);

    savingsAccounts.checkEstimatedPosition("Epargne LCL", 1000);

    savingsView.checkSeriesAmounts("Epargne LCL", "Epargne", 0, 200);
    savingsView.checkSeriesAmounts("Epargne LCL", "Travaux", 0, -400);
  }

  public void testCreateSavingsSeriesAndAssociateLaterToAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .addTransaction("2008/07/10", -100.00, "Virement")
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    categorization
      .selectTransactions("Virement")
      .selectSavings()
      .createSeries()
      .setName("Epargne")
      .setFromAccount("Main accounts")
      .setToAccount("External Account")
      .validate();

    timeline.selectMonth("2008/06");
    budgetView.savings.alignAndPropagate("Epargne");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("08/10/2008", "Planned: Epargne", -100.00, "Epargne", -200.00, "Main accounts")
      .add("08/09/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, "Main accounts")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/07/2008", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/06/2008", "VIREMENT", -100.00, "Epargne", 200.00, 200.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    savingsAccounts.createNewAccount()
      .setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();

    budgetView.savings.editSeries("Epargne")
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .validate();

    categorization.selectTransactions("VIREMENT").selectSavings().selectSeries("Epargne");

    timeline.selectMonth("2008/06");
    budgetView.savings.alignAndPropagate("Epargne");

    timeline.selectAll();
    transactions.initAmountContent()
      .add("08/10/2008", "Planned: Epargne", 100.00, "Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("08/10/2008", "Planned: Epargne", -100.00, "Epargne", -200.00, "Main accounts")
      .add("08/09/2008", "Planned: Epargne", 100.00, "Epargne", 1100.00, 1100.00, "Epargne LCL")
      .add("08/09/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, "Main accounts")
      .add("10/08/2008", "VIREMENT", 100.00, "Epargne", 1000.00, 1000.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/07/2008", "VIREMENT", 100.00, "Epargne", 900.00, 900.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/06/2008", "VIREMENT", 100.00, "Epargne", 800.00, 800.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "Epargne", 200.00, 200.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
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

    savingsAccounts.createNewAccount()
      .setAsSavings()
      .setAccountName("Epargne LCL")
      .selectBank("LCL")
      .setPosition(1000)
      .validate();

    categorization
      .selectTransactions("Virement")
      .selectSavings().createSeries()
      .setFromAccount("Main accounts")
      .setToAccount("Epargne LCL")
      .setName("Epargne")
      .validate();

    timeline.selectMonth("2008/06");
    budgetView.savings.alignAndPropagate("Epargne");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("08/10/2008", "Planned: Epargne", 100.00, "Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("08/10/2008", "Planned: Epargne", -100.00, "Epargne", -200.00, "Main accounts")
      .add("08/09/2008", "Planned: Epargne", 100.00, "Epargne", 1100.00, 1100.00, "Epargne LCL")
      .add("08/09/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, "Main accounts")
      .add("10/08/2008", "VIREMENT", 100.00, "Epargne", 1000.00, 1000.00, "Epargne LCL")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/07/2008", "VIREMENT", 100.00, "Epargne", 900.00, 900.00, "Epargne LCL")
      .add("10/07/2008", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/06/2008", "VIREMENT", 100.00, "Epargne", 800.00, 800.00, "Epargne LCL")
      .add("10/06/2008", "VIREMENT", -100.00, "Epargne", 200.00, 200.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();
    savingsAccounts.createNewAccount()
      .setAsSavings()
      .setAccountName("Epargne CIC")
      .selectBank("CIC")
      .setPosition(100)
      .validate();

    categorization.selectTransactions("VIREMENT")
      .setUncategorized();

    budgetView.savings.editSeries("Epargne")
      .setToAccount("Epargne CIC")
      .validate();
    transactions.initAmountContent()
      .add("08/10/2008", "Planned: Epargne", 100.00, "Epargne", 400.00, 1400.00, "Epargne CIC")
      .add("08/10/2008", "Planned: Epargne", -100.00, "Epargne", -300.00, "Main accounts")
      .add("08/09/2008", "Planned: Epargne", 100.00, "Epargne", 300.00, 1300.00, "Epargne CIC")
      .add("08/09/2008", "Planned: Epargne", -100.00, "Epargne", -200.00, "Main accounts")
      .add("10/08/2008", "Planned: Epargne", 100.00, "Epargne", 200.00, 1200.00, "Epargne CIC")
      .add("10/08/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, "Main accounts")
      .add("10/08/2008", "VIREMENT", -100.00, "To categorize", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/07/2008", "VIREMENT", -100.00, "To categorize", 100.00, 100.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/06/2008", "VIREMENT", -100.00, "To categorize", 200.00, 200.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    categorization
      .selectTransactions("Virement")
      .selectSavings()
      .selectSeries("Epargne");
    transactions.initAmountContent()
      .add("08/10/2008", "Planned: Epargne", 100.00, "Epargne", 300.00, 1300.00, "Epargne CIC")
      .add("08/10/2008", "Planned: Epargne", -100.00, "Epargne", -200.00, "Main accounts")
      .add("08/09/2008", "Planned: Epargne", 100.00, "Epargne", 200.00, 1200.00, "Epargne CIC")
      .add("08/09/2008", "Planned: Epargne", -100.00, "Epargne", -100.00, "Main accounts")
      .add("10/08/2008", "VIREMENT", 100.00, "Epargne", 100.00, 1100.00, "Epargne CIC")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/07/2008", "VIREMENT", 100.00, "Epargne", 0.00, 1000.00, "Epargne CIC")
      .add("10/07/2008", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/06/2008", "VIREMENT", 100.00, "Epargne", -100.00, 900.00, "Epargne CIC")
      .add("10/06/2008", "VIREMENT", -100.00, "Epargne", 200.00, 200.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();
  }

  public void testSplitPreviouslyCategorizedInSavings() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    timeline.selectAll();

    savingsAccounts.createSavingsAccount("Epargne", 1000.);
    categorization
      .selectTransactions("Virement")
      .selectSavings().createSeries()
      .setName("Epargne")
      .setFromAccount("Main accounts")
      .setToAccount("Epargne")
      .validate();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("10/08/2008", "VIREMENT", 100.00, "Epargne", 1000.00, 1000.00, "Epargne")
      .add("10/08/2008", "VIREMENT", -100.00, "Epargne", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    categorization.selectTransactions("Virement");
    transactionDetails.split("50", "Comportement impossible?");
    categorization.selectVariable().selectNewSeries("Occasional");
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("10/08/2008", "VIREMENT", 50.00, "Epargne", 1000.00, 1000.00, "Epargne")
      .add("10/08/2008", "VIREMENT", -50.00, "Epargne", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/08/2008", "VIREMENT", -50.00, "Occasional", 50.00, 50.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();
  }

  public void testExternalToNotImportedSavingsWithDate() throws Exception {
    // force creation of month in the past
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "FNAC")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    savingsAccounts.createSavingsAccount("Epargne", 1000.);
    budgetView.savings.createSeries()
      .setName("CAF")
      .setFromAccount("External account")
      .setToAccount("Epargne")
      .selectAllMonths()
      .setAmount("300")
      .setDay("5")
      .validate();

    timeline.selectMonth("2008/06");
    savingsAccounts.checkEstimatedPosition("Epargne", 400);
    savingsAccounts.checkEstimatedPosition(400);
    savingsAccounts.checkSummary(1000, "2008/08/05");

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEstimatedPosition("Epargne", 1000);
    savingsAccounts.checkEstimatedPosition(1000);
    savingsAccounts.checkSummary(1000, "2008/08/05");

    timeline.selectMonth("2008/09");
    savingsAccounts.checkEstimatedPosition("Epargne", 1300);
    savingsAccounts.checkEstimatedPosition(1300);

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("01/10/2008", "Planned: CAF", 300.00, "CAF", 1600.00, 1600.00, "Epargne")
      .add("01/09/2008", "Planned: CAF", 300.00, "CAF", 1300.00, 1300.00, "Epargne")
      .add("05/08/2008", "CAF", 300.00, "CAF", 1000.00, 1000.00, "Epargne")
      .add("05/07/2008", "CAF", 300.00, "CAF", 700.00, 700.00, "Epargne")
      .add("10/06/2008", "FNAC", -100.00, "To categorize", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/06/2008", "CAF", 300.00, "CAF", 400.00, 400.00, "Epargne")
      .check();

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.savings.checkTotalAmounts(0, 0);

    views.selectSavings();

    timeline.selectMonth("2008/10");
    savingsView.checkSeriesAmounts("Epargne", "CAF", 0, 300);
    timeline.selectMonth("2008/06");
    savingsView.checkSeriesAmounts("Epargne", "CAF", 300, 300);

    savingsView.editSeries("Epargne", "CAF")
      .selectMonth(200806)
      .setAmount(0)
      .validate();
    views.selectSavings();

    // back to normal to see if dateChooser is hidden

    savingsView.editSeries("Epargne", "CAF")
      .setFromAccount("Main accounts")
      .checkDateChooserIsHidden()
      .validate();

    timeline.selectAll();
    transactions.initAmountContent()
      .add("08/10/2008", "Planned: CAF", 300.00, "CAF", 2200.00, 2200.00, "Epargne")
      .add("08/10/2008", "Planned: CAF", -300.00, "CAF", -1200.00, "Main accounts")
      .add("08/09/2008", "Planned: CAF", 300.00, "CAF", 1900.00, 1900.00, "Epargne")
      .add("08/09/2008", "Planned: CAF", -300.00, "CAF", -900.00, "Main accounts")
      .add("08/08/2008", "Planned: CAF", 300.00, "CAF", 1600.00, 1600.00, "Epargne")
      .add("08/08/2008", "Planned: CAF", -300.00, "CAF", -600.00, "Main accounts")
      .add("08/07/2008", "Planned: CAF", 300.00, "CAF", 1300.00, 1300.00, "Epargne")
      .add("08/07/2008", "Planned: CAF", -300.00, "CAF", -300.00, "Main accounts")
      .add("10/06/2008", "FNAC", -100.00, "To categorize", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    views.selectBudget();
    budgetView.savings.editSeries("CAF")
      .checkDateChooserIsHidden()
      .cancel();
  }

  // ==> test de l'effet de suppression de transaction référencée dans account
  public void testUpdateAccountOnSeriesChange() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Caf")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    savingsAccounts.createSavingsAccount("Epargne", 1000.00);

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CAF")
      .setFromAccount("External account")
      .setToAccount("Epargne")
      .selectAllMonths()
      .setAmount("100")
      .setDay("5")
      .validate();

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEstimatedPosition("Epargne", 1000);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkEstimatedPosition("Epargne", 1100);

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("01/10/2008", "Planned: CAF", 100.00, "CAF", 1200.00, 1200.00, "Epargne")
      .add("01/09/2008", "Planned: CAF", 100.00, "CAF", 1100.00, 1100.00, "Epargne")
      .add("10/08/2008", "CAF", -100.00, "To categorize", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/08/2008", "CAF", 100.00, "CAF", 1000.00, 1000.00, "Epargne")
      .check();

    savingsView
      .editSeries("Epargne", "CAF")
      .setFromAccount("Main accounts")
      .validate();

    // Todo : ne passe pas pourquoi?
//    timeline.selectAll();
//    //    transactions.initContent()
//      .add("10/08/2008", TransactionType.PRELEVEMENT, "Caf", "", -100.00)
//      .check();

    categorization.setSavings("Caf", "CAF");

    transactions
      .initAmountContent()
      .add("08/10/2008", "Planned: CAF", 100.00, "CAF", 1300.00, 1300.00, "Epargne")
      .add("08/10/2008", "Planned: CAF", -100.00, "CAF", -200.00, "Main accounts")
      .add("08/09/2008", "Planned: CAF", 100.00, "CAF", 1200.00, 1200.00, "Epargne")
      .add("08/09/2008", "Planned: CAF", -100.00, "CAF", -100.00, "Main accounts")
      .add("10/08/2008", "CAF", 100.00, "CAF", 1100.00, 1100.00, "Epargne")
      .add("10/08/2008", "CAF", -100.00, "CAF", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    views.selectBudget();
    timeline.selectMonth("2008/08");
    savingsAccounts.checkEstimatedPosition("Epargne", 1100);
    timeline.selectMonth("2008/09");
    savingsAccounts.checkEstimatedPosition("Epargne", 1200);
  }

  public void testAutomaticBudget() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    savingsAccounts.createSavingsAccount("Epargne", 1000.);
    budgetView.savings.createSeries()
      .setName("CA")
      .setFromAccount("Main accounts")
      .setToAccount("Epargne")
      .validate();

    categorization.setSavings("Virement", "CA");

    budgetView.savings.alignAndPropagate("CA");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("08/10/2008", "Planned: CA", 100.00, "CA", 1200.00, 1200.00, "Epargne")
      .add("08/10/2008", "Planned: CA", -100.00, "CA", -200.00, "Main accounts")
      .add("08/09/2008", "Planned: CA", 100.00, "CA", 1100.00, 1100.00, "Epargne")
      .add("08/09/2008", "Planned: CA", -100.00, "CA", -100.00, "Main accounts")
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Epargne")
      .add("10/08/2008", "VIREMENT", -100.00, "CA", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    budgetView.savings.editSeries("CA")
      .validate();

    timeline.selectAll();
    transactions.initAmountContent()
      .add("08/10/2008", "Planned: CA", 100.00, "CA", 1200.00, 1200.00, "Epargne")
      .add("08/10/2008", "Planned: CA", -100.00, "CA", -200.00, "Main accounts")
      .add("08/09/2008", "Planned: CA", 100.00, "CA", 1100.00, 1100.00, "Epargne")
      .add("08/09/2008", "Planned: CA", -100.00, "CA", -100.00, "Main accounts")
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Epargne")
      .add("10/08/2008", "VIREMENT", -100.00, "CA", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();
  }

  public void testImportedSavingsAccountFromExternal() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "CAF")
      .load();

    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CAF")
      .setToAccount("Account n. 111")
      .setFromAccount("External Account")
      .checkDateChooserIsHidden()
      .validate();
    categorization.setSavings("CAF", "CAF");
    timeline.selectMonth("2008/08");
    views.selectSavings();
    savingsView.alignAndPropagate("Account n. 111", "CAF");

    savingsAccounts.checkEstimatedPosition("Account n. 111", 1000);

    timeline.selectMonth("2008/09");
    savingsAccounts.checkEstimatedPosition("Account n. 111", 1100);
    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("08/10/2008", "Planned: CAF", 100.00, "CAF", 1200.00, 1200.00, "Account n. 111")
      .add("08/09/2008", "Planned: CAF", 100.00, "CAF", 1100.00, 1100.00, "Account n. 111")
      .add("10/08/2008", "CAF", 100.00, "CAF", 1000.00, 1000.00, "Account n. 111")
      .check();
  }

  public void testDisableBudgetAreaIfSavingTransaction() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Prelevement")
      .load();
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")  //compte d'épargne
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    categorization.showSelectedMonthsOnly();
    timeline.selectMonth("2008/08");

    categorization.selectTransaction("Virement");
    categorization.checkAllButSavingBudgetAreaAreDisable();

    categorization.selectTransaction("Prelevement");
    categorization.checkAllBudgetAreaAreEnable();

    categorization.setNewSavings("Virement", "epargne", "Main accounts", "Account n. 111");
    categorization.setNewVariable("Prelevement", "economie du mois");

    categorization.selectTransactions("Prelevement", "Virement")
      .checkAllButSavingBudgetAreaAreDisable()
      .checkMultipleSeriesSelection()
      .setUncategorized()
      .selectTransaction("Prelevement")
      .checkSavingPreSelected();
  }

  public void testSimpleCase() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")  //compte d'épargne
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Prelevement")
      .addTransaction("2008/07/05", 12.00, "McDo")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CA")
      .setFromAccount("Main accounts")
      .setToAccount("Account n. 111")
      .validate();
    budgetView.savings.alignAndPropagate("CA");
    categorization.showSelectedMonthsOnly();

    timeline.selectMonth("2008/08");
    categorization.setSavings("Prelevement", "CA");
    categorization.setSavings("Virement", "CA");
    views.selectBudget();
    budgetView.savings.alignAndPropagate("CA");
    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("08/10/2008", "Planned: CA", 100.00, "CA", 1200.00, 1200.00, "Account n. 111")
      .add("08/10/2008", "Planned: CA", -100.00, "CA", -200.00, "Main accounts")
      .add("08/09/2008", "Planned: CA", 100.00, "CA", 1100.00, 1100.00, "Account n. 111")
      .add("08/09/2008", "Planned: CA", -100.00, "CA", -100.00, "Main accounts")
      .add("10/08/2008", "PRELEVEMENT", -100.00, "CA", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Account n. 111")
      .add("05/07/2008", "MCDO", 12.00, "To categorize", 100.00, 100.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    timeline.selectMonth("2008/10");
    savingsAccounts.checkEstimatedPosition("Account n. 111", 1200);
    mainAccounts.checkEstimatedPosition(-200);
  }

  public void testImportedSavingsAccountWithMainAccount() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")  //compte d'épargne
      .addTransaction("2008/08/10", 100.00, "Virement")
      .addTransaction("2008/07/10", -200.00, "Prelevement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Prelevement")
      .addTransaction("2008/07/10", 200.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CA")
      .setFromAccount("Main accounts")
      .setToAccount("Account n. 111")
      .validate();
    budgetView.savings.createSeries()
      .setName("Project")
      .setFromAccount("Account n. 111")
      .setToAccount("Main accounts")
      .setOnceAYear()
      .toggleMonth(7)
      .validate();

    categorization.showSelectedMonthsOnly();
    timeline.selectMonth("2008/07");
    categorization.setSavings("Prelevement", "Project");
    categorization.setSavings("Virement", "Project");
    timeline.selectMonth("2008/08");
    categorization.setSavings("Prelevement", "CA");
    categorization.setSavings("Virement", "CA");

    views.selectBudget();
    budgetView.savings
      .alignAndPropagate("CA");
    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("08/10/2008", "Planned: CA", 100.00, "CA", 1200.00, 1200.00, "Account n. 111")
      .add("08/10/2008", "Planned: CA", -100.00, "CA", -200.00, "Main accounts")
      .add("08/09/2008", "Planned: CA", 100.00, "CA", 1100.00, 1100.00, "Account n. 111")
      .add("08/09/2008", "Planned: CA", -100.00, "CA", -100.00, "Main accounts")
      .add("10/08/2008", "PRELEVEMENT", -100.00, "CA", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Account n. 111")
      .add("10/07/2008", "VIREMENT", 200.00, "Project", 100.00, 100.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/07/2008", "PRELEVEMENT", -200.00, "Project", 900.00, 900.00, "Account n. 111")
      .check();

    timeline.selectMonth("2008/10");
    savingsAccounts.checkEstimatedPosition("Account n. 111", 1200);
    mainAccounts.checkEstimatedPosition(-200);

    views.selectBudget();
    timeline.selectMonth("2008/08");
    budgetView.savings.checkSeries("CA", 100, 100);

    views.selectSavings();
    savingsView.checkSeriesAmounts("Account n. 111", "CA", 100, 100);

    views.selectBudget();
    budgetView.savings.checkTotalAmounts(100, 100);
    budgetView.savings.editSeries("CA")
      .selectAllMonths()
      .setAmount(200)
      .validate();

    views.selectSavings();
    savingsView.editSeries("Account n. 111", "CA")
      .selectAllMonths()
      .checkAmount(200.00)
      .checkChart(new Object[][]{
        {"2008", "July", 0.00, 200.00, true},
        {"2008", "August", 100.00, 200.00, true},
        {"2008", "September", 0.00, 200.00, true},
        {"2008", "October", 0.00, 200.00, true},
      }
      )
      .validate();

    views.selectBudget();
    budgetView.savings.checkTotalAmounts(100, 200);
  }

//  public void testBothImportedButCategorizedOnlyOne() throws Exception {
//    OfxBuilder.init(this)
//      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")  //compte d'épargne
//      .addTransaction("2008/08/10", 100.00, "Virement Epargne")
//      .addTransaction("2008/07/10", -200.00, "Prelevement Epargne")
//      .load();
//    OfxBuilder.init(this)
//      .addTransaction("2008/08/10", -100.00, "Prelevement CC")
//      .addTransaction("2008/07/10", 200.00, "Virement CC")
//      .load();
//    operations.openPreferences().setFutureMonthsCount(2).validate();
//
//    this.mainAccounts.edit("Account n. 111")
//      .setAsSavings()
//      .validate();
//
//    openApplication();
//    this.categorization
//      .setSavings("Prelevement Epargne", "From Account n. 111");
//    this.savingsView.editPlannedAmount("Account n. 111", "From Account n. 111")
//      .checkActualAmount("-200")
//      .checkAmount("0");
//
//    this.budgetView.savings.checkSeries("To Account n. 111", -200, 0);
//    this.budgetView.savings.editPlannedAmount("To Account n. 111")
//      .checkActualAmount("200")
//      .checkAmount("0")
//      .setPropagationEnabled()
//      .alignPlannedAndActual();
//
//    this.savingsView.editPlannedAmount("Account n. 111", "From Account n. 111")
//      .checkActualAmount("-200")
//      .checkAmount("-200");
//
//  }


  public void testImportedSavingsAccountWithMainAccountInManual() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "Virement")
      .load();
    OfxBuilder.init(this)
      .addTransaction("2008/08/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CA")
      .setFromAccount("Main accounts")
      .setToAccount("Account n. 111")
      .selectAllMonths()
      .setAmount(50)
      .validate();

    categorization.setSavings("Virement", "CA");
    timeline.selectAll();

    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("08/10/2008", "Planned: CA", 50.00, "CA", 1100.00, 1100.00, "Account n. 111")
      .add("08/10/2008", "Planned: CA", -50.00, "CA", -100.00, "Main accounts")
      .add("08/09/2008", "Planned: CA", 50.00, "CA", 1050.00, 1050.00, "Account n. 111")
      .add("08/09/2008", "Planned: CA", -50.00, "CA", -50.00, "Main accounts")
      .add("10/08/2008", "VIREMENT", -100.00, "CA", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/08/2008", "VIREMENT", 100.00, "CA", 1000.00, 1000.00, "Account n. 111")
      .check();

    timeline.selectMonth("2008/10");
    savingsAccounts.checkEstimatedPosition("Account n. 111", 1100);
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
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
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

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();
    savingsAccounts.createSavingsAccount("Epargne", 1000.);

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
      .setFromAccount("Main accounts")
      .setToAccount("Account n. 111222")
      .validate();

    categorization.setSavings("Virement Epargne", "Placement");
    categorization.setSavings("Virement vers Epargne", "Placement");

    timeline.selectMonth("2008/06");
    budgetView.savings.alignAndPropagate("Placement");

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("01/10/2008", "Planned: Virement CAF", 300.00, "Virement CAF", 1600.00, 4800.00, "Epargne")
      .add("01/10/2008", "Planned: Placement", 100.00, "Placement", 3200.00, 4500.00, "Account n. 111222")
      .add("01/10/2008", "Planned: Placement", -100.00, "Placement", -200.00, "Main accounts")
      .add("01/09/2008", "Planned: Virement CAF", 300.00, "Virement CAF", 1300.00, 4400.00, "Epargne")
      .add("01/09/2008", "Planned: Placement", 100.00, "Placement", 3100.00, 4100.00, "Account n. 111222")
      .add("01/09/2008", "Planned: Placement", -100.00, "Placement", -100.00, "Main accounts")
      .add("06/08/2008", "VIREMENT VERS EPARGNE", -100.00, "Placement", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/08/2008", "VIREMENT EPARGNE", 100.00, "Placement", 3000.00, 4000.00, "Account n. 111222")
      .add("05/08/2008", "VIREMENT CAF", 300.00, "Virement CAF", 1000.00, 3900.00, "Epargne")
      .add("06/07/2008", "VIREMENT VERS EPARGNE", -100.00, "Placement", 100.00, 100.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/07/2008", "VIREMENT EPARGNE", 100.00, "Placement", 2900.00, 3600.00, "Account n. 111222")
      .add("05/07/2008", "VIREMENT CAF", 300.00, "Virement CAF", 700.00, 3500.00, "Epargne")
      .add("06/06/2008", "VIREMENT VERS EPARGNE", -100.00, "Placement", 200.00, 200.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/06/2008", "VIREMENT EPARGNE", 100.00, "Placement", 2800.00, 3200.00, "Account n. 111222")
      .add("05/06/2008", "VIREMENT CAF", 300.00, "Virement CAF", 400.00, 3100.00, "Epargne")
      .check();

    timeline.selectMonth("2008/06");
    savingsAccounts.checkEstimatedPosition("Epargne", 400);
    savingsAccounts.checkEstimatedPosition("Account n. 111222", 2800);
    savingsAccounts.checkEstimatedPosition(3200);
    savingsAccounts.checkSummary(4000, "2008/08/06");

    timeline.selectMonth("2008/08");
    savingsAccounts.checkEstimatedPosition("Epargne", 1000);
    savingsAccounts.checkEstimatedPosition("Account n. 111222", 3000);
    savingsAccounts.checkEstimatedPosition(4000);
    savingsAccounts.checkSummary(4000, "2008/08/06");

    views.selectSavings();
    savingsView.checkSeriesAmounts("Epargne", "Virement CAF", 300, 300);
    savingsView.checkSeriesAmounts("Account n. 111222", "Placement", 100, 100);

    timeline.selectMonth("2008/09");
    savingsAccounts.checkEstimatedPosition("Epargne", 1300);
    savingsAccounts.checkEstimatedPosition("Account n. 111222", 3100);
    savingsAccounts.checkEstimatedPosition(4400);

    views.selectSavings();
    savingsView.checkSeriesAmounts("Epargne", "Virement CAF", 0, 300);
    savingsView.checkSeriesAmounts("Account n. 111222", "Placement", 0, 100);

    views.selectBudget();
    timeline.selectMonth("2008/06");
    budgetView.savings.checkTotalGauge(-100, -100);
    timeline.selectMonth("2008/08");
    budgetView.savings.checkTotalGauge(-100, -100);
    timeline.selectMonth("2008/09");
    budgetView.savings.checkTotalGauge(0, -100);

    timeline.selectMonth("2008/06");
    budgetView.savings.checkSeries("Placement", 100, 100);
    budgetView.savings.checkSeriesNotPresent("Virement CAF");
    views.selectSavings();
    savingsView.checkSeriesAmounts("Account n. 111222", "Placement", 100, 100);

    timeline.selectMonth("2008/09");
    views.selectBudget();
    budgetView.savings.checkSeries("Placement", 0, 100);
    budgetView.savings.checkSeriesNotPresent("Virement CAF");

    views.selectSavings();
    savingsView.checkSeriesAmounts("Account n. 111222", "Placement", 0, 100);

    savingsView.editSeries("Account n. 111222", "Placement").deleteSavingsSeriesWithConfirmation();
    savingsView.editSeries("Epargne", "Virement CAF").deleteSavingsSeriesWithConfirmation();
    String fileName = operations.backup(this);
    operations.restore(fileName);
  }

  public void testSavingsGauge() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111", 1000.00, "2008/08/10")  //compte d'épargne
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

    this.mainAccounts.edit("Account n. 111")
      .setAsSavings()
      .validate();
    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CA")
      .setFromAccount("Main accounts")
      .setToAccount("Account n. 111")
      .validate();
    budgetView.savings.createSeries()
      .setName("Project")
      .setFromAccount("Account n. 111")
      .setToAccount("Main accounts")
      .setCustom()
      .setStartDate(200807)
      .setEndDate(200809)
      .validate();

    categorization.showSelectedMonthsOnly();

    timeline.selectMonth("2008/07");
    categorization.setSavings("Prelevement CE", "Project");
    categorization.setSavings("Virement CE", "CA");
    categorization.setSavings("Virement CC", "Project");
    categorization.setSavings("Prelevement CC", "CA");
    views.selectBudget();
    budgetView.savings.alignAndPropagate("CA");

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

    budgetView.savings.checkSeries("CA", 120, 100);
    transactions.initAmountContent()
      .add("12/08/2008", "P3 CC", -20.00, "CA", 0.00, 0.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("12/08/2008", "V3 CC", 100.00, "Project", 20.00, 20.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("12/08/2008", "V3 CE", 20.00, "CA", 870.00, 870.00, "Account n. 111")
      .add("12/08/2008", "P3 CE", -100.00, "Project", 850.00, 850.00, "Account n. 111")
      .add("11/08/2008", "P2 CC", -50.00, "CA", -80.00, -80.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("11/08/2008", "V2 CC", 100.00, "Project", -30.00, -30.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("11/08/2008", "V2 CE", 50.00, "CA", 950.00, 950.00, "Account n. 111")
      .add("11/08/2008", "P2 CE", -100.00, "Project", 900.00, 900.00, "Account n. 111")
      .add("10/08/2008", "P1 CC", -50.00, "CA", -130.00, -130.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/08/2008", "V1 CC", 100.00, "Project", -80.00, -80.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/08/2008", "V1 CE", 50.00, "CA", 1000.00, 1000.00, "Account n. 111")
      .add("10/08/2008", "P1 CE", -100.00, "Project", 950.00, 950.00, "Account n. 111")
      .check();

    String fileName = operations.backup(this);

    budgetView.savings.editSeries("Project")
      .deleteSavingsSeriesWithConfirmation();
    budgetView.savings.editSeries("CA")
      .deleteSavingsSeriesWithConfirmation();

    checkDeleteSeries();

    operations.restore(fileName);
    views.selectSavings();
    savingsView.editSeries("Account n. 111", "CA").deleteSavingsSeriesWithConfirmation();
    savingsView.editSeries("Account n. 111", "Project").deleteSavingsSeriesWithConfirmation();

    checkDeleteSeries();
  }

  private void checkDeleteSeries() throws Exception {

    String fileName = operations.backup(this);
    operations.restore(fileName);

    timeline.selectAll();
    transactions.initContent()
      .add("12/08/2008", TransactionType.PRELEVEMENT, "P3 CC", "", -20.00)
      .add("12/08/2008", TransactionType.VIREMENT, "V3 CC", "", 100.00)
      .add("12/08/2008", TransactionType.VIREMENT, "V3 CE", "", 20.00)
      .add("12/08/2008", TransactionType.PRELEVEMENT, "P3 CE", "", -100.00)
      .add("11/08/2008", TransactionType.PRELEVEMENT, "P2 CC", "", -50.00)
      .add("11/08/2008", TransactionType.VIREMENT, "V2 CC", "", 100.00)
      .add("11/08/2008", TransactionType.VIREMENT, "V2 CE", "", 50.00)
      .add("11/08/2008", TransactionType.PRELEVEMENT, "P2 CE", "", -100.00)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "P1 CC", "", -50.00)
      .add("10/08/2008", TransactionType.VIREMENT, "V1 CC", "", 100.00)
      .add("10/08/2008", TransactionType.VIREMENT, "V1 CE", "", 50.00)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "P1 CE", "", -100.00)
      .add("10/07/2008", TransactionType.VIREMENT, "VIREMENT CC", "", 200.00)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "PRELEVEMENT CC", "", -100.00)
      .add("10/07/2008", TransactionType.PRELEVEMENT, "PRELEVEMENT CE", "", -200.00)
      .add("10/07/2008", TransactionType.VIREMENT, "VIREMENT CE", "", 100.00)
      .check();
  }

  public void testChangeAccountDirectionDoesNotChangeBudgetSign() throws Exception {
    mainAccounts.createMainAccount("Main", 99);
    savingsAccounts.createSavingsAccount("Epargne", 1000.);

    views.selectBudget();
    SeriesEditionDialogChecker editionDialogChecker = budgetView.savings.createSeries();
    editionDialogChecker
      .setName("Test")
      .setFromAccount("Main accounts")
      .setToAccount("External")
      .selectAllMonths()
      .setAmount("300")
      .checkChart(new Object[][]{
        {"2008", "August", 0.00, 300.00, true}
      });
    editionDialogChecker
      .setFromAccount("External")
      .setToAccount("Main")
      .checkChart(new Object[][]{
        {"2008", "August", 0.00, 300.00, true}
      })
      .validate();
  }

  public void testBothNotImportedAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    savingsAccounts.createSavingsAccount("Savings 1", 1000.);
    savingsAccounts.createSavingsAccount("Savings 2", 1000.);
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
    savingsView.checkSeriesAmounts("Savings 1", "Test", -300, -300);
    savingsView.checkSeriesAmounts("Savings 2", "Test", 300, 300);
    savingsView.editSeries("Savings 1", "Test").deleteSavingsSeriesWithConfirmation();
    String fileName = operations.backup(this);
    operations.restore(fileName);
  }

  public void testSavingsAccountWithNoTransactionShouldNotBeIgnored() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();
    categorization.setNewSavings("Virement", "Epargne", "Main accounts", "External account");

    savingsAccounts.createNewAccount()
      .setAccountName("Livret")
      .selectBank("ING Direct")
      .setPosition(0)
      .validate();
    savingsAccounts.createNewAccount()
      .setAccountName("Livret 2")
      .selectBank("ING Direct")
      .validate();
    categorization.selectTransactions("Virement")
      .editSeries("Epargne")
      .setToAccount("Livret")
      .validate();
    categorization.selectSavings().selectSeries("Epargne");
    views.selectBudget();
    budgetView.savings.alignAndPropagate("Epargne");

    timeline.selectMonth("2008/07");
    savingsAccounts.checkEstimatedPosition(100);
    timeline.selectMonth("2008/08");
    savingsAccounts.checkEstimatedPosition(200);
  }

  public void testInverseAccountAfterCategorizationIsNotPossible() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/10", -100.00, "Virement")
      .load();
    operations.openPreferences().setFutureMonthsCount(2).validate();

    savingsAccounts.createNewAccount()
      .setAccountName("Livret")
      .selectBank("ING Direct")
      .setPosition(100)
      .validate();
    categorization.setNewSavings("Virement", "Epargne", "Main accounts", "Livret");

    budgetView.savings.editSeries("Epargne")
      .checkFromContentEquals("Main accounts")
      .checkToContentEquals("External account", "Livret")
      .validate();

    views.selectSavings();
    savingsView.editSeries("Livret", "Epargne")
      .checkFromContentEquals("External account", "Main accounts")
      .checkToContentEquals("Livret")
      .validate();
  }

  public void testSavingsAccounts() throws Exception {
    views.selectSavings();
    savingsView.checkNoAccounts();
    savingsView.checkNoEstimatedTotalPosition();

    mainAccounts.createNewAccount()
      .setAccountName("Main")
      .setAccountNumber("4321")
      .selectBank("CIC")
      .setAsMain()
      .checkIsMain()
      .setPosition(99.0)
      .validate();
    savingsAccounts.createSavingsAccount("Epargne", 1000.);

    savingsView.checkNoEstimatedTotalPosition();
    savingsView.checkAccountWithNoPosition("Epargne");

    savingsView.createSeries()
      .setName("Virement")
      .setToAccount("Epargne")
      .setFromAccount("Main accounts")
      .selectAllMonths()
      .setAmount("300")
      .setDay("5")
      .validate();

    savingsView.checkAccount("Epargne", 1300.00, "31/08/2008");

    savingsView.checkTotalEstimatedPosition("1300.00", "31/08/2008");
  }

  public void testAllInManualTransactionModeFromSavingsToMainWithCreationOfTransactionInMain() throws Exception {
    mainAccounts.createNewAccount().setAccountName("Main")
      .setAsMain()
      .setPosition(1000)
      .selectBank(SOCIETE_GENERALE)
      .setUpdateModeToManualInput()
      .validate();

    savingsAccounts.createNewAccount().setAccountName("Savings")
      .selectBank(SOCIETE_GENERALE)
      .setAsSavings()
      .setPosition(1000)
      .setUpdateModeToManualInput()
      .validate();

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

    transactions.selectAccount("Savings");
    transactions
      .initAmountContent()
      .add("02/08/2008", "FINANCEMENT", -100.00, "Savings Series", 1000.00, 1000.00, "Savings")
      .check();
  }

  public void testTakePositionDateIntoAccountIfNoOperationsInThePast() throws Exception {

    operations.openPreferences().setFutureMonthsCount(6).validate();

    mainAccounts.createNewAccount().setAccountName("Main")
      .setAsMain()
      .setPosition(1000)
      .selectBank(SOCIETE_GENERALE)
      .setUpdateModeToManualInput()
      .validate();

    savingsAccounts.createNewAccount().setAccountName("Savings")
      .selectBank(SOCIETE_GENERALE)
      .setAsSavings()
      .setPosition(1000)
      .setUpdateModeToManualInput()
      .validate();

    mainAccounts
      .checkSummary(1000, "2008/08/31");

    savingsAccounts
      .checkSummary(1000, "2008/08/31");

    views.selectBudget();
    budgetView.recurring.createSeries()
      .setName("EDF")
      .selectAllMonths()
      .setAmount("50")
      .validate();

    timeline.selectMonth("2008/09");

    views.selectBudget();
    budgetView.getSummary().checkEndPosition(900.00);
  }

  public void testCheckComboAccountContents() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", 100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    categorization.selectAllTransactions()
      .selectSavings()
      .createSeries()
      .setName("Epargne")
      .checkToContentEquals("Account n. 111222")
      .checkToAccount("Account n. 111222")
      .checkFromContentEquals("Main accounts")
      .checkFromAccount("Main accounts")
      .validate();

    categorization
      .selectUncategorized()
      .selectTransaction("Virement vers Epargne")
      .checkSavingsSeriesIsSelected("Epargne")
      .selectTransaction("Virement Epargne")
      .checkSavingsSeriesIsSelected("Epargne");
  }

  public void testCreatingSavingsFromCategorisationDoNotAssign() throws Exception {

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", -100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    mainAccounts.edit(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setAsSavings()
      .validate();

    budgetView.savings.editSeries("From Account n. 111222").deleteCurrentSeries();
    budgetView.savings.editSeries("To Account n. 111222").deleteCurrentSeries();
    budgetView.savings.editSeries("From Account n. 00001123").deleteCurrentSeries();
    budgetView.savings.editSeries("To Account n. 00001123").deleteCurrentSeries();

    categorization.selectAllTransactions()
      .selectSavings()
      .createSeries()
      .setName("Epargne")
      .checkToContentEquals("External account", "Main accounts", OfxBuilder.DEFAULT_ACCOUNT_NAME, "Account n. 111222")
      .setToAccount("Account n. 111222")
      .checkFromContentEquals("External account", "Main accounts", OfxBuilder.DEFAULT_ACCOUNT_NAME, "Account n. 111222")
      .setFromAccount("External account")
      .validate();

    categorization.selectTransaction("Virement vers Epargne")
      .checkSavingPreSelected()
      .selectSavings()
      .checkContainsNoSeries();

    categorization.selectTransaction("Virement Epargne")
      .checkSavingPreSelected()
      .selectSavings()
      .checkContainsNoSeries();
  }

  public void testDeleteSavingSeriesAskForConfirmation() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setAsSavings()
      .validate();

    categorization.selectAllTransactions()
      .selectSavings()
      .createSeries()
      .setName("Epargne")
      .setToAccount("External account")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .validate();

    categorization.selectTransaction("Virement vers Epargne")
      .checkSavingsSeriesIsSelected("Epargne");

    SeriesEditionDialogChecker seriesEditionDialogChecker = categorization.editSeries("Epargne");
    seriesEditionDialogChecker
      .deleteSavingsSeriesWithConfirmationAndCancel()
      .cancel();
    seriesEditionDialogChecker.cancel();
  }

  public void testCategorisationOnSavingCreation() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", -100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    mainAccounts.edit(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setAsSavings()
      .validate();

    categorization.selectTransaction("Virement Epargne")
      .selectSavings()
      .createSeries()
      .setName("Epargne")
      .checkFromContentEquals("Account n. 111222")
      .checkToContentEquals("External account", "Main accounts", OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .validate();

    categorization.selectTransaction("Virement Epargne")
      .checkSavingsSeriesIsSelected("Epargne");
  }

  public void testCanNotChooseTheSameAccount() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", -100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    mainAccounts.edit(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setAsSavings()
      .validate();

    SeriesEditionDialogChecker editionDialogChecker = categorization.selectAllTransactions()
      .selectSavings()
      .createSeries();
    editionDialogChecker
      .setName("Epargne")
      .setToAccount("Main accounts")
      .setFromAccount("Main accounts")
      .checkSavingsMessageVisibility(true)
      .checkOkEnabled(false);
    editionDialogChecker
      .setFromAccount("Account n. 111222")
      .checkSavingsMessageVisibility(false)
      .checkOkEnabled(true);
    editionDialogChecker
      .setToAccount("Account n. 111222")
      .checkSavingsMessageVisibility(true)
      .checkOkEnabled(false)
      .cancel();
  }

  public void testDeleteSeriesForBothImportedAccountWithTransactionInFrom() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", -100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    mainAccounts.edit(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setAsSavings()
      .validate();

    categorization.selectTransaction("Virement vers Epargne")
      .selectSavings()
      .createSeries()
      .setName("Epargne")
      .setFromAccount(OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .setToAccount("Account n. 111222")
      .validate();

    categorization
      .selectTransaction("Virement vers Epargne")
      .editSeries("Epargne")
      .deleteSavingsSeriesWithConfirmation();

    categorization.checkBudgetAreaSelectionPanelDisplayed();
  }

  public void testCheckComboAccountContentsWithNotImportedAccount() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", 100.00, "Virement de l'Epargne")
      .load();

    SeriesEditionDialogChecker editionDialogChecker = categorization.selectAllTransactions()
      .selectSavings()
      .createSeries()
      .setName("Financement");
    editionDialogChecker.createAccount().setAccountName("CODEVI")
      .selectBank("CIC")
      .setPosition(0).validate();
    editionDialogChecker
      .checkToContentEquals("Main accounts")
      .checkToAccount("Main accounts")
      .setFromAccount("CODEVI")
      .checkFromContentEquals("External account", "CODEVI")
      .validate();

    SeriesEditionDialogChecker seriesEdition = categorization.selectTransaction("Virement de l'Epargne")
      .editSeries("Financement");
    seriesEdition
      .createAccount()
      .setAccountName("Livret A")
      .selectBank("CIC")
      .setPosition(0)
      .validate();
    seriesEdition.checkFromContentEquals("External account", "CODEVI", "Livret A")
      .setFromAccount("Livret A")
      .validate();

    categorization.selectAllTransactions()
      .selectSavings()
      .selectSeries("Financement");

    views.selectBudget();
    budgetView.savings.alignAndPropagate("Financement");
    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initContent()
      .add("01/10/2008", TransactionType.PLANNED, "Planned: Financement", "", -100.00, "Financement")
      .add("01/10/2008", TransactionType.PLANNED, "Planned: Financement", "", 100.00, "Financement")
      .add("01/09/2008", TransactionType.PLANNED, "Planned: Financement", "", -100.00, "Financement")
      .add("01/09/2008", TransactionType.PLANNED, "Planned: Financement", "", 100.00, "Financement")
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Financement", "", -100.00, "Financement")
      .add("01/08/2008", TransactionType.PLANNED, "Planned: Financement", "", 100.00, "Financement")
      .add("01/07/2008", TransactionType.PLANNED, "Planned: Financement", "", -100.00, "Financement")
      .add("01/07/2008", TransactionType.PLANNED, "Planned: Financement", "", 100.00, "Financement")
      .add("06/06/2008", TransactionType.PRELEVEMENT, "VIREMENT DE L'EPARGNE", "", -100.00, "Financement")
      .add("06/06/2008", TransactionType.VIREMENT, "VIREMENT DE L'EPARGNE", "", 100.00, "Financement")
      .check();

  }

  public void testDeleteSavingsAccount() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", 100.00, "Virement Epargne")
      .load();

    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .load();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    categorization.selectTransaction("Virement vers Epargne")
      .selectSavings()
      .createSeries()
      .setName("Epargne")
      .setFromAccount("Main accounts")
      .setToAccount("Account n. 111222")
      .validate();

    categorization.selectTransaction("Virement Epargne")
      .selectSavings()
      .selectSeries("Epargne");

    transactions.initContent()
      .add("06/06/2008", TransactionType.PRELEVEMENT, "VIREMENT VERS EPARGNE", "", -100.00, "Epargne")
      .add("06/06/2008", TransactionType.VIREMENT, "VIREMENT EPARGNE", "", 100.00, "Epargne")
      .check();

    savingsAccounts.edit("Account n. 111222").delete().validate();
    transactions.initContent()
      .add("06/06/2008", TransactionType.PRELEVEMENT, "VIREMENT VERS EPARGNE", "", -100.00)
      .check();
  }

  public void testChangePlannedFromBudgetView() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/08/06", -100.00, "Virement vers Epargne")
      .load();

    savingsAccounts.createSavingsAccount("ING", 1000.);
    views.selectBudget();
    budgetView.getSummary().checkEndPosition(0);
    budgetView.savings.createSeries().setName("Main to Savings")
      .setFromAccount("Main accounts")
      .setToAccount("External account")
      .validate();
    budgetView.savings.editPlannedAmount("Main to Savings").setPropagationEnabled().setAmountAndValidate("500");
    budgetView.savings.editSeries("Main to Savings")
      .selectMonth(200808)
      .checkAmount("500.00")
      .cancel();
    budgetView.getSummary().checkEndPosition(-500.);
    timeline.selectMonth("2008/09");
    budgetView.getSummary().checkEndPosition(-1000.);
    timeline.selectMonth("2008/10");
    budgetView.getSummary().checkEndPosition(-1500.);

    timeline.selectMonth("2008/08");
    budgetView.savings.createSeries().setName("Savings to Main")
      .setToAccount("Main accounts")
      .setFromAccount("External account")
      .validate();
    budgetView.savings.editPlannedAmount("Savings to Main").setPropagationEnabled().setAmountAndValidate("500");
    budgetView.savings.editSeries("Savings to Main")
      .selectMonth(200808)
      .checkAmount("500.00")
      .cancel();

    budgetView.getSummary().checkEndPosition(0.);

    timeline.selectMonth("2008/09");
    budgetView.getSummary().checkEndPosition(0.);
    timeline.selectMonth("2008/10");
    budgetView.getSummary().checkEndPosition(0.);
  }

  public void testChange() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
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

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();
    views.selectBudget();

    budgetView.savings.createSeries()
      .setName("Placement")
      .setFromAccount("Main accounts")
      .setToAccount("Account n. 111222")
      .selectAllMonths()
      .setAmount("100")
      .validate();

    categorization
      .setSavings("Virement vers Epargne", "Placement");

    categorization
      .setSavings("Virement Epargne", "Placement");

    views.selectBudget();
    budgetView.savings.editPlannedAmount("Placement")
      .setAmount("200")
      .validate();
    budgetView.savings.editSeries("Placement")
      .checkAmount("200.00")
      .cancel();
  }

  public void testAutomaticalyCreateSeriesAtSavingCreationWithOtherSavingAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .addTransaction("2008/06/06", 100.00, "Virement de Epargne")
      .load();

    categorization.selectTransaction("Virement vers Epargne")
      .selectSavings()
      .createSavingsAccount().setAccountName("suisse account")
      .selectBank("CIC").validate();

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", 100.00, "Virement de courant")
      .addTransaction("2008/06/06", -100.00, "Virement vers courant")
      .loadInNewAccount();

    mainAccounts.edit("Account n. 111222")
      .setAsSavings()
      .validate();

    views.selectBudget();
    budgetView.savings.checkSeriesPresent("To Account n. 111222");
    budgetView.savings.checkSeriesPresent("From Account n. 111222");
    categorization.selectTransaction("Virement de Epargne")
      .selectSavings()
      .selectSeries("From Account n. 111222")
      .editSeries("From Account n. 111222").alignPlannedAndActual().setPropagationEnabled().validate();
    categorization.selectTransaction("Virement vers Epargne")
      .selectSavings()
      .selectSeries("To Account n. 111222")
      .editSeries("To Account n. 111222").alignPlannedAndActual().setPropagationEnabled().validate();
    categorization.selectTransaction("Virement de courant")
      .selectSavings()
      .checkDoesNotContainSeries("suisse account")
      .selectSeries("To Account n. 111222")
      .editSeries("To Account n. 111222").alignPlannedAndActual().setPropagationEnabled().validate();
    categorization.selectTransaction("Virement vers courant")
      .selectSavings()
      .selectSeries("From Account n. 111222")
      .editSeries("From Account n. 111222").alignPlannedAndActual().setPropagationEnabled().validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("01/08/2008", "Planned: From Account n. 111222", -100.00, "From Account n. 111222", 3000.00, 3000.00, "Account n. 111222")
      .add("01/08/2008", "Planned: To Account n. 111222", 100.00, "To Account n. 111222", 3100.00, 3100.00, "Account n. 111222")
      .add("01/08/2008", "Planned: To Account n. 111222", -100.00, "To Account n. 111222", 0.0, "Main accounts")
      .add("01/08/2008", "Planned: From Account n. 111222", 100.00, "From Account n. 111222", 100., "Main accounts")
      .add("01/07/2008", "Planned: From Account n. 111222", -100.00, "From Account n. 111222", 3000.00, 3000.00, "Account n. 111222")
      .add("01/07/2008", "Planned: To Account n. 111222", 100.00, "To Account n. 111222", 3100.00, 3100.00, "Account n. 111222")
      .add("01/07/2008", "Planned: To Account n. 111222", -100.00, "To Account n. 111222", 0.00, "Main accounts")
      .add("01/07/2008", "Planned: From Account n. 111222", 100.00, "From Account n. 111222", 100.00, "Main accounts")
      .add("06/06/2008", "VIREMENT VERS COURANT", -100.00, "From Account n. 111222", 3000.00, 3000.00, "Account n. 111222")
      .add("06/06/2008", "VIREMENT DE COURANT", 100.00, "To Account n. 111222", 3100.00, 3100.00, "Account n. 111222")
      .add("06/06/2008", "VIREMENT DE EPARGNE", 100.00, "From Account n. 111222", 0.00, 0.00, "Account n. 00001123")
      .add("06/06/2008", "VIREMENT VERS EPARGNE", -100.00, "To Account n. 111222", -100.00, -100.00, "Account n. 00001123")
      .check();

    views.selectBudget();
    budgetView.savings.editPlannedAmount("To account n. 111222")
      .setAmount(150)
      .validate();
    timeline.selectMonth("2008/06");
    budgetView.savings.checkSeries("To account n. 111222", 100., 150.);
  }

  public void testImportOnMirrorAccount() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/06/06", -100.00, "Virement vers Epargne")
      .addTransaction("2008/06/06", 100.00, "Virement de Epargne")
      .load();

    savingsAccounts.createSavingsAccount("epargne", 100.);

    categorization.selectTransaction("Virement de Epargne")
      .selectSavings()
      .selectSeries("From Account epargne")
      .editSeries("From Account epargne").alignPlannedAndActual().setPropagationEnabled().validate();
    categorization.selectTransaction("Virement vers Epargne")
      .selectSavings()
      .selectSeries("To Account epargne")
      .editSeries("To Account epargne").alignPlannedAndActual().setPropagationEnabled().validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("01/08/2008", "Planned: From account epargne", -100.00, "From account epargne", 200.00, 200.00, "epargne")
      .add("01/08/2008", "Planned: To account epargne", 100.00, "To account epargne", 300.00, 300.00, "epargne")
      .add("01/08/2008", "Planned: To account epargne", -100.00, "To account epargne", 0.00, "Main accounts")
      .add("01/08/2008", "Planned: From account epargne", 100.00, "From account epargne", 100., "Main accounts")
      .add("01/07/2008", "Planned: From account epargne", -100.00, "From account epargne", 200.00, 200.00, "epargne")
      .add("01/07/2008", "Planned: To account epargne", 100.00, "To account epargne", 300.00, 300.00, "epargne")
      .add("01/07/2008", "Planned: To account epargne", -100.00, "To account epargne", 0.00, "Main accounts")
      .add("01/07/2008", "Planned: From account epargne", 100.00, "From account epargne", 100.00, "Main accounts")
      .add("06/06/2008", "VIREMENT VERS EPARGNE", 100.00, "To account epargne", 200.00, 200.00, "epargne")
      .add("06/06/2008", "VIREMENT DE EPARGNE", -100.00, "From account epargne", 100.00, 100.00, "epargne")
      .add("06/06/2008", "VIREMENT DE EPARGNE", 100.00, "From account epargne", 0.00, 0.00, "Account n. 00001123")
      .add("06/06/2008", "VIREMENT VERS EPARGNE", -100.00, "To account epargne", -100.00, -100.00, "Account n. 00001123")
      .check();

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/06/06", 100.00, "Virement de courant")
      .addTransaction("2008/06/06", -100.00, "Virement vers courant")
      .load("Account n. 111", "epargne");

    timeline.selectAll();
    transactions.initAmountContent()
      .add("01/08/2008", "Planned: From account epargne", -100.00, "From account epargne", 3000.00, 3000.00, "epargne")
      .add("01/08/2008", "Planned: To account epargne", 100.00, "To account epargne", 3100.00, 3100.00, "epargne")
      .add("01/08/2008", "Planned: To account epargne", -100.00, "To account epargne", 0.00, "Main accounts")
      .add("01/08/2008", "Planned: From account epargne", 100.00, "From account epargne", 100.00, "Main accounts")
      .add("01/07/2008", "Planned: From account epargne", -100.00, "From account epargne", 3000.00, 3000.00, "epargne")
      .add("01/07/2008", "Planned: To account epargne", 100.00, "To account epargne", 3100.00, 3100.00, "epargne")
      .add("01/07/2008", "Planned: To account epargne", -100.00, "To account epargne", 0.00, "Main accounts")
      .add("01/07/2008", "Planned: From account epargne", 100.00, "From account epargne", 100.00, "Main accounts")
      .add("06/06/2008", "Planned: From account epargne", -100.00, "From account epargne", 3000.00, 3000.00, "epargne")
      .add("06/06/2008", "Planned: To account epargne", 100.00, "To account epargne", 3100.00, 3100.00, "epargne")
      .add("06/06/2008", "VIREMENT VERS COURANT", -100.00, "To categorize", 3000.00, 3000.00, "epargne")
      .add("06/06/2008", "VIREMENT DE COURANT", 100.00, "To categorize", 3100.00, 3100.00, "epargne")
      .add("06/06/2008", "VIREMENT DE EPARGNE", 100.00, "From account epargne", 0.00, 0.00, "Account n. 00001123")
      .add("06/06/2008", "VIREMENT VERS EPARGNE", -100.00, "To account epargne", -100.00, -100.00, "Account n. 00001123")
      .check();

    categorization.selectTransaction("Virement de courant")
      .selectSavings()
      .selectSeries("To Account epargne");
    categorization.selectTransaction("Virement vers courant")
      .selectSavings()
      .selectSeries("From Account epargne");

    timeline.selectAll();
    transactions.initAmountContent()
      .add("01/08/2008", "Planned: From account epargne", -100.00, "From account epargne", 3000.00, 3000.00, "epargne")
      .add("01/08/2008", "Planned: To account epargne", 100.00, "To account epargne", 3100.00, 3100.00, "epargne")
      .add("01/08/2008", "Planned: To account epargne", -100.00, "To account epargne", 0.00, "Main accounts")
      .add("01/08/2008", "Planned: From account epargne", 100.00, "From account epargne", 100.00, "Main accounts")
      .add("01/07/2008", "Planned: From account epargne", -100.00, "From account epargne", 3000.00, 3000.00, "epargne")
      .add("01/07/2008", "Planned: To account epargne", 100.00, "To account epargne", 3100.00, 3100.00, "epargne")
      .add("01/07/2008", "Planned: To account epargne", -100.00, "To account epargne", 0.00, "Main accounts")
      .add("01/07/2008", "Planned: From account epargne", 100.00, "From account epargne", 100.00, "Main accounts")
      .add("06/06/2008", "VIREMENT VERS COURANT", -100.00, "From account epargne", 3000.00, 3000.00, "epargne")
      .add("06/06/2008", "VIREMENT DE COURANT", 100.00, "To account epargne", 3100.00, 3100.00, "epargne")
      .add("06/06/2008", "VIREMENT DE EPARGNE", 100.00, "From account epargne", 0.00, 0.00, "Account n. 00001123")
      .add("06/06/2008", "VIREMENT VERS EPARGNE", -100.00, "To account epargne", -100.00, -100.00, "Account n. 00001123")
      .check();
  }

  public void testImportOnSavingWithExternalSeries() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/08/06", -100.00, "ope")
      .load();

    savingsAccounts.createSavingsAccount("epargne", 100.);

    views.selectBudget();
    budgetView.savings.createSeries()
      .setName("CAF")
      .setFromAccount("External account")
      .setToAccount("epargne")
      .selectAllMonths()
      .setAmount(200)
      .validate();

    timeline.selectAll();
    transactions
      .showPlannedTransactions()
      .initAmountContent()
      .add("01/10/2008", "Planned: CAF", 200.00, "CAF", 500.00, 500.00, "epargne")
      .add("01/09/2008", "Planned: CAF", 200.00, "CAF", 300.00, 300.00, "epargne")
      .add("06/08/2008", "OPE", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("01/08/2008", "CAF", 200.00, "CAF", 100.00, 100.00, "epargne")
      .check();

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/07/06", 100.00, "Alloc")
      .addTransaction("2008/08/06", 100.00, "Alloc")
      .load("Account n. 111", "epargne");

    timeline.selectAll();
    transactions.initAmountContent()
      .add("08/10/2008", "Planned: CAF", 200.00, "CAF", 3600.00, 3600.00, "epargne")
      .add("08/09/2008", "Planned: CAF", 200.00, "CAF", 3400.00, 3400.00, "epargne")
      .add("08/08/2008", "Planned: CAF", 200.00, "CAF", 3200.00, 3200.00, "epargne")
      .add("06/08/2008", "ALLOC", 100.00, "To categorize", 3000.00, 3000.00, "epargne")
      .add("06/08/2008", "OPE", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .add("06/07/2008", "ALLOC", 100.00, "To categorize", 2900.00, 2900.00, "epargne")
      .check();
  }

  public void testImportOfxOnExistingAccountWithouAccountDoNotAskForAmount() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/08/06", -100.00, "ope")
      .load();

    savingsAccounts.createSavingsAccount("epargne", null);

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 111, "111222", 3000.00, "2008/08/10")
      .addTransaction("2008/07/06", 100.00, "Alloc")
      .addTransaction("2008/08/06", 100.00, "Alloc")
      .load("Account n. 111", "epargne");

    transactions.initContent()
      .add("06/08/2008", TransactionType.VIREMENT, "ALLOC", "", 100.00)
      .add("06/08/2008", TransactionType.PRELEVEMENT, "OPE", "", -100.00)
      .check();
  }

}
