package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;

import java.io.File;
import java.util.Locale;

public class DemoGenerationTest extends LoggedInFunctionalTestCase {
  private static final String PREVAYLER_DIR = "tmp/demo/";
  private static final String OFX_PATH = "tmp/demo.ofx";

  protected void setUp() throws Exception {
    super.setCurrentMonth("2008/11");
    Locale.setDefault(Locale.FRENCH);
    super.setUp();
  }

  public static void main(String[] args) throws Exception {

    System.setProperty("uispec4j.test.library", "junit");

    Locale.setDefault(Locale.ENGLISH);

    DemoGenerationTest test = new DemoGenerationTest();
    test.setLocalPrevaylerPath(PREVAYLER_DIR);
    test.setInMemory(false);
    test.setDeleteLocalPrevayler(true);
    test.setUp();
    test.test();
    test.tearDown();
    System.exit(0);
  }

  public void test() throws Exception {

    Locale.setDefault(Locale.FRENCH);

    operations.openPreferences().setFutureMonthsCount(12).validate();

    OfxBuilder.init(OFX_PATH)
      .addBankAccount(30066, 10678, "00000123456", 1410.20, "2008/11/15")
        // Income
      .addTransaction("2008/10/28", 1760.50, "WORLDCO")
      .addTransaction("2008/10/29", 1312.80, "BIGCORP")
        // Fixed
      .addTransaction("2008/10/09", -1010.00, "PRET IMMO N.3325566")
      .addTransaction("2008/11/09", -1010.00, "PRET IMMO N.3325566")
      .addTransaction("2008/10/20", -289.75, "PRET CONSO N.6784562 F657")
      .addTransaction("2008/10/13", -83.10, "VROUMBOUM ASSUR. CONTRAT 5G7878HJ")
      .addTransaction("2008/11/15", -83.10, "VROUMBOUM ASSUR. CONTRAT 5G7878HJ")
      .addTransaction("2008/10/05", -110.70, "TRESOR PUBLIC I.R. 23225252323")
      .addTransaction("2008/11/05", -110.70, "TRESOR PUBLIC I.R. 23225252323")
      .addTransaction("2008/10/02", -70.30, "RATP NAVIGO 10/08")
      .addTransaction("2008/11/02", -70.30, "RATP NAVIGO 11/08")
      .addTransaction("2008/10/17", -67.00, "GROUPE SCOLAIRE R.L OCT. 2008")
      .addTransaction("2008/11/17", -67.00, "GROUPE SCOLAIRE R.L NOV. 2008")
      .addTransaction("2008/10/11", -25.50, "TVSAT")
      .addTransaction("2008/11/12", -25.50, "TVSAT")
      .addTransaction("2008/10/08", -45.30, "RED TELECOMS")
      .addTransaction("2008/11/10", -66.10, "RED TELECOMS")
      .addTransaction("2008/10/02", -29.90, "OPTIBOX")
      .addTransaction("2008/11/02", -29.90, "OPTIBOX")
      .addTransaction("2008/10/15", -65.89, "EDF")
        // Envelopes
      .addTransaction("2008/10/02", -100.60, "HYPER M")
      .addTransaction("2008/10/07", -230.30, "HYPER M")
      .addTransaction("2008/10/15", -130.00, "HYPER M")
      .addTransaction("2008/10/23", -200.30, "HYPER M")
      .addTransaction("2008/11/05", -121.20, "HYPER M")
      .addTransaction("2008/10/19", -35.50, "BIO PLUS")
      .addTransaction("2008/10/11", -41.15, "BIO PLUS")
      .addTransaction("2008/10/08", -20.00, "RETRAIT GAB 4463")
      .addTransaction("2008/10/12", -40.00, "RETRAIT GAB 5234")
      .addTransaction("2008/10/22", -20.00, "RETRAIT GAB 5642")
      .addTransaction("2008/10/30", -20.00, "RETRAIT GAB 0301")
      .addTransaction("2008/11/01", -20.00, "RETRAIT GAB 1867")
      .addTransaction("2008/11/09", -20.00, "RETRAIT GAB 9011")
      .addTransaction("2008/11/02", -18.30, "GROUPE CINE SPECT.")
      .addTransaction("2008/10/10", -35.30, "RESA CONCERTS. N151435")
      .addTransaction("2008/10/08", -5.30, "JOURNAUX 2000")
      .addTransaction("2008/10/16", -3.70, "JOURNAUX 2000")
      .addTransaction("2008/10/24", -12.50, "JOURNAUX 2000")
      .addTransaction("2008/10/11", -55.65, "CHAUSS'MODE")
      .addTransaction("2008/10/26", -69.90, "AU PIED AGILE")
      .addTransaction("2008/10/27", -50.00, "PARIS MODE CENTRE")
      .addTransaction("2008/11/07", -75.00, "PARIS MODE CENTRE")
      .addTransaction("2008/10/19", -13.50, "ZINGMAN")
      .addTransaction("2008/11/09", -6.50, "DAILY MAGS")
        // SPECIAL
      .addTransaction("2008/10/28", -680.50, "PLOMBERIE 24/7")
        // SAVINGS
      .addTransaction("2008/10/03", -200.00, "VIRT MENS. LIVRET")
      .addTransaction("2008/11/28", -200.00, "VIRT MENS. LIVRET")
      .save();

    operations.importOfxFile(OFX_PATH);

    views.selectHome();
    mainAccounts.edit("Account n. 00000123456")
      .setAccountName("Compte courant")
      .validate();

    mainAccounts.createNewAccount()
      .setAccountName("Liquide")
      .selectBank("Autre")
      .setUpdateModeToManualInput()
      .setPosition(0.)
      .validate();

    //======== CATEGORIZATION ===========

    views.selectCategorization();

    categorization.setNewIncome("WORLDCO", "Salaire Marie");
    categorization.setNewIncome("BIGCORP", "Salaire Eric");

    categorization.setNewRecurring("PRET IMMO N.3325566", "Credit immo");
    categorization.setNewRecurring("PRET CONSO N.6784562 F657", "Credit auto");
    categorization.setNewRecurring("VROUMBOUM ASSUR. CONTRAT 5G7878HJ", "Assurance auto");
    categorization.setNewRecurring("TRESOR PUBLIC I.R. 23225252323", "Impots revenu");
    categorization.setNewRecurring("RATP NAVIGO 10/08", "Navigo");
    categorization.setNewRecurring("GROUPE SCOLAIRE R.L OCT. 2008", "Ecole");
    categorization.setRecurring("GROUPE SCOLAIRE R.L NOV. 2008", "Ecole");
    categorization.setNewRecurring("TVSAT", "TV Sat");
    categorization.setNewRecurring("RED TELECOMS", "Tel. mobile");
    categorization.setNewRecurring("OPTIBOX", "Internet");
    categorization.setNewRecurring("EDF", "EDF");

    categorization.setNewEnvelope("HYPER M", "Courses");
    categorization.setEnvelope("BIO PLUS", "Courses");
    categorization.selectTransactions("RETRAIT GAB 4463", "RETRAIT GAB 5234", "RETRAIT GAB 0301",
                                   "RETRAIT GAB 5642", "RETRAIT GAB 1867", "RETRAIT GAB 9011")
      .selectEnvelopes().selectNewSeries("Liquide");

    categorization.setNewEnvelope("GROUPE CINE SPECT.", "Loisirs");
    categorization.setEnvelope("RESA CONCERTS. N151435", "Loisirs");
    categorization.setEnvelope("JOURNAUX 2000", "Loisirs");

    categorization.setNewEnvelope("CHAUSS'MODE", "Habillement");
    categorization.setEnvelope("AU PIED AGILE", "Habillement");
    categorization.setEnvelope("PARIS MODE CENTRE", "Habillement");

    categorization.setNewEnvelope("ZINGMAN", "Divers");

    categorization.setNewSpecial("PLOMBERIE 24/7", "Plombier");

    //  ================ SAVINGS   ================
    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAccountName("Livret")
      .selectBank("ING Direct")
      .setPosition(1000)
      .validate();

    views.selectCategorization();
    
    categorization.setNewSavings("VIRT MENS. LIVRET", "Virt. auto livret", "Compte courant", "Livret");

    categorization.getCompletionGauge().hideProgressMessage();

    // Gestion du liquide
    timeline.selectMonth("2008/10");
    transactionCreation.show()
      .setLabel("Retrait").setAmount(20).setDay(8).create()
      .setLabel("Retrait").setAmount(40).setDay(12).create()
      .setLabel("Retrait").setAmount(20).setDay(22).create()
      .setLabel("Retrait").setAmount(20).setDay(30).create()
      .setLabel("Boulangerie").setAmount(-20.).setDay(28).create()
      .setLabel("Boucherie").setAmount(-40).setDay(28).create()
      .setLabel("Primeur").setAmount(-40).setDay(28).create();

    timeline.selectMonth("2008/11");
    transactionCreation
      .setLabel("Retrait").setAmount(20).setDay(1).create()
      .setLabel("Retrait").setAmount(20).setDay(9).create()
      .setLabel("Boulangerie").setAmount(-5).setDay(6).create()
      .setLabel("Primeur").setAmount(-20).setDay(9).create();

    categorization.setNewEnvelope("Boulangerie", "Tous les jours");
    categorization.setEnvelope("Primeur", "Tous les jours");
    categorization.setEnvelope("Boucherie", "Tous les jours");
    categorization.setEnvelope("Retrait", "Liquide");

    //======== SERIES TUNING ===========

    views.selectBudget();
    timeline.selectMonth("2008/10");
    budgetView.hideHelpMessage();
    budgetView.recurring.editSeries("EDF").setTwoMonths().validate();

    budgetView.envelopes.editSeries("Courses")
      .switchToManual()
      .selectAllMonths()
      .setAmount(750.0)
      .validate();

    budgetView.envelopes.editSeries("Habillement")
      .switchToManual()
      .selectAllMonths()
      .setAmount(50.0)
      .validate();

    timeline.selectMonth("2008/12");
    budgetView.specials.createSeries()
      .setName("Noël")
      .selectAllMonths()
      .setAmount(400)
      .validate();

    timeline.selectMonth("2009/08");
    budgetView.specials.createSeries()
      .setName("Vacances")
      .selectAllMonths()
      .setAmount(2250)
      .validate();

    //======== POSITION LEVEL ===========

    views.selectHome();
    mainAccounts.setThreshold(3100);

    //======== SAVINGS ===========


    views.selectBudget();

    //======== PROVISIONS ===========

    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAccountName("Compte provisions")
      .selectBank("CIC")
      .setPosition(1000)
      .validate();

    views.selectBudget();
    timeline.selectMonth("2008/12");
    budgetView.savings.createSeries()
      .setName("Prov. vacances aout")
      .setFromAccount("Compte courant")
      .setToAccount("Compte provisions")
      .setStartDate(200811)
      .setEndDate(200907)
      .switchToManual()
      .selectAllMonths()
      .setAmount(150)
      .validate();

    budgetView.savings.createSeries()
      .setName("Reglement vacances")
      .setFromAccount("Compte provisions")
      .setToAccount("Compte courant")
      .setStartDate(200908)
      .setEndDate(200908)
      .selectAllMonths()
      .setAmount(2500)
      .validate();

    views.selectCategorization();


    String outputFile = System.getProperty("outfile");
    if (outputFile != null) {
      File out = new File(outputFile);
      out.delete();
      operations.backup(out.getAbsoluteFile().getAbsolutePath());
    }
  }
}
