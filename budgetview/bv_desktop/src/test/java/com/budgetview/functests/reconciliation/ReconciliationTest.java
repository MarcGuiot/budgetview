package com.budgetview.functests.reconciliation;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import org.junit.Test;

public class ReconciliationTest extends LoggedInFunctionalTestCase {

  public void setUp() throws Exception {
    setCurrentDate("2012/05/15");
    super.setUp();
  }

  @Test
  public void testImportingTransactionsInAManualAccount() throws Exception {

    accounts.createNewAccount()
      .setName("Account 1")
      .selectBank("CIC")
      .setAccountNumber("00123")
      .setPosition(100.00)
      .validate();
    accounts.createNewAccount()
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
      .editSubSeries()
      .addSubSeries("Misc")
      .validate();
    categorization.setVariable("[R] AUCHAN", "Groceries", "Misc");
    categorization.checkCategorizationShown();

    categorization.selectTransaction("[R] FNAC");
    categorization.checkCategorizationShown();

    categorization.selectTransaction("[R] AUCHAN");
    categorization.checkReconciliationShown();

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
      .add("12/05/2012", "Groceries / Misc", "[R] AUCHAN", -50.00)
      .add("15/05/2012", "", "CARREFOUR", -100.00)
      .add("01/05/2012", "", "[R] FNAC", -100.00)
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
      .add("12/05/2012", "Groceries / Misc", "AUCHAN", -50.00)
      .add("15/05/2012", "", "CARREFOUR", -100.00)
      .add("01/05/2012", "", "[R] FNAC", -100.00)
      .add("12/04/2012", "", "FNAC PAIEMENTS", -75.00)
      .add("12/05/2012", "Food", "VELIZY AUCHAN", -25.00)
      .check();

    categorization.selectTableRow(0);
    transactionDetails
      .checkLabel("AUCHAN")
      .checkNote("A comment...");

    // Reconciled operations remain reconciled after new import
    OfxBuilder.init(this)
      .addBankAccount("00123", 1000.00, "2012/05/20")
      .addTransaction("2012/05/20", -75.00, "VELIZY AUCHAN")
      .loadInAccount("Account 1");
    categorization.initContent()
      .add("12/05/2012", "Groceries / Misc", "AUCHAN", -50.00)
      .add("15/05/2012", "", "CARREFOUR", -100.00)
      .add("01/05/2012", "", "[R] FNAC", -100.00)
      .add("12/04/2012", "", "FNAC PAIEMENTS", -75.00)
      .add("12/05/2012", "Food", "VELIZY AUCHAN", -25.00)
      .add("20/05/2012", "Food", "VELIZY AUCHAN", -75.00)
      .check();
  }

