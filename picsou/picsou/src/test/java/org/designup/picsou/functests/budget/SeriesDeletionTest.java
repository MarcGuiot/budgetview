package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.checkers.SeriesDeletionDialogChecker;
import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

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
      handler.process(seriesDialog.openDeleteDialog());
      seriesDialog.checkClosed();
    }

    public void transferVariable(String sourceSeries, String targetSeries) {
      budgetView.variable.editSeries(sourceSeries)
        .openDeleteDialog()
        .selectTransferSeries(targetSeries)
        .transfer();
    }

    public void setEndDate(String seriesName, String month) {
      SeriesEditionDialogChecker seriesDialog = budgetView.variable.editSeries(seriesName);
      seriesDialog.openDeleteDialog()
        .checkEndDateMessageContains(month)
        .setEndDate();
      seriesDialog
        .checkVisible()
        .checkNoStartDate()
        .checkEndDate(month)
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

  public void testDeleteNewlyCreatedSeriesFromDialog() throws Exception {
    views.selectBudget();
    budgetView.income.createSeries()
      .setName("AA")
      .deleteCurrentSeries();
    budgetView.income.checkNoSeriesShown();
  }

  public void testDeleteNewlyCreatedSeriesFromPopup() throws Exception {
    views.selectBudget();
    budgetView.income.createSeries()
      .setName("AA")
      .validate();
    budgetView.income.deleteSeries("AA");
    budgetView.income.checkNoSeriesShown();
  }

  public void testDeleteUsedSeriesFromDialog() throws Exception {
    deleteUsedSeries(DIALOG);
  }

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

    views.selectBudget();
    access.uncategorizeVariable("AA");

    budgetView.variable.checkSeriesNotPresent("AA");
    categorization.initContent()
      .add("30/06/2008", "", "FORFAIT KRO", -60.00)
      .check();
  }

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
  }

  public void testDeleteSavingsInManual() throws Exception {
    mainAccounts.createNewAccount().setName("Main")
      .setAsMain()
      .setPosition(1000)
      .selectBank(SOCIETE_GENERALE)
      .validate();

    savingsAccounts.createNewAccount().setName("Savings")
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
      .selectSavings().createSeries()
      .setName("Savings Series")
      .setFromAccount("Savings")
      .setToAccount("Main")
      .validate();

    categorization.selectTransactions("Financement")
      .selectSavings()
      .editSeries("Savings Series")
      .deleteSavingsSeriesWithConfirmation();

    // il reste des SeriesBudget miroir sans la serie principale
    // NPE sur recalcul de PeriodStat.
    String name = operations.backup(this);
    operations.restore(name);
    timeline.selectAll();
  }

  public void testTransferFromDialog() throws Exception {
    transfer(DIALOG);
  }

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

    savingsAccounts.createSavingsAccount("Livret", 1000.00);
    budgetView.savings.createSeries()
      .setName("Epargne")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Livret")
      .validate();

    access.processDeleteVariable("Drinks", new DeleteHandler() {
      public void process(SeriesDeletionDialogChecker deletionDialog) {
        deletionDialog
          .checkMessage("Drinks")
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
      .checkStartDate("Feb 2008")
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
  }

  public void testTransferFromSubseriesFromDialog() throws Exception {
    transferFromSubseries(DIALOG);
  }

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
      .gotoSubSeriesTab()
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
  }

  public void testSetEndDateFromDialog() throws Exception {
    doSetEndDate(DIALOG);
  }

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
      .checkEndDate("May 2008")
      .validate();
  }

  public void testTransferOnlyIfSameAccount() throws Exception {
    mainAccounts.createMainAccount("her account", 10);
    OfxBuilder.init(this)
      .addTransaction("2010/12/01", 100.00, "Auchan")
      .loadInAccount("her account");
    mainAccounts.createMainAccount("his account", 10);

    budgetView.variable.createSeries("courses", "her account");
    budgetView.variable.createSeries("courses sans compte");
    categorization.setVariable("Auchan", "courses");
    budgetView.variable.createSeries("his courses", "his account");
    budgetView.variable.openDeleteSeries("courses")
    .checkTransferSeries("courses sans compte").cancel();
    budgetView.variable.createSeries("her courses", "her account");
    budgetView.variable.openDeleteSeries("courses")
      .checkTransferSeries("courses sans compte", "her courses")
      .selectTransferSeries("courses sans compte").transfer();
    budgetView.variable.editSeries("courses sans compte").checkTargetAccount("her account");
  }
}
