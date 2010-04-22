package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.checkers.SeriesDeleteDialogChecker;

public class SeriesDeletionTest extends LoggedInFunctionalTestCase {
    public void testDeleteNewlyCreatedSeries() throws Exception {
    views.selectBudget();
    budgetView.income.createSeries()
      .setName("AA")
      .deleteSelectedSeries()
      .checkSeriesListIsEmpty()
      .validate();
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
    SeriesEditionDialogChecker edition = budgetView.variable.editSeriesList();

    SeriesDeleteDialogChecker deleteDialog = edition
      .selectSeries("AA")
      .deleteSelectedSeriesWithConfirmation();

    deleteDialog
      .checkMessage()
      .validate();

    edition.checkSeriesListIsEmpty();
    edition.validate();
    budgetView.variable.checkSeriesNotPresent("AA");
  }

  public void testDeleteFromSingleSeriesEditionDialog() throws Exception {
    OfxBuilder
      .init(this)
      .addTransaction("2008/06/30", -60, "Forfait Kro")
      .load();

    views.selectCategorization();
    categorization.setNewVariable("Forfait Kro", "Drinks");
    categorization.checkTable(new Object[][]{
      {"30/06/2008", "Drinks", "Forfait Kro", -60.0}
    });

    views.selectBudget();
    budgetView.variable.editSeries("Drinks").deleteCurrentSeriesWithConfirmationAndCancel().validate();
    budgetView.variable.checkSeriesPresent("Drinks");
    budgetView.variable.editSeries("Drinks").deleteCurrentSeriesWithConfirmation();
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

    categorization.selectTransactions("Financement")
      .selectSavings()
      .editSeries("Savings Series")
      .deleteCurrentSeriesWithConfirmation();

    // il reste des SeriesBudget miroir sans la serie principale
    // NPE sur recalcul de PeriodStat.
    String name = operations.backup(this);
    operations.restore(name);
    timeline.selectAll();
  }
}