  @Test
  public void testReconciliationWarningAndFilter() throws Exception {

    categorization.checkReconciliationWarningHidden();

    accounts.createNewAccount()
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

  @Test
  public void testManagingChecks() throws Exception {
    OfxBuilder.init(this)
      .addBankAccount("00123", 1000.00, "2012/05/01")
      .addTransaction("2012/05/01", -50.00, "VELIZY AUCHAN")
      .load();
    categorization.initContent()
      .add("01/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();

    mainAccounts.checkReferencePosition(1000.00, "2012/05/01");
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
    mainAccounts.checkReferencePosition(1000.00, "2012/05/01");

    setCurrentDate("2012/05/22");
    restartApplicationFromBackup();

    mainAccounts.checkPosition("Account n. 00123", 1000);
    mainAccounts.checkReferencePosition(1000.00, "2012/05/01");

    OfxBuilder.init(this)
      .addBankAccount("00123", 1000.00, "2012/05/15")
      .addTransaction("2012/05/21", -100.00, "CHEQUE 00012345")
      .load();

    mainAccounts.checkPosition("Account n. 00123", 900);
    mainAccounts.checkReferencePosition(900.00, "2012/05/21");

    categorization.initContent()
      .add("20/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("20/05/2012", "", "[R] CHEQUE N° 12345", -100.00)
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

    mainAccounts.checkPosition("Account n. 00123", 900);
    mainAccounts.checkReferencePosition(900.00, "2012/05/21");

    categorization.initContent()
      .add("20/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("21/05/2012", "", "CHEQUE N°00012345", -100.00)
      .add("01/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();
  }

  @Test
  public void testManualOperationAreNotTakenInAccountWithImport() throws Exception {
    accounts.createMainAccount("Main", "4321", 1000.00);

    transactionCreation
      .show()
      .createToBeReconciled(10, "CHEQUE 12345", -100.00)
      .createToBeReconciled(10, "AUCHAN 1", -50.00);

    OfxBuilder.init(this)
      .addBankAccount("4321", 1000.00, "2012/05/10")
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

  @Test
  public void testKeepingAManualTransaction() throws Exception {

    accounts.createMainAccount("Main", "4321", 1000.00);

    transactionCreation
      .show()
      .createToBeReconciled(10, "CHEQUE N° 12345", -100.00)
      .createToBeReconciled(10, "Auchan 1", -50.00);
    categorization.initContent()
      .add("10/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("10/05/2012", "", "[R] CHEQUE N° 12345", -100.00)
      .check();

    mainAccounts.checkPosition("Main", 1000.00);
    mainAccounts.checkReferencePosition(1000.00, "2012/05/01");

    notifications.checkHidden();

    OfxBuilder.init(this)
      .addBankAccount("4321", 0.00, "2012/05/11")
      .addTransaction("2012/05/10", -100.00, "CHEQUE 0012345")
      .addTransaction("2012/05/11", -50.00, "AUCHAN 1")
      .loadInAccount("Main");

    transactions.initAmountContent()
      .add("11/05/2012", "AUCHAN 1", -50.00, "To categorize", 850.00, 850.00, "Main")
      .add("10/05/2012", "AUCHAN 1", -50.00, "To categorize", 700.00, 700.00, "Main")
      .add("10/05/2012", "CHEQUE N° 12345", -100.00, "To categorize", 750.00, 750.00, "Main")
      .add("10/05/2012", "CHEQUE N°0012345", -100.00, "To categorize", 900.00, 900.00, "Main")
      .check();
    categorization.initContent()
      .add("10/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("11/05/2012", "", "AUCHAN 1", -50.00)
      .add("10/05/2012", "", "[R] CHEQUE N° 12345", -100.00)
      .add("10/05/2012", "", "CHEQUE N°0012345", -100.00)
      .check();

    mainAccounts.checkPosition("Main", 850);
    mainAccounts.checkReferencePosition(850.00, "2012/05/11");
    notifications.checkVisible(1)
      .openDialog()
      .checkMessageCount(1)
      .checkMessage(0, "The last computed position for 'Main' (850.00) is not the same as the " +
                       "imported one (0.00)")
      .close();

    categorization.selectTransaction("[R] CHEQUE N° 12345");

    categorization.switchToReconciliation().keepManualTransaction();
    categorization.selectTransaction("[R] AUCHAN 1")
      .switchToReconciliation().select("AUCHAN 1").reconcile();

    transactions.initAmountContent()
      .add("11/05/2012", "AUCHAN 1", -50.00, "To categorize", 750.00, 750.00, "Main")
      .add("10/05/2012", "CHEQUE N°0012345", -100.00, "To categorize", 900.00, 900.00, "Main")
      .add("10/05/2012", "CHEQUE N° 12345", -100.00, "To categorize", 800.00, 800.00, "Main")
      .check();

    categorization.initContent()
      .add("11/05/2012", "", "AUCHAN 1", -50.00)
      .add("10/05/2012", "", "CHEQUE N° 12345", -100.00)
      .add("10/05/2012", "", "CHEQUE N°0012345", -100.00)
      .check();

    mainAccounts.checkPosition("Main", 750);
    mainAccounts.checkReferencePosition(750.00, "2012/05/11");

    // à l'import suivant la transaction n'est plus marquée toReconcile
    OfxBuilder.init(this)
      .addBankAccount("4321", -200.00, "2012/05/15")
      .addTransaction("2012/05/10", -100.00, "CHEQUE 0012345")
      .addTransaction("2012/05/15", -100.00, "FNAC")
      .loadInAccount("Main");

    categorization.initContent()
      .add("11/05/2012", "", "AUCHAN 1", -50.00)
      .add("10/05/2012", "", "CHEQUE N° 12345", -100.00)
      .add("10/05/2012", "", "CHEQUE N°0012345", -100.00)
      .add("15/05/2012", "", "FNAC", -100.00)
      .check();
  }

  @Test
  public void testNavigation() throws Exception {
    accounts.createMainAccount("Main", "4321", 1000.00);

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
      .addBankAccount("4321", 1000.00, "2012/05/10")
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

  @Test
  public void testReconciliationWithASplitTransaction() throws Exception {
    accounts.createMainAccount("Main", "4321", 1000.00);

    transactionCreation
      .show()
      .createToBeReconciled(10, "split 1", -50.00)
      .createToBeReconciled(10, "split 2", -40.00);

    OfxBuilder.init(this)
      .addBankAccount("4321", 1000.00, "2012/05/10")
      .addTransaction("2012/05/10", -100.00, "one splitted op")
      .loadInAccount("Main");
    views.selectCategorization();
    categorization.selectTransaction("ONE SPLITTED OP");

    transactionDetails.split("-50", "split 1");
    transactionDetails.split("-40", "split 2");

    categorization.reconcile("[R] SPLIT 1", "ONE SPLITTED OP - split 1");
    categorization.reconcile("[R] SPLIT 2", "ONE SPLITTED OP - split 2");
    transactions.initAmountContent()
      .add("10/05/2012", "ONE SPLITTED OP", -10.00, "To categorize", 900.00, 900.00, "Main")
      .add("10/05/2012", "ONE SPLITTED OP", -40.00, "To categorize", 910.00, 910.00, "Main")
      .add("10/05/2012", "ONE SPLITTED OP", -50.00, "To categorize", 950.00, 950.00, "Main")
      .check();
  }

  @Test
  public void testAtImportManualOperationAreShift() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    accounts.createMainAccount("Main", "4321", 1000.00);

    transactionCreation
      .show()
      .createToBeReconciled(20, "op 1", -50.00)
      .createToBeReconciled(12, "op 2", -40.00);

    OfxBuilder.init(this)
      .addBankAccount("4321", 1000.00, "2012/05/14")
      .addTransaction("2012/05/14", -10.00, "import op 1")
      .loadInAccount("Main");

    transactions.initAmountContent()
      .add("20/05/2012", "OP 1", -50.00, "To categorize", 900.00, 900.00, "Main")
      .add("14/05/2012", "IMPORT OP 1", -10.00, "To categorize", 990.00, 990.00, "Main")
      .add("12/05/2012", "OP 2", -40.00, "To categorize", 950.00, 950.00, "Main")
      .check();

    setCurrentDate("2012/05/22");
    operations.changeDate();
    OfxBuilder.init(this)
      .addBankAccount("4321", 990.00, "2012/05/21")
      .addTransaction("2012/05/21", -10.00, "import op 3")
      .loadInAccount("Main");

    transactions.initAmountContent()
      .add("21/05/2012", "IMPORT OP 3", -10.00, "To categorize", 980.00, 980.00, "Main")
      .add("20/05/2012", "OP 1", -50.00, "To categorize", 930.00, 930.00, "Main")
      .add("14/05/2012", "IMPORT OP 1", -10.00, "To categorize", 990.00, 990.00, "Main")
      .add("12/05/2012", "OP 2", -40.00, "To categorize", 890.00, 890.00, "Main")
      .check();

    setCurrentDate("2012/06/03");
    operations.changeDate();
    OfxBuilder.init(this)
      .addBankAccount("4321", 980.00, "2012/06/02")
      .addTransaction("2012/06/02", -10.00, "import op 4")
      .loadInAccount("Main");

    transactions.initAmountContent()
      .add("02/06/2012", "IMPORT OP 4", -10.00, "To categorize", 970.00, 970.00, "Main")
      .add("20/05/2012", "OP 1", -50.00, "To categorize", 920.00, 920.00, "Main")
      .add("12/05/2012", "OP 2", -40.00, "To categorize", 880.00, 880.00, "Main")
      .check();
  }

  @Test
  public void testReconciliationWithSavingsAccounts() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();

    accounts.createSavingsAccount("Epargne LCL", "1234", 1000.00);

    OfxBuilder.init(this)
      .addBankAccount("00001123", 0.00, "2012/05/10")
      .addTransaction("2012/03/10", -100.00, "Virement")
      .addTransaction("2012/04/10", -100.00, "Virement")
      .addTransaction("2012/05/10", -100.00, "Virement")
      .load();

    categorization
      .selectTransactions("Virement")
      .selectTransfers().createSeries()
      .setName("Epargne")
      .setFromAccount("Account n. 00001123")
      .setToAccount("Epargne LCL")
      .validate();
    views.selectBudget();
    timeline.selectMonth("2012/04");
    budgetView.transfer.alignAndPropagate("Epargne");
    views.selectCategorization();
    transactionCreation
      .show()
      .selectAccount("Epargne LCL")
      .createToBeReconciled(20, "op 1", 50.00)
      .createToBeReconciled(12, "op 2", 40.00);
    categorization.selectTransaction("[R] op 1").switchToReconciliation().keepManualTransaction();
    categorization.selectTransaction("[R] op 2").switchToReconciliation().keepManualTransaction();
    categorization.setTransfer("op 1", "Epargne");

    // est-ce vraiment ce qu'on attends (creation de l'operation miroir)
    transactions.initAmountContent()
      .add("20/04/2012", "OP 1", 50.00, "Epargne", 1090.00, 1090.00, "Epargne LCL")
//      .add("20/04/2012", "OP 1", -50.00, "Epargne", 50.00, 50.00, "Account n. 00001123")
      .add("12/04/2012", "OP 2", 40.00, "To categorize", 1040.00, 1040.00, "Epargne LCL")
//      .add("10/04/2012", "VIREMENT", 100.00, "Epargne", 1200.00, 1200.00, "Epargne LCL")
      .add("10/04/2012", "VIREMENT", -100.00, "Epargne", 100.00, 100.00, "Account n. 00001123")
      .check();
  }

  @Test
  public void testManualOnly() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    accounts.createMainAccount("Main", "4321", 1000.00);

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
      .add("13/05/2012", "REAL OP 2", -20.00, "To categorize", 970.00, 970.00, "Main")
      .add("12/05/2012", "OP 2", -40.00, "To categorize", 930.00, 930.00, "Main")
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
      .add("21/05/2012", "REAL OP 3", -50.00, "To categorize", 920.00, 920.00, "Main")
      .add("13/05/2012", "REAL OP 2", -20.00, "To categorize", 970.00, 970.00, "Main")
      .add("12/05/2012", "OP 2", -40.00, "To categorize", 880.00, 880.00, "Main")
      .add("10/05/2012", "REAL OP 1", -10.00, "To categorize", 990.00, 990.00, "Main")
      .check();

  }

}
