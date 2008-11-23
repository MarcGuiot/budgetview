package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.MasterCategory;

public class DemoGenerationTest extends LoggedInFunctionalTestCase {
  private static final String PREVAYLER_DIR = "tmp/demo/";
  private static final String OFX_PATH = "tmp/demo.ofx";

  protected void setUp() throws Exception {
    super.setUp();
  }

  public static void main(String[] args) throws Exception {

    System.setProperty("uispec4j.test.library", "junit");

    DemoGenerationTest test = new DemoGenerationTest();
    test.setLocalPrevaylerPath(PREVAYLER_DIR);
    test.setInMemory(false);
    test.setDeleteLocalPrevayler(true);
    test.setCurrentMonth("2008/11");
    test.setUp();
    test.test();
    test.tearDown();
    System.exit(0);
  }

  public void test() throws Exception {

    operations.openPreferences().setFutureMonthsCount(12).validate();

    OfxBuilder.init(OFX_PATH)
      .addBankAccount(30006, 10678, "00000123456", 1410.20, "2008/11/15")
        // Income
      .addTransaction("2008/10/28", 2360.50, "WORLDCO")
      .addTransaction("2008/10/29", 1912.80, "BIGCORP")
        // Fixed
      .addTransaction("2008/10/09", -1410.00, "PRET IMMO N.3325566")
      .addTransaction("2008/11/09", -1410.00, "PRET IMMO N.3325566")
      .addTransaction("2008/10/20", -359.75, "PRET CONSO N.6784562 F657")
      .addTransaction("2008/10/13", -83.10, "VROUMBOUM ASSUR. CONTRAT 5G7878HJ")
      .addTransaction("2008/11/15", -83.10, "VROUMBOUM ASSUR. CONTRAT 5G7878HJ")
      .addTransaction("2008/10/05", -270.70, "TRESOR PUBLIC I.R. 23225252323")
      .addTransaction("2008/11/05", -270.70, "TRESOR PUBLIC I.R. 23225252323")
      .addTransaction("2008/10/02", -70.30, "RATP NAVIGO 10/08")
      .addTransaction("2008/11/02", -70.30, "RATP NAVIGO 11/08")
      .addTransaction("2008/10/17", -97.00, "GROUPE SCOLAIRE R.L OCT. 2008")
      .addTransaction("2008/11/17", -97.00, "GROUPE SCOLAIRE R.L NOV. 2008")
      .addTransaction("2008/10/11", -25.50, "TVSAT")
      .addTransaction("2008/11/12", -25.50, "TVSAT")
      .addTransaction("2008/10/08", -45.30, "RED TELECOMS")
      .addTransaction("2008/11/10", -66.10, "RED TELECOMS")
      .addTransaction("2008/10/02", -29.90, "OPTIBOX")
      .addTransaction("2008/11/02", -29.90, "OPTIBOX")
      .addTransaction("2008/10/15", -65.89, "EDF")
        // Envelopes
      .addTransaction("2008/10/02", -122.60, "HYPER M")
      .addTransaction("2008/10/07", -260.30, "HYPER M")
      .addTransaction("2008/10/15", -160.00, "HYPER M")
      .addTransaction("2008/10/23", -260.30, "HYPER M")
      .addTransaction("2008/10/19", -35.50, "BIO PLUS")
      .addTransaction("2008/10/11", -41.15, "BIO PLUS")
      .addTransaction("2008/10/08", -20.00, "RETRAIT GAB 4463")
      .addTransaction("2008/10/12", -40.00, "RETRAIT GAB 5234")
      .addTransaction("2008/10/22", -20.00, "RETRAIT GAB 5642")
      .addTransaction("2008/10/30", -20.00, "RETRAIT GAB 0301")
      .addTransaction("2008/11/01", -20.00, "RETRAIT GAB 1867")
      .addTransaction("2008/11/09", -20.00, "RETRAIT GAB 9011")
      .addTransaction("2008/11/02", -18.30, "GROUPE CINE SPECT.")
      .addTransaction("2008/10/10", -65.30, "RESA CONCERTS. N151435")
      .addTransaction("2008/10/08", -5.30, "JOURNAUX 2000")
      .addTransaction("2008/10/16", -3.70, "JOURNAUX 2000")
      .addTransaction("2008/10/24", -12.50, "JOURNAUX 2000")
      .addTransaction("2008/10/11", -155.65, "CHAUSS'MODE")
      .addTransaction("2008/10/26", -69.90, "AU PIED AGILE")
      .addTransaction("2008/10/27", -50.00, "PARIS MODE CENTRE")
      .addTransaction("2008/11/07", -158.00, "PARIS MODE CENTRE")
        // OCCASIONAL
      .addTransaction("2008/10/19", -13.50, "ZINGMAN")
      .addTransaction("2008/11/09", -6.50, "DAILY MAGS")
        // SPECIAL
      .addTransaction("2008/10/28", -680.50, "PLOMBERIE 24/7")
        // SAVINGS
      .addTransaction("2008/10/03", -250.00, "VIRT MENS. LIVRET")
      .addTransaction("2008/11/28", -250.00, "VIRT MENS. LIVRET")
      .save();

    operations.importOfxFile(OFX_PATH);

    views.selectHome();
    mainAccounts.edit("Account n. 00000123456").setAccountName("Compte courant").validate();

    //======== CATEGORIZATION ===========

    views.selectCategorization();

    categorization.setIncome("WORLDCO", "Salaire Marie", true);
    categorization.setIncome("BIGCORP", "Salaire Eric", true);

    categorization.setRecurring("PRET IMMO N.3325566", "Credit immo", MasterCategory.HOUSE, true);
    categorization.setRecurring("PRET CONSO N.6784562 F657", "Credit auto", MasterCategory.TRANSPORTS, true);
    categorization.setRecurring("VROUMBOUM ASSUR. CONTRAT 5G7878HJ", "Assurance auto", MasterCategory.TRANSPORTS, true);
    categorization.setRecurring("TRESOR PUBLIC I.R. 23225252323", "Impots revenu", MasterCategory.TAXES, true);
    categorization.setRecurring("RATP NAVIGO 10/08", "Navigo", MasterCategory.TRANSPORTS, true);
    categorization.setRecurring("GROUPE SCOLAIRE R.L OCT. 2008", "Ecole", MasterCategory.EDUCATION, true);
    categorization.setRecurring("GROUPE SCOLAIRE R.L NOV. 2008", "Ecole", MasterCategory.EDUCATION, false);
    categorization.setRecurring("TVSAT", "TV Sat", MasterCategory.LEISURES, true);
    categorization.setRecurring("RED TELECOMS", "Tel. mobile", MasterCategory.TELECOMS, true);
    categorization.setRecurring("OPTIBOX", "Internet", MasterCategory.TELECOMS, true);
    categorization.setRecurring("EDF", "EDF", MasterCategory.HOUSE, true);

    categorization.setEnvelope("HYPER M", "Courses", MasterCategory.FOOD, true);
    categorization.setEnvelope("BIO PLUS", "Courses", MasterCategory.FOOD, false);
    categorization.setEnvelope("RETRAIT GAB 4463", "Liquide", MasterCategory.CASH, true);

    categorization.setEnvelope("GROUPE CINE SPECT.", "Loisirs", MasterCategory.LEISURES, true);
    categorization.setEnvelope("RESA CONCERTS. N151435", "Loisirs", MasterCategory.LEISURES, false);
    categorization.setEnvelope("JOURNAUX 2000", "Loisirs", MasterCategory.LEISURES, false);

    categorization.setEnvelope("CHAUSS'MODE", "Habillement", MasterCategory.CLOTHING, true);
    categorization.setEnvelope("AU PIED AGILE", "Habillement", MasterCategory.CLOTHING, false);
    categorization.setEnvelope("PARIS MODE CENTRE", "Habillement", MasterCategory.CLOTHING, false);

    categorization.setOccasional("ZINGMAN", MasterCategory.HOUSE);

    categorization.setSpecial("PLOMBERIE 24/7", "Plombier", MasterCategory.HOUSE, true);

    categorization.setSavings("VIRT MENS. LIVRET", "Virt. auto livret", MasterCategory.SAVINGS, true);

    categorization.getGauge().hideProgressMessage();

    //======== SERIES TUNING ===========

    views.selectBudget();
    timeline.selectMonth("2008/10");
    budgetView.hideHelpMessage();
    budgetView.recurring.editSeries("EDF").setTwoMonths().validate();

    budgetView.envelopes.editSeries("Courses")
      .switchToManual()
      .selectAllMonths()
      .setAmount(1000.0)
      .validate();

    budgetView.envelopes.editSeries("Habillement")
      .switchToManual()
      .selectAllMonths()
      .setAmount(200.0)
      .validate();

    timeline.selectMonth("2008/12");
    budgetView.specials.createSeries()
      .setName("Noël")
      .setCategory(MasterCategory.GIFTS)
      .selectAllMonths()
      .setAmount(400)
      .validate();

    timeline.selectMonth("2009/08");
    budgetView.specials.createSeries()
      .setName("Vacances")
      .setCategory(MasterCategory.LEISURES)
      .selectAllMonths()
      .setAmount(2250)
      .validate();

    //======== LEVEL ===========
    views.selectHome();
    balanceSummary.setLimit(4100, false);
  }
}
