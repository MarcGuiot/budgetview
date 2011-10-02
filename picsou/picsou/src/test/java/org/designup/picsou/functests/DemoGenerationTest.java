package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.model.Month;
import org.designup.picsou.gui.MainWindow;
import org.designup.picsou.utils.Lang;

import java.io.File;
import java.util.Date;
import java.util.Locale;

public class DemoGenerationTest extends LoggedInFunctionalTestCase {
  private static final String PREVAYLER_DIR = "tmp/demo/";
  private static final String OFX_PATH = "tmp/demo.ofx";
  private static final String OFX_UPDATE_PATH = "tmp/demo_update.ofx";
  private static final String OFX_SAVINGS_PATH = "tmp/demo_savings.ofx";
  private static final String SNAPSHOT_PATH = "tmp/demo.budgetview";

  private int fourthMonth;
  private int thirdMonth;
  private int secondMonth;
  private int firstMonth;

  protected void setUp() throws Exception {
    Locale.setDefault(Locale.FRENCH);

    thirdMonth = Month.getMonthId(new Date());
    secondMonth = Month.previous(thirdMonth);
    firstMonth = Month.previous(secondMonth);
    fourthMonth = Month.next(thirdMonth);

    setCurrentMonth(Month.toString(thirdMonth) + "/20");

    super.setUp();

    operations.logout();
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    loginChecker.logNewUser(MainWindow.DEMO_USER_NAME, MainWindow.DEMO_PASSWORD);
    initCheckers();

    Locale.setDefault(Locale.FRENCH);

    operations.openPreferences().setFutureMonthsCount(12).validate();
  }

  public static void main(String[] args) throws Exception {

    System.setProperty("uispec4j.test.library", "junit");

    Locale.setDefault(Lang.ROOT);

    DemoGenerationTest test = createTest();
    test.test();
    test.tearDown();

    DemoGenerationTest test2 = createTest();
    test2.testCreateNextMonthFile();
    test2.tearDown();

    System.exit(0);
  }

  private static DemoGenerationTest createTest() throws Exception {
    DemoGenerationTest test = new DemoGenerationTest();
    test.setLocalPrevaylerPath(PREVAYLER_DIR);
    test.setInMemory(false);
    test.setDeleteLocalPrevayler(true);
    test.setUp();
    return test;
  }

  public void test() throws Exception {

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
      .addTransaction(first(20), -189.75, "PRET CONSO N.6784562 F657")
      .addTransaction(second(20), -189.75, "PRET CONSO N.6784562 F657")
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
      .addTransaction(third(19), 25.80, "REMB. MUTUELLE SANTEPLUS")

        // EXTRAS
      .addTransaction(second(28), -680.50, "PLOMBERIE 24/7")
        // SAVINGS
      .addTransaction(first(5), -200.00, "VIRT MENS. LIVRET A")
      .addTransaction(second(3), -200.00, "VIRT MENS. LIVRET A")
      .addTransaction(third(20), -200.00, "VIRT MENS. LIVRET A")
      .save();

    operations.importOfxFile(OFX_PATH);
    System.out.println("OFX File saved in: " + new File(OFX_PATH).getAbsolutePath());

    OfxBuilder.init(OFX_SAVINGS_PATH)
      .addBankAccount(14559, 22500, "000123321", 1000, third(20))
      .addTransaction(first(5), 200, "VIRT 1")
      .addTransaction(second(3), 200, "VIRT 2")
      .addTransaction(third(20), 200, "VIRT 3")
      .save();
    System.out.println("OFX Savings File saved in: " + new File(OFX_SAVINGS_PATH).getAbsolutePath());

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
      .selectVariable().selectNewSeries("Liquide", 0);

    categorization.setNewVariable("GROUPE CINE SPECT.", "Loisirs", 20.);
    categorization.setVariable("RESA CONCERTS. N151435", "Loisirs");
    categorization.setVariable("JOURNAUX 2000", "Loisirs");
    categorization.setVariable("UGC", "Loisirs");

    categorization.setNewVariable("CHAUSS'MODE", "Habillement");
    categorization.setVariable("AU PIED AGILE", "Habillement");
    categorization.setVariable("PARIS MODE CENTRE", "Habillement");

    categorization.setNewVariable("ZINGMAN", "Divers", 10.);

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

    budgetView.savings.editSeries("Virt. auto livret")
      .selectFirstMonth()
      .setPropagationEnabled()
      .setAmount(200)
      .validate();

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

    categorization.setNewVariable("Boulangerie", "Tous les jours", 30.);
    categorization.setVariable("Primeur", "Tous les jours");
    categorization.setVariable("Boucherie", "Tous les jours");
    categorization.setVariable("Retrait", "Liquide");

    //======== SERIES TUNING ===========

    views.selectBudget();
    timeline.selectMonth(Month.toString(secondMonth));
    budgetView.recurring.editSeries("EDF").setTwoMonths().validate();

    timeline.selectMonth(Month.toString(secondMonth));
    budgetView.variable.editSeries("Courses")
      .selectAllMonths()
      .setAmount(750.0)
      .validate();

    budgetView.variable.editSeries("Habillement")
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

    //======== PROVISIONS ===========

    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAccountName("Provisions")
      .selectBank("CIC")
      .setPosition(1000)
      .validate();

    views.selectBudget();
    timeline.selectMonth(Month.toString(Month.next(thirdMonth)));
    budgetView.savings.createSeries()
      .setName("Prov. vacances")
      .setFromAccount("Main accounts")
      .setToAccount("Provisions")
      .setStartDate(firstMonth)
      .selectAllMonths()
      .setAmount(150)
      .validate();

    budgetView.savings.createSeries()
      .setName("Reglement vacances")
      .setFromAccount("Provisions")
      .setToAccount("Main accounts")
      .setIrregular()
      .selectAllMonths()
      .setStartDate(holidaysMonth)
      .setEndDate(holidaysMonth)
      .setAmount(1800)
      .validate();

    views.selectCategorization();

    String backupPath = new File(OFX_SAVINGS_PATH).getAbsolutePath();
    new File(OFX_SAVINGS_PATH).delete();
    operations.backup(backupPath);
    System.out.println("Backup file saved in: " + backupPath);
  }

