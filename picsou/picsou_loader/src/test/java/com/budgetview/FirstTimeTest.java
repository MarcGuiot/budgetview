package com.budgetview;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.checkers.components.TimeViewChecker;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Files;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.utils.DummyRepaintManager;
import org.uispec4j.interception.WindowInterceptor;

import java.io.*;
import java.util.Locale;

public class FirstTimeTest extends UISpecTestCase {

  protected void setUp() throws Exception {
//    UISpec4J.setAssertionTimeLimit(100000);
//    UISpec4J.setWindowInterceptionTimeLimit(100000);
//    LicenseActivationChecker.open(window).enterLicense("admin", "1234").validate();
//
//    operations.backupAndLaunchApplication(LoginPanel.AUTOLOG_USER, LoginPanel.AUTOLOG_USER,
//                                          Dates.parse("2011/02/14"));

    super.setUp();
    Lang.setLocale(Locale.ENGLISH);
    Locale.setDefault(Locale.ENGLISH);
    System.setProperty("SINGLE_INSTANCE_DISABLED", "true");
    String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "budgetview";
    Files.deleteSubtree(new File(tmpDir));
    DummyRepaintManager.init();
  }

  public void testLoad() throws Exception {
    String file = OfxBuilder.init(this)
      .addBankAccount(30066, 1234, OfxBuilder.DEFAULT_ACCOUNT_ID, 1900.0, "2008/09/28")
      .addTransaction("2008/09/02", 30, "VIR Mutuelle")
      .addTransaction("2008/09/03", -100, "VIR epargne")
      .addTransaction("2008/09/03", -30, "Habille moi")
      .addTransaction("2008/09/05", -50, "Cheque 32")
      .addTransaction("2008/09/05", -60, "ED")
      .addTransaction("2008/09/05", -700, "Credit")
      .addTransaction("2008/09/06", -40, "Institut pasteur")
      .addTransaction("2008/09/06", -29.90, "Free telecom")
      .addTransaction("2008/09/07", -10, "Centre nautique")
      .addTransaction("2008/09/07", 40, "CPAM")
      .addTransaction("2008/09/09", -400, "Impots")
      .addTransaction("2008/09/10", -50, "Chausse moi")
      .addTransaction("2008/09/11", -40, "Auchan")
      .addTransaction("2008/09/19", -60, "Gaz de France")
      .addTransaction("2008/09/20", -40, "Retrait")
      .addTransaction("2008/09/21", -40, "SFR")
      .addTransaction("2008/09/22", -150, "Intermarché")
      .addTransaction("2008/09/22", -100, "GMF")
      .addTransaction("2008/09/23", -4, "Cotisation carte bleue")
      .addTransaction("2008/09/22", -30, "Coup'coup")
      .addTransaction("2008/09/25", -25, "jeux pour tous")
      .addTransaction("2008/09/27", 2000, "Salaire")
      .addTransaction("2008/09/28", -40, "Ecole")
      .save();

    Window window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {

        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "budgetview";
        String tmpJarDir = tmpDir + File.separator + "jars";
        File jarDir = new File(tmpJarDir);
        File prevaylerDir = new File(tmpDir + File.separator + "data");
        Files.deleteSubtree(prevaylerDir);
        Files.deleteSubtree(jarDir);
        jarDir.mkdirs();
        prevaylerDir.mkdirs();
        InputStream stream =
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "budgetview.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/budgetview9999999.jar");
        System.setProperty("budgetview.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("budgetview.prevayler.path", prevaylerDir.getAbsolutePath());
        Window dialog = WindowInterceptor.getModalDialog(new Trigger() {
          public void run() throws Exception {
            Main.main(new String[0]);
          }
        });
        final SlaValidationDialogChecker slaValidationDialogChecker =
          new SlaValidationDialogChecker(dialog);

        slaValidationDialogChecker.acceptTerms();
        slaValidationDialogChecker.validate();
      }
    });
    OperationChecker operations = new OperationChecker(window);
    ImportDialogChecker ofxImportDialog = operations.openImportDialog();
    ofxImportDialog.browseAndSelect(file)
      .acceptFile()
      .setMainAccount()
      .completeImport();

    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectData();
    TransactionChecker transaction = new TransactionChecker(window);
    transaction.initContent()
      .add("28/09/2008", TransactionType.PRELEVEMENT, "Ecole", "", -40.00)
      .add("27/09/2008", TransactionType.VIREMENT, "Salaire", "", 2000.00)
      .add("25/09/2008", TransactionType.PRELEVEMENT, "jeux pour tous", "", -25.00)
      .add("23/09/2008", TransactionType.PRELEVEMENT, "Cotisation carte bleue", "", -4.00)
      .add("22/09/2008", TransactionType.PRELEVEMENT, "Coup'coup", "", -30.00)
      .add("22/09/2008", TransactionType.PRELEVEMENT, "GMF", "", -100.00)
      .add("22/09/2008", TransactionType.PRELEVEMENT, "Intermarché", "", -150.00)
      .add("21/09/2008", TransactionType.PRELEVEMENT, "SFR", "", -40.00)
      .add("20/09/2008", TransactionType.PRELEVEMENT, "Retrait", "", -40.00)
      .add("19/09/2008", TransactionType.PRELEVEMENT, "Gaz de France", "", -60.00)
      .add("11/09/2008", TransactionType.PRELEVEMENT, "Auchan", "", -40.00)
      .add("10/09/2008", TransactionType.PRELEVEMENT, "Chausse moi", "", -50.00)
      .add("09/09/2008", TransactionType.PRELEVEMENT, "Impots", "", -400.00)
      .add("07/09/2008", TransactionType.VIREMENT, "CPAM", "", 40.00)
      .add("07/09/2008", TransactionType.PRELEVEMENT, "Centre nautique", "", -10.00)
      .add("06/09/2008", TransactionType.PRELEVEMENT, "Free telecom", "", -29.90)
      .add("06/09/2008", TransactionType.PRELEVEMENT, "Institut pasteur", "", -40.00)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "Credit", "", -700.00)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "ED", "", -60.00)
      .add("05/09/2008", TransactionType.CHECK, "CHEQUE N°32", "", -50.00)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "Habille moi", "", -30.00)
      .add("03/09/2008", TransactionType.VIREMENT, "EPARGNE", "", -100.00)
      .add("02/09/2008", TransactionType.VIREMENT, "MUTUELLE", "", 30.00)
      .check();

    views.selectCategorization();
    CategorizationChecker categorization = new CategorizationChecker(window);
    categorization
      .setVariable("MUTUELLE", "Health")
      .setNewSavings("EPARGNE", "Regular savings", "Main accounts", "External account");

    categorization.editSeries("Regular savings")
      .selectMonth(200809)
      .alignPlannedAndActual()
      .setPropagationEnabled()
      .validate();
    categorization.selectTransactions("Habille moi", "Chausse moi");
    categorization
      .selectVariable().createSeries().setName("Fringue")
      .validate();
    categorization.setVariable("CHEQUE N°32", "Health");

    categorization.selectTransactions("ED", "Auchan", "Intermarché")
      .selectVariable().selectSeries("Groceries");
    categorization
      .setRecurring("Credit", "Mortgage")
      .setNewRecurring("Institut pasteur", "Don")
      .setRecurring("Free telecom", "Internet")
      .setVariable("Centre nautique", "Leisures")
      .setVariable("CPAM", "Health")
      .selectTransactions("Impots");

    categorization.selectRecurring().createSeries()
      .setName("impots")
      .setCustom().toggleMonth(1, 3, 4, 5, 7, 8, 10, 11, 12)
      .validate();

    categorization
      .setRecurring("Gaz de France", "Gas")
      .setVariable("Retrait", "cash")
      .setRecurring("SFR", "Cell phone 1")
      .selectTransaction("GMF")
      .selectRecurring()
      .createSeries()
      .setName("Assurance")
      .validate();

    categorization
      .setRecurring("GMF", "Assurance")
      .setNewRecurring("Cotisation carte bleue", "Frais banque")
      .setNewVariable("Coup'coup", "Occasionel")
      .setVariable("jeux pour tous", "Occasionel")
      .setIncome("Salaire", "Income 1")
      .setNewRecurring("Ecole", "Ecole");

    categorization.initContent()
      .add("11/09/2008", "Groceries", "Auchan", -40.0)
      .add("07/09/2008", "Leisures", "Centre nautique", -10.0)
      .add("10/09/2008", "Fringue", "Chausse moi", -50.0)
      .add("05/09/2008", "Health", "CHEQUE N°32", -50.0)
      .add("23/09/2008", "Frais banque", "Cotisation carte bleue", -4.0)
      .add("22/09/2008", "Occasionel", "Coup'coup", -30.0)
      .add("07/09/2008", "Health", "CPAM", 40.0)
      .add("05/09/2008", "Rent/Mortgage", "Credit", -700.0)
      .add("28/09/2008", "Ecole", "Ecole", -40.0)
      .add("05/09/2008", "Groceries", "ED", -60.0)
      .add("03/09/2008", "Regular savings", "EPARGNE", -100.0)
      .add("06/09/2008", "Internet", "Free telecom", -29.9)
      .add("19/09/2008", "Gas", "Gaz de France", -60.0)
      .add("22/09/2008", "Assurance", "GMF", -100.0)
      .add("03/09/2008", "Fringue", "Habille moi", -30.0)
      .add("09/09/2008", "impots", "Impots", -400.0)
      .add("06/09/2008", "Don", "Institut pasteur", -40.0)
      .add("22/09/2008", "Groceries", "Intermarché", -150.0)
      .add("25/09/2008", "Occasionel", "jeux pour tous", -25.0)
      .add("02/09/2008", "Health", "MUTUELLE", 30.0)
      .add("20/09/2008", "Cash", "Retrait", -40.0)
      .add("27/09/2008", "Income 1", "Salaire", 2000.0)
      .add("21/09/2008", "Cell phone 1", "SFR", -40.0)
      .check();
    CategorizationGaugeChecker categorizationGauge = categorization.getCompletionGauge();
    categorizationGauge.checkHidden();

    views.selectBudget();
    BudgetViewChecker budget = new BudgetViewChecker(window);
    budget.income.checkTotalAmounts(2000.00, 2000.00);
    budget.variable.alignAndPropagate("Occasionel");
    budget.variable.alignAndPropagate("Health");
    budget.variable.alignAndPropagate("Leisures");
    budget.variable.alignAndPropagate("cash");
    budget.variable.alignAndPropagate("Groceries");
    budget.variable.alignAndPropagate("Fringue");
    budget.variable.checkTotalAmounts(-415.00, -415.00);
    budget.recurring.checkTotalAmounts(-1413.90, -1413.90);

    views.selectData();
    transaction.initContent()
      .add("28/09/2008", TransactionType.PRELEVEMENT, "ECOLE", "", -40.00, "Ecole")
      .add("27/09/2008", TransactionType.VIREMENT, "SALAIRE", "", 2000.00, "Income 1")
      .add("25/09/2008", TransactionType.PRELEVEMENT, "JEUX POUR TOUS", "", -25.00, "Occasionel")
      .add("23/09/2008", TransactionType.PRELEVEMENT, "COTISATION CARTE BLEUE", "", -4.00, "Frais banque")
      .add("22/09/2008", TransactionType.PRELEVEMENT, "COUP'COUP", "", -30.00, "Occasionel")
      .add("22/09/2008", TransactionType.PRELEVEMENT, "GMF", "", -100.00, "Assurance")
      .add("22/09/2008", TransactionType.PRELEVEMENT, "INTERMARCHÉ", "", -150.00, "Groceries")
      .add("21/09/2008", TransactionType.PRELEVEMENT, "SFR", "", -40.00, "Cell phone 1")
      .add("20/09/2008", TransactionType.PRELEVEMENT, "RETRAIT", "", -40.00, "Cash")
      .add("19/09/2008", TransactionType.PRELEVEMENT, "GAZ DE FRANCE", "", -60.00, "Gas")
      .add("11/09/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -40.00, "Groceries")
      .add("10/09/2008", TransactionType.PRELEVEMENT, "CHAUSSE MOI", "", -50.00, "Fringue")
      .add("09/09/2008", TransactionType.PRELEVEMENT, "IMPOTS", "", -400.00, "impots")
      .add("07/09/2008", TransactionType.VIREMENT, "CPAM", "", 40.00, "Health")
      .add("07/09/2008", TransactionType.PRELEVEMENT, "CENTRE NAUTIQUE", "", -10.00, "Leisures")
      .add("06/09/2008", TransactionType.PRELEVEMENT, "FREE TELECOM", "", -29.90, "Internet")
      .add("06/09/2008", TransactionType.PRELEVEMENT, "INSTITUT PASTEUR", "", -40.00, "Don")
      .add("05/09/2008", TransactionType.PRELEVEMENT, "CREDIT", "", -700.00, "Rent/Mortgage")
      .add("05/09/2008", TransactionType.PRELEVEMENT, "ED", "", -60.00, "Groceries")
      .add("05/09/2008", TransactionType.CHECK, "CHEQUE N°32", "", -50.00, "Health")
      .add("03/09/2008", TransactionType.PRELEVEMENT, "HABILLE MOI", "", -30.00, "Fringue")
      .add("03/09/2008", TransactionType.VIREMENT, "EPARGNE", "", -100.00, "Regular savings")
      .add("02/09/2008", TransactionType.VIREMENT, "MUTUELLE", "", 30.00, "Health")
      .check();


    transaction
      .showPlannedTransactions()
      .initAmountContent()
      .add("28/09/2008", "ECOLE", -40.00, "Ecole", 1900.00, 1900.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("27/09/2008", "SALAIRE", 2000.00, "Income 1", 1940.00, 1940.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("25/09/2008", "JEUX POUR TOUS", -25.00, "Occasionel", -60.00, -60.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("23/09/2008", "COTISATION CARTE BLEUE", -4.00, "Frais banque", -35.00, -35.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("22/09/2008", "COUP'COUP", -30.00, "Occasionel", -31.00, -31.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("22/09/2008", "GMF", -100.00, "Assurance", -1.00, -1.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("22/09/2008", "INTERMARCHÉ", -150.00, "Groceries", 99.00, 99.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("21/09/2008", "SFR", -40.00, "Cell phone 1", 249.00, 249.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("20/09/2008", "RETRAIT", -40.00, "Cash", 289.00, 289.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("19/09/2008", "GAZ DE FRANCE", -60.00, "Gas", 329.00, 329.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("11/09/2008", "AUCHAN", -40.00, "Groceries", 389.00, 389.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/09/2008", "CHAUSSE MOI", -50.00, "Fringue", 429.00, 429.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("09/09/2008", "IMPOTS", -400.00, "impots", 479.00, 479.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("07/09/2008", "CPAM", 40.00, "Health", 879.00, 879.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("07/09/2008", "CENTRE NAUTIQUE", -10.00, "Leisures", 839.00, 839.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/09/2008", "FREE TELECOM", -29.90, "Internet", 849.00, 849.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/09/2008", "INSTITUT PASTEUR", -40.00, "Don", 878.90, 878.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/09/2008", "CREDIT", -700.00, "Rent/Mortgage", 918.90, 918.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/09/2008", "ED", -60.00, "Groceries", 1618.90, 1618.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/09/2008", "CHEQUE N°32", -50.00, "Health", 1678.90, 1678.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/09/2008", "HABILLE MOI", -30.00, "Fringue", 1728.90, 1728.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/09/2008", "EPARGNE", -100.00, "Regular savings", 1758.90, 1758.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("02/09/2008", "MUTUELLE", 30.00, "Health", 1858.90, 1858.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();



    TimeViewChecker times = new TimeViewChecker(window);
    times.selectMonth("2008/10");

    transaction
      .showPlannedTransactions()
      .initAmountContent()
      .add("15/10/2008", "Planned: Health", 20.00, "Health", 1971.10, "Main accounts")
      .add("15/10/2008", "Planned: Income 1", 2000.00, "Income 1", 1951.10, "Main accounts")
      .add("08/10/2008", "Planned: Fringue", -80.00, "Fringue", -48.90, "Main accounts")
      .add("08/10/2008", "Planned: Groceries", -250.00, "Groceries", 31.10, "Main accounts")
      .add("08/10/2008", "Planned: Cash", -40.00, "Cash", 281.10, "Main accounts")
      .add("08/10/2008", "Planned: Leisures", -10.00, "Leisures", 321.10, "Main accounts")
      .add("08/10/2008", "Planned: Occasionel", -55.00, "Occasionel", 331.10, "Main accounts")
      .add("08/10/2008", "Planned: Ecole", -40.00, "Ecole", 386.10, "Main accounts")
      .add("08/10/2008", "Planned: Assurance", -100.00, "Assurance", 426.10, "Main accounts")
      .add("08/10/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 526.10, "Main accounts")
      .add("08/10/2008", "Planned: Gas", -60.00, "Gas", 566.10, "Main accounts")
      .add("08/10/2008", "Planned: impots", -400.00, "impots", 626.10, "Main accounts")
      .add("08/10/2008", "Planned: Internet", -29.90, "Internet", 1026.10, "Main accounts")
      .add("08/10/2008", "Planned: Don", -40.00, "Don", 1056.00, "Main accounts")
      .add("08/10/2008", "Planned: Rent/Mortgage", -700.00, "Rent/Mortgage", 1096.00, "Main accounts")
      .add("08/10/2008", "Planned: Regular savings", -100.00, "Regular savings", 1796.00, "Main accounts")
      .add("01/10/2008", "Planned: Frais banque", -4.00, "Frais banque", 1896.00, "Main accounts")
      .check();

    views.selectBudget();

    String file2 = QifBuilder.init(this)
      .addTransaction("2008/10/03", -100, "VIR epargne")
      .addTransaction("2008/10/03", -30, "Habille moi")
      .addTransaction("2008/10/05", -30, "Cheque 34")
      .addTransaction("2008/10/05", -40, "ED")
      .addTransaction("2008/10/05", -700, "Credit")
      .addTransaction("2008/10/06", -40, "Institut pasteur")
      .addTransaction("2008/10/06", -29.90, "Free telecom")
      .addTransaction("2008/10/10", -60, "Chausse moi")
      .addTransaction("2008/10/11", -30, "Auchan")
      .addTransaction("2008/10/19", -60, "Gaz de France")
      .save();

    OperationChecker operation = new OperationChecker(window);

    operation.importQifFile(file2, "CIC");

    views.selectData();
    transaction
      .showPlannedTransactions()
      .initAmountContent()
      .add("19/10/2008", "Planned: Groceries", -180.00, "Groceries", 1831.10, "Main accounts")
      .add("19/10/2008", "Planned: Cash", -40.00, "Cash", 2011.10, "Main accounts")
      .add("19/10/2008", "Planned: Leisures", -10.00, "Leisures", 2051.10, "Main accounts")
      .add("19/10/2008", "Planned: Health", 20.00, "Health", 2061.10, "Main accounts")
      .add("19/10/2008", "Planned: Occasionel", -55.00, "Occasionel", 2041.10, "Main accounts")
      .add("19/10/2008", "Planned: Ecole", -40.00, "Ecole", 2096.10, "Main accounts")
      .add("19/10/2008", "Planned: Income 1", 2000.00, "Income 1", 2136.10, "Main accounts")
      .add("19/10/2008", "Planned: Frais banque", -4.00, "Frais banque", 136.10, "Main accounts")
      .add("19/10/2008", "Planned: Assurance", -100.00, "Assurance", 140.10, "Main accounts")
      .add("19/10/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 240.10, "Main accounts")
      .add("19/10/2008", "Planned: impots", -400.00, "impots", 280.10, "Main accounts")
      .add("19/10/2008", "Planned: Regular savings", -100.00, "Regular savings", 680.10, "Main accounts")
      .add("19/10/2008", "GAZ DE FRANCE", -60.00, "Gas", 780.10, 780.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("11/10/2008", "AUCHAN", -30.00, "Groceries", 840.10, 840.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/10/2008", "CHAUSSE MOI", -60.00, "Fringue", 870.10, 870.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/10/2008", "FREE TELECOM", -29.90, "Internet", 930.10, 930.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/10/2008", "INSTITUT PASTEUR", -40.00, "Don", 960.00, 960.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/10/2008", "CREDIT", -700.00, "Rent/Mortgage", 1000.00, 1000.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/10/2008", "ED", -40.00, "Groceries", 1700.00, 1700.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/10/2008", "CHEQUE 34", -30.00, "To categorize", 1740.00, 1740.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/10/2008", "HABILLE MOI", -30.00, "Fringue", 1770.00, 1770.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/10/2008", "VIR EPARGNE", -100.00, "To categorize", 1800.00, 1800.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    window.dispose();
  }

  public void testReloadSnapshotV3() throws Exception {
    Window window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        String userId = "795391559";
        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "budgetview";
        String tmpJarDir = tmpDir + File.separator + "jars";
        File jarDir = new File(tmpJarDir);
        File prevaylerDir = new File(tmpDir + File.separator + "data");
        Files.deleteSubtree(prevaylerDir);
        Files.deleteSubtree(jarDir);
        jarDir.mkdirs();
        prevaylerDir.mkdirs();
        File userDir = new File(prevaylerDir + File.separator + "data" + File.separator + "users");
        userDir.mkdirs();
        File dataDir = new File(prevaylerDir + File.separator + "data" + File.separator + userId);
        dataDir.mkdirs();

        InputStream dataStream =
          getClass().getResourceAsStream("/files/v3/data/" + userId + "/0000000000000000040.snapshot");
        Files.copyStreamTofile(dataStream, dataDir.getAbsolutePath() + "/0000000000000000040.snapshot");

        InputStream usersDataStream =
          getClass().getResourceAsStream("/files/v3/data/users/0000000000000000003.snapshot");
        Files.copyStreamTofile(usersDataStream, userDir.getAbsolutePath() + "/0000000000000000003.snapshot");

        InputStream stream =
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "budgetview.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/budgetview9999999.jar");
        System.setProperty("budgetview.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("budgetview.prevayler.path", prevaylerDir.getAbsolutePath());
        com.budgetview.Main.main(new String[0]);
      }
    });
    LoginChecker login = new LoginChecker(window);
    login.logExistingUser("toto", "toto", true);

    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectBudget();
    TimeViewChecker timeView = new TimeViewChecker(window);
    timeView.selectMonth("2008/10");

//    BudgetSummaryViewChecker budgetSummary = new BudgetSummaryViewChecker(window);
//    budgetSummary.openPositionDialog()
//      .checkPosition(2386.10)
//      .checkInitialPosition(780.10)
//      .checkIncome(2000)
//      .checkFixed(184)
//      .checkSavingsIn(0)
//      .checkVariable(210)
//      .close();

    views.selectData();

    TransactionChecker transaction = new TransactionChecker(window);
    transaction
      .showPlannedTransactions()
      .initAmountContent()
      .add("31/10/2008", "Planned: Income 1", 2000.00, "Income 1", 2386.10, "Main accounts")
      .add("26/10/2008", "Planned: Ecole", -40.00, "Ecole", 386.10, "Main accounts")
      .add("21/10/2008", "Planned: Groceries", -150.00, "Groceries", 426.10, "Main accounts")
      .add("21/10/2008", "Planned: Assurance", -100.00, "Assurance", 576.10, "Main accounts")
      .add("21/10/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 676.10, "Main accounts")
      .add("19/10/2008", "Planned: Health", 20.00, "Health", 716.10, "Main accounts")
      .add("19/10/2008", "Planned: Frais banque", -4.00, "Frais banque", 696.10, "Main accounts")
      .add("19/10/2008", "Planned: Cash", -40.00, "Cash", 700.10, "Main accounts")
      .add("19/10/2008", "Planned: Leisures", -10.00, "Leisures", 740.10, "Main accounts")
      .add("19/10/2008", "Planned: Groceries", -30.00, "Groceries", 750.10, "Main accounts")
      .add("19/10/2008", "GAZ DE FRANCE", -60.00, "Gas", 780.10, 780.10, "Compte 00001123")
      .add("11/10/2008", "AUCHAN", -30.00, "Groceries", 840.10, 840.10, "Compte 00001123")
      .add("10/10/2008", "CHAUSSE MOI", -60.00, "Fringue", 870.10, 870.10, "Compte 00001123")
      .add("06/10/2008", "FREE TELECOM", -29.90, "Internet", 930.10, 930.10, "Compte 00001123")
      .add("06/10/2008", "INSTITUT PASTEUR", -40.00, "Don", 960.00, 960.00, "Compte 00001123")
      .add("05/10/2008", "CREDIT", -700.00, "Mortgage", 1000.00, 1000.00, "Compte 00001123")
      .add("05/10/2008", "ED", -40.00, "Groceries", 1700.00, 1700.00, "Compte 00001123")
      .add("05/10/2008", "CHEQUE N. 34", -30.00, "To categorize", 1740.00, 1740.00, "Compte 00001123")
      .add("03/10/2008", "HABILLE MOI", -30.00, "Fringue", 1770.00, 1770.00, "Compte 00001123")
      .add("03/10/2008", "EPARGNE", -100.00, "Regular savings", 1800.00, 1800.00, "Compte 00001123")
      .check();

    views.selectBudget();
    BudgetViewChecker budgetView = new BudgetViewChecker(window);
    budgetView.income.editSeries("Income 1").setName("Revenu").validate();

    window.dispose();
  }

  public void testReloadSnapshotV4() throws Exception {
    Window window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        String userId = "49585955";
        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "budgetview";
        String tmpJarDir = tmpDir + File.separator + "jars";
        File jarDir = new File(tmpJarDir);
        File prevaylerDir = new File(tmpDir + File.separator + "data");
        Files.deleteSubtree(prevaylerDir);
        Files.deleteSubtree(jarDir);
        jarDir.mkdirs();
        prevaylerDir.mkdirs();
        File userDir = new File(prevaylerDir + File.separator + "data" + File.separator + "users");
        userDir.mkdirs();
        File dataDir = new File(prevaylerDir + File.separator + "data" + File.separator + userId);
        dataDir.mkdirs();

        InputStream dataStream =
          getClass().getResourceAsStream("/files/v4/data/" + userId + "/0000000000000000041.snapshot");
        Files.copyStreamTofile(dataStream, dataDir.getAbsolutePath() + "/0000000000000000041.snapshot");

        InputStream usersDataStream =
          getClass().getResourceAsStream("/files/v4/data/users/0000000000000000003.snapshot");
        Files.copyStreamTofile(usersDataStream, userDir.getAbsolutePath() + "/0000000000000000003.snapshot");

        InputStream stream =
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "budgetview.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/budgetview9999999.jar");
        System.setProperty("budgetview.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("budgetview.prevayler.path", prevaylerDir.getAbsolutePath());
        Main.main(new String[0]);
      }
    });
    LoginChecker login = new LoginChecker(window);
    login.logExistingUser("toto", "toto", true);

    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectBudget();

//    BudgetSummaryViewChecker budgetSummary = new BudgetSummaryViewChecker(window);
//    budgetSummary.openPositionDialog()
//      .checkPosition(2386.10)
//      .checkInitialPosition(780.1)
//      .checkIncome(2000)
//      .checkFixed(184)
//      .checkSavingsIn(0)
//      .checkSavingsOut(0)
//      .checkVariable(210)
//      .close();

    views.selectData();

    TransactionChecker transaction = new TransactionChecker(window);
    transaction
      .showPlannedTransactions()
      .initAmountContent()
      .add("31/10/2008", "Planned: Income 1", 2000.00, "Income 1", 2386.10, "Main accounts")
      .add("26/10/2008", "Planned: Ecole", -40.00, "Ecole", 386.10, "Main accounts")
      .add("21/10/2008", "Planned: Groceries", -150.00, "Groceries", 426.10, "Main accounts")
      .add("21/10/2008", "Planned: Assurance", -100.00, "Assurance", 576.10, "Main accounts")
      .add("21/10/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 676.10, "Main accounts")
      .add("19/10/2008", "Planned: Health", 20.00, "Health", 716.10, "Main accounts")
      .add("19/10/2008", "Planned: Frais banque", -4.00, "Frais banque", 696.10, "Main accounts")
      .add("19/10/2008", "Planned: Cash", -40.00, "Cash", 700.10, "Main accounts")
      .add("19/10/2008", "Planned: Leisures", -10.00, "Leisures", 740.10, "Main accounts")
      .add("19/10/2008", "Planned: Groceries", -30.00, "Groceries", 750.10, "Main accounts")
      .add("19/10/2008", "GAZ DE FRANCE", -60.00, "Gas", 780.10, 780.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("11/10/2008", "AUCHAN", -30.00, "Groceries", 840.10, 840.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/10/2008", "CHAUSSE MOI", -60.00, "Fringue", 870.10, 870.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/10/2008", "FREE TELECOM", -29.90, "Internet", 930.10, 930.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/10/2008", "INSTITUT PASTEUR", -40.00, "Don", 960.00, 960.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/10/2008", "CREDIT", -700.00, "Mortgage", 1000.00, 1000.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/10/2008", "ED", -40.00, "Groceries", 1700.00, 1700.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/10/2008", "CHEQUE N. 34", -30.00, "To categorize", 1740.00, 1740.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/10/2008", "HABILLE MOI", -30.00, "Fringue", 1770.00, 1770.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/10/2008", "EPARGNE", -100.00, "Regular savings", 1800.00, 1800.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();


    views.selectBudget();
    BudgetViewChecker budgetView = new BudgetViewChecker(window);
    budgetView.income.editSeries("Income 1").setName("Revenu").validate();

    window.dispose();
  }

  public void testReloadSnapshotV5() throws Exception {
    Window window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        String userId = "1801464342";
        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "budgetview";
        String tmpJarDir = tmpDir + File.separator + "jars";
        File jarDir = new File(tmpJarDir);
        File prevaylerDir = new File(tmpDir + File.separator + "data");
        Files.deleteSubtree(prevaylerDir);
        Files.deleteSubtree(jarDir);
        jarDir.mkdirs();
        prevaylerDir.mkdirs();
        File userDir = new File(prevaylerDir + File.separator + "data" + File.separator + "users");
        userDir.mkdirs();
        File dataDir = new File(prevaylerDir + File.separator + "data" + File.separator + userId);
        dataDir.mkdirs();

        InputStream dataStream =
          getClass().getResourceAsStream("/files/v5/data/" + userId + "/0000000000000000041.snapshot");
        Files.copyStreamTofile(dataStream, dataDir.getAbsolutePath() + "/0000000000000000041.snapshot");

        InputStream usersDataStream =
          getClass().getResourceAsStream("/files/v5/data/users/0000000000000000003.snapshot");
        Files.copyStreamTofile(usersDataStream, userDir.getAbsolutePath() + "/0000000000000000003.snapshot");

        InputStream stream =
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "budgetview.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/budgetview9999999.jar");
        System.setProperty("budgetview.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("budgetview.prevayler.path", prevaylerDir.getAbsolutePath());
        Main.main(new String[0]);
      }
    });
    LoginChecker login = new LoginChecker(window);
    login.logExistingUser("toto", "toto", true);
    MainAccountViewChecker mainAccounts = new MainAccountViewChecker(window);

    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectBudget();

//    BudgetSummaryViewChecker budgetSummary = new BudgetSummaryViewChecker(window);
//    budgetSummary.openPositionDialog()
//      .checkPosition(2386.10)
//      .checkInitialPosition(780.1)
//      .checkIncome(2000)
//      .checkFixed(184)
//      .checkSavingsIn(0)
//      .checkSavingsOut(0)
//      .checkVariable(210)
//      .close();

    views.selectData();

    TransactionChecker transaction = new TransactionChecker(window);
    transaction
      .showPlannedTransactions()
      .initAmountContent()
      .add("31/10/2008", "Planned: Income 1", 2000.00, "Income 1", 2386.10, "Main accounts")
      .add("26/10/2008", "Planned: Ecole", -40.00, "Ecole", 386.10, "Main accounts")
      .add("21/10/2008", "Planned: Groceries", -150.00, "Groceries", 426.10, "Main accounts")
      .add("21/10/2008", "Planned: Assurance", -100.00, "Assurance", 576.10, "Main accounts")
      .add("21/10/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 676.10, "Main accounts")
      .add("19/10/2008", "Planned: Health", 20.00, "Health", 716.10, "Main accounts")
      .add("19/10/2008", "Planned: Frais banque", -4.00, "Frais banque", 696.10, "Main accounts")
      .add("19/10/2008", "Planned: Cash", -40.00, "Cash", 700.10, "Main accounts")
      .add("19/10/2008", "Planned: Leisures", -10.00, "Leisures",740.10, "Main accounts")
      .add("19/10/2008", "Planned: Groceries", -30.00, "Groceries", 750.10, "Main accounts")
      .add("19/10/2008", "GAZ DE FRANCE", -60.00, "Gas", 780.10, 780.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("11/10/2008", "AUCHAN", -30.00, "Groceries", 840.10, 840.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/10/2008", "CHAUSSE MOI", -60.00, "Fringue", 870.10, 870.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/10/2008", "FREE TELECOM", -29.90, "Internet", 930.10, 930.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/10/2008", "INSTITUT PASTEUR", -40.00, "Don", 960.00, 960.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/10/2008", "CREDIT", -700.00, "Mortgage", 1000.00, 1000.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/10/2008", "ED", -40.00, "Groceries", 1700.00, 1700.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/10/2008", "CHEQUE N. 34", -30.00, "To categorize", 1740.00, 1740.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/10/2008", "HABILLE MOI", -30.00, "Fringue", 1770.00, 1770.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/10/2008", "EPARGNE", -100.00, "Regular savings", 1800.00, 1800.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();

    views.selectBudget();
    BudgetViewChecker budgetView = new BudgetViewChecker(window);
    budgetView.income.editSeries("Income 1").setName("Revenu").validate();

    budgetView.savings.editSeries("Regular savings")
      .setFromAccount("Main accounts")
      .validate();

    views.selectCategorization();
    CategorizationChecker categorization = new CategorizationChecker(window);
    categorization.setSavings("EPARGNE", "Regular savings");

    TimeViewChecker checker = new TimeViewChecker(window);
    checker.selectMonths("2008/09", "2008/10", "2008/11");
    views.selectData();
    transaction
      .showPlannedTransactions()
      .initAmountContent()
      .add("30/11/2008", "Planned: Revenu", 2000.00, "Revenu", 2902.20, "Main accounts")
      .add("26/11/2008", "Planned: Ecole", -40.00, "Ecole", 902.20, "Main accounts")
      .add("21/11/2008", "Planned: Groceries", -150.00, "Groceries", 942.20, "Main accounts")
      .add("21/11/2008", "Planned: Assurance", -100.00, "Assurance", 1092.20, "Main accounts")
      .add("21/11/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 1192.20, "Main accounts")
      .add("16/11/2008", "Planned: Cash", -40.00, "Cash", 1232.20, "Main accounts")
      .add("16/11/2008", "Planned: Gas", -60.00, "Gas", 1272.20, "Main accounts")
      .add("11/11/2008", "Planned: Groceries", -40.00, "Groceries", 1332.20, "Main accounts")
      .add("10/11/2008", "Planned: Health", 12.00, "Health", 1372.20, "Main accounts")
      .add("06/11/2008", "Planned: Fringue", -57.00, "Fringue", 1360.20, "Main accounts")
      .add("06/11/2008", "Planned: Leisures", -10.00, "Leisures", 1417.20, "Main accounts")
      .add("06/11/2008", "Planned: Internet", -29.90, "Internet", 1427.20, "Main accounts")
      .add("06/11/2008", "Planned: Don", -40.00, "Don", 1457.10, "Main accounts")
      .add("05/11/2008", "Planned: Health", 8.00, "Health", 1497.10, "Main accounts")
      .add("01/11/2008", "Planned: Regular savings", -100.00, "Regular savings", 1489.10, "Main accounts")
      .add("01/11/2008", "Planned: Frais banque", -4.00, "Frais banque", 1589.10, "Main accounts")
      .add("01/11/2008", "Planned: Mortgage", -700.00, "Mortgage", 1593.10, "Main accounts")
      .add("01/11/2008", "Planned: Groceries", -60.00, "Groceries", 2293.10, "Main accounts")
      .add("01/11/2008", "Planned: Fringue", -33.00, "Fringue", 2353.10, "Main accounts")
      .add("31/10/2008", "Planned: Revenu", 2000.00, "Revenu", 2386.10, "Main accounts")
      .add("26/10/2008", "Planned: Ecole", -40.00, "Ecole", 386.10, "Main accounts")
      .add("21/10/2008", "Planned: Groceries", -150.00, "Groceries", 426.10, "Main accounts")
      .add("21/10/2008", "Planned: Assurance", -100.00, "Assurance", 576.10, "Main accounts")
      .add("21/10/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 676.10, "Main accounts")
      .add("19/10/2008", "Planned: Health", 20.00, "Health", 716.10, "Main accounts")
      .add("19/10/2008", "Planned: Frais banque", -4.00, "Frais banque", 696.10, "Main accounts")
      .add("19/10/2008", "Planned: Cash", -40.00, "Cash", 700.10, "Main accounts")
      .add("19/10/2008", "Planned: Leisures", -10.00, "Leisures", 740.10, "Main accounts")
      .add("19/10/2008", "Planned: Groceries", -30.00, "Groceries", 750.10, "Main accounts")
      .add("19/10/2008", "GAZ DE FRANCE", -60.00, "Gas", 780.10, 780.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("11/10/2008", "AUCHAN", -30.00, "Groceries", 840.10, 840.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/10/2008", "CHAUSSE MOI", -60.00, "Fringue", 870.10, 870.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/10/2008", "FREE TELECOM", -29.90, "Internet", 930.10, 930.10, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/10/2008", "INSTITUT PASTEUR", -40.00, "Don", 960.00, 960.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/10/2008", "CREDIT", -700.00, "Mortgage", 1000.00, 1000.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/10/2008", "ED", -40.00, "Groceries", 1700.00, 1700.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/10/2008", "CHEQUE N. 34", -30.00, "To categorize", 1740.00, 1740.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/10/2008", "HABILLE MOI", -30.00, "Fringue", 1770.00, 1770.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/10/2008", "EPARGNE", -100.00, "Regular savings", 1800.00, 1800.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("28/09/2008", "ECOLE", -40.00, "Ecole", 1900.00, 1900.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("27/09/2008", "SALAIRE", 2000.00, "Revenu", 1940.00, 1940.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("25/09/2008", "JEUX POUR TOUS", -25.00, "To categorize", -60.00, -60.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("23/09/2008", "COTISATION CARTE BLEUE", -4.00, "Frais banque", -35.00, -35.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("22/09/2008", "COUP'COUP", -30.00, "To categorize", -31.00, -31.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("22/09/2008", "GMF", -100.00, "Assurance", -1.00, -1.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("22/09/2008", "INTERMARCHÉ", -150.00, "Groceries", 99.00, 99.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("21/09/2008", "SFR", -40.00, "Cell phone 1", 249.00, 249.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("20/09/2008", "RETRAIT", -40.00, "Cash", 289.00, 289.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("19/09/2008", "GAZ DE FRANCE", -60.00, "Gas", 329.00, 329.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("11/09/2008", "AUCHAN", -40.00, "Groceries", 389.00, 389.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("10/09/2008", "CHAUSSE MOI", -50.00, "Fringue", 429.00, 429.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("09/09/2008", "IMPOTS", -400.00, "impots", 479.00, 479.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("07/09/2008", "CPAM", 40.00, "Health", 879.00, 879.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("07/09/2008", "CENTRE NAUTIQUE", -10.00, "Leisures", 839.00, 839.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/09/2008", "FREE TELECOM", -29.90, "Internet", 849.00, 849.00, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("06/09/2008", "INSTITUT PASTEUR", -40.00, "Don", 878.90, 878.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/09/2008", "CREDIT", -700.00, "Mortgage", 918.90, 918.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/09/2008", "ED", -60.00, "Groceries", 1618.90, 1618.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("05/09/2008", "CHEQUE N. 32", -50.00, "Health", 1678.90, 1678.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/09/2008", "HABILLE MOI", -30.00, "Fringue", 1728.90, 1728.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("03/09/2008", "EPARGNE", -100.00, "Regular savings", 1758.90, 1758.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .add("02/09/2008", "MUTUELLE", 30.00, "Health", 1858.90, 1858.90, OfxBuilder.DEFAULT_ACCOUNT_NAME)
      .check();


    window.dispose();
  }

  public void testReloadSnapshotV20() throws Exception {
    Window window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        String userId = "2109354532";
        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "budgetview";
        String tmpJarDir = tmpDir + File.separator + "jars";
        File jarDir = new File(tmpJarDir);
        File prevaylerDir = new File(tmpDir + File.separator + "data");
        Files.deleteSubtree(prevaylerDir);
        Files.deleteSubtree(jarDir);
        jarDir.mkdirs();
        prevaylerDir.mkdirs();
        File userDir = new File(prevaylerDir + File.separator + "data" + File.separator + "users");
        userDir.mkdirs();
        File dataDir = new File(prevaylerDir + File.separator + "data" + File.separator + userId);
        dataDir.mkdirs();

        InputStream dataStream =
          getClass().getResourceAsStream("/files/v6/data/" + userId + "/0000000000000000045.snapshot");
        Files.copyStreamTofile(dataStream, dataDir.getAbsolutePath() + "/0000000000000000045.snapshot");

        InputStream usersDataStream =
          getClass().getResourceAsStream("/files/v6/data/users/0000000000000000003.snapshot");
        Files.copyStreamTofile(usersDataStream, userDir.getAbsolutePath() + "/0000000000000000003.snapshot");

        InputStream stream =
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "budgetview.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/budgetview9999999.jar");
        System.setProperty("budgetview.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("budgetview.prevayler.path", prevaylerDir.getAbsolutePath());
        Main.main(new String[0]);
      }
    });
    LoginChecker login = new LoginChecker(window);
    login.logExistingUser("toto", "toto", true);

    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectBudget();

    TimeViewChecker checker = new TimeViewChecker(window);
    checker.selectMonths("2008/10");

    SeriesAnalysisChecker seriesAnalysis = new SeriesAnalysisChecker(window);
    seriesAnalysis.balanceChart.getLeftDataset()
      .checkSize(1)
      .checkValue("Income", 2000.00);
    seriesAnalysis.balanceChart.getRightDataset()
      .checkSize(3)
      .checkValue("Recurring", 1413.90)
      .checkValue("Variable", 370.00)
      .checkValue("Savings", 100.00);

//    BudgetSummaryViewChecker budgetSummary = new BudgetSummaryViewChecker(window);
//    budgetSummary.openPositionDialog()
//      .checkPosition(1886.10)
//      .checkInitialPosition(780.1)
//      .checkIncome(2000)
//      .checkFixed(584)
//      .checkSavingsIn(0)
//      .checkSavingsOut(100)
//      .checkVariable(210)
//      .close();

    checker.selectMonths("2008/10", "2008/11");
    views.selectData();
    TransactionChecker transaction = new TransactionChecker(window);
    transaction
      .showPlannedTransactions()
      .initAmountContent()
      .add("30/11/2008", "Planned: Income 1", 2000.00, "Income 1", 2002.20, "Main accounts")
      .add("26/11/2008", "Planned: Ecole", -40.00, "Ecole", 2.20, "Main accounts")
      .add("21/11/2008", "Planned: Groceries", -150.00, "Groceries", 42.20, "Main accounts")
      .add("21/11/2008", "Planned: Assurance", -100.00, "Assurance", 192.20, "Main accounts")
      .add("21/11/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 292.20, "Main accounts")
      .add("16/11/2008", "Planned: Cash", -40.00, "Cash", 332.20, "Main accounts")
      .add("16/11/2008", "Planned: Gas", -60.00, "Gas", 372.20, "Main accounts")
      .add("11/11/2008", "Planned: Groceries", -40.00, "Groceries", 432.20, "Main accounts")
      .add("10/11/2008", "Planned: Health", 12.00, "Health", 472.20, "Main accounts")
      .add("06/11/2008", "Planned: Fringue", -57.00, "Fringue", 460.20, "Main accounts")
      .add("06/11/2008", "Planned: impots", -400.00, "impots", 517.20, "Main accounts")
      .add("06/11/2008", "Planned: Leisures", -10.00, "Leisures", 917.20, "Main accounts")
      .add("06/11/2008", "Planned: Internet", -29.90, "Internet", 927.20, "Main accounts")
      .add("06/11/2008", "Planned: Don", -40.00, "Don", 957.10, "Main accounts")
      .add("05/11/2008", "Planned: Health", 8.00, "Health", 997.10, "Main accounts")
      .add("01/11/2008", "Planned: Regular savings", -100.00, "Regular savings", 989.10, "Main accounts")
      .add("01/11/2008", "Planned: Frais banque", -4.00, "Frais banque", 1089.10, "Main accounts")
      .add("01/11/2008", "Planned: Mortgage", -700.00, "Mortgage", 1093.10, "Main accounts")
      .add("01/11/2008", "Planned: Groceries", -60.00, "Groceries", 1793.10, "Main accounts")
      .add("01/11/2008", "Planned: Fringue", -33.00, "Fringue", 1853.10, "Main accounts")
      .add("31/10/2008", "Planned: Income 1", 2000.00, "Income 1", 1886.10, "Main accounts")
      .add("26/10/2008", "Planned: Ecole", -40.00, "Ecole", -113.90, "Main accounts")
      .add("21/10/2008", "Planned: Groceries", -150.00, "Groceries", -73.90, "Main accounts")
      .add("21/10/2008", "Planned: Assurance", -100.00, "Assurance", 76.10, "Main accounts")
      .add("21/10/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 176.10, "Main accounts")
      .add("19/10/2008", "Planned: Regular savings", -100.00, "Regular savings", 216.10, "Main accounts")
      .add("19/10/2008", "Planned: Frais banque", -4.00, "Frais banque", 316.10, "Main accounts")
      .add("19/10/2008", "Planned: Cash", -40.00, "Cash", 320.10, "Main accounts")
      .add("19/10/2008", "Planned: impots", -400.00, "impots", 360.10, "Main accounts")
      .add("19/10/2008", "Planned: Leisures", -10.00, "Leisures", 760.10, "Main accounts")
      .add("19/10/2008", "Planned: Groceries", -30.00, "Groceries", 770.10, "Main accounts")
      .add("19/10/2008", "Planned: Health", 20.00, "Health", 800.10, "Main accounts")
      .add("19/10/2008", "GAZ DE FRANCE", -60.00, "Gas", 780.10, 780.10, "Account n. 00001123")
      .add("11/10/2008", "AUCHAN", -30.00, "Groceries", 840.10, 840.10, "Account n. 00001123")
      .add("10/10/2008", "CHAUSSE MOI", -60.00, "Fringue", 870.10, 870.10, "Account n. 00001123")
      .add("06/10/2008", "FREE TELECOM", -29.90, "Internet", 930.10, 930.10, "Account n. 00001123")
      .add("06/10/2008", "INSTITUT PASTEUR", -40.00, "Don", 960.00, 960.00, "Account n. 00001123")
      .add("05/10/2008", "CREDIT", -700.00, "Mortgage", 1000.00, 1000.00, "Account n. 00001123")
      .add("05/10/2008", "ED", -40.00, "Groceries", 1700.00, 1700.00, "Account n. 00001123")
      .add("05/10/2008", "CHEQUE 34", -30.00, "To categorize", 1740.00, 1740.00, "Account n. 00001123")
      .add("03/10/2008", "HABILLE MOI", -30.00, "Fringue", 1770.00, 1770.00, "Account n. 00001123")
      .add("03/10/2008", "VIR EPARGNE", -100.00, "To categorize", 1800.00, 1800.00, "Account n. 00001123")
      .check();
    window.dispose();
  }

}
