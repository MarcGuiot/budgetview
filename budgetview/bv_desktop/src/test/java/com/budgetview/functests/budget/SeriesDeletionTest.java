package com.budgetview.functests.budget;

import com.budgetview.functests.checkers.SeriesDeletionDialogChecker;
import com.budgetview.functests.checkers.SeriesEditionDialogChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.BankEntity;
import com.budgetview.model.TransactionType;
import org.junit.Test;

public class SeriesDeletionTest extends LoggedInFunctionalTestCase {

  private interface Access {

    void uncategorizeVariable(String seriesName);

    void processDeleteVariable(String seriesName, DeleteHandler handler);

    void transferVariable(String sourceSeries, String targetSeries);

    void setEndDate(String seriesName, String month);
  }

  private interface DeleteHandler {
    void process(SeriesDeletionDialogChecker deletionDialog);
  }

  private final Access DIALOG = new Access() {
    public void uncategorizeVariable(String seriesName) {
      budgetView.variable.editSeries(seriesName)
        .deleteCurrentSeriesWithConfirmation(seriesName);
    }

    public void processDeleteVariable(String seriesName, DeleteHandler handler) {
      SeriesEditionDialogChecker seriesDialog = budgetView.variable.editSeries("Drinks");
      handler.process(seriesDialog.openDelete());
      seriesDialog.checkClosed();
    }

    public void transferVariable(String sourceSeries, String targetSeries) {
      budgetView.variable.editSeries(sourceSeries)
        .openDelete()
        .selectTransferSeries(targetSeries)
        .transfer();
    }

    public void setEndDate(String seriesName, String month) {
      SeriesEditionDialogChecker seriesDialog = budgetView.variable.editSeries(seriesName);
      seriesDialog.openDelete()
        .checkEndDateMessageContains(month)
        .setEndDate();
      seriesDialog
        .checkVisible()
        .checkNoStartDate()
        .checkEndDate(month.toLowerCase())
        .validate();
    }
  };

  private final Access POPUP = new Access() {
    public void uncategorizeVariable(String seriesName) {
      budgetView.variable.openDeleteSeries(seriesName).uncategorize();
    }

    public void processDeleteVariable(String seriesName, DeleteHandler handler) {
      handler.process(budgetView.variable.openDeleteSeries("Drinks"));
    }

    public void transferVariable(String sourceSeries, String targetSeries) {
      budgetView.variable.openDeleteSeries(sourceSeries)
        .selectTransferSeries(targetSeries)
        .transfer();
    }

    public void setEndDate(String seriesName, String month) {
      budgetView.variable.openDeleteSeries(seriesName)
        .checkEndDateMessageContains(month)
        .setEndDate();
    }
  };

  @Test
  public void testDeleteNewlyCreatedSeriesFromDialog() throws Exception {
    views.selectBudget();
    budgetView.income.createSeries()
      .setName("AA")
      .deleteCurrentSeries();
    budgetView.income.checkNoSeriesShown();
  }

  @Test
  public void testDeleteNewlyCreatedSeriesFromPopup() throws Exception {
    budgetView.income.createSeries()
      .setName("AA")
      .setAmount(500)
      .validate();
    budgetView.income.deleteSeries("AA");
    budgetView.income.checkNoSeriesShown();
  }

  @Test
  public void testDeleteUsedSeriesFromDialog() throws Exception {
    deleteUsedSeries(DIALOG);
  }

  @Test
  public void testDeleteUsedSeriesFromPopup() throws Exception {
    deleteUsedSeries(POPUP);
  }

  private void deleteUsedSeries(Access access) {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectVariable().createSeries()
      .setName("AA")
      .validate();
    categorization.setVariable("Forfait Kro", "AA");

    uncategorized.checkNotShown();

    views.selectBudget();
    access.uncategorizeVariable("AA");

    budgetView.variable.checkSeriesNotPresent("AA");
    uncategorized.checkAmountAndTransactions(60.00, "| 30/06/2008 |  | FORFAIT KRO | -60.00 |\n");
  }