  public void testCreateNextMonthFile() throws Exception {

    OfxBuilder.init(OFX_UPDATE_PATH)
      .addBankAccount(30066, 10678, "00000123456", 1688.12, fourth(18))
        // Income
      .addTransaction(third(28), 1760.50, "WORLDCO")
      .addTransaction(third(28), 1312.80, "BIGCORP PAIE " + third(28))
        // Fixed
      .addTransaction(fourth(9), -1010.00, "PRET IMMO N.3325566")
      .addTransaction(third(21), -189.75, "PRET CONSO N.6784562 F657")
      .addTransaction(fourth(18), -189.75, "PRET CONSO N.6784562 F657")
      .addTransaction(fourth(13), -83.10, "VROUMBOUM ASSUR. CONTRAT 5G7878HJ 23 2343TA AA3 A45 43ZQERZ EZR")
      .addTransaction(fourth(5), -110.70, "TRESOR PUBLIC I.R. 23225252323")
      .addTransaction(fourth(2), -70.30, "RATP NAVIGO 07/08")
      .addTransaction(fourth(12), -25.50, "TVSAT")
      .addTransaction(fourth(17), -66.10, "RED TELECOMS MOBILE")
      .addTransaction(fourth(2), -29.90, "OPTIBOX ABT INTERNET 2523Z233")
      .addTransaction(fourth(15), -65.89, "EDF")
        // Envelopes
      .addTransaction(third(15), -105.00, "HYPER M")
      .addTransaction(third(23), -271.30, "HYPER M")
      .addTransaction(third(29), -81.60, "HYPER M")
      .addTransaction(third(18), -98.20, "HYPER M")

      .addTransaction(third(19), -35.50, "BIO PLUS")
      .addTransaction(fourth(1), -41.15, "BIO PLUS")
      .addTransaction(fourth(5), -41.15, "BIO PLUS")

      .addTransaction(third(1), -20.00, "RETRAIT GAB 1867")
      .addTransaction(third(9), -20.00, "RETRAIT GAB 9011")
      .addTransaction(fourth(15), -20.00, "RETRAIT LILLE 29A11")
      .addTransaction(fourth(10), -35.30, "RESA CONCERTS. N1Y3454")
      .addTransaction(fourth(17), -19.30, "CINE MAX BERCY")
      .addTransaction(third(15), -13.70, "JOURNAUX 2000")
      .addTransaction(third(16), -3.70, "JOURNAUX 2000")
      .addTransaction(third(24), -12.50, "JOURNAUX 2000")
      .addTransaction(third(17), -55.65, "CHAUSS'MODE")
      .addTransaction(fourth(5), -126.00, "MOD MOD")
      .addTransaction(third(27), -50.00, "PARIS MODE CENTRE")
      .addTransaction(third(29), -6.50, "DAILY MAGAZINES")
      .addTransaction(fourth(5), -7.50, "DAILY MAGAZINES")
      .addTransaction(fourth(12), -8.80, "DAILY MAGAZINES")
      .addTransaction(fourth(12), -14.20, "677 LEO MAGS")
      .addTransaction(fourth(12), -160.20, "HI-FI MEDIA STORE 632526")
      .addTransaction(third(24), -16.80, "PHARMACIE DES 4 CHEMINS")
      .addTransaction(fourth(9), -16.80, "PHARMACIE DES 4 CHEMINS")
      .addTransaction(fourth(15), 35.00, "REMB. MUTUELLE SANTEPLUS")
      .addTransaction(fourth(22), 12.50, "REMB. MUTUELLE SANTEPLUS")
      .addTransaction(fourth(5), 7.80, "REMB. MUTUELLE SANTEPLUS")
      .addTransaction(fourth(9), -16.80, "PHARMACIE DES 4 CHEMINS")
      .addTransaction(fourth(11), -45.00, "PHARMA DES LYS")
      .addTransaction(fourth(8), -35.00, "DR PHU")
      .addTransaction(fourth(22), 25.80, "REMB. MUTUELLE SANTEPLUS")
        // SAVINGS
      .addTransaction(fourth(17), -200.00, "VIRT MENS. LIVRET A")
      .save();

    System.out.println("OFX File update saved in: " + new File(OFX_UPDATE_PATH).getAbsolutePath());
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

  private String fourth(int day) {
    return Month.toString(fourthMonth) + "/" + day;
  }
}
