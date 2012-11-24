package org.designup.picsou.functests.categorization;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

public class ReconciliationTest extends LoggedInFunctionalTestCase {

  public void setUp() throws Exception {
    setCurrentDate("2012/05/15");
    super.setUp();
  }

  public void testImportingTransactionsInAManualAccount() throws Exception {

    mainAccounts.createNewAccount()
      .setName("Account 1")
      .selectBank("CIC")
      .setAccountNumber("00123")
      .setPosition(100.00)
      .validate();
    mainAccounts.createNewAccount()
      .setName("Account 2")
      .selectBank("CIC")
      .setAccountNumber("00234")
      .setPosition(200.00)
      .validate();

    transactionCreation.show();
    transactionCreation
      .selectAccount("Account 1")
      .createToBeReconciled(1, "FNAC", -100.00)
      .createToBeReconciled(12, "Auchan", -50.00);
    categorization.initContent()
      .add("12/05/2012", "", "[R] AUCHAN", -50.00)
      .add("01/05/2012", "", "[R] FNAC", -100.00)
      .check();
    budgetView.variable.createSeries()
      .setName("Groceries")
      .gotoSubSeriesTab()
      .addSubSeries("Misc")
      .validate();
    categorization.setVariable("[R] AUCHAN", "Groceries", "Misc");

    setCurrentDate("2012/05/28");
    operations.changeDate();
    OfxBuilder.init(this)
      .addBankAccount("00123", 1000.00, "2012/05/15")
      .addTransaction("2012/05/12", -50.00, "VELIZY AUCHAN")
      .addTransaction("2012/05/15", -100.00, "CARREFOUR")
      .addTransaction("2012/05/12", -25.00, "VELIZY AUCHAN")
      .loadInAccount("Account 1");
    OfxBuilder.init(this)
      .addBankAccount("00234", 2000.00, "2012/05/15")
      .addTransaction("2012/04/12", -75.00, "FNAC PAIEMENTS")
      .loadInAccount("Account 2");
    categorization.setNewVariable("VELIZY AUCHAN", "Food", 200.00);
    categorization.initContent()
      .add("15/05/2012", "Groceries / Misc", "[R] AUCHAN", -50.00)
      .add("15/05/2012", "", "CARREFOUR", -100.00)
      .add("15/05/2012", "", "[R] FNAC", -100.00)
      .add("12/04/2012", "", "FNAC PAIEMENTS", -75.00)
      .add("12/05/2012", "Food", "VELIZY AUCHAN", -50.00)
      .add("12/05/2012", "Food", "VELIZY AUCHAN", -25.00)
      .check();

    categorization.selectTableRow(0);
    transactionDetails.setNote("A comment...");
    categorization
      .checkReconciliationShown()
      .getReconciliation()
      .initTable()
      .add("2012/05/12", "Food", "VELIZY AUCHAN", -50.00)
      .add("2012/05/12", "Food", "VELIZY AUCHAN", -25.00)
      .add("2012/05/15", "", "CARREFOUR", -100.00)
      .check();

    categorization.getReconciliation()
      .checkReconcileDisabled()
      .select("VELIZY AUCHAN")
      .reconcile();

    categorization.initContent()
      .add("15/05/2012", "", "CARREFOUR", -100.00)
      .add("15/05/2012", "", "[R] FNAC", -100.00)
      .add("12/04/2012", "", "FNAC PAIEMENTS", -75.00)
      .add("12/05/2012", "Groceries / Misc", "VELIZY AUCHAN", -50.00)
      .add("12/05/2012", "Food", "VELIZY AUCHAN", -25.00)
      .check();

    categorization.selectTableRow(3);
    transactionDetails
      .checkLabel("VELIZY AUCHAN")
      .checkNote("A comment...");

    // Reconciled operations remain reconciled after new import
    OfxBuilder.init(this)
      .addBankAccount("00123", 1000.00, "2012/05/20")
      .addTransaction("2012/05/20", -75.00, "VELIZY AUCHAN")
      .loadInAccount("Account 1");
    categorization.initContent()
      .add("15/05/2012", "", "CARREFOUR", -100.00)
      .add("20/05/2012", "", "[R] FNAC", -100.00)
      .add("12/04/2012", "", "FNAC PAIEMENTS", -75.00)
      .add("12/05/2012", "Groceries / Misc", "VELIZY AUCHAN", -50.00)
      .add("12/05/2012", "Food", "VELIZY AUCHAN", -25.00)
      .add("20/05/2012", "", "VELIZY AUCHAN", -75.00)
      .check();
  }

