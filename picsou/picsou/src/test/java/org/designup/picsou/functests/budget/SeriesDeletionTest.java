package org.designup.picsou.functests.budget;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;

public class SeriesDeletionTest extends LoggedInFunctionalTestCase {
  public void testDeleteNewlyCreatedSeries() throws Exception {
    views.selectBudget();
    budgetView.income.createSeries()
      .setName("AA")
      .deleteCurrentSeries();
    budgetView.income.checkNoSeriesShown();
  }

  public void testDeleteUsedSeries() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    views.selectCategorization();
    categorization.selectTableRow(0);
    categorization.selectVariable();
    categorization.selectVariable().createSeries()
      .setName("AA")
      .validate();
    categorization.setVariable("Forfait Kro", "AA");

    views.selectBudget();
    budgetView.variable.editSeries("AA")
      .deleteCurrentSeriesWithConfirmation("AA");

    budgetView.variable.checkSeriesNotPresent("AA");
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

  public void testTransfer() throws Exception {
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
      .setFromAccount("Main accounts")
      .setToAccount("Livret")
      .validate();

    SeriesEditionDialogChecker seriesDialog = budgetView.variable.editSeries("Drinks");
    seriesDialog.openDeleteDialog()
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
    seriesDialog.checkClosed();

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

    budgetView.variable.editSeries("Health")
      .openDeleteDialog()
      .selectTransferSeries("Misc")
      .transfer();

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

  public void testTransferFromSubseries() throws Exception {
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

    categorization.getVariable().editSeries("Drinks")
      .openDeleteDialog()
      .selectTransferSeries("Health")
      .transfer();

    categorization.checkTable(new Object[][]{
      {"20/05/2008", "Health", "FORFAIT KRO", -30.0}
    });
  }

  public void testSetEndDate() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60.00, "Something else")
      .addTransaction("2008/05/20", -30.00, "Forfait Kro")
      .addTransaction("2008/04/20", -30.00, "Forfait Kro")
      .addTransaction("2008/03/20", -30.00, "Forfait Kro")
      .load();

    categorization.setNewVariable("Forfait Kro", "Drinks");

    SeriesEditionDialogChecker seriesDialog = budgetView.variable.editSeries("Drinks");
    seriesDialog.openDeleteDialog()
      .checkEndDateMessageContains("May 2008")
      .setEndDate();
    seriesDialog
      .checkVisible()
      .checkNoStartDate()
      .checkEndDate("May 2008")
      .validate();
  }
}
