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
      .create(1, "FNAC", -100.00)
      .create(12, "Auchan", -50.00);
    categorization.initContent()
      .add("12/05/2012", "", "AUCHAN", -50.00)
      .add("01/05/2012", "", "FNAC", -100.00)
      .check();
    budgetView.variable.createSeries()
      .setName("Groceries")
      .gotoSubSeriesTab()
      .addSubSeries("Misc")
      .validate();
    categorization.setVariable("AUCHAN", "Groceries", "Misc");

    OfxBuilder.init(this)
      .addBankAccount("00123", 1000.00, "2012/05/15")
      .addTransaction("2012/05/12", -50.00, "VELIZY AUCHAN")
      .addTransaction("2012/05/15", -100.00, "CARREFOUR")
      .addTransaction("2012/07/12", -25.00, "VELIZY AUCHAN")
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
      .add("12/07/2012", "Food", "VELIZY AUCHAN", -25.00)
      .check();

    categorization.selectTableRow(0);
    transactionDetails.setNote("A comment...");
    categorization.getReconciliation().initTable()
      .add("2012/05/12", "Food", "VELIZY AUCHAN", -50.00)
      .add("2012/05/15", "", "CARREFOUR", -100.00)
      .check();

    categorization.getReconciliation()
      .checkReconcileDisabled()
      .select("VELIZY AUCHAN")
      .reconcile();

    categorization.initContent()
      .add("15/05/2012", "", "CARREFOUR", -100.00)
      .add("01/05/2012", "", "[R] FNAC", -100.00)
      .add("12/04/2012", "", "FNAC PAIEMENTS", -75.00)
      .add("12/05/2012", "Groceries / Misc", "VELIZY AUCHAN", -50.00)
      .add("12/07/2012", "Food", "VELIZY AUCHAN", -25.00)
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
      .add("01/05/2012", "", "[R] FNAC", -100.00)
      .add("12/04/2012", "", "FNAC PAIEMENTS", -75.00)
      .add("12/05/2012", "Groceries / Misc", "VELIZY AUCHAN", -50.00)
      .add("20/05/2012", "", "VELIZY AUCHAN", -75.00)
      .add("12/07/2012", "Food", "VELIZY AUCHAN", -25.00)
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
      .create(7, "AUCHAN", -25.00)
      .create(8, "AUCHAN", -50.00)
      .selectMonth(201204)
      .create(9, "AUCHAN", -75.00);

    categorization.checkReconciliationWarningShown("3 transactions to reconcile");

    categorization.initContent()
      .add("09/04/2012", "", "[R] AUCHAN", -75.00)
      .add("07/05/2012", "", "[R] AUCHAN", -25.00)
      .add("08/05/2012", "", "[R] AUCHAN", -50.00)
      .add("15/03/2012", "", "FNAC", -35.00)
      .add("12/04/2012", "", "FNAC", -25.00)
      .add("12/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();

    categorization.clickReconciliationWarningButton();
    categorization.checkShowsToReconcile();
    categorization.initContent()
      .add("09/04/2012", "", "[R] AUCHAN", -75.00)
      .add("07/05/2012", "", "[R] AUCHAN", -25.00)
      .add("08/05/2012", "", "[R] AUCHAN", -50.00)
      .check();

    timeline.selectMonth(201205);
    categorization.showSelectedMonthsOnly();
    categorization.initContent()
      .add("07/05/2012", "", "[R] AUCHAN", -25.00)
      .add("08/05/2012", "", "[R] AUCHAN", -50.00)
      .add("12/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();

    categorization.clickReconciliationWarningButton();
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

    transactionCreation
      .show()
      .create(20, "CHEQUE N° 12345", -100.00)
      .create(20, "Auchan 1", -50.00);
    categorization.initContent()
      .add("20/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("20/05/2012", "", "[R] CHEQUE N° 12345", -100.00)
      .add("01/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();

    fail("tbd - comment doit évoluer le solde du compte ? on met à jour lors de la création d'opérations, sauf quand on arrive dans le futur ?");
    mainAccounts.checkSummary(1000.00, "2012/05/01");

    setCurrentDate("2008/08/22");

    fail("tbd - solde du compte ci-dessous ??");
    mainAccounts.checkSummary(1000.00, "2012/05/01");

    OfxBuilder.init(this)
      .addBankAccount("00123", 1000.00, "2012/05/15")
      .addTransaction("2012/05/21", -100.00, "CHEQUE 00012345")
      .load();

    categorization.initContent()
      .add("20/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("20/05/2012", "", "[R] CHEQUE N° 12345", -100.00)
      .add("21/05/2012", "", "CHEQUE N°00012345", -100.00)
      .add("01/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();
    
    categorization
      .selectTransaction("[R] CHEQUE N° 12345")
      .checkReconciliationShown();

    categorization.getReconciliation().initTable()
      .add("2012/05/21", "", "CHEQUE N°00012345", -100.00)
      .add("2012/05/01", "", "VELIZY AUCHAN", -50.00)
      .check();

    categorization.getReconciliation().select("CHEQUE N°00012345").reconcile();

    categorization.initContent()
      .add("20/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("21/05/2012", "", "CHEQUE N°00012345", -100.00)
      .add("01/05/2012", "", "VELIZY AUCHAN", -50.00)
      .check();
  }

  public void testKeepingAManualTransaction() throws Exception {

    mainAccounts.createMainAccount("Main", 1000.00);

    transactionCreation
      .show()
      .create(10, "CHEQUE N° 12345", -100.00)
      .create(10, "Auchan 1", -50.00);
    categorization.initContent()
      .add("10/05/2012", "", "AUCHAN 1", -50.00)
      .add("10/05/2012", "", "CHEQUE N° 12345", -100.00)
      .check();

    OfxBuilder.init(this)
      .addTransaction("2012/05/10", -100.00, "CHEQUE 0012345")
      .loadInAccount("Main");
    categorization.initContent()
      .add("10/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("10/05/2012", "", "[R] CHEQUE N° 12345", -100.00)
      .add("10/05/2012", "", "CHEQUE N°0012345", -100.00)
      .check();

    categorization.selectTransaction("[R] CHEQUE N° 12345");
    categorization.getReconciliation().keepManualTransaction();

    categorization.initContent()
      .add("10/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("10/05/2012", "", "CHEQUE N° 12345", -100.00)
      .add("10/05/2012", "", "CHEQUE N°0012345", -100.00)
      .check();

    fail("tbd - effet sur le solde ?");

    // à l'import suivant la transaction n'est plus marquée toReconcile
    OfxBuilder.init(this)
      .addTransaction("2012/05/10", -100.00, "CHEQUE 0012345")
      .addTransaction("2012/05/15", -100.00, "FNAC")
      .loadInAccount("Main");
    categorization.initContent()
      .add("10/05/2012", "", "[R] AUCHAN 1", -50.00)
      .add("10/05/2012", "", "CHEQUE N° 12345", -100.00)
      .add("10/05/2012", "", "CHEQUE N°0012345", -100.00)
      .add("15/05/2012", "", "FNAC", -100.00)
      .check();
  }

  public void testReconciliationWithASplitTransaction() throws Exception {
    fail("tbd - voir les impacts sur le split");
  }

  public void testReconciliationWithAShiftedTransaction() throws Exception {
    fail("tbd - voir les impacts sur le shift");
  }

  public void testReconciliationWithSavingsAccounts() throws Exception {
    fail("tbd - on ne montre que les opérations qui sont 'dans le bon sens'");
  }

  public void testReconciledTransactionsAreAutomaticallyAnnotated() throws Exception {
    fail("tbd - es opérations réconciliées sont-elles automatiquement pointées ?");
  }
}