  public void testReconciliationWarningAndFilter() throws Exception {

    categorization.checkReconciliationWarningHidden();

    mainAccounts.createNewAccount()
      .setName("Account 1")
      .selectBank("CIC")
      .setAccountNumber("00123")
      .setPosition(100.00)
      .validate();

    categorization.checkReconciliationWarningHidden();

    OfxBuilder.init(this)
      .addBankAccount("00123", 1000.00, "2012/05/15")
      .addTransaction("2012/05/12", -50.00, "VELIZY AUCHAN")
      .addTransaction("2012/04/12", -25.00, "FNAC")
      .addTransaction("2012/03/15", -35.00, "FNAC")
      .loadInAccount("Account 1");

    categorization.checkReconciliationWarningHidden();

    transactionCreation
      .show()
      .selectAccount("Account 1")
      .createToBeReconciled(7, "AUCHAN", -25.00)
      .createToBeReconciled(8, "AUCHAN", -50.00)
      .selectMonth(201204)
      .createToBeReconciled(9, "AUCHAN", -75.00);

    categorization.checkReconciliationWarningShown("3 transactions to reconcile");

    categorization.initContent()
      .add("09/04/2012", "", "[R] AUCHAN", -75.00)
      .add("07/05/2012", "", "[R] AUCHAN", -25.00)
      .add("08/05/2012", "", "[R] AUCHAN", -50.00)
      .add("15/03/2012", "", "FNAC", -35.00)
      .add("12/04/2012", "", "FNAC", -25.00)
      .add("12/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();

    categorization.clickReconciliationWarningButton("3 transactions");
    categorization.checkShowsToReconcile();
    categorization.checkReconciliationWarningHidden();
    categorization.initContent()
      .add("09/04/2012", "", "[R] AUCHAN", -75.00)
      .add("07/05/2012", "", "[R] AUCHAN", -25.00)
      .add("08/05/2012", "", "[R] AUCHAN", -50.00)
      .check();

    timeline.selectMonth(201205);
    categorization.showSelectedMonthsOnly();
    categorization.checkReconciliationWarningShown("3 transactions to reconcile");
    categorization.initContent()
      .add("07/05/2012", "", "[R] AUCHAN", -25.00)
      .add("08/05/2012", "", "[R] AUCHAN", -50.00)
      .add("12/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();

    categorization.clickReconciliationWarningButton("3 transactions");
    categorization.checkShowsToReconcile();
    categorization.initContent()
      .add("09/04/2012", "", "[R] AUCHAN", -75.00)
      .add("07/05/2012", "", "[R] AUCHAN", -25.00)
      .add("08/05/2012", "", "[R] AUCHAN", -50.00)
      .check();
  }

  public void testManagingChecks() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("00123", 1000.00, "2012/05/01")
      .addTransaction("2012/05/01", -50.00, "VELIZY AUCHAN")
      .load();
    categorization.initContent()
      .add("01/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();

    mainAccounts.checkSummary(1000.00, "2012/05/01");
    mainAccounts.checkPosition("Account n. 00123", 1000);
    transactionCreation
      .show()
      .createToBeReconciled(20, "CHEQUE N° 12345", -100.00)
      .createToBeReconciled(20, "Auchan 1", -50.00);
    categorization.initContent()
      .add("20/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("20/05/2012", "", "[R] CHEQUE N° 12345", -100.00)
      .add("01/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();

    mainAccounts.checkPosition("Account n. 00123", 1000);
    mainAccounts.checkSummary(1000.00, "2012/05/01");

    setCurrentDate("2012/05/22");
    restartApplicationFromBackup();

    mainAccounts.checkPosition("Account n. 00123", 850);
    mainAccounts.checkSummary(850.00, "2012/05/20");

    OfxBuilder.init(this)
      .addBankAccount("00123", 1000.00, "2012/05/15")
      .addTransaction("2012/05/21", -100.00, "CHEQUE 00012345")
      .load();

    mainAccounts.checkPosition("Account n. 00123", 750);
    mainAccounts.checkSummary(750.00, "2012/05/21");

    categorization.initContent()
      .add("21/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("21/05/2012", "", "[R] CHEQUE N° 12345", -100.00)
      .add("21/05/2012", "", "CHEQUE N°00012345", -100.00)
      .add("01/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();

    categorization
      .selectTransaction("[R] CHEQUE N° 12345")
      .switchToReconciliation();

    categorization.getReconciliation().initTable()
      .add("2012/05/21", "", "CHEQUE N°00012345", -100.00)
      .add("2012/05/01", "", "VELIZY AUCHAN", -50.00)
      .check();

    categorization.getReconciliation().select("CHEQUE N°00012345").reconcile();

    mainAccounts.checkPosition("Account n. 00123", 850);
    mainAccounts.checkSummary(850.00, "2012/05/21");

    categorization.initContent()
      .add("21/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("21/05/2012", "", "CHEQUE N°00012345", -100.00)
      .add("01/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();
  }

  public void testManualOperationAreNotTakenInAccountWithImport() throws Exception {
    mainAccounts.createMainAccount("Main", 1000.00);

    transactionCreation
      .show()
      .createToBeReconciled(10, "CHEQUE 12345", -100.00)
      .createToBeReconciled(10, "AUCHAN 1", -50.00);

    OfxBuilder.init(this)
      .addTransaction("2012/05/10", -100.00, "CHEQUE 12345")
      .addTransaction("2012/05/10", -50.00, "AUCHAN 1")
      .loadInAccount("Main");

    categorization.initContent()
      .add("10/05/2012", "", "AUCHAN 1", -50.00)
      .add("10/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("10/05/2012", "", "[R] CHEQUE 12345", -100.00)
      .add("10/05/2012", "", "CHEQUE N°12345", -100.00)
      .check();
    views.selectData();
    transactions.initAmountContent()
      .add("10/05/2012", "AUCHAN 1", -50.00, "To categorize", 700.00, 700.00, "Main")
      .add("10/05/2012", "CHEQUE 12345", -100.00, "To categorize", 750.00, 750.00, "Main")
      .add("10/05/2012", "AUCHAN 1", -50.00, "To categorize", 850.00, 850.00, "Main")
      .add("10/05/2012", "CHEQUE N°12345", -100.00, "To categorize", 900.00, 900.00, "Main")
      .check();
  }

  public void testKeepingAManualTransaction() throws Exception {

    mainAccounts.createMainAccount("Main", 1000.00);

    transactionCreation
      .show()
      .createToBeReconciled(10, "CHEQUE N° 12345", -100.00)
      .createToBeReconciled(10, "Auchan 1", -50.00);
    categorization.initContent()
      .add("10/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("10/05/2012", "", "[R] CHEQUE N° 12345", -100.00)
      .check();

    mainAccounts.checkPosition("Main", 850);
    mainAccounts.checkSummary(850.00, "2012/05/10");

    notifications.checkHidden();

    OfxBuilder.init(this)
      .addTransaction("2012/05/10", -100.00, "CHEQUE 0012345")
      .addTransaction("2012/05/11", -50.00, "AUCHAN 1")
      .loadInAccount("Main");

    transactions.initAmountContent()
      .add("11/05/2012", "AUCHAN 1", -50.00, "To categorize", 700.00, 700.00, "Main")
      .add("11/05/2012", "CHEQUE N° 12345", -100.00, "To categorize", 750.00, 750.00, "Main")
      .add("11/05/2012", "AUCHAN 1", -50.00, "To categorize", 850.00, 850.00, "Main")
      .add("10/05/2012", "CHEQUE N°0012345", -100.00, "To categorize", 900.00, 900.00, "Main")
      .check();
    categorization.initContent()
      .add("11/05/2012", "", "AUCHAN 1", -50.00)
      .add("11/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("11/05/2012", "", "[R] CHEQUE N° 12345", -100.00)
      .add("10/05/2012", "", "CHEQUE N°0012345", -100.00)
      .check();

    mainAccounts.checkPosition("Main", 700);
    mainAccounts.checkSummary(700.00, "2012/05/11");
    notifications.checkVisible(1)
      .openDialog()
      .checkMessageCount(1)
      .checkMessage(0, "The last computed position for 'Main' (700.00) is not the same as the " +
                       "imported one (0.00)")
      .validate();

    categorization.selectTransaction("[R] CHEQUE N° 12345");

    categorization.switchToReconciliation().keepManualTransaction();
    categorization.selectTransaction("[R] AUCHAN 1")
      .switchToReconciliation().select("AUCHAN 1").reconcile();

    transactions.initAmountContent()
      .add("11/05/2012", "AUCHAN 1", -50.00, "To categorize", 750.00, 750.00, "Main")
      .add("11/05/2012", "CHEQUE N° 12345", -100.00, "To categorize", 800.00, 800.00, "Main")
      .add("10/05/2012", "CHEQUE N°0012345", -100.00, "To categorize", 900.00, 900.00, "Main")
      .check();

    categorization.initContent()
      .add("11/05/2012", "", "AUCHAN 1", -50.00)
      .add("11/05/2012", "", "CHEQUE N° 12345", -100.00)
      .add("10/05/2012", "", "CHEQUE N°0012345", -100.00)
      .check();

    mainAccounts.checkPosition("Main", 750);
    mainAccounts.checkSummary(750.00, "2012/05/11");

    // à l'import suivant la transaction n'est plus marquée toReconcile
    OfxBuilder.init(this)
      .addTransaction("2012/05/10", -100.00, "CHEQUE 0012345")
      .addTransaction("2012/05/15", -100.00, "FNAC")
      .loadInAccount("Main");

    categorization.initContent()
      .add("11/05/2012", "", "AUCHAN 1", -50.00)
      .add("11/05/2012", "", "CHEQUE N° 12345", -100.00)
      .add("10/05/2012", "", "CHEQUE N°0012345", -100.00)
      .add("15/05/2012", "", "FNAC", -100.00)
      .check();
  }

  public void testNavigation() throws Exception {
    mainAccounts.createMainAccount("Main", 1000.00);

    categorization.checkReconciliationSwitchLinksHidden();

    transactionCreation
      .show()
      .createToBeReconciled(10, "Manual 1", -100.00)
      .createToBeReconciled(10, "ToReconcile", -50.00);
    categorization.initContent()
      .add("10/05/2012", "", "[R] MANUAL 1", -100.00)
      .add("10/05/2012", "", "[R] TORECONCILE", -50.00)
      .check();

    OfxBuilder.init(this)
      .addTransaction("2012/05/10", -100.00, "ToReconcile-Imported")
      .loadInAccount("Main");
    categorization.initContent()
      .add("10/05/2012", "", "[R] MANUAL 1", -100.00)
      .add("10/05/2012", "", "[R] TORECONCILE", -50.00)
      .add("10/05/2012", "", "TORECONCILE-IMPORTED", -100.00)
      .check();

    categorization.unselectAllTransactions();
    categorization.checkReconciliationSwitchLinksHidden();

    categorization.selectTableRow(0)
      .checkCategorizationShown()
      .setNewVariable(0, "Misc");

    categorization.initContent()
      .add("10/05/2012", "Misc", "[R] MANUAL 1", -100.00)
      .add("10/05/2012", "", "[R] TORECONCILE", -50.00)
      .add("10/05/2012", "", "TORECONCILE-IMPORTED", -100.00)
      .check();

    categorization.selectTableRow(1)
      .checkCategorizationShown()
      .checkSwitchToReconciliationLinkShown()
      .switchToReconciliation();

    categorization.checkReconciliationShown()
      .checkSwitchToCategorizationLinkShown()
      .switchToCategorization()
      .checkCategorizationShown();

    categorization.selectTableRow(0)
      .checkReconciliationShown()
      .checkSwitchToCategorizationLinkShown()
      .switchToCategorization();

    categorization.checkCategorizationShown()
      .checkSwitchToReconciliationLinkShown()
      .switchToReconciliation();

    categorization.checkReconciliationShown();
  }

  public void testReconciliationWithASplitTransaction() throws Exception {
    mainAccounts.createMainAccount("Main", 1000.00);

    transactionCreation
      .show()
      .createToBeReconciled(10, "split 1", -50.00)
      .createToBeReconciled(10, "split 2", -40.00);

    OfxBuilder.init(this)
      .addTransaction("2012/05/10", -100.00, "one splitted op")
      .loadInAccount("Main");
    views.selectCategorization();
    categorization.selectTransaction("ONE SPLITTED OP");

    transactionDetails.split("-50", "split 1");
    transactionDetails.split("-40", "split 2");

    categorization.reconcile("[R] SPLIT 1", "split 1 : ONE SPLITTED OP");
    categorization.reconcile("[R] SPLIT 2", "split 2 : ONE SPLITTED OP");
    transactions.initAmountContent()
      .add("10/05/2012", "ONE SPLITTED OP", -10.00, "To categorize", 900.00, 900.00, "Main")
      .add("10/05/2012", "split 2 : ONE SPLITTED OP", -40.00, "To categorize", 910.00, 910.00, "Main")
      .add("10/05/2012", "split 1 : ONE SPLITTED OP", -50.00, "To categorize", 950.00, 950.00, "Main")
      .check();
  }

  public void testAtImportManualOperationAreShift() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    mainAccounts.createMainAccount("Main", 1000.00);

    transactionCreation
      .show()
      .createToBeReconciled(20, "op 1", -50.00)
      .createToBeReconciled(12, "op 2", -40.00);

    OfxBuilder.init(this)
      .addTransaction("2012/05/14", -10.00, "import op 1")
      .loadInAccount("Main");

    transactions.initAmountContent()
      .add("20/05/2012", "OP 1", -50.00, "To categorize", 900.00, 900.00, "Main")
      .add("14/05/2012", "OP 2", -40.00, "To categorize", 950.00, 950.00, "Main")
      .add("14/05/2012", "IMPORT OP 1", -10.00, "To categorize", 990.00, 990.00, "Main")
      .check();

    setCurrentDate("2012/05/22");
    operations.changeDate();
    OfxBuilder.init(this)
      .addTransaction("2012/05/21", -10.00, "import op 3")
      .loadInAccount("Main");

    transactions.initAmountContent()
      .add("21/05/2012", "OP 2", -40.00, "To categorize", 890.00, 890.00, "Main")
      .add("21/05/2012", "OP 1", -50.00, "To categorize", 930.00, 930.00, "Main")
      .add("21/05/2012", "IMPORT OP 3", -10.00, "To categorize", 980.00, 980.00, "Main")
      .add("14/05/2012", "IMPORT OP 1", -10.00, "To categorize", 990.00, 990.00, "Main")
      .check();

    setCurrentDate("2012/06/03");
    operations.changeDate();
    OfxBuilder.init(this)
      .addTransaction("2012/06/02", -10.00, "import op 4")
      .loadInAccount("Main");

    transactions.initAmountContent()
      .add("02/06/2012", "OP 2", -40.00, "To categorize", 880.00, 880.00, "Main")
      .add("02/06/2012", "OP 1", -50.00, "To categorize", 920.00, 920.00, "Main")
      .add("02/06/2012", "IMPORT OP 4", -10.00, "To categorize", 970.00, 970.00, "Main")
      .check();
  }

  public void testReconciliationWithSavingsAccounts() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2012/03/10", -100.00, "Virement")
      .addTransaction("2012/04/10", -100.00, "Virement")
      .addTransaction("2012/05/10", -100.00, "Virement")
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
    timeline.selectMonth("2012/04");
    budgetView.savings.alignAndPropagate("Epargne");
    views.selectCategorization();
    transactionCreation
      .show()
      .selectAccount("Epargne LCL")
      .createToBeReconciled(20, "op 1", 50.00)
      .createToBeReconciled(12, "op 2", 40.00);
    categorization.selectTransaction("[R] op 1").switchToReconciliation().keepManualTransaction();
    categorization.selectTransaction("[R] op 2").switchToReconciliation().keepManualTransaction();
    categorization.setSavings("op 1", "Epargne");

    // est-ce vraiment ce qu'on attends (creation de l'operation miroir)
    transactions.initAmountContent()
      .add("20/04/2012", "OP 1", 50.00, "Epargne", 1290.00, 1290.00, "Epargne LCL")
      .add("20/04/2012", "OP 1", -50.00, "Epargne", 50.00, "Main accounts")
      .add("12/04/2012", "OP 2", 40.00, "To categorize", 1240.00, 1240.00, "Epargne LCL")
      .add("10/04/2012", "VIREMENT", 100.00, "Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("10/04/2012", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, "Account n. 00001123")
      .check();
  }

  public void testReconciliationWithAShiftedTransaction() throws Exception {
    fail("tbd - voir les impacts sur le shift");
  }

  public void testReconciledTransactionsAreAutomaticallyAnnotated() throws Exception {
    fail("tbd - les opérations réconciliées sont-elles automatiquement pointées ?");
  }

  public void testShiftManualOpNotPossible() throws Exception {
  }

  public void testManualOnly() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    mainAccounts.createMainAccount("Main", 1000.00);

    transactionCreation
      .show()
      .create(10, "real op 1", -10)
      .create(13, "real op 2", -20)
      .createToBeReconciled(20, "op 1", -50.00)
      .createToBeReconciled(12, "op 2", -40.00);

    transactions.initAmountContent()
      .add("20/05/2012", "OP 1", -50.00, "To categorize", 880.00, 880.00, "Main")
      .add("13/05/2012", "REAL OP 2", -20.00, "To categorize", 930.00, 930.00, "Main")
      .add("12/05/2012", "OP 2", -40.00, "To categorize", 950.00, 950.00, "Main")
      .add("10/05/2012", "REAL OP 1", -10.00, "To categorize", 990.00, 990.00, "Main")
      .check();

    setCurrentDate("2012/05/22");
    operations.changeDate();
    transactions.initAmountContent()
      .add("20/05/2012", "OP 1", -50.00, "To categorize", 880.00, 880.00, "Main")
      .add("13/05/2012", "OP 2", -40.00, "To categorize", 930.00, 930.00, "Main")
      .add("13/05/2012", "REAL OP 2", -20.00, "To categorize", 970.00, 970.00, "Main")
      .add("10/05/2012", "REAL OP 1", -10.00, "To categorize", 990.00, 990.00, "Main")
      .check();


    views.selectCategorization();
    transactionCreation
      .create(21, "real op 3", -50);

    categorization.selectTransaction("[R] OP 1")
      .switchToReconciliation()
      .select("REAL OP 3")
      .reconcile();

    transactions.initAmountContent()
      .add("21/05/2012", "OP 2", -40.00, "To categorize", 880.00, 880.00, "Main")
      .add("21/05/2012", "REAL OP 3", -50.00, "To categorize", 920.00, 920.00, "Main")
      .add("13/05/2012", "REAL OP 2", -20.00, "To categorize", 970.00, 970.00, "Main")
      .add("10/05/2012", "REAL OP 1", -10.00, "To categorize", 990.00, 990.00, "Main")
      .check();

  }
  
}
