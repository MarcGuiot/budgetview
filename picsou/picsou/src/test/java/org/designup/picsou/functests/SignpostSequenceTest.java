package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.SeriesAmountEditionDialogChecker;
import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.checkers.SignpostDialogChecker;
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

    signpostView.checkInnerHelpLink("integrated help", "Index");
    signpostView.checkSupportSiteLink("online guides", "http://support.mybudgetview.fr");
    signpostView.checkFeedbackLink("contact us");

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
    categorization.checkGotoBudgetSignpostShown();

    // === Editing series amounts in SeriesEvolution does not remove budget view signpost ===

    views.selectAnalysis();
    seriesAnalysis.editSeries("Groceries", "May 2010").validate();

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

    views.selectBudget();
    SignpostDialogChecker
      .open(
        budgetView.variable.editPlannedAmount("Clothing").setAmount(10.00).triggerValidate())
      .close();

    views.checkHomeSelected();
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

  public void testSkipCategorization() throws Exception {
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
      .addTransaction("2010/05/29", -100, "shoes")
      .load();

    // === Categorization selection ===

    views.checkDataSelected();
    views.checkCategorizationSignpostVisible("Categorization");
    categorization.selectTableRow(0);
    categorization.checkAreaSelectionSignpostDisplayed("Select the budget area for this operation");
    categorization.selectVariable();
    checkNoSignpostVisible();

    categorization.checkSkipMessageHidden();

    // === Categorization ===

    categorization.setNewRecurring("rent", "Rent"); // SED shown
    categorization.checkFirstCategorizationSignpostDisplayed("The operation is categorized, continue");

    categorization.selectTransaction("auchan");
    categorization.checkSkipMessageDisplayed();

    categorization.setVariable("auchan", "Groceries");

    // === Skip ===

    categorization.skipAndCloseSignpostDialog();
    categorization.checkSkipAndGotoBudgetSignpostShown();

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

    signpostView.checkSignpostViewShown();
    views.selectBudget();
    SignpostDialogChecker
      .open(
        budgetView.variable.editPlannedAmount("Groceries").setAmount(10.00).triggerValidate())
      .close();

    views.checkHomeSelected();
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
