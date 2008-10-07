package com.fourmics;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.utils.OfxBuilder;
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
  }

  public void testLoad() throws Exception {
    String file = OfxBuilder.init(this)
      .addTransaction("2008/10/02", 30, "VIR Mutuelle")
      .addTransaction("2008/10/03", -100, "VIR epargne")
      .addTransaction("2008/10/03", -30, "Habille moi")
      .addTransaction("2008/10/05", -50, "Cheque 32")
      .addTransaction("2008/10/05", -60, "ED")
      .addTransaction("2008/10/05", -700, "Credit")
      .addTransaction("2008/10/06", -40, "Institut pasteur")
      .addTransaction("2008/10/06", -29.90, "Free telecom")
      .addTransaction("2008/10/07", -10, "Centre nautique")
      .addTransaction("2008/10/07", 40, "CPAM")
      .addTransaction("2008/10/09", -400, "Impots")
      .addTransaction("2008/10/10", -50, "Chausse moi")
      .addTransaction("2008/10/11", -40, "Auchan")
      .addTransaction("2008/10/19", -60, "Gaz de France")
      .addTransaction("2008/10/20", -40, "Retrait")
      .addTransaction("2008/10/21", -40, "SFR")
      .addTransaction("2008/10/22", -150, "Intermarché")
      .addTransaction("2008/10/22", -100, "GMF")
      .addTransaction("2008/10/23", -4, "Cotisation carte bleue")
      .addTransaction("2008/10/22", -30, "Coup'coup")
      .addTransaction("2008/10/25", -25, "jeux pour tous")
      .addTransaction("2008/10/27", 2000, "Salaire")
      .addTransaction("2008/10/28", -40, "Ecole")
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
      .add("28/10/2008", TransactionType.PRELEVEMENT, "Ecole", "", -40.00)
      .add("27/10/2008", TransactionType.VIREMENT, "Salaire", "", 2000.00)
      .add("25/10/2008", TransactionType.PRELEVEMENT, "jeux pour tous", "", -25.00)
      .add("23/10/2008", TransactionType.PRELEVEMENT, "Cotisation carte bleue", "", -4.00)
      .add("22/10/2008", TransactionType.PRELEVEMENT, "Coup'coup", "", -30.00)
      .add("22/10/2008", TransactionType.PRELEVEMENT, "GMF", "", -100.00)
      .add("22/10/2008", TransactionType.PRELEVEMENT, "Intermarché", "", -150.00)
      .add("21/10/2008", TransactionType.PRELEVEMENT, "SFR", "", -40.00)
      .add("20/10/2008", TransactionType.PRELEVEMENT, "Retrait", "", -40.00)
      .add("19/10/2008", TransactionType.PRELEVEMENT, "Gaz de France", "", -60.00)
      .add("11/10/2008", TransactionType.PRELEVEMENT, "Auchan", "", -40.00)
      .add("10/10/2008", TransactionType.PRELEVEMENT, "Chausse moi", "", -50.00)
      .add("09/10/2008", TransactionType.PRELEVEMENT, "Impots", "", -400.00)
      .add("07/10/2008", TransactionType.VIREMENT, "CPAM", "", 40.00)
      .add("07/10/2008", TransactionType.PRELEVEMENT, "Centre nautique", "", -10.00)
      .add("06/10/2008", TransactionType.PRELEVEMENT, "Free telecom", "", -29.90)
      .add("06/10/2008", TransactionType.PRELEVEMENT, "Institut pasteur", "", -40.00)
      .add("05/10/2008", TransactionType.PRELEVEMENT, "Credit", "", -700.00)
      .add("05/10/2008", TransactionType.PRELEVEMENT, "ED", "", -60.00)
      .add("05/10/2008", TransactionType.CHECK, "CHEQUE N. 32", "", -50.00)
      .add("03/10/2008", TransactionType.PRELEVEMENT, "Habille moi", "", -30.00)
      .add("03/10/2008", TransactionType.VIREMENT, "EPARGNE", "", -100.00)
      .add("02/10/2008", TransactionType.VIREMENT, "MUTUELLE", "", 30.00)
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
      .selectRecurring();
    SeriesEditionDialogChecker series = categorization
      .createRecurringSeries();
    series
      .setName("impots")
      .setCustom().toggleMonth(2, 6, 10)
      .openCategory().selectCategory(DataChecker.getCategoryName(MasterCategory.TAXES), true);
    series.validate();
    categorization
      .setRecurring("Impots", "impots", MasterCategory.TAXES, false)
      .setRecurring("Gaz de France", "gas", MasterCategory.HOUSE, false)
      .setEnvelope("Retrait", "cash", MasterCategory.CASH, false)
      .setRecurring("SFR", "Cell phone 1", MasterCategory.TELECOMS, false)
      .selectRecurring();
    SeriesEditionDialogChecker seriesEdition = categorization.editSeries(true)
      .createSeries();
    CategoryChooserChecker categoryChooser = seriesEdition.setName("Assurance")
      .openCategory();
    categoryChooser.openCategoryEdition()
      .createMasterCategory("Assurance all-in-one")
      .validate();
    categoryChooser.selectCategory("Assurance all-in-one");
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
      .add("11/10/2008", "Groceries", "Auchan", -40.0)
      .add("05/10/2008", "Health", "CHEQUE N. 32", -50.0)
      .add("07/10/2008", "impots", "CPAM", 40.0)
      .add("07/10/2008", "Leisures", "Centre nautique", -10.0)
      .add("10/10/2008", "Fringue", "Chausse moi", -50.0)
      .add("23/10/2008", "Frais banque", "Cotisation carte bleue", -4.0)
      .add("22/10/2008", "Beauty", "Coup'coup", -30.0)
      .add("05/10/2008", "Mortgage", "Credit", -700.0)
      .add("05/10/2008", "Groceries", "ED", -60.0)
      .add("03/10/2008", "Regular savings", "EPARGNE", -100.0)
      .add("28/10/2008", "Ecole", "Ecole", -40.0)
      .add("06/10/2008", "Internet", "Free telecom", -29.9)
      .add("22/10/2008", "Assurance", "GMF", -100.0)
      .add("19/10/2008", "Gas", "Gaz de France", -60.0)
      .add("03/10/2008", "Fringue", "Habille moi", -30.0)
      .add("09/10/2008", "impots", "Impots", -400.0)
      .add("06/10/2008", "Don", "Institut pasteur", -40.0)
      .add("22/10/2008", "Groceries", "Intermarché", -150.0)
      .add("02/10/2008", "Health", "MUTUELLE", 30.0)
      .add("20/10/2008", "Cash", "Retrait", -40.0)
      .add("21/10/2008", "Cell phone 1", "SFR", -40.0)
      .add("27/10/2008", "Income 1", "Salaire", 2000.0)
      .add("25/10/2008", "Gifts", "jeux pour tous", -25.0)
      .check();
    CategorizationGaugeChecker categorizationGauge = categorization.getGauge();
    categorizationGauge.checkHidden();

    views.selectHome();
    monhSummary
      .checkEnvelope(400., 400.)
      .checkRecurring(1373.90, 1013.90)
      .checkIncome(2000., 2000.)
      .checkOccasional(55, 486.1)
      .checkSavings(100, 100);
  }
}