  @Test
  public void testDeleteFromSingleSeriesEditionDialog() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    categorization.setNewVariable("Forfait Kro", "Drinks");
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "Drinks", "Forfait Kro", -60.0}
    });

    budgetView.variable.editSeries("Drinks").deleteCurrentSeriesWithConfirmationAndCancel().validate();
    budgetView.variable.checkSeriesPresent("Drinks");
    budgetView.variable.editSeries("Drinks").deleteCurrentSeriesWithConfirmation("Drinks");
    budgetView.variable.checkSeriesNotPresent("Drinks");

    views.selectCategorization();
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "", "Forfait Kro", -60.0}
    });

    views.selectBudget();
    budgetView.variable.createSeries()
      .setName("Empty")
      .validate();

    budgetView.variable.editSeries("Empty").deleteCurrentSeries();
    budgetView.variable.checkSeriesNotPresent("Empty");

    uncategorized.checkAmountAndTransactions(60.00, "| 30/06/2008 |  | FORFAIT KRO | -60.00 |\n");
  }

  @Test
  public void testDeleteTranferWithNoTransactions() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 1, "111", 1000.00, "2008/08/10")
      .addTransaction("2008/08/10", 100.00, "OP1")
      .load();
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 1, "222", 2000.00, "2008/08/10")
      .addTransaction("2008/08/10", 200.00, "OP2")
      .load();
    mainAccounts.edit("Account n. 222")
      .setAsSavings()
      .validate();

    budgetView.transfer.createSeries()
      .setName("111 to 222")
      .setFromAccount("Account n. 111")
      .setToAccount("Account n. 222")
      .setAmount(100.00)
      .validate();

    transactions.showPlannedTransactions();
    transactions.initAmountContent()
      .add("11/08/2008", "Planned: 111 to 222", 100.00, "111 to 222", 2100.00, 2100.00, "Account n. 222")
      .add("11/08/2008", "Planned: 111 to 222", -100.00, "111 to 222", 900.00, 900.00, "Account n. 111")
      .add("10/08/2008", "OP2", 200.00, "To categorize", 2000.00, 2000.00, "Account n. 222")
      .add("10/08/2008", "OP1", 100.00, "To categorize", 1000.00, 1000.00, "Account n. 111")
      .check();

    budgetView.transfer.deleteSeries("111 to 222");

    budgetView.transfer.checkNoSeriesShown();
    transactions.initAmountContent()
      .add("10/08/2008", "OP2", 200.00, "To categorize", 2000.00, 2000.00, "Account n. 222")
      .add("10/08/2008", "OP1", 100.00, "To categorize", 1000.00, 1000.00, "Account n. 111")
      .check();

    uncategorized.checkAmountAndTransactions(300.00, "| 10/08/2008 |  | OP1 | 100.00 |\n" +
                                                     "| 10/08/2008 |  | OP2 | 200.00 |\n");
  }

  @Test
  public void testDeleteSavingsInManual() throws Exception {
    accounts.createNewAccount().setName("Main")
      .setAsMain()
      .setPosition(1000)
      .selectBank(SOCIETE_GENERALE)
      .validate();

    accounts.createNewAccount()
      .setName("Savings")
      .selectBank(SOCIETE_GENERALE)
      .setAsSavings()
      .setPosition(1000)
      .validate();

    views.selectCategorization();
    transactionCreation.show()
      .selectAccount("Savings")
      .setAmount(-100)
      .setLabel("Financement")
      .setDay(2)
      .create();

    categorization.selectTransactions("Financement")
      .selectTransfers().createSeries()
      .setName("Savings Series")
      .setFromAccount("Savings")
      .setToAccount("Main")
      .validate();

    categorization.selectTransactions("Financement")
      .selectTransfers()
      .editSeries("Savings Series")
      .deleteSavingsSeriesWithConfirmation();

    // il reste des SeriesBudget miroir sans la serie principale
    // NPE sur recalcul de PeriodStat.
    String name = operations.backup(this);
    operations.restore(name);
    timeline.selectAll();
  }

  @Test
  public void testTransferFromDialog() throws Exception {
    transfer(DIALOG);
  }

  @Test
  public void testTransferFromPopup() throws Exception {
    transfer(POPUP);
  }

  public void transfer(Access access) throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60.00, "Forfait Kro")
      .addTransaction("2008/06/15", -30.00, "Kro")
      .addTransaction("2008/05/20", -30.00, "Forfait Kro")
      .addTransaction("2008/04/20", -30.00, "Forfait Kro")
      .addTransaction("2008/03/20", -30.00, "Forfait Kro")
      .addTransaction("2008/02/20", -30.00, "Forfait Kro")
      .load();

    categorization.setNewVariable("Forfait Kro", "Drinks");
    categorization.setVariable("Kro", "Drinks");
    budgetView.variable.createSeries("Health");
    budgetView.extras.createSeries("Misc");
    budgetView.variable.createSeries("Groceries");
    budgetView.income.createSeries("Salary");

    budgetView.variable.editSeries("Health")
      .setStartDate(200804)
      .setEndDate(200805)
      .validate();

    accounts.createSavingsAccount("Livret", 1000.00);
    budgetView.transfer.createSeries()
      .setName("Epargne")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Livret")
      .validate();

    access.processDeleteVariable("Drinks", new DeleteHandler() {
      public void process(SeriesDeletionDialogChecker deletionDialog) {
        deletionDialog
          .checkExistingTransactionsMessage("Drinks")
          .checkTransferDisabled()
          .checkTransferSeries("Groceries", "Health", "Misc", "Salary")
          .setTransferSeriesFilter("e")
          .checkTransferSeries("Groceries", "Health")
          .setTransferSeriesFilter("ea")
          .checkTransferSeries("Health")
          .setTransferSeriesFilter("")
          .checkTransferSeries("Groceries", "Health", "Misc", "Salary")
          .selectTransferSeries("Health")
          .checkTransferEnabled()
          .transfer();
      }
    });

    timeline.selectAll();
    transactions.initContent()
      .add("30/06/2008", TransactionType.PRELEVEMENT, "FORFAIT KRO", "", -60.00, "Health")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "KRO", "", -30.00, "Health")
      .add("20/05/2008", TransactionType.PRELEVEMENT, "FORFAIT KRO", "", -30.00, "Health")
      .add("20/04/2008", TransactionType.PRELEVEMENT, "FORFAIT KRO", "", -30.00, "Health")
      .add("20/03/2008", TransactionType.PRELEVEMENT, "FORFAIT KRO", "", -30.00, "Health")
      .add("20/02/2008", TransactionType.PRELEVEMENT, "FORFAIT KRO", "", -30.00, "Health")
      .check();

    budgetView.variable.editSeries("Health")
      .checkStartDate("february 2008")
      .checkNoEndDate()
      .validate();

    access.transferVariable("Health", "Misc");

    transactions.initContent()
      .add("30/06/2008", TransactionType.PRELEVEMENT, "FORFAIT KRO", "", -60.00, "Misc")
      .add("15/06/2008", TransactionType.PRELEVEMENT, "KRO", "", -30.00, "Misc")
      .add("20/05/2008", TransactionType.PRELEVEMENT, "FORFAIT KRO", "", -30.00, "Misc")
      .add("20/04/2008", TransactionType.PRELEVEMENT, "FORFAIT KRO", "", -30.00, "Misc")
      .add("20/03/2008", TransactionType.PRELEVEMENT, "FORFAIT KRO", "", -30.00, "Misc")
      .add("20/02/2008", TransactionType.PRELEVEMENT, "FORFAIT KRO", "", -30.00, "Misc")
      .check();

    budgetView.extras.editSeries("Misc")
      .checkNoStartDate()
      .checkNoEndDate()
      .validate();

    uncategorized.checkNotShown();
  }

  @Test
  public void testTransferFromSubseriesFromDialog() throws Exception {
    transferFromSubseries(DIALOG);
  }

  @Test
  public void testTransferFromSubseriesFromPopup() throws Exception {
    transferFromSubseries(POPUP);
  }

  private void transferFromSubseries(Access access) {
    OfxBuilder
      .init(this)
      .addTransaction("2008/05/20", -30.00, "Forfait Kro")
      .load();

    categorization.selectTransaction("Forfait Kro");
    categorization.selectVariable().createSeries()
      .setName("Drinks")
      .editSubSeries()
      .addSubSeries("Kro")
      .validate();
    categorization.setVariable("Forfait Kro", "Kro");

    budgetView.variable.createSeries("Health");

    categorization.checkTable(new Object[][]{
      {"20/05/2008", "Drinks / Kro", "FORFAIT KRO", -30.0}
    });

    access.transferVariable("Drinks", "Health");

    categorization.checkTable(new Object[][]{
      {"20/05/2008", "Health", "FORFAIT KRO", -30.0}
    });

    uncategorized.checkNotShown();
  }

  @Test
  public void testSetEndDateFromDialog() throws Exception {
    doSetEndDate(DIALOG);
  }

  @Test
  public void testSetEndDateFromPopup() throws Exception {
    doSetEndDate(POPUP);
  }

  private void doSetEndDate(Access access) throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60.00, "Something else")
      .addTransaction("2008/05/20", -30.00, "Forfait Kro")
      .addTransaction("2008/04/20", -30.00, "Forfait Kro")
      .addTransaction("2008/03/20", -30.00, "Forfait Kro")
      .load();

    categorization.setNewVariable("Forfait Kro", "Drinks");

    access.setEndDate("Drinks", "May 2008");

    timeline.selectMonth(200806);
    budgetView.variable.checkSeriesNotPresent("Drinks");
    timeline.selectMonth(200805);
    budgetView.variable.editSeries("Drinks")
      .checkEndDate("may 2008")
      .validate();

    uncategorized.checkNotShown();
  }

  @Test
  public void testTransferIsProposedForAllMainAccounts() throws Exception {
    accounts.createMainAccount("Main1", "4321", 10);
    OfxBuilder.init(this)
      .addBankAccount("4321", 10.00, "2010/12/01")
      .addTransaction("2010/12/01", 100.00, "Auchan")
      .loadInAccount("Main1");
    accounts.createMainAccount("Main2", "4321", 10);

    budgetView.variable.createSeries("SeriesA for Main1", "Main1");
    categorization.setVariable("Auchan", "SeriesA for Main1");

    budgetView.variable.createSeries("Series for all accounts");

    budgetView.variable.createSeries("Series for Main2", "Main2");

    budgetView.variable.openDeleteSeries("SeriesA for Main1")
      .checkTransferSeries("Series for Main2", "Series for all accounts")
      .cancel();

    budgetView.variable.createSeries("SeriesB for Main1", "Main1");
    budgetView.variable.openDeleteSeries("SeriesA for Main1")
      .checkTransferSeries("Series for Main2", "Series for all accounts", "SeriesB for Main1")
      .selectTransferSeries("Series for all accounts")
      .transfer();

    budgetView.variable.editSeries("Series for all accounts")
      .checkEditableTargetAccount("Main accounts")
      .cancel();
  }

  @Test
  public void testDeleteWithTransferSetsTargetAccountToAllMainAccountsIfNeeded() throws Exception {

    operations.openPreferences().setFutureMonthsCount(2).validate();

    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 1, "111", 1000.00, "2008/08/10")
      .addTransaction("2008/08/08", 100.00, "OP1A")
      .addTransaction("2008/08/08", 100.00, "OP1B")
      .load();
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 1, "222", 2000.00, "2008/08/10")
      .addTransaction("2008/08/08", 200.00, "OP2")
      .load();
    OfxBuilder.init(this)
      .addBankAccount(BankEntity.GENERIC_BANK_ENTITY_ID, 1, "333", 3000.00, "2008/08/10")
      .addTransaction("2008/08/08", 200.00, "OP3")
      .load();
    mainAccounts.edit("Account n. 333")
      .setAsSavings()
      .validate();

    categorization.setNewVariable("OP1A", "Series1A", 100.00, "Account n. 111");
    categorization.setNewVariable("OP1B", "Series1B", 150.00, "Account n. 111");
    categorization.setNewVariable("OP2", "Series2", 200.00, "Account n. 222");

    timeline.selectMonths(200808, 200809);
    transactions.showPlannedTransactions();
    transactions.initAmountContent()
      .add("11/09/2008", "Planned: Series2", 200.00, "Series2", 2200.00, 3500.00, "Account n. 222")
      .add("11/09/2008", "Planned: Series1A", 100.00, "Series1A", 1300.00, 3300.00, "Account n. 111")
      .add("11/09/2008", "Planned: Series1B", 150.00, "Series1B", 1200.00, 3200.00, "Account n. 111")
      .add("11/08/2008", "Planned: Series1B", 50.00, "Series1B", 1050.00, 3050.00, "Account n. 111")
      .add("08/08/2008", "OP3", 200.00, "To categorize", 3000.00, 3000.00, "Account n. 333")
      .add("08/08/2008", "OP2", 200.00, "Series2", 2000.00, 3000.00, "Account n. 222")
      .add("08/08/2008", "OP1B", 100.00, "Series1B", 1000.00, 2800.00, "Account n. 111")
      .add("08/08/2008", "OP1A", 100.00, "Series1A", 900.00, 2700.00, "Account n. 111")
      .check();

    budgetView.variable.openDeleteSeries("Series1B")
      .checkTransferSeries("Series1A", "Series2")
      .selectTransferSeries("Series1A")
      .transfer();

    budgetView.variable.editSeries("Series1A")
      .checkEditableTargetAccount("Account n. 111")
      .validate();

    transactions.initAmountContent()
      .add("11/09/2008", "Planned: Series2", 200.00, "Series2", 2200.00, 3300.00, "Account n. 222")
      .add("11/09/2008", "Planned: Series1A", 100.00, "Series1A", 1100.00, 3100.00, "Account n. 111")
      .add("08/08/2008", "OP3", 200.00, "To categorize", 3000.00, 3000.00, "Account n. 333")
      .add("08/08/2008", "OP2", 200.00, "Series2", 2000.00, 3000.00, "Account n. 222")
      .add("08/08/2008", "OP1B", 100.00, "Series1A", 1000.00, 2800.00, "Account n. 111")
      .add("08/08/2008", "OP1A", 100.00, "Series1A", 900.00, 2700.00, "Account n. 111")
      .check();

    budgetView.variable.openDeleteSeries("Series2")
      .checkTransferSeries("Series1A")
      .selectTransferSeries("Series1A")
      .transfer();

    budgetView.variable.editSeries("Series1A")
      .checkReadOnlyTargetAccount("Main accounts")
      .validate();

    transactions.initAmountContent()
      .add("11/09/2008", "Planned: Series1A", 50.00, "Series1A", 2050.00, 3100.00, "Account n. 222")
      .add("11/09/2008", "Planned: Series1A", 50.00, "Series1A", 1050.00, 3050.00, "Account n. 111")
      .add("08/08/2008", "OP3", 200.00, "To categorize", 3000.00, 3000.00, "Account n. 333")
      .add("08/08/2008", "OP2", 200.00, "Series1A", 2000.00, 3000.00, "Account n. 222")
      .add("08/08/2008", "OP1B", 100.00, "Series1A", 1000.00, 2800.00, "Account n. 111")
      .add("08/08/2008", "OP1A", 100.00, "Series1A", 900.00, 2700.00, "Account n. 111")
      .check();

    uncategorized.checkAmountAndTransactions(200.00,
                                             "| 08/08/2008 |  | OP3 | 200.00 |\n");
  }
}
