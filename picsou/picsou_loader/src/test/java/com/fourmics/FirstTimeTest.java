package com.fourmics;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.globsframework.utils.Files;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import java.io.File;
import java.io.InputStream;
import java.util.Locale;

public class FirstTimeTest extends UISpecTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    Lang.setLocale(Locale.ENGLISH);
    Locale.setDefault(Locale.ENGLISH);
    System.setProperty("SINGLE_INSTANCE_DISABLED", "true");
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

        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "fourmics";
        String tmpJarDir = tmpDir + File.separator + "jars";
        File jarDir = new File(tmpJarDir);
        File prevaylerDir = new File(tmpDir + File.separator + "data");
        Files.deleteSubtree(prevaylerDir);
        Files.deleteSubtree(jarDir);
        jarDir.mkdirs();
        prevaylerDir.mkdirs();
        InputStream stream =
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "fourmics.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/fourmics1.jar");
        System.setProperty("fourmics.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("fourmics.prevayler.path", prevaylerDir.getAbsolutePath());
        Main.main(new String[0]);
      }
    });
    LoginChecker login = new LoginChecker(window);
    login.logNewUser("toto", "toto");
    MonthSummaryChecker monhSummary = new MonthSummaryChecker(window);
    ImportChecker ofxImport = monhSummary.openImport();
    ofxImport.browseAndSelect(file)
      .acceptFile()
      .completeImport();

    monhSummary.openSeriesWizard()
      .select("Income 1")
      .select("Mortgage")
      .select("Cell phone 1")
      .select("Internet")
      .select("Groceries")
      .select("Leisures")
      .select("Health")
      .select("gas")
      .select("Regular savings")
      .validate();

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
      .add("05/09/2008", TransactionType.CHECK, "CHEQUE N. 32", "", -50.00)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "Habille moi", "", -30.00)
      .add("03/09/2008", TransactionType.VIREMENT, "EPARGNE", "", -100.00)
      .add("02/09/2008", TransactionType.VIREMENT, "MUTUELLE", "", 30.00)
      .check();

    views.selectCategorization();
    CategorizationChecker categorization = new CategorizationChecker(window);
    categorization
      .setEnvelope("MUTUELLE", "Health", MasterCategory.HEALTH, false)
      .setSavings("EPARGNE", "Regular savings", MasterCategory.SAVINGS, false);
    categorization.selectTableRows("Habille moi", "Chausse moi");
    categorization
      .selectEnvelopes()
      .createEnvelopeSeries().setName("Fringue").setCategory(MasterCategory.CLOTHING)
      .validate();
    categorization.setEnvelope("CHEQUE N. 32", "Health", MasterCategory.HEALTH, false);

    categorization.selectTableRows("ED", "Auchan", "Intermarché")
      .selectEnvelopes().selectEnvelopeSeries("Groceries", MasterCategory.FOOD, false)
      .setRecurring("Credit", "Mortgage", MasterCategory.HOUSE, false)
      .setRecurring("Institut pasteur", "Don", MasterCategory.GIFTS, true)
      .setRecurring("Free telecom", "Internet", MasterCategory.TELECOMS, false)
      .setEnvelope("Centre nautique", "Leisures", MasterCategory.LEISURES, false)
      .setEnvelope("CPAM", "Health", MasterCategory.HEALTH, false)
      .selectTableRows("Impots")
      .selectRecurring();
    SeriesEditionDialogChecker series = categorization
      .createRecurringSeries();
    series
      .setName("impots")
      .setCustom().toggleMonth(1, 3, 4, 5, 7, 8, 10, 11, 12)
      .openCategory().selectCategory(DataChecker.getCategoryName(MasterCategory.TAXES), true);
    series.validate();
    categorization
      .setRecurring("Gaz de France", "Gas", MasterCategory.HOUSE, false)
      .setEnvelope("Retrait", "cash", MasterCategory.CASH, false)
      .setRecurring("SFR", "Cell phone 1", MasterCategory.TELECOMS, false)
      .selectRecurring();
    SeriesEditionDialogChecker seriesEdition = categorization.editSeries(true)
      .checkSeriesListContains("Gas", "Cell phone 1")
      .createSeries();
    CategoryChooserChecker categoryChooser = seriesEdition.setName("Assurance")
      .openCategory();
    categoryChooser.openCategoryEdition()
      .createMasterCategory("Assurance all-in-one")
      .validate();
    categoryChooser.selectCategory("Assurance all-in-one", true);
    categoryChooser.checkClosed();
    seriesEdition.checkCategory("Assurance all-in-one");
    seriesEdition.validate();

    categorization
      .selectTableRows("GMF")
      .selectRecurring()
      .selectRecurringSeries("Assurance")
      .setRecurring("Cotisation carte bleue", "Frais banque", MasterCategory.BANK, true)
      .setOccasional("Coup'coup", MasterCategory.BEAUTY)
      .setOccasional("jeux pour tous", MasterCategory.GIFTS)
      .setIncome("Salaire", "Income 1", false)
      .setRecurring("Ecole", "Ecole", MasterCategory.EDUCATION, true);

    categorization.initContent()
      .add("11/09/2008", "Groceries", "Auchan", -40.0)
      .add("05/09/2008", "Health", "CHEQUE N. 32", -50.0)
      .add("07/09/2008", "Health", "CPAM", 40.0)
      .add("07/09/2008", "Leisures", "Centre nautique", -10.0)
      .add("10/09/2008", "Fringue", "Chausse moi", -50.0)
      .add("23/09/2008", "Frais banque", "Cotisation carte bleue", -4.0)
      .add("22/09/2008", "Beauty", "Coup'coup", -30.0)
      .add("05/09/2008", "Mortgage", "Credit", -700.0)
      .add("05/09/2008", "Groceries", "ED", -60.0)
      .add("03/09/2008", "Regular savings", "EPARGNE", -100.0)
      .add("28/09/2008", "Ecole", "Ecole", -40.0)
      .add("06/09/2008", "Internet", "Free telecom", -29.9)
      .add("22/09/2008", "Assurance", "GMF", -100.0)
      .add("19/09/2008", "Gas", "Gaz de France", -60.0)
      .add("03/09/2008", "Fringue", "Habille moi", -30.0)
      .add("09/09/2008", "impots", "Impots", -400.0)
      .add("06/09/2008", "Don", "Institut pasteur", -40.0)
      .add("22/09/2008", "Groceries", "Intermarché", -150.0)
      .add("02/09/2008", "Health", "MUTUELLE", 30.0)
      .add("20/09/2008", "Cash", "Retrait", -40.0)
      .add("21/09/2008", "Cell phone 1", "SFR", -40.0)
      .add("27/09/2008", "Income 1", "Salaire", 2000.0)
      .add("25/09/2008", "Gifts", "jeux pour tous", -25.0)
      .check();
    CategorizationGaugeChecker categorizationGauge = categorization.getGauge();
    categorizationGauge.checkHidden();

    views.selectHome();
    monhSummary
      .checkEnvelope(360., 360.)
      .checkRecurring(1413.90, 1413.90)
      .checkIncome(2000., 2000.)
      .checkOccasional(55, 55) //126.1)
      .checkSavings(100, 100);

    BalanceSummaryChecker balance = new BalanceSummaryChecker(window);
    balance.checkTotal(1900.);

    transaction.initAmountContent()
      .add("Ecole", -40.00, 1900.00, 1900.00)
      .add("Salaire", 2000.00, 1940.00, 1940.00)
      .add("jeux pour tous", -25.00, -60.00, -60.00)
      .add("Cotisation carte bleue", -4.00, -35.00, -35.00)
      .add("Coup'coup", -30.00, -31.00, -31.00)
      .add("GMF", -100.00, -1.00, -1.00)
      .add("Intermarché", -150.00, 99.00, 99.00)
      .add("SFR", -40.00, 249.00, 249.00)
      .add("Retrait", -40.00, 289.00, 289.00)
      .add("Gaz de France", -60.00, 329.00, 329.00)
      .add("Auchan", -40.00, 389.00, 389.00)
      .add("Chausse moi", -50.00, 429.00, 429.00)
      .add("Impots", -400.00, 479.00, 479.00)
      .add("CPAM", 40.00, 879.00, 879.00)
      .add("Centre nautique", -10.00, 839.00, 839.00)
      .add("Free telecom", -29.90, 849.00, 849.00)
      .add("Institut pasteur", -40.00, 878.90, 878.90)
      .add("Credit", -700.00, 918.90, 918.90)
      .add("ED", -60.00, 1618.90, 1618.90)
      .add("CHEQUE N. 32", -50.00, 1678.90, 1678.90)
      .add("Habille moi", -30.00, 1728.90, 1728.90)
      .add("EPARGNE", -100.00, 1758.90, 1758.90)
      .add("MUTUELLE", 30.00, 1858.90, 1858.90)
      .check();


    TimeViewChecker times = new TimeViewChecker(window);
    times.selectMonth("2008/10");

    transaction.initAmountContent()
      .add("Planned: Ecole", -40.00, 2351.10)
      .add("Planned: Frais banque", -4.00, 2391.10)
      .add("Planned: Fringue", -80.00, 2395.10)
      .add("Planned: Don", -40.00, 2475.10)
      .add("Planned: Income 1", 2000.00, 2515.10)
      .add("Planned: Occasional", -55.00, 515.10)
      .add("Planned: Assurance", -100.00, 570.10)
      .add("Planned: Cell phone 1", -40.00, 670.1)
      .add("Planned: Cash", -40.00, 710.10)
      .add("Planned: Gas", -60.00, 750.10)
      .add("Planned: Leisures", -10.00, 810.1)
      .add("Planned: Internet", -29.90, 820.10)
      .add("Planned: Mortgage", -700.00, 850.00)
      .add("Planned: Groceries", -250.00, 1550.00)
      .add("Planned: Regular savings", -100.00, 1800.00)
      .check();

    balance.checkTotal(2351.1)
      .checkBalance(1900)
      .checkEnvelope(-380)
      .checkFixed(-1013.9)
      .checkSavings(-100)
      .checkIncome(2000)
      .checkOccasional(-55.0);

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

    views.selectHome();
    balance.checkTotal(2311.1)
      .checkBalance(780.1)
      .checkIncome(2000)
      .checkFixed(-184)
      .checkSavings(0)
      .checkEnvelope(-230)
      .checkOccasional(-55);

    views.selectData();
    transaction.initAmountContent()
      .add("Planned: Ecole", -40.00, 2311.10)
      .add("Planned: Frais banque", -4.00, 2351.10)
      .add("Planned: Income 1", 2000.00, 2355.10)
      .add("Planned: Occasional", -55.00, 355.10)
      .add("Planned: Assurance", -100.00, 410.10)
      .add("Planned: Cell phone 1", -40.00, 510.10)
      .add("Planned: Cash", -40.00, 550.10)
      .add("Planned: Leisures", -10.00, 590.10)
      .add("Planned: Groceries", -180.00, 600.10)
      .add("Gaz de France", -60.00, 780.10, 780.10)
      .add("Auchan", -30.00, 840.10, 840.10)
      .add("Chausse moi", -60.00, 870.10, 870.10)
      .add("Free telecom", -29.90, 930.10, 930.10)
      .add("Institut pasteur", -40.00, 960.00, 960.00)
      .add("Credit", -700.00, 1000.00, 1000.00)
      .add("ED", -40.00, 1700.00, 1700.00)
      .add("CHEQUE N. 34", -30.00, 1740.00, 1740.00)
      .add("Habille moi", -30.00, 1770.00, 1770.00)
      .add("EPARGNE", -100.00, 1800.00, 1800.00)
      .check();

    window.dispose();
  }

  public void testReloadSnapshotV3() throws Exception {
    Window window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        String userId = "795391559";
        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "fourmics";
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
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "fourmics.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/fourmics1.jar");
        System.setProperty("fourmics.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("fourmics.prevayler.path", prevaylerDir.getAbsolutePath());
        Main.main(new String[0]);
      }
    });
    LoginChecker login = new LoginChecker(window);
    login.logUser("toto", "toto");

    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectHome();
    BalanceSummaryChecker balance = new BalanceSummaryChecker(window);
    balance.checkTotal(2366.1)
      .checkBalance(780.1)
      .checkIncome(2000)
      .checkFixed(-184)
      .checkSavings(0)
      .checkEnvelope(-230);

    views.selectData();

    TransactionChecker transaction = new TransactionChecker(window);
    transaction.initAmountContent()
      .add("Planned: Income 1", 2000.00, 2366.10)
      .add("Planned: Cell phone 1", -40.00, 366.10)
      .add("Planned: Cash", -40.00, 406.10)
      .add("Planned: Leisures", -10.00, 446.10)
      .add("Planned: Groceries", -180.00, 456.10)
      .add("Planned: Ecole", -40.00, 636.10)
      .add("Planned: Frais banque", -4.00, 676.10)
      .add("Planned: Assurance", -100.00, 680.10)
      .add("Gaz de France", -60.00, 780.10, 780.10)
      .add("Auchan", -30.00, 840.10, 840.10)
      .add("Chausse moi", -60.00, 870.10, 870.10)
      .add("Free telecom", -29.90, 930.10, 930.10)
      .add("Institut pasteur", -40.00, 960.00, 960.00)
      .add("Credit", -700.00, 1000.00, 1000.00)
      .add("ED", -40.00, 1700.00, 1700.00)
      .add("CHEQUE N. 34", -30.00, 1740.00, 1740.00)
      .add("Habille moi", -30.00, 1770.00, 1770.00)
      .add("EPARGNE", -100.00, 1800.00, 1800.00)
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
        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "fourmics";
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
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "fourmics.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/fourmics1.jar");
        System.setProperty("fourmics.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("fourmics.prevayler.path", prevaylerDir.getAbsolutePath());
        Main.main(new String[0]);
      }
    });
    LoginChecker login = new LoginChecker(window);
    login.logUser("toto", "toto");

    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectHome();
    BalanceSummaryChecker balance = new BalanceSummaryChecker(window);
    balance.checkTotal(2311.10) //  2366.1)
      .checkOccasional(-55)
      .checkBalance(780.1)
      .checkIncome(2000)
      .checkFixed(-184)
      .checkSavings(0)
      .checkEnvelope(-230);

    views.selectData();

    TransactionChecker transaction = new TransactionChecker(window);
    transaction.initAmountContent()
      .add("Planned: Ecole", -40.00, 2311.10)
      .add("Planned: Frais banque", -4.00, 2351.10)
      .add("Planned: Income 1", 2000.00, 2355.10)
      .add("Planned: Occasional", -55.00, 355.10)
      .add("Planned: Assurance", -100.00, 410.10)
      .add("Planned: Cell phone 1", -40.00, 510.10)
      .add("Planned: Cash", -40.00, 550.10)
      .add("Planned: Leisures", -10.00, 590.10)
      .add("Planned: Groceries", -180.00, 600.10)
      .add("Gaz de France", -60.00, 780.10, 780.10)
      .add("Auchan", -30.00, 840.10, 840.10)
      .add("Chausse moi", -60.00, 870.10, 870.10)
      .add("Free telecom", -29.90, 930.10, 930.10)
      .add("Institut pasteur", -40.00, 960.00, 960.00)
      .add("Credit", -700.00, 1000.00, 1000.00)
      .add("ED", -40.00, 1700.00, 1700.00)
      .add("CHEQUE N. 34", -30.00, 1740.00, 1740.00)
      .add("Habille moi", -30.00, 1770.00, 1770.00)
      .add("EPARGNE", -100.00, 1800.00, 1800.00)
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
        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "fourmics";
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
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "fourmics.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/fourmics1.jar");
        System.setProperty("fourmics.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("fourmics.prevayler.path", prevaylerDir.getAbsolutePath());
        Main.main(new String[0]);
      }
    });
    LoginChecker login = new LoginChecker(window);
    login.logUser("toto", "toto");

    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectHome();
    BalanceSummaryChecker balance = new BalanceSummaryChecker(window);
    balance.checkTotal(2311.10) //  2366.1)
      .checkOccasional(-55)
      .checkBalance(780.1)
      .checkIncome(2000)
      .checkFixed(-184)
      .checkSavings(0)
      .checkEnvelope(-230);

    views.selectData();

    TransactionChecker transaction = new TransactionChecker(window);
    transaction.initAmountContent()
      .add("Planned: Ecole", -40.00, 2311.10)
      .add("Planned: Frais banque", -4.00, 2351.10)
      .add("Planned: Income 1", 2000.00, 2355.10)
      .add("Planned: Occasional", -55.00, 355.10)
      .add("Planned: Assurance", -100.00, 410.10)
      .add("Planned: Cell phone 1", -40.00, 510.10)
      .add("Planned: Cash", -40.00, 550.10)
      .add("Planned: Leisures", -10.00, 590.10)
      .add("Planned: Groceries", -180.00, 600.10)
      .add("Gaz de France", -60.00, 780.10, 780.10)
      .add("Auchan", -30.00, 840.10, 840.10)
      .add("Chausse moi", -60.00, 870.10, 870.10)
      .add("Free telecom", -29.90, 930.10, 930.10)
      .add("Institut pasteur", -40.00, 960.00, 960.00)
      .add("Credit", -700.00, 1000.00, 1000.00)
      .add("ED", -40.00, 1700.00, 1700.00)
      .add("CHEQUE N. 34", -30.00, 1740.00, 1740.00)
      .add("Habille moi", -30.00, 1770.00, 1770.00)
      .add("EPARGNE", -100.00, 1800.00, 1800.00)
      .check();

    views.selectBudget();
    BudgetViewChecker budgetView = new BudgetViewChecker(window);
    budgetView.income.editSeries("Income 1").setName("Revenu").validate();

    window.dispose();
  }

}
