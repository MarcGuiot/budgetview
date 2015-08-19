package org.designup.picsou.functests.initial;

import org.designup.picsou.functests.checkers.SeriesEditionDialogChecker;
import org.designup.picsou.functests.specificbanks.SpecificBankTestCase;
import org.designup.picsou.functests.checkers.SignpostDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.Series;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.xml.XmlChangeSetWriter;

import java.io.PrintWriter;
import java.util.Set;

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

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }

  protected void selectInitialView() {
    views.selectHome();
  }

  public void testCompleteSequence() throws Exception {
    addOns.activateAnalysis();

    views.selectHome();
    welcomeView.checkWelcomeViewShown();
    accounts.checkHidden();
    checkNoSignpostVisible();
    welcomeView.start();
    accounts.checkHidden();

    signpostView.checkSignpostViewShown();
    accounts.checkHidden();

    // === Import ===

    importPanel.checkImportSignpostDisplayed("Click here to import your operations");
    importPanel.openImport().close();
    checkNoSignpostVisible();

    OfxBuilder
      .init(this)
      .addTransaction("2010/05/27", -100, "rent")
      .addTransaction("2010/05/28", +500, "income")
      .addTransaction("2010/05/29", -100, "auchan")
      .addTransaction("2010/05/29", -100, "auchan")
      .addTransaction("2010/04/29", -100, "auchan")
      .addTransaction("2010/03/29", -100, "auchan")
      .addTransaction("2010/05/29", -100, "Chausse")
      .load();

    // === Categorization selection ===

    views.checkDataSelected();
    views.checkCategorizationSignpostVisible("Categorization");

    views.selectHome();
    accounts.checkHidden();
    views.checkCategorizationSignpostVisible("Categorization");

    views.selectCategorization();
    views.checkNoSignpostVisible();
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

    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Groceries", "Click on the planned amounts to set your own values");

    views.selectHome();
    accounts.checkHidden();

    // === Editing series amounts in SeriesEvolution does not remove budget view signpost ===

    views.selectAnalysis();
    analysis.table().editSeries("Groceries", "May 2010").validate();
    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Groceries", "Click on the planned amounts to set your own values");

    // === Series amounts ===

    budgetView.variable.checkPlannedUnsetButNotHighlighted("Health");
    budgetView.variable.checkPlannedUnsetButNotHighlighted("Fuel");
    budgetView.variable.checkPlannedUnsetAndHighlighted("Groceries");
    SeriesEditionDialogChecker amountDialog = budgetView.variable.editPlannedAmount("Groceries");
    checkNoSignpostVisible();
    amountDialog.cancel();

    // === Edit series amounts ===

    budgetView.variable.checkPlannedUnsetAndHighlighted("Groceries");
    budgetView.variable.editPlannedAmount("Groceries")
      .checkSelectedMonths(201003, 201004, 201005)
      .checkAmountEditionHighlighted()
      .setAmount(10.00)
      .validate();
    budgetView.variable.checkPlannedNotHighlighted("Groceries");
    budgetView.variable.editPlannedAmount("Groceries")
      .checkAmountEditionNotHighlighted()
      .checkAmount(10.00)
      .cancel();

    signpostView.checkSignpostViewShown();

    budgetView.variable.checkPlannedUnsetButNotHighlighted("Health");
    budgetView.variable.checkPlannedUnsetButNotHighlighted("Fuel");
    budgetView.variable.checkPlannedUnsetAndHighlighted("Clothing");
    SignpostDialogChecker
      .open(budgetView.variable.editPlannedAmount("Clothing").setAmount(10.00).triggerValidate())
      .close();

    views.checkBudgetSelected();
    views.selectHome();
    signpostView.checkDashboardViewShown();
    accounts.checkShown();

    // === Restart ===

    restartApplication();

    views.selectHome();
    checkNoSignpostVisible();
    signpostView.checkDashboardViewShown();

    views.selectBudget();
    checkNoSignpostVisible();

    views.selectCategorization();
    checkNoSignpostVisible();
  }

  public void testSeriesHighlightInBudgetPhase() throws Exception {
    welcomeView.start();
    signpostView.checkSignpostViewShown();

    // === Import ===

    views.selectData();
    importPanel.checkImportSignpostDisplayed("Click here to import your operations");
    importPanel.openImport().close();
    OfxBuilder
      .init(this)
      .addTransaction("2010/05/27", -100, "rent")
      .addTransaction("2010/05/28", +2500, "income")
      .addTransaction("2010/05/29", -200, "auchan")
      .addTransaction("2010/04/29", -200, "auchan")
      .addTransaction("2010/03/29", -200, "auchan")
      .addTransaction("2010/05/29", -100, "Chausse")
      .load();

    // === Categorization ===

    categorization.setNewRecurring("rent", "Rent"); // SED shown
    categorization.setIncome("income", "Income 1");
    categorization.setVariable("auchan", "Groceries");
    categorization.setVariable("Chausse", "Clothing");

    // === Series amounts ===

    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Groceries", "Click on the planned amounts to set your own values");

    budgetView.variable.checkPlannedUnsetButNotHighlighted("Health");
    budgetView.variable.checkPlannedUnsetButNotHighlighted("Fuel");

    // Nothing changes if we just open/close SED
    budgetView.variable.checkPlannedUnsetAndHighlighted("Groceries");
    budgetView.variable.editPlannedAmount("Groceries").validate();
    budgetView.variable.checkPlannedUnsetAndHighlighted("Groceries");

    // Go through SAED
    budgetView.variable.checkPlannedUnsetAndHighlighted("Groceries");
    budgetView.variable.editPlannedAmount("Groceries")
      .setPropagationDisabled()
      .selectMonth(201005)
      .setAmount(10.00)
      .validate();
    budgetView.variable.checkPlannedNotHighlighted("Groceries");

    // Go through SED
    budgetView.variable.checkPlannedUnsetAndHighlighted("Clothing");
    budgetView.variable.editSeries("Clothing")
      .setPropagationDisabled()
      .selectMonth(201005)
      .setAmount(10.00)
      .validate();
    budgetView.variable.checkPlannedNotHighlighted("Clothing");

    signpostView.checkDashboardViewShown();
  }

  public void testSeriesUnsetOnlyTakesCurrentSelectionIntoAccount() throws Exception {

    welcomeView.start();
    views.selectData();
    importPanel.checkImportSignpostDisplayed("Click here to import your operations");
    importPanel.openImport().close();
    OfxBuilder
      .init(this)
      .addTransaction("2010/05/28", +500, "income")
      .addTransaction("2010/05/29", -100, "auchan")
      .addTransaction("2010/04/29", -100, "auchan")
      .addTransaction("2010/04/29", -100, "vroum")
      .load();

    categorization.setIncome("income", "Income 1");
    categorization.setVariable("auchan", "Groceries");

    categorization.selectTransactions("vroum");
    SignpostDialogChecker
      .open(categorization.selectVariable().createSeries().setName("Car").triggerValidate())
      .close();
    categorization.editSeries("Car").setEndDate(201004).validate();

    timeline.selectMonth(201005);

    signpostView.checkSignpostViewShown();

    SignpostDialogChecker
      .open(budgetView.variable.editPlannedAmount("Groceries")
              .checkAmountEditionHighlighted()
              .setPropagationEnabled()
              .setAmount(50.00)
              .triggerValidate())
      .close();
    budgetView.variable.checkPlannedNotHighlighted("Groceries");

    signpostView.checkDashboardViewShown();
  }

  public void testSkipCategorization() throws Exception {
    welcomeView.start();
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
    views.selectCategorization();
    categorization.checkSkipMessageHidden();
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

    SeriesEditionDialogChecker amountDialog = budgetView.variable.editPlannedAmount("Groceries");
    checkNoSignpostVisible();
    amountDialog.cancel();

    signpostView.checkSignpostViewShown();
    views.selectBudget();
    SignpostDialogChecker
      .open(
        budgetView.variable.editPlannedAmount("Groceries").setAmount(10.00).triggerValidate())
      .close();

    views.checkBudgetSelected();
    views.selectHome();
    signpostView.checkDashboardViewShown();

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
      "Misc", "Click on the planned amounts to set your own values");
    views.selectHome();
    accounts.checkHidden();

    // === Restart ===
    restartApplication();

    // === Amount signpost is shown on restart ===
    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Misc", "Click on the planned amounts to set your own values");
    views.selectHome();
    accounts.checkHidden();
  }

  public void testRestartDuringBudgetTuning() throws Exception {
    welcomeView.start();
    signpostView.checkSignpostViewShown();

    // === Import ===

    views.selectData();
    importPanel.checkImportSignpostDisplayed("Click here to import your operations");
    importPanel.openImport().close();
    OfxBuilder
      .init(this)
      .addTransaction("2010/05/27", -100, "rent")
      .addTransaction("2010/05/28", +500, "income")
      .addTransaction("2010/05/29", -100, "auchan")
      .addTransaction("2010/05/29", -100, "auchan")
      .addTransaction("2010/04/29", -100, "auchan")
      .addTransaction("2010/03/29", -100, "auchan")
      .addTransaction("2010/05/29", -100, "Chausse")
      .load();

    // === Categorization ===

    categorization.setNewRecurring("rent", "Rent"); // SED shown
    categorization.setIncome("income", "Income 1");
    categorization.setVariable("auchan", "Groceries");
    categorization.setVariable("Chausse", "Clothing");

    // === Series amounts ===

    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Groceries", "Click on the planned amounts to set your own values");

    budgetView.variable.checkPlannedUnsetButNotHighlighted("Health");
    budgetView.variable.checkPlannedUnsetButNotHighlighted("Fuel");

    budgetView.variable.checkPlannedUnsetAndHighlighted("Groceries");
    budgetView.variable.editPlannedAmount("Groceries")
      .checkSelectedMonths(201003, 201004, 201005)
      .setAmount(10.00)
      .validate();
    budgetView.variable.checkPlannedNotHighlighted("Groceries");
    budgetView.variable.checkPlannedUnsetAndHighlighted("Clothing");

    signpostView.checkSignpostViewShown();

    // === Restart ===

    restartApplication();

    signpostView.checkSignpostViewShown();

    views.selectBudget();
    budgetView.variable.checkPlannedNotHighlighted("Groceries");
    budgetView.variable.checkPlannedUnsetAndHighlighted("Clothing");
    SignpostDialogChecker
      .open(budgetView.variable.editPlannedAmount("Clothing")
              .checkSelectedMonths(201003, 201004, 201005)
              .setAmount(10.00)
              .triggerValidate())
      .close();

    views.checkBudgetSelected();
    views.selectHome();
    signpostView.checkDashboardViewShown();
    checkNoSignpostVisible();

    views.selectBudget();
    checkNoSignpostVisible();

    views.selectCategorization();
    checkNoSignpostVisible();
  }

  public void testSignpostsNotShownAfterRestore() throws Exception {
    welcomeView.start();
    importPanel.checkImportSignpostDisplayed("Click here to import your operations");
    importPanel.openImport().close();
    OfxBuilder
      .init(this)
      .addTransaction("2010/05/27", -100, "auchan")
      .load();
    categorization.setNewRecurring("auchan", "Groceries");
    views.selectBudget();
    budgetView.recurring.editPlannedAmount("Groceries")
      .setPropagationDisabled()
      .selectMonth(201005)
      .setAmount(10.00)
      .validate();
    budgetView.variable.checkPlannedNotHighlighted("Groceries");
    signpostView.checkDashboardViewShown();

    setDeleteLocalPrevayler(true);
    String backupFilePath = operations.backup(this);

    restartApplication(true);

    operations.restore(backupFilePath);
    signpostView.checkDashboardViewShown();
  }

  public void testImportMoneyWithOneUncategorized() throws Exception {
    welcomeView.start();
    signpostView.checkSignpostViewShown();

    operations.openImportDialog()
      .setFilePath(SpecificBankTestCase.getFile("money_export_standard.qif", this))
      .acceptFile()
      .createNewAccount("CIC", "Main account", "", 0.)
      .setMainAccount()
      .importSeries()
      .checkContains("Loisirs-culture-sport:Journaux",
                     "Auto-moto:Remboursement de pret auto-moto", "Alimentation:Epicerie", "Auto-moto:Essence")
      .setRecurring("Alimentation:Epicerie", "Auto-moto:Remboursement de pret auto-moto")
      .setVariable("Loisirs-culture-sport:Journaux", "Loisirs-culture-sport", "Auto-moto:Essence")
      .unset("[Test]")
      .validateAndFinishImport();

    views.selectCategorization();

    categorization.checkFirstCategorizationSignpostDisplayed("Select the operations to categorize");

    categorization.selectTableRow(0);

    categorization.checkSkipMessageDisplayed();
  }

  public void testImportMoneyWithAllCategorised() throws Exception {
    welcomeView.start();
    signpostView.checkSignpostViewShown();

    operations.openImportDialog()
      .setFilePath(SpecificBankTestCase.getFile("money_export_standard.qif", this))
      .acceptFile()
      .createNewAccount("CIC", "Main account", "", 0.)
      .setMainAccount()
      .importSeries()
      .checkContains("Loisirs-culture-sport:Journaux",
                     "Auto-moto:Remboursement de pret auto-moto", "Alimentation:Epicerie", "Auto-moto:Essence")
      .setRecurring("Alimentation:Epicerie", "Auto-moto:Remboursement de pret auto-moto")
      .setVariable("Loisirs-culture-sport:Journaux", "Loisirs-culture-sport", "Auto-moto:Essence", "[Test]")
      .validateAndFinishImport();

    views.selectCategorization();

    categorization.checkFirstCategorizationSignpostDisplayed("Select the transactions to see it's categorization or categorize it");
  }

  public void testSwitchingToTheDemoAccountAndBackPreservesSignposts() throws Exception {

    demoMessage.checkHidden();
    addOns.activateAnalysis();

    // === Goto data ===

    gotoDemoAccountAndBack();

    welcomeView.checkWelcomeViewShown();
    welcomeView.start();
    signpostView.checkSignpostViewShown();
    demoMessage.checkHidden();

    // === Import file ===

    views.selectData();
    importPanel.checkImportSignpostDisplayed("Click here to import your operations");
    gotoDemoAccountAndBack();
    views.selectData();
    importPanel.checkImportSignpostDisplayed("Click here to import your operations");

    OfxBuilder
      .init(this)
      .addTransaction("2010/05/29", -100, "rent")
      .addTransaction("2010/05/29", -100, "auchan")
      .load();

    // === Categorization selection ===

    views.checkCategorizationSignpostVisible("Categorization");
    gotoDemoAccountAndBack(false);
    views.checkCategorizationSignpostVisible("Categorization");

    views.selectCategorization();
    categorization.selectTableRow(0);

    // === Back to the categorization ===

    categorization.checkAreaSelectionSignpostDisplayed("Select the budget area for this operation");
    gotoDemoAccountAndBack();
    views.selectCategorization();
    categorization.checkTableContains("AUCHAN");
    categorization.selectTableRow(0);
    categorization.checkAreaSelectionSignpostDisplayed("Select the budget area for this operation");

    categorization.selectVariable();
    checkNoSignpostVisible();

    // === Categorization completion ===

    categorization.setNewRecurring("rent", "Rent"); // SED shown

    categorization.checkFirstCategorizationSignpostDisplayed("The operation is categorized, continue");
    gotoDemoAccountAndBack();
    views.selectCategorization();
    categorization.checkFirstCategorizationSignpostDisplayed("The operation is categorized, continue");
    categorization.setVariable("auchan", "Groceries");

    categorization.checkGotoBudgetSignpostShown();
    gotoDemoAccountAndBack();
    categorization.checkGotoBudgetSignpostShown();

    // === Editing series amounts in SeriesEvolution does not remove budget view signpost ===

    views.selectAnalysis();
    analysis.table().editSeries("Groceries", "May 2010").validate();

    // === Series amounts ===

    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Groceries", "Click on the planned amounts to set your own values");
    gotoDemoAccountAndBack();
    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Groceries", "Click on the planned amounts to set your own values");

    budgetView.variable.checkPlannedUnsetButNotHighlighted("Health");
    budgetView.variable.checkPlannedUnsetButNotHighlighted("Fuel");
    budgetView.variable.checkPlannedUnsetAndHighlighted("Groceries");
    SeriesEditionDialogChecker amountDialog = budgetView.variable.editPlannedAmount("Groceries");
    checkNoSignpostVisible();
    amountDialog.cancel();

    // === Edit series amounts ===

    signpostView.checkSignpostViewShown();

    views.selectBudget();
    SignpostDialogChecker
      .open(budgetView.variable.editPlannedAmount("Groceries").setAmount(10.00).triggerValidate())
      .close();

    views.checkBudgetSelected();
    views.selectHome();
    signpostView.checkDashboardViewShown();
  }

  public void testStartEnteringTransactionsManually() throws Exception {
    welcomeView.start();
    signpostView.checkSignpostViewShown();

    operations.openImportDialog()
      .getBankDownload()
      .selectCountry("France")
      .selectBank("CIC")
      .enterTransactionsManually();

    transactionCreation.checkSignpostShown("Click here to enter transactions");

    transactionCreation
      .clickAndOpenAccountCreationMessage()
      .setName("Cash")
      .setAccountNumber("012345")
      .selectBank("CIC")
      .validate();

    transactionCreation
      .setAmount(10.00)
      .setDay(10)
      .setLabel("Mc do")
      .create();

    categorization.checkAreaSelectionSignpostDisplayed("Select the budget area for this operation");

    categorization.selectVariable();

    categorization.getVariable().selectSeries("Groceries");

    categorization.checkSkipMessageDisplayed();

    transactionCreation
      .setAmount(10.00)
      .setDay(10)
      .setLabel("Quick")
      .create();
    categorization.setVariable("QUICK", "Groceries");

    categorization.checkSkipMessageDisplayed();

    categorization.skipAndCloseSignpostDialog();

    categorization.checkSkipAndGotoBudgetSignpostShown();

    views.selectBudget();
    budgetView.variable.checkAmountSignpostDisplayed(
      "Groceries", "Click on the planned amounts to set your own values");

    SeriesEditionDialogChecker amountDialog = budgetView.variable.editPlannedAmount("Groceries");
    checkNoSignpostVisible();
    amountDialog.cancel();

    signpostView.checkSignpostViewShown();
    views.selectBudget();
    SignpostDialogChecker
      .open(
        budgetView.variable.editPlannedAmount("Groceries").setAmount(10.00).triggerValidate())
      .close();

    views.checkBudgetSelected();
    views.selectHome();
    signpostView.checkDashboardViewShown();

    views.selectBudget();
    checkNoSignpostVisible();

    views.selectCategorization();
    checkNoSignpostVisible();
}

  private void gotoDemoAccountAndBack() {
    gotoDemoAccountAndBack(true);
  }

  private void gotoDemoAccountAndBack(boolean checkData) {
    welcomeView.gotoDemoAccount();
    signpostView.checkDashboardViewShown();
    demoMessage.checkVisible();
    checkNoSignpostVisible();
    if (checkData) {
      transactions.checkNotEmpty();
      categorization.checkTableContains("WORLDCO SALARY");
    }
    demoMessage.exit();
    demoMessage.checkHidden();
  }
}
