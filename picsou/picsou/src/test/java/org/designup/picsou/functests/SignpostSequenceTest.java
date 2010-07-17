package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.PositionChecker;
import org.designup.picsou.functests.checkers.SeriesAmountEditionDialogChecker;
import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class SignpostSequenceTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    createDefaultSeries = true;
    resetWindow();
    setCurrentDate("2010/05/31");
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  public void testCompleteSequence() throws Exception {

    // === Import ===

    views.selectHome();
    actions.checkImportSignpostDisplayed("Click here to import your operations");

    views.selectCategorization();
    checkNoSignpostVisible();

    views.selectHome();
    actions.checkImportSignpostDisplayed("Click here to import your operations");

    actions.openImport().close();
    checkNoSignpostVisible();

    OfxBuilder
      .init(this)
      .addTransaction("2010/05/27", -100, "rent")
      .addTransaction("2010/05/28", +500, "income")
      .addTransaction("2010/05/29", -10, "auchan")
      .load();

    // === Categorization selection ===

    views.checkCategorizationSelected();
    categorization.checkSelectionSignpostDisplayed("Select the operations to categorize");
    categorization.selectTableRow(0);

    // === Amount signpost is already visible in the budget view ===

    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Groceries", "Click on the planned amounts to set your own values");

    // === Back to the categorization ===

    views.selectCategorization();
    categorization.checkAreaSelectionSignpostDisplayed("Select the budget area for this operation");

    categorization.selectVariable();
    checkNoSignpostVisible();

    // === Categorization completion ===

    categorization.setNewRecurring("rent", "Rent"); // SED shown
    categorization.setIncome("income", "Income 1");
    categorization.setVariable("auchan", "Groceries");
    categorization.checkCompleteProgressMessageShown();

    // === Editing series amounts in SeriesEvolution does not remove budget view signpost ===

    views.selectEvolution();
    seriesEvolution.editSeries("Groceries", "May 10").validate();

    // === Series amounts ===

    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Groceries", "Click on the planned amounts to set your own values");

    SeriesAmountEditionDialogChecker amountDialog = budgetView.variable.editPlannedAmount("Groceries");
    checkNoSignpostVisible();
    amountDialog.cancel();

    // === Gauge details ===

    budgetView.variable.checkGaugeSignpostDisplayed(
      "Groceries",
      "Click on the gauges for a description");
    budgetView.variable.clickGaugeAndCloseTip("Groceries");

    // === Series periodicity ===

    budgetView.recurring.checkNameSignpostDisplayed(
      "Electricity",
      "Click on the envelope names to change their periodicity " +
      "(for instance once every two months)");

    SeriesEditionDialogChecker editionDialog = budgetView.recurring.editSeries("Electricity");
    checkNoSignpostVisible();

    editionDialog.cancel();

    budgetView.getSummary().checkPositionSignpostDisplayed();

    PositionChecker positionDialog = budgetView.getSummary().openPositionDialog();
    checkNoSignpostVisible();

    positionDialog.close();
    checkNoSignpostVisible();
    
    // === Restart ===

    restartApplication();

    views.selectHome();
    checkNoSignpostVisible();

    views.selectBudget();
    checkNoSignpostVisible();

    views.selectCategorization();
    checkNoSignpostVisible();
  }

  public void testRestartDuringCategorization() throws Exception {

    OfxBuilder
      .init(this)
      .addTransaction("2010/05/27", -100, "rent")
      .addTransaction("2010/05/28", +500, "income")
      .addTransaction("2010/05/29", -10, "auchan")
      .load();

    views.checkCategorizationSelected();
    categorization.selectTableRow(0);
    categorization.selectVariable();

    // === Amount signpost is shown in the budget view ===
    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Groceries", "Click on the planned amounts to set your own values");

    // === Restart ===
    restartApplication();

    // === Amount signpost is shown on restart ===
    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Groceries", "Click on the planned amounts to set your own values");
  }
}
