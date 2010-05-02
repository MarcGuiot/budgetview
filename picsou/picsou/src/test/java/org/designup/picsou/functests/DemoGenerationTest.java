package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.Month;
import org.designup.picsou.gui.TimeService;

import java.io.File;
import java.util.Locale;

public class DemoGenerationTest extends LoggedInFunctionalTestCase {
  private static final String PREVAYLER_DIR = "tmp/demo/";
  private static final String OFX_PATH = "tmp/demo.ofx";

  private int thirdMonth;
  private int secondMonth;
  private int firstMonth;

  protected void setUp() throws Exception {
    Locale.setDefault(Locale.FRENCH);

    thirdMonth = Month.previous(TimeService.getCurrentMonth());
    secondMonth = Month.previous(thirdMonth);
    firstMonth = Month.previous(secondMonth);

    setCurrentMonth(Month.toString(thirdMonth) + "/20");

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
      .addBankAccount(30066, 10678, "00000123456", 1410.20, third(20))
        // Income
      .addTransaction(first(28), 1760.50, "WORLDCO")
      .addTransaction(first(29), 1312.80, "BIGCORP PAIE " + first(29))
      .addTransaction(second(28), 1760.50, "WORLDCO")
      .addTransaction(second(28), 1312.80, "BIGCORP PAIE " + second(28))
        // Fixed
      .addTransaction(first(9), -1010.00, "PRET IMMO N.3325566")
      .addTransaction(second(9), -1010.00, "PRET IMMO N.3325566")
      .addTransaction(third(9), -1010.00, "PRET IMMO N.3325566")
      .addTransaction(first(20), -289.75, "PRET CONSO N.6784562 F657")
      .addTransaction(second(20), -289.75, "PRET CONSO N.6784562 F657")
      .addTransaction(first(9), -83.10, "VROUMBOUM ASSUR. CONTRAT 5G7878HJ 23 2343TA AA3 A45 43ZQERZ EZR")
      .addTransaction(second(8), second(9), -83.10, "VROUMBOUM ASSUR. CONTRAT 5G7878HJ 23 2343TA AA3 A45 43ZQERZ EZR")
      .addTransaction(third(12), third(14), -83.10, "VROUMBOUM ASSUR. CONTRAT 5G7878HJ 23 2343TA AA3 A45 43ZQERZ EZR")
      .addTransaction(first(5), -110.70, "TRESOR PUBLIC I.R. 23225252323")
      .addTransaction(second(5), -110.70, "TRESOR PUBLIC I.R. 23225252323")
      .addTransaction(third(5), -110.70, "TRESOR PUBLIC I.R. 23225252323")
      .addTransaction(first(1), -70.30, "RATP NAVIGO 10/08")
      .addTransaction(second(2), -70.30, "RATP NAVIGO 10/08")
      .addTransaction(third(2), -70.30, "RATP NAVIGO 11/08")
      .addTransaction(first(17), -67.00, "GROUPE SCOLAIRE R.L OCT. 2008")
      .addTransaction(second(17), -67.00, "GROUPE SCOLAIRE R.L OCT. 2008")
      .addTransaction(third(17), -67.00, "GROUPE SCOLAIRE R.L NOV. 2008")
      .addTransaction(first(11), -25.50, "TVSAT")
      .addTransaction(second(11), -25.50, "TVSAT")
      .addTransaction(third(12), -25.50, "TVSAT")
      .addTransaction(first(8), -45.30, "RED TELECOMS MOBILE")
      .addTransaction(second(8), -45.30, "RED TELECOMS MOBILE")
      .addTransaction(third(10), -66.10, "RED TELECOMS MOBILE")
      .addTransaction(first(3), -29.90, "OPTIBOX ABT INTERNET 2523Z233")
      .addTransaction(second(2), -29.90, "OPTIBOX ABT INTERNET 2523Z233")
      .addTransaction(third(2), -29.90, "OPTIBOX ABT INTERNET 2523Z233")
      .addTransaction(second(15), -65.89, "EDF")
        // Envelopes
      .addTransaction(first(1), first(2), -100.60, "HYPER M")
      .addTransaction(first(7), -230.30, "HYPER M")
      .addTransaction(first(15), -130.00, "HYPER M")
      .addTransaction(first(23), -200.30, "HYPER M")
      .addTransaction(first(29), second(2), -100.60, "HYPER M")
      .addTransaction(second(7), -230.30, "HYPER M")
      .addTransaction(second(15), -130.00, "HYPER M")
      .addTransaction(second(23), -200.30, "HYPER M")
      .addTransaction(third(5), -121.20, "HYPER M")
      .addTransaction(first(17), -35.50, "BIO PLUS")
      .addTransaction(first(9), -37.55, "BIO PLUS")
      .addTransaction(second(19), -35.50, "BIO PLUS")
      .addTransaction(second(11), -41.15, "BIO PLUS")
      .addTransaction(first(7), -20.00, "RETRAIT GAB 4463")
      .addTransaction(first(16), -40.00, "RETRAIT GAB 5234")
      .addTransaction(first(20), -20.00, "RETRAIT GAB 5642")
      .addTransaction(second(8), -20.00, "RETRAIT GAB 4463")
      .addTransaction(second(12), -30.00, "RETRAIT GAB 5234")
      .addTransaction(second(22), -20.00, "RETRAIT GAB 5642")
      .addTransaction(second(30), -20.00, "RETRAIT GAB 0301")
      .addTransaction(third(1), -20.00, "RETRAIT GAB 1867")
      .addTransaction(third(9), -20.00, "RETRAIT GAB 9011")
      .addTransaction(third(2), -18.30, "GROUPE CINE SPECT.")
      .addTransaction(second(10), -35.30, "RESA CONCERTS. N151435")
      .addTransaction(first(10), -19.30, "UGC")
      .addTransaction(first(15), -9.70, "JOURNAUX 2000")
      .addTransaction(second(8), -5.30, "JOURNAUX 2000")
      .addTransaction(second(16), -3.70, "JOURNAUX 2000")
      .addTransaction(second(24), -12.50, "JOURNAUX 2000")
      .addTransaction(second(11), -55.65, "CHAUSS'MODE")
      .addTransaction(second(26), -69.90, "AU PIED AGILE")
      .addTransaction(first(27), -126.00, "PARIS MODE CENTRE")
      .addTransaction(second(27), -50.00, "PARIS MODE CENTRE")
      .addTransaction(third(7), -75.00, "PARIS MODE CENTRE")
      .addTransaction(first(19), -13.50, "ZINGMAN")
      .addTransaction(second(19), -11.50, "ZINGMAN")
      .addTransaction(third(9), -6.50, "DAILY MAGAZINES")
      .addTransaction(first(7), -57.00, "CENTRE MEDICAL DES FLORETTES")
      .addTransaction(first(9), -16.80, "PHARMACIE DES 4 CHEMINS")
      .addTransaction(second(5), 7.80, "REMB. MUTUELLE SANTEPLUS")
      .addTransaction(second(15), 35.00, "REMB. MUTUELLE SANTEPLUS")
      .addTransaction(second(22), 12.50, "REMB. MUTUELLE SANTEPLUS")
      .addTransaction(second(9), -16.80, "PHARMACIE DES 4 CHEMINS")
      .addTransaction(first(7), -57.00, "CENTRE MEDICAL DES FLORETTES")
      .addTransaction(second(8), -35.00, "DR PHU")
      .addTransaction(third(22), 25.80, "REMB. MUTUELLE SANTEPLUS")

        // EXTRAS
      .addTransaction(second(28), -680.50, "PLOMBERIE 24/7")
        // SAVINGS
      .addTransaction(first(5), -200.00, "VIRT MENS. LIVRET A")
      .addTransaction(second(3), -200.00, "VIRT MENS. LIVRET A")
      .addTransaction(third(20), -200.00, "VIRT MENS. LIVRET A")
      .save();

    operations.importOfxFile(OFX_PATH);
    System.out.println("OFX File saved in: " + new File(OFX_PATH).getAbsolutePath());

    views.selectHome();
    mainAccounts.edit("Account n. 00000123456")
      .setAccountName("Compte courant")
      .validate();

    mainAccounts.createNewAccount()
      .setAccountName("Liquide")
      .selectBank("Autre")
      .setUpdateModeToManualInput()
      .setPosition(0.00)
      .validate();

    //======== CATEGORIZATION ===========

    views.selectCategorization();

    categorization.setNewIncome("WORLDCO", "Salaire Marie");
    categorization.setNewIncome("BIGCORP PAIE " + first(29), "Salaire Eric");
    categorization.setNewIncome("BIGCORP PAIE " + second(28), "Salaire Eric");

    categorization.setNewRecurring("PRET IMMO N.3325566", "Credit immo");
    categorization.setNewRecurring("PRET CONSO N.6784562 F657", "Credit auto");
    categorization.setNewRecurring("VROUMBOUM ASSUR. CONTRAT 5G7878HJ 23 2343TA AA3 A45 43ZQERZ EZR", "Assurance auto");
    categorization.setNewRecurring("TRESOR PUBLIC I.R. 23225252323", "Impots revenu");
    categorization.setNewRecurring("RATP NAVIGO 10/08", "Navigo");
    categorization.setNewRecurring("GROUPE SCOLAIRE R.L OCT. 2008", "Ecole");
    categorization.setRecurring("GROUPE SCOLAIRE R.L NOV. 2008", "Ecole");
    categorization.setNewRecurring("TVSAT", "TV Sat");
    categorization.setNewRecurring("RED TELECOMS MOBILE", "Tel. mobile");
    categorization.setNewRecurring("OPTIBOX ABT INTERNET 2523Z233", "Internet");
    categorization.setNewRecurring("EDF", "EDF");

    categorization.setNewVariable("HYPER M", "Courses");
    categorization.setVariable("BIO PLUS", "Courses");
    categorization.selectTransactions("RETRAIT GAB 4463", "RETRAIT GAB 5234", "RETRAIT GAB 0301",
                                      "RETRAIT GAB 5642", "RETRAIT GAB 1867", "RETRAIT GAB 9011")
      .selectVariable().selectNewSeries("Liquide");

    categorization.setNewVariable("GROUPE CINE SPECT.", "Loisirs");
    categorization.setVariable("RESA CONCERTS. N151435", "Loisirs");
    categorization.setVariable("JOURNAUX 2000", "Loisirs");
    categorization.setVariable("UGC", "Loisirs");

    categorization.setNewVariable("CHAUSS'MODE", "Habillement");
    categorization.setVariable("AU PIED AGILE", "Habillement");
    categorization.setVariable("PARIS MODE CENTRE", "Habillement");

    categorization.setNewVariable("ZINGMAN", "Divers");

    categorization.setNewExtra("PLOMBERIE 24/7", "Plombier");

    //  ================ SAVINGS   ================
    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAccountName("Livret")
      .selectBank("ING Direct")
      .setPosition(1000)
      .validate();

    views.selectCategorization();

    categorization.setNewSavings("VIRT MENS. LIVRET A", "Virt. auto livret", "Main accounts", "Livret");

    categorization.getCompletionGauge().hideProgressMessage();

    // Gestion du liquide
    timeline.selectMonth(Month.toString(secondMonth));
    transactionCreation.show()
      .setLabel("Retrait").setAmount(20).setDay(8).create()
      .setLabel("Retrait").setAmount(40).setDay(12).create()
      .setLabel("Retrait").setAmount(20).setDay(22).create()
      .setLabel("Retrait").setAmount(20).setDay(27).create()
      .setLabel("Boulangerie").setAmount(-20.).setDay(28).create()
      .setLabel("Boucherie").setAmount(-40).setDay(28).create()
      .setLabel("Primeur").setAmount(-40).setDay(28).create();

    timeline.selectMonth(Month.toString(thirdMonth));
    transactionCreation
      .setLabel("Retrait").setAmount(20).setDay(1).create()
      .setLabel("Retrait").setAmount(20).setDay(9).create()
      .setLabel("Boulangerie").setAmount(-5).setDay(6).create()
      .setLabel("Primeur").setAmount(-20).setDay(9).create();

    categorization.setNewVariable("Boulangerie", "Tous les jours");
    categorization.setVariable("Primeur", "Tous les jours");
    categorization.setVariable("Boucherie", "Tous les jours");
    categorization.setVariable("Retrait", "Liquide");

    //======== SERIES TUNING ===========

    views.selectBudget();
    timeline.selectMonth(Month.toString(secondMonth));
    budgetView.recurring.editSeries("EDF").setTwoMonths().validate();

    timeline.selectMonth(Month.toString(secondMonth));
    budgetView.variable.editSeries("Courses")
      .switchToManual()
      .selectAllMonths()
      .setAmount(750.0)
      .validate();

    budgetView.variable.editSeries("Habillement")
      .switchToManual()
      .selectAllMonths()
      .setAmount(50.0)
      .validate();

    timeline.selectMonth(Month.toString(Month.next(thirdMonth, 2)));
    budgetView.extras.createSeries()
      .setName("Cadeaux")
      .selectAllMonths()
      .setAmount(150)
      .validate();

    int holidaysMonth = Month.next(thirdMonth, 4);
    timeline.selectMonth(Month.toString(holidaysMonth));
    budgetView.extras.createSeries()
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
    timeline.selectMonth(Month.toString(Month.next(thirdMonth)));
    budgetView.savings.createSeries()
      .setName("Prov. vacances")
      .setFromAccount("Main accounts")
      .setToAccount("Compte provisions")
      .setStartDate(firstMonth)
      .switchToManual()
      .selectAllMonths()
      .setAmount(150)
      .validate();

    budgetView.savings.createSeries()
      .setName("Reglement vacances")
      .setFromAccount("Compte provisions")
      .setToAccount("Main accounts")
      .setIrregular()
      .selectAllMonths()
      .setStartDate(holidaysMonth)
      .setEndDate(holidaysMonth)
      .setAmount(1800)
      .validate();

    views.selectCategorization();

    String outputFile = System.getProperty("outfile");
    if (outputFile != null) {
      File out = new File(outputFile);
      out.delete();
      operations.backup(out.getAbsoluteFile().getAbsolutePath());
    }
  }

  private String first(int day) {
    return Month.toString(firstMonth) + "/" + day;
  }

  private String second(int day) {
    return Month.toString(secondMonth) + "/" + day;
  }

  private String third(int day) {
    return Month.toString(thirdMonth) + "/" + day;
  }
}
