package com.cashpilot;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.QifBuilder;
import org.designup.picsou.model.MasterCategory;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.gui.time.TimeView;
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

        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "cashpilot";
        String tmpJarDir = tmpDir + File.separator + "jars";
        File jarDir = new File(tmpJarDir);
        File prevaylerDir = new File(tmpDir + File.separator + "data");
        Files.deleteSubtree(prevaylerDir);
        Files.deleteSubtree(jarDir);
        jarDir.mkdirs();
        prevaylerDir.mkdirs();
        InputStream stream =
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "cashpilot.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/cashpilot1.jar");
        System.setProperty("cashpilot.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("cashpilot.prevayler.path", prevaylerDir.getAbsolutePath());
        com.cashpilot.Main.main(new String[0]);
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
      .add("05/09/2008", TransactionType.CHECK, "CHEQUE N°32", "", -50.00)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "Habille moi", "", -30.00)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "EPARGNE", "", -100.00)
      .add("02/09/2008", TransactionType.VIREMENT, "MUTUELLE", "", 30.00)
      .check();

    views.selectCategorization();
    CategorizationChecker categorization = new CategorizationChecker(window);
    categorization
      .setEnvelope("MUTUELLE", "Health", MasterCategory.HEALTH, false)
      .createAndSetSavings("EPARGNE", "Regular savings", OfxBuilder.DEFAULT_ACCOUNT_NAME, "External account");
    categorization.selectTableRows("Habille moi", "Chausse moi");
    categorization
      .selectEnvelopes()
      .createEnvelopeSeries().setName("Fringue").setCategory(MasterCategory.CLOTHING)
      .validate();
    categorization.setEnvelope("CHEQUE N°32", "Health", MasterCategory.HEALTH, false);

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
      .openCategory().selectCategory(GuiChecker.getCategoryName(MasterCategory.TAXES), true);
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
      .add("07/09/2008", "Leisures", "Centre nautique", -10.0)
      .add("10/09/2008", "Fringue", "Chausse moi", -50.0)
      .add("05/09/2008", "Health", "CHEQUE N°32", -50.0)
      .add("23/09/2008", "Frais banque", "Cotisation carte bleue", -4.0)
      .add("22/09/2008", "Beauty", "Coup'coup", -30.0)
      .add("07/09/2008", "Health", "CPAM", 40.0)
      .add("05/09/2008", "Mortgage", "Credit", -700.0)
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
      .add("25/09/2008", "Gifts", "jeux pour tous", -25.0)
      .add("02/09/2008", "Health", "MUTUELLE", 30.0)
      .add("20/09/2008", "Cash", "Retrait", -40.0)
      .add("27/09/2008", "Income 1", "Salaire", 2000.0)
      .add("21/09/2008", "Cell phone 1", "SFR", -40.0)
      .check();
    CategorizationGaugeChecker categorizationGauge = categorization.getGauge();
    categorizationGauge.checkHidden();

    views.selectHome();
    monhSummary
      .checkEnvelope(360., 360.)
      .checkRecurring(1413.90, 1413.90)
      .checkIncome(2000., 2000.)
      .checkOccasional(55, 55) //126.1)
      .checkSavingsIn(100, 100);

    MainAccountViewChecker mainAccounts = new MainAccountViewChecker(window);
    EstimatedPositionDetailsChecker balance = mainAccounts.openEstimatedPositionDetails();
    balance.checkTotal(1900.)
      .close();

    views.selectData();
    transaction.initContent()
      .add("28/09/2008", TransactionType.PRELEVEMENT, "ECOLE", "", -40.00, "Ecole", MasterCategory.EDUCATION)
      .add("27/09/2008", TransactionType.VIREMENT, "SALAIRE", "", 2000.00, "Income 1", MasterCategory.INCOME)
      .add("25/09/2008", TransactionType.PRELEVEMENT, "JEUX POUR TOUS", "", -25.00, "Occasional", MasterCategory.GIFTS)
      .add("23/09/2008", TransactionType.PRELEVEMENT, "COTISATION CARTE BLEUE", "", -4.00, "Frais banque", MasterCategory.BANK)
      .add("22/09/2008", TransactionType.PRELEVEMENT, "COUP'COUP", "", -30.00, "Occasional", MasterCategory.BEAUTY)
      .add("22/09/2008", TransactionType.PRELEVEMENT, "GMF", "", -100.00, "Assurance", "Assurance all-in-one")
      .add("22/09/2008", TransactionType.PRELEVEMENT, "INTERMARCHÉ", "", -150.00, "Groceries", MasterCategory.FOOD)
      .add("21/09/2008", TransactionType.PRELEVEMENT, "SFR", "", -40.00, "Cell phone 1", MasterCategory.TELECOMS)
      .add("20/09/2008", TransactionType.PRELEVEMENT, "RETRAIT", "", -40.00, "Cash", MasterCategory.CASH)
      .add("19/09/2008", TransactionType.PRELEVEMENT, "GAZ DE FRANCE", "", -60.00, "Gas", "Energy")
      .add("11/09/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -40.00, "Groceries", MasterCategory.FOOD)
      .add("10/09/2008", TransactionType.PRELEVEMENT, "CHAUSSE MOI", "", -50.00, "Fringue", MasterCategory.CLOTHING)
      .add("09/09/2008", TransactionType.PRELEVEMENT, "IMPOTS", "", -400.00, "impots", MasterCategory.TAXES)
      .add("07/09/2008", TransactionType.VIREMENT, "CPAM", "", 40.00, "Health", MasterCategory.HEALTH)
      .add("07/09/2008", TransactionType.PRELEVEMENT, "CENTRE NAUTIQUE", "", -10.00, "Leisures", MasterCategory.LEISURES)
      .add("06/09/2008", TransactionType.PRELEVEMENT, "FREE TELECOM", "", -29.90, "Internet", MasterCategory.TELECOMS)
      .add("06/09/2008", TransactionType.PRELEVEMENT, "INSTITUT PASTEUR", "", -40.00, "Don", MasterCategory.GIFTS)
      .add("05/09/2008", TransactionType.PRELEVEMENT, "CREDIT", "", -700.00, "Mortgage", "Mortgage")
      .add("05/09/2008", TransactionType.PRELEVEMENT, "ED", "", -60.00, "Groceries", MasterCategory.FOOD)
      .add("05/09/2008", TransactionType.CHECK, "CHEQUE N°32", "", -50.00, "Health", MasterCategory.HEALTH)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "HABILLE MOI", "", -30.00, "Fringue", MasterCategory.CLOTHING)
      .add("03/09/2008", TransactionType.PRELEVEMENT, "EPARGNE", "", -100.00, "Regular savings", MasterCategory.SAVINGS)
      .add("02/09/2008", TransactionType.VIREMENT, "MUTUELLE", "", 30.00, "Health", MasterCategory.HEALTH)
      .check();


    transaction.initAmountContent()
      .add("28/09/2008", "ECOLE", -40.00, "Ecole", 1900.00, 1900.00, "Account n. 00001123")
      .add("27/09/2008", "SALAIRE", 2000.00, "Income 1", 1940.00, 1940.00, "Account n. 00001123")
      .add("25/09/2008", "JEUX POUR TOUS", -25.00, "Occasional", -60.00, -60.00, "Account n. 00001123")
      .add("23/09/2008", "COTISATION CARTE BLEUE", -4.00, "Frais banque", -35.00, -35.00, "Account n. 00001123")
      .add("22/09/2008", "COUP'COUP", -30.00, "Occasional", -31.00, -31.00, "Account n. 00001123")
      .add("22/09/2008", "GMF", -100.00, "Assurance", -1.00, -1.00, "Account n. 00001123")
      .add("22/09/2008", "INTERMARCHÉ", -150.00, "Groceries", 99.00, 99.00, "Account n. 00001123")
      .add("21/09/2008", "SFR", -40.00, "Cell phone 1", 249.00, 249.00, "Account n. 00001123")
      .add("20/09/2008", "RETRAIT", -40.00, "Cash", 289.00, 289.00, "Account n. 00001123")
      .add("19/09/2008", "GAZ DE FRANCE", -60.00, "Gas", 329.00, 329.00, "Account n. 00001123")
      .add("11/09/2008", "AUCHAN", -40.00, "Groceries", 389.00, 389.00, "Account n. 00001123")
      .add("10/09/2008", "CHAUSSE MOI", -50.00, "Fringue", 429.00, 429.00, "Account n. 00001123")
      .add("09/09/2008", "IMPOTS", -400.00, "impots", 479.00, 479.00, "Account n. 00001123")
      .add("07/09/2008", "CPAM", 40.00, "Health", 879.00, 879.00, "Account n. 00001123")
      .add("07/09/2008", "CENTRE NAUTIQUE", -10.00, "Leisures", 839.00, 839.00, "Account n. 00001123")
      .add("06/09/2008", "FREE TELECOM", -29.90, "Internet", 849.00, 849.00, "Account n. 00001123")
      .add("06/09/2008", "INSTITUT PASTEUR", -40.00, "Don", 878.90, 878.90, "Account n. 00001123")
      .add("05/09/2008", "CREDIT", -700.00, "Mortgage", 918.90, 918.90, "Account n. 00001123")
      .add("05/09/2008", "ED", -60.00, "Groceries", 1618.90, 1618.90, "Account n. 00001123")
      .add("05/09/2008", "CHEQUE N°32", -50.00, "Health", 1678.90, 1678.90, "Account n. 00001123")
      .add("03/09/2008", "HABILLE MOI", -30.00, "Fringue", 1728.90, 1728.90, "Account n. 00001123")
      .add("03/09/2008", "EPARGNE", -100.00, "Regular savings", 1758.90, 1758.90, "Account n. 00001123")
      .add("02/09/2008", "MUTUELLE", 30.00, "Health", 1858.90, 1858.90, "Account n. 00001123")
      .check();



    TimeViewChecker times = new TimeViewChecker(window);
    times.selectMonth("2008/10");

    transaction.initAmountContent()
      .add("28/10/2008", "Planned: Ecole", -40.00, "Ecole", 1971.10, "Main accounts")
      .add("23/10/2008", "Planned: Frais banque", -4.00, "Frais banque", 2011.10, "Main accounts")
      .add("10/10/2008", "Planned: Fringue", -80.00, "Fringue", 2015.10, "Main accounts")
      .add("09/10/2008", "Planned: impots", -400.00, "impots", 2095.10, "Main accounts")
      .add("06/10/2008", "Planned: Don", -40.00, "Don", 2495.10, "Main accounts")
      .add("03/10/2008", "Planned: Regular savings", -100.00, "Regular savings", 1800.00, 2535.10, "Account n. 00001123")
      .add("01/10/2008", "Planned: Income 1", 2000.00, "Income 1", 2635.10, "Main accounts")
      .add("01/10/2008", "Planned: Occasional", -55.00, "Occasional", 635.10, "Main accounts")
      .add("01/10/2008", "Planned: Assurance", -100.00, "Assurance", 690.10, "Main accounts")
      .add("01/10/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 790.10, "Main accounts")
      .add("01/10/2008", "Planned: Cash", -40.00, "Cash", 830.10, "Main accounts")
      .add("01/10/2008", "Planned: Gas", -60.00, "Gas", 870.10, "Main accounts")
      .add("01/10/2008", "Planned: Leisures", -10.00, "Leisures", 930.10, "Main accounts")
      .add("01/10/2008", "Planned: Internet", -29.90, "Internet", 940.10, "Main accounts")
      .add("01/10/2008", "Planned: Mortgage", -700.00, "Mortgage", 970.00, "Main accounts")
      .add("01/10/2008", "Planned: Groceries", -250.00, "Groceries", 1670.00, "Main accounts")
      .add("01/10/2008", "Planned: Health", 20.00, "Health", 1920.00, "Main accounts")
      .check();


    balance = mainAccounts.openEstimatedPositionDetails();
    balance.checkTotal(1971.10)
      .checkInitialPosition(1900)
      .checkEnvelope(-360)
      .checkFixed(-1413.9)
      .checkSavingsIn(-100)
      .checkIncome(2000)
      .checkOccasional(-55.0)
      .close();

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
    balance.checkTotal(1831.10)
      .checkInitialPosition(780.1)
      .checkIncome(2000)
      .checkFixed(-584)
      .checkSavingsIn(-100)
      .checkEnvelope(-210)
      .checkOccasional(-55);

    views.selectData();
    transaction.initAmountContent()
      .add("28/10/2008", "Planned: Ecole", -40.00, "Ecole", 1831.10, "Main accounts")
      .add("23/10/2008", "Planned: Frais banque", -4.00, "Frais banque", 1871.10, "Main accounts")
      .add("19/10/2008", "Planned: Income 1", 2000.00, "Income 1", 1875.10, "Main accounts")
      .add("19/10/2008", "Planned: Occasional", -55.00, "Occasional", -124.90, "Main accounts")
      .add("19/10/2008", "Planned: Assurance", -100.00, "Assurance", -69.90, "Main accounts")
      .add("19/10/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 30.10, "Main accounts")
      .add("19/10/2008", "Planned: Cash", -40.00, "Cash", 70.10, "Main accounts")
      .add("19/10/2008", "Planned: impots", -400.00, "impots", 110.10, "Main accounts")
      .add("19/10/2008", "Planned: Leisures", -10.00, "Leisures", 510.10, "Main accounts")
      .add("19/10/2008", "Planned: Groceries", -180.00, "Groceries", 520.10, "Main accounts")
      .add("19/10/2008", "Planned: Regular savings", -100.00, "Regular savings", 680.10, 700.10, "Account n. 00001123")
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

  public void testReloadSnapshotV3() throws Exception {
    Window window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        String userId = "795391559";
        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "cashpilot";
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
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "cashpilot.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/cashpilot1.jar");
        System.setProperty("cashpilot.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("cashpilot.prevayler.path", prevaylerDir.getAbsolutePath());
        com.cashpilot.Main.main(new String[0]);
      }
    });
    LoginChecker login = new LoginChecker(window);
    login.logExistingUser("toto", "toto");

    MainAccountViewChecker mainAccounts = new MainAccountViewChecker(window);

    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectHome();
    TimeViewChecker timeView = new TimeViewChecker(window);
    timeView.selectMonth("2008/10");

    EstimatedPositionDetailsChecker balance = mainAccounts.openEstimatedPositionDetails();
    balance.checkTotal(1860.00)
      .checkInitialPosition(780.10)
      .checkIncome(2000)
      .checkFixed(-184)
      .checkSavingsIn(0)
      .checkEnvelope(-210)
      .close();

    views.selectData();

    TransactionChecker transaction = new TransactionChecker(window);
    transaction.initAmountContent()
      .add("31/10/2008", "Planned: Occasional", -526.10, "Occasional", 1860.00, "Main accounts")
      .add("31/10/2008", "Planned: Health", 20.00, "Health", 2386.10, "Main accounts")
      .add("31/10/2008", "Planned: Income 1", 2000.00, "Income 1", 2366.10, "Main accounts")
      .add("31/10/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 366.10, "Main accounts")
      .add("31/10/2008", "Planned: Cash", -40.00, "Cash", 406.10, "Main accounts")
      .add("31/10/2008", "Planned: Leisures", -10.00, "Leisures", 446.10, "Main accounts")
      .add("31/10/2008", "Planned: Groceries", -180.00, "Groceries", 456.10, "Main accounts")
      .add("28/10/2008", "Planned: Ecole", -40.00, "Ecole", 636.10, "Main accounts")
      .add("23/10/2008", "Planned: Frais banque", -4.00, "Frais banque", 676.10, "Main accounts")
      .add("19/10/2008", "Planned: Assurance", -100.00, "Assurance", 680.10, "Main accounts")
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
        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "cashpilot";
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
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "cashpilot.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/cashpilot1.jar");
        System.setProperty("cashpilot.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("cashpilot.prevayler.path", prevaylerDir.getAbsolutePath());
        Main.main(new String[0]);
      }
    });
    LoginChecker login = new LoginChecker(window);
    login.logExistingUser("toto", "toto");

    MainAccountViewChecker mainAccounts = new MainAccountViewChecker(window);
    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectHome();

    EstimatedPositionDetailsChecker balance = mainAccounts.openEstimatedPositionDetails();
    balance.checkTotal(2331.10) //  2366.1)
      .checkOccasional(-55)
      .checkInitialPosition(780.1)
      .checkIncome(2000)
      .checkFixed(-184)
      .checkSavingsIn(0)
      .checkEnvelope(-210)
      .close();

    views.selectData();

    TransactionChecker transaction = new TransactionChecker(window);
    transaction.initAmountContent()
      .add("28/10/2008", "Planned: Ecole", -40.00, "Ecole", 2331.10, "Main accounts")
      .add("23/10/2008", "Planned: Frais banque", -4.00, "Frais banque", 2371.10, "Main accounts")
      .add("19/10/2008", "Planned: Health", 20.00, "Health", 2375.10, "Main accounts")
      .add("19/10/2008", "Planned: Income 1", 2000.00, "Income 1", 2355.10, "Main accounts")
      .add("19/10/2008", "Planned: Occasional", -55.00, "Occasional", 355.10, "Main accounts")
      .add("19/10/2008", "Planned: Assurance", -100.00, "Assurance", 410.10, "Main accounts")
      .add("19/10/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 510.10, "Main accounts")
      .add("19/10/2008", "Planned: Cash", -40.00, "Cash", 550.10, "Main accounts")
      .add("19/10/2008", "Planned: Leisures", -10.00, "Leisures", 590.10, "Main accounts")
      .add("19/10/2008", "Planned: Groceries", -180.00, "Groceries", 600.10, "Main accounts")
      .add("19/10/2008", "GAZ DE FRANCE", -60.00, "Gas", 780.10, 780.10, "Account n. 00001123")
      .add("11/10/2008", "AUCHAN", -30.00, "Groceries", 840.10, 840.10, "Account n. 00001123")
      .add("10/10/2008", "CHAUSSE MOI", -60.00, "Fringue", 870.10, 870.10, "Account n. 00001123")
      .add("06/10/2008", "FREE TELECOM", -29.90, "Internet", 930.10, 930.10, "Account n. 00001123")
      .add("06/10/2008", "INSTITUT PASTEUR", -40.00, "Don", 960.00, 960.00, "Account n. 00001123")
      .add("05/10/2008", "CREDIT", -700.00, "Mortgage", 1000.00, 1000.00, "Account n. 00001123")
      .add("05/10/2008", "ED", -40.00, "Groceries", 1700.00, 1700.00, "Account n. 00001123")
      .add("05/10/2008", "CHEQUE N. 34", -30.00, "To categorize", 1740.00, 1740.00, "Account n. 00001123")
      .add("03/10/2008", "HABILLE MOI", -30.00, "Fringue", 1770.00, 1770.00, "Account n. 00001123")
      .add("03/10/2008", "EPARGNE", -100.00, "Regular savings", 1800.00, 1800.00, "Account n. 00001123")
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
        String tmpDir = System.getProperty("java.io.tmpdir") + File.separator + "cashpilot";
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
          getClass().getResourceAsStream(File.separator + "jars" + File.separator + "cashpilot.jar");
        Files.copyStreamTofile(stream, jarDir.getAbsolutePath() + "/cashpilot1.jar");
        System.setProperty("cashpilot.exe.dir", jarDir.getAbsolutePath());
        System.setProperty("cashpilot.prevayler.path", prevaylerDir.getAbsolutePath());
        Main.main(new String[0]);
      }
    });
    LoginChecker login = new LoginChecker(window);
    login.logExistingUser("toto", "toto");
    MainAccountViewChecker mainAccounts = new MainAccountViewChecker(window);

    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectHome();

    EstimatedPositionDetailsChecker balance = mainAccounts.openEstimatedPositionDetails();
    balance.checkTotal(2331.10) //  2366.1)
      .checkOccasional(-55)
      .checkInitialPosition(780.1)
      .checkIncome(2000)
      .checkFixed(-184)
      .checkSavingsIn(0)
      .checkEnvelope(-210)
      .close();

    views.selectData();

    TransactionChecker transaction = new TransactionChecker(window);
    transaction.initAmountContent()
      .add("28/10/2008", "Planned: Ecole", -40.00, "Ecole", 2331.10, "Main accounts")
      .add("23/10/2008", "Planned: Frais banque", -4.00, "Frais banque", 2371.10, "Main accounts")
      .add("19/10/2008", "Planned: Health", 20.00, "Health", 2375.10, "Main accounts")
      .add("19/10/2008", "Planned: Income 1", 2000.00, "Income 1", 2355.10, "Main accounts")
      .add("19/10/2008", "Planned: Occasional", -55.00, "Occasional", 355.10, "Main accounts")
      .add("19/10/2008", "Planned: Assurance", -100.00, "Assurance", 410.10, "Main accounts")
      .add("19/10/2008", "Planned: Cell phone 1", -40.00, "Cell phone 1", 510.10, "Main accounts")
      .add("19/10/2008", "Planned: Cash", -40.00, "Cash", 550.10, "Main accounts")
      .add("19/10/2008", "Planned: Leisures", -10.00, "Leisures", 590.10, "Main accounts")
      .add("19/10/2008", "Planned: Groceries", -180.00, "Groceries", 600.10, "Main accounts")
      .add("19/10/2008", "GAZ DE FRANCE", -60.00, "Gas", 780.10, 780.10, "Account n. 00001123")
      .add("11/10/2008", "AUCHAN", -30.00, "Groceries", 840.10, 840.10, "Account n. 00001123")
      .add("10/10/2008", "CHAUSSE MOI", -60.00, "Fringue", 870.10, 870.10, "Account n. 00001123")
      .add("06/10/2008", "FREE TELECOM", -29.90, "Internet", 930.10, 930.10, "Account n. 00001123")
      .add("06/10/2008", "INSTITUT PASTEUR", -40.00, "Don", 960.00, 960.00, "Account n. 00001123")
      .add("05/10/2008", "CREDIT", -700.00, "Mortgage", 1000.00, 1000.00, "Account n. 00001123")
      .add("05/10/2008", "ED", -40.00, "Groceries", 1700.00, 1700.00, "Account n. 00001123")
      .add("05/10/2008", "CHEQUE N. 34", -30.00, "To categorize", 1740.00, 1740.00, "Account n. 00001123")
      .add("03/10/2008", "HABILLE MOI", -30.00, "Fringue", 1770.00, 1770.00, "Account n. 00001123")
      .add("03/10/2008", "EPARGNE", -100.00, "Regular savings", 1800.00, 1800.00, "Account n. 00001123")
      .check();

    views.selectBudget();
    BudgetViewChecker budgetView = new BudgetViewChecker(window);
    budgetView.income.editSeries("Income 1").setName("Revenu").validate();

    window.dispose();
  }

}
