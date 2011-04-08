package org.designup.picsou.functests;

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
    setInitialGuidesShown(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  protected void selectInitialView() {
    views.selectHome();
  }

  public void testCompleteSequence() throws Exception {

    signpostView.checkSignpostViewShown();

    // === Import ===

    views.checkDataSignpostVisible();

    views.selectData();
    importPanel.checkImportSignpostDisplayed("Click here to import your operations");

    views.selectCategorization();
    checkNoSignpostVisible();

    views.selectData();
    importPanel.checkImportSignpostDisplayed("Click here to import your operations");

    importPanel.openImport().close();
    checkNoSignpostVisible();

    OfxBuilder
      .init(this)
      .addTransaction("2010/05/27", -100, "rent")
      .addTransaction("2010/05/28", +500, "income")
      .addTransaction("2010/05/29", -100, "auchan")
      .addTransaction("2010/05/29", -100, "auchan")
      .addTransaction("2010/05/29", -100, "Chausse")
      .load();

    // === Categorization selection ===

    views.checkDataSelected();
    views.checkCategorizationSignpostVisible("Categorization");

    views.selectCategorization();
//    categorization.checkSelectionSignpostDisplayed("Select the operations to categorize");
    categorization.selectTableRow(0);

    // === Back to the categorization ===

    categorization.checkAreaSelectionSignpostDisplayed("Select the budget area for this operation");
    categorization.selectVariable();
    checkNoSignpostVisible();

    // === Categorization completion ===

    categorization.setNewRecurring("rent", "Rent"); // SED shown

    categorization.checkFirstCategorizationSignpostDisplayed("The operation is categorized, continue");
    categorization.setIncome("income", "Income 1");

    checkNoSignpostVisible();
    categorization.setVariable("auchan", "Groceries");
    categorization.setVariable("Chausse", "Clothing");
    categorization.checkCompleteProgressMessageShown();

    // === Editing series amounts in SeriesEvolution does not remove budget view signpost ===

    views.selectEvolution();
    seriesEvolution.editSeries("Groceries", "May 2010").validate();

    // === Series amounts ===

    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Groceries", "Click on the planned amounts to set your own values");

    SeriesAmountEditionDialogChecker amountDialog = budgetView.variable.editPlannedAmount("Groceries");
    checkNoSignpostVisible();
    amountDialog.cancel();

    // === Series periodicity ===

    budgetView.recurring.checkNameSignpostDisplayed(
      "Electricity",
      "Click on the envelope names to change their periodicity " +
      "(for instance once every two months)");

    SeriesEditionDialogChecker editionDialog = budgetView.recurring.editSeries("Electricity");
    checkNoSignpostVisible();
    editionDialog.cancel();

    checkNoSignpostVisible();

    budgetView.variable.editPlannedAmount("Groceries").setAmount(10.00).validate();

    signpostView.checkSignpostViewShown();

    budgetView.variable.editPlannedAmount("Clothing").setAmount(10.00).validate();

    signpostView.checkSummaryViewShown();

    // === Restart ===

    restartApplication();

    views.selectHome();
    checkNoSignpostVisible();
    signpostView.checkSummaryViewShown();

    views.selectBudget();
    checkNoSignpostVisible();

    views.selectCategorization();
    checkNoSignpostVisible();
  }

  public void testRestartDuringCategorization() throws Exception {

    views.selectData();
    OfxBuilder
      .init(this)
      .addTransaction("2010/05/27", -100, "rent")
      .addTransaction("2010/05/28", +500, "income")
      .addTransaction("2010/05/29", -10, "auchan")
      .load();

    views.selectCategorization();
    categorization.selectAllTransactions();
    categorization.selectVariable().selectNewSeries("Misc");

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
