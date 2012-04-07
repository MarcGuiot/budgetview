package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.MainWindow;
import org.designup.picsou.model.Month;
import org.designup.picsou.utils.Lang;

import java.io.File;
import java.util.Date;
import java.util.Locale;

public class DemoGenerationTest extends LoggedInFunctionalTestCase {

  private static final String LANG = "fr";

  private static final String PREVAYLER_DIR = "tmp/demo/";
  private static final String OFX_PATH = "tmp/demo.ofx";
  private static final String OFX_UPDATE_PATH = "tmp/demo_update.ofx";
  private static final String OFX_SAVINGS_PATH = "tmp/demo_savings.ofx";
  private static final String SNAPSHOT_PATH = "tmp/demo.budgetview";

  private int fourthMonth;
  private int thirdMonth;
  private int secondMonth;
  private int firstMonth;

  private static Locale DEFAULT_LOCALE = new Locale("__", "", "");

  protected void setUp() throws Exception {
    Locale.setDefault(DEFAULT_LOCALE);

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

    operations.openPreferences().setFutureMonthsCount(12).validate();
  }

  protected Locale getDefaultLocale() {
    return DEFAULT_LOCALE;
  }

  private static Locale getLocale() {
    String lang = System.getProperty("LANG", LANG); // sert dans le picsou/pom.xml
    return lang.equalsIgnoreCase("fr") ? Locale.FRANCE : Locale.ENGLISH;
  }

  public static void main(String[] args) throws Exception {

    DEFAULT_LOCALE = getLocale();
    Lang.setLocale(DEFAULT_LOCALE);

    System.setProperty("uispec4j.test.library", "junit");

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

    operations.hideSignposts();

    OfxBuilder.init(OFX_PATH)
      .addBankAccount(30066, 10678, "00000123456", 1410.20, third(20))
        // Income
      .addTransaction(first(28), 1760.50, transaction("salary1"))
      .addTransaction(first(29), 1312.80, transaction("salary2", first(29)))
      .addTransaction(second(28), 1760.50, transaction("salary1"))
      .addTransaction(second(28), 1312.80, transaction("salary2", second(28)))
        // Fixed
      .addTransaction(first(9), -1010.00, transaction("house"))
      .addTransaction(second(9), -1010.00, transaction("house"))
      .addTransaction(third(9), -1010.00, transaction("house"))
      .addTransaction(first(20), -195.75, transaction("car.loan"))
      .addTransaction(second(20), -195.75, transaction("car.loan"))
      .addTransaction(first(9), -83.10, transaction("car.insurance"))
      .addTransaction(second(8), second(9), -83.10, transaction("car.insurance"))
      .addTransaction(third(12), third(14), -83.10, transaction("car.insurance"))
      .addTransaction(first(5), -130.70, transaction("taxes"))
      .addTransaction(second(5), -130.70, transaction("taxes"))
      .addTransaction(third(5), -130.70, transaction("taxes"))
      .addTransaction(first(1), -70.30, transaction("train"))
      .addTransaction(second(2), -70.30, transaction("train"))
      .addTransaction(third(2), -70.30, transaction("train"))
      .addTransaction(first(17), -157.00, transaction("school", "OCT"))
      .addTransaction(second(17), -157.00, transaction("school", "NOV"))
      .addTransaction(third(17), -157.00, transaction("school", "DEC"))
      .addTransaction(first(11), -25.50, transaction("tv"))
      .addTransaction(second(11), -25.50, transaction("tv"))
      .addTransaction(third(12), -25.50, transaction("tv"))
      .addTransaction(first(8), -45.30, transaction("mobile"))
      .addTransaction(second(8), -45.30, transaction("mobile"))
      .addTransaction(third(10), -66.10, transaction("mobile"))
      .addTransaction(first(3), -29.90, transaction("internet"))
      .addTransaction(second(2), -29.90, transaction("internet"))
      .addTransaction(third(2), -29.90, transaction("internet"))
      .addTransaction(second(15), -65.89, transaction("electricity"))
        // Envelopes
      .addTransaction(first(1), first(2), -100.60, transaction("groceries1"))
      .addTransaction(first(7), -230.30, transaction("groceries1"))
      .addTransaction(first(15), -130.00, transaction("groceries1"))
      .addTransaction(first(23), -200.30, transaction("groceries1"))
      .addTransaction(first(29), second(2), -100.60, transaction("groceries1"))
      .addTransaction(second(7), -230.30, transaction("groceries1"))
      .addTransaction(second(15), -130.00, transaction("groceries1"))
      .addTransaction(second(23), -200.30, transaction("groceries1"))
      .addTransaction(third(5), -121.20, transaction("groceries1"))
      .addTransaction(first(17), -35.50, transaction("groceries2"))
      .addTransaction(first(9), -37.55, transaction("groceries2"))
      .addTransaction(second(19), -35.50, transaction("groceries2"))
      .addTransaction(second(11), -41.15, transaction("groceries2"))
      .addTransaction(first(7), -20.00, transaction("cash", "4463"))
      .addTransaction(first(16), -40.00, transaction("cash", "5234"))
      .addTransaction(first(20), -20.00, transaction("cash", "5642"))
      .addTransaction(second(8), -20.00, transaction("cash", "4463"))
      .addTransaction(second(12), -30.00, transaction("cash", "5234"))
      .addTransaction(second(22), -20.00, transaction("cash", "5642"))
      .addTransaction(second(30), -20.00, transaction("cash", "0301"))
      .addTransaction(third(1), -20.00, transaction("cash", "1867"))
      .addTransaction(third(9), -20.00, transaction("cash", "9011"))
      .addTransaction(third(2), -18.30, transaction("movies"))
      .addTransaction(second(10), -35.30, transaction("concert", "N151435"))
      .addTransaction(first(10), -19.30, transaction("movies2"))
      .addTransaction(first(15), -9.70, transaction("newspaper"))
      .addTransaction(second(8), -5.30, transaction("newspaper"))
      .addTransaction(second(16), -3.70, transaction("newspaper"))
      .addTransaction(second(24), -12.50, transaction("newspaper"))
      .addTransaction(second(11), -55.65, transaction("shoes"))
      .addTransaction(second(26), -69.90, transaction("shoes2"))
      .addTransaction(first(27), -126.00, transaction("clothing"))
      .addTransaction(second(27), -50.00, transaction("clothing"))
      .addTransaction(third(7), -75.00, transaction("clothing"))
      .addTransaction(first(19), -13.50, transaction("misc"))
      .addTransaction(second(19), -11.50, transaction("misc"))
      .addTransaction(third(9), -6.50, transaction("newspaper2"))
      .addTransaction(first(7), -57.00, transaction("health1"))
      .addTransaction(first(9), -16.80, transaction("health2"))
      .addTransaction(second(5), 7.80, transaction("health.reimbursements"))
      .addTransaction(second(15), 35.00, transaction("health.reimbursements"))
      .addTransaction(second(22), 12.50, transaction("health.reimbursements"))
      .addTransaction(second(9), -16.80, transaction("health2"))
      .addTransaction(first(7), -57.00, transaction("health1"))
      .addTransaction(second(8), -35.00, transaction("health.doctor"))
      .addTransaction(third(19), 25.80, transaction("health.reimbursements"))
      .addTransaction(first(19), 900, transaction("house2"))
      .addTransaction(second(17), 500, transaction("house2"))

        // EXTRAS
      .addTransaction(second(28), -680.50, transaction("plumber"))
        // SAVINGS
      .addTransaction(first(5), -200.00, transaction("savings"))
      .addTransaction(second(3), -200.00, transaction("savings"))
      .addTransaction(third(20), -200.00, transaction("savings"))
      .save();

    operations.importOfxFile(OFX_PATH);
    System.out.println("OFX File saved in: " + new File(OFX_PATH).getAbsolutePath());

    OfxBuilder.init(OFX_SAVINGS_PATH)
      .addBankAccount(14559, 22500, "000123321", 1000, third(20))
      .addTransaction(first(5), 200, transaction("savings1"))
      .addTransaction(second(3), 200, transaction("savings2"))
      .addTransaction(third(20), 200, transaction("savings3"))
      .save();
    System.out.println("OFX Savings File saved in: " + new File(OFX_SAVINGS_PATH).getAbsolutePath());

    views.selectHome();
    mainAccounts.edit("00000123456")
      .setAccountName(account("main"))
      .validate();

    mainAccounts.createNewAccount()
      .setAccountName(account("cash"))
      .selectBank("Other")
      .setUpdateModeToManualInput()
      .setPosition(0.00)
      .validate();

    //======== CATEGORIZATION ===========

    views.selectCategorization();

    categorization.setNewIncome(transaction("salary1"), series("salary.mary"));
    categorization.setNewIncome(transaction("salary2", first(29)), series("salary.eric"));
    categorization.setIncome(transaction("salary2", second(28)), series("salary.eric"));

    categorization.setNewRecurring(transaction("house"), series("house"));
    categorization.setNewRecurring(transaction("car.loan"), series("car.loan"));
    categorization.setNewRecurring(transaction("car.insurance"), series("car.insurance"));
    categorization.setNewRecurring(transaction("taxes"), series("taxes"));
    categorization.setNewRecurring(transaction("train"), series("train"));
    categorization.setNewRecurring(transaction("school", "OCT"), series("school"));
    categorization.setRecurring(transaction("school", "NOV"), series("school"));
    categorization.setNewRecurring(transaction("tv"), series("tv"));
    categorization.setNewRecurring(transaction("mobile"), series("mobile"));
    categorization.setNewRecurring(transaction("internet"), series("internet"));
    categorization.setNewRecurring(transaction("electricity"), series("electricity"));

    categorization.setNewVariable(transaction("groceries1"), series("groceries"));
    categorization.setVariable(transaction("groceries2"), series("groceries"));
    categorization.selectTransactions(transaction("cash", "4463"),
                                      transaction("cash", "5234"),
                                      transaction("cash", "0301"),
                                      transaction("cash", "5642"),
                                      transaction("cash", "1867"),
                                      transaction("cash", "9011"))
      .selectVariable().selectNewSeries(series("cash"), 0.00);

    categorization.setNewVariable(transaction("movies"), series("leisures"), -150.00);
    categorization.setVariable(transaction("concert", "N151435"), series("leisures"));
    categorization.setVariable(transaction("newspaper"), series("leisures"));
    categorization.setVariable(transaction("movies2"), series("leisures"));

    categorization.setNewVariable(transaction("shoes"), series("clothing"), -100.00);
    categorization.setVariable(transaction("shoes2"), series("clothing"));
    categorization.setVariable(transaction("clothing"), series("clothing"));

    categorization.setNewVariable(transaction("misc"), series("misc"), -10.00);

    categorization.selectTransaction(transaction("plumber")).selectExtras()
      .createSeries()
      .setName(series("plumber"))
      .setSingleMonth()
      .validate();

    //  ================ SAVINGS   ================

    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAccountName(account("savings"))
      .selectBank("ING Direct")
      .setPosition(1000)
      .validate();

    views.selectCategorization();

    categorization.setNewSavings(transaction("savings"),
                                 series("savings"),
                                 mainAccounts(),
                                 account("savings"));

    budgetView.savings.editSeries(series("savings"))
      .selectFirstMonth()
      .setPropagationEnabled()
      .setAmount(200)
      .validate();

    // Gestion du liquide
    timeline.selectMonth(Month.toString(secondMonth));
    transactionCreation.show()
      .setLabel(transaction("manual1")).setAmount(20).setDay(8).create()
      .setLabel(transaction("manual1")).setAmount(40).setDay(12).create()
      .setLabel(transaction("manual1")).setAmount(20).setDay(22).create()
      .setLabel(transaction("manual1")).setAmount(20).setDay(27).create()
      .setLabel(transaction("manual2")).setAmount(-20.).setDay(28).create()
      .setLabel(transaction("manual3")).setAmount(-40).setDay(28).create()
      .setLabel(transaction("manual4")).setAmount(-40).setDay(28).create();

    timeline.selectMonth(Month.toString(thirdMonth));
    transactionCreation
      .setLabel(transaction("manual1")).setAmount(20).setDay(1).create()
      .setLabel(transaction("manual1")).setAmount(20).setDay(9).create()
      .setLabel(transaction("manual2")).setAmount(-5).setDay(6).create()
      .setLabel(transaction("manual4")).setAmount(-20).setDay(9).create();

    categorization.setNewVariable(transaction("manual2"), series("manual.daily"), -30.00);
    categorization.setVariable(transaction("manual4"), series("manual.daily"));
    categorization.setVariable(transaction("manual3"), series("manual.daily"));
    categorization.setVariable(transaction("manual1"), series("cash"));

    //======== SERIES TUNING ===========

    views.selectBudget();
    timeline.selectMonth(Month.toString(secondMonth));
    budgetView.recurring.editSeries(series("electricity")).setTwoMonths().validate();

    timeline.selectMonth(Month.toString(secondMonth));
    budgetView.variable.editSeries(series("groceries"))
      .selectAllMonths()
      .setAmount(800.0)
      .validate();

    budgetView.variable.editSeries(series("clothing"))
      .selectAllMonths()
      .setAmount(100.0)
      .validate();

    timeline.selectMonth(Month.toString(Month.next(thirdMonth, 2)));
    budgetView.extras.createSeries()
      .setName(series("gifts"))
      .setPropagationDisabled()
      .setAmount(150)
      .validate();

    //======== "HOUSE RENOVATION" PROJECT ===========

    budgetView.extras.createProject()
      .setName(project("house"))
      .setItem(0, "First", firstMonth, -900.00)
      .addItem(1, "Second", secondMonth, -600.00)
      .validate();

    //======== "TRIP" PROJECT ===========

    int holidaysMonth1 = Month.next(thirdMonth, 2);
    int holidaysMonth2 = Month.next(thirdMonth, 3);
    int holidaysMonth3 = Month.next(thirdMonth, 4);
    timeline.selectMonth(Month.toString(holidaysMonth3));
    budgetView.extras.createProject()
      .setName(project("trip"))
      .setItem(0, "Accomodation reservation", holidaysMonth1, -600.00)
      .addItem(1, "Flight tickets", holidaysMonth1, -450.00)
      .addItem(2, "Equipment", holidaysMonth2, -400.00)
      .addItem(3, "Accomodation", holidaysMonth3, -1000.00)
      .validate();

    //======== PROVISIONS ===========

    views.selectHome();
    savingsAccounts.createNewAccount()
      .setAccountName(account("provisions"))
      .selectBank("CIC")
      .setPosition(1000)
      .validate();

    views.selectBudget();
    timeline.selectMonth(Month.toString(Month.next(thirdMonth)));
    budgetView.savings.createSeries()
      .setName(series("savings.trip"))
      .setFromAccount(mainAccounts())
      .setToAccount(account("provisions"))
      .setStartDate(firstMonth)
      .setEndDate(firstMonth)
      .selectAllMonths()
      .setAmount(400)
      .validate();

    budgetView.savings.createSeries()
      .setName(series("trip.payment"))
      .setFromAccount(account("provisions"))
      .setToAccount(mainAccounts())
      .setIrregular()
      .setPropagationDisabled()
      .selectMonth(holidaysMonth1)
      .setAmount(700)
      .selectMonth(holidaysMonth2)
      .setAmount(600)
      .selectMonth(holidaysMonth3)
      .setAmount(900)
      .validate();

    views.selectCategorization();

    //======== CLEANUP ===========

    budgetView.savings.editSeries(fromAccount("provisions")).deleteCurrentSeries();
    budgetView.savings.editSeries(toAccount("provisions")).deleteCurrentSeries();
    budgetView.savings.editSeries(fromAccount("savings")).deleteCurrentSeries();
    budgetView.savings.editSeries(toAccount("savings")).deleteCurrentSeries();

    operations.hideSignposts();

    //======== BACKUP ===========

    // ne pas supprimer ce code il permet de generer les snaphots de demo qui sont integre a la version
    // cf pom.xml

    String outputFile = System.getProperty("outfile");
    if (outputFile != null) {
      File out = new File(outputFile);
      out.delete();
      operations.backup(out.getAbsoluteFile().getAbsolutePath());
      System.out.println("DemoGenerationTest.test : snapshot generated in " + out.getAbsoluteFile().getAbsolutePath());
    }

    String backupPath = new File(SNAPSHOT_PATH).getAbsolutePath();
    new File(SNAPSHOT_PATH).delete();
    operations.backup(backupPath);
    System.out.println("Backup file saved in: " + backupPath);
  }

  private String toAccount(String accountName) {
    return Lang.get("savings.series.auto.create.name.to.savings", account(accountName));
  }

  private String fromAccount(String accountName) {
    return Lang.get("savings.series.auto.create.name.from.savings", account(accountName));
  }

  public void testCreateNextMonthFile() throws Exception {

    OfxBuilder.init(OFX_UPDATE_PATH)
      .addBankAccount(30066, 10678, "00000123456", 1688.12, fourth(18))
        // Income
      .addTransaction(third(28), 1760.50, transaction("salary1"))
      .addTransaction(third(28), 1312.80, transaction("salary2", third(28)))
        // Fixed
      .addTransaction(fourth(9), -1010.00, transaction("house"))
      .addTransaction(third(21), -189.75, transaction("car.loan"))
      .addTransaction(fourth(18), -189.75, transaction("car.loan"))
      .addTransaction(fourth(13), -83.10, transaction("car.insurance"))
      .addTransaction(fourth(5), -110.70, transaction("taxes"))
      .addTransaction(fourth(2), -70.30, transaction("train"))
      .addTransaction(fourth(12), -25.50, transaction("tv"))
      .addTransaction(fourth(17), -66.10, transaction("mobile"))
      .addTransaction(fourth(2), -29.90, transaction("internet"))
      .addTransaction(fourth(15), -65.89, transaction("electricity"))
        // Envelopes
      .addTransaction(third(15), -105.00, transaction("groceries1"))
      .addTransaction(third(23), -271.30, transaction("groceries1"))
      .addTransaction(third(29), -81.60, transaction("groceries1"))
      .addTransaction(third(18), -98.20, transaction("groceries1"))

      .addTransaction(third(19), -35.50, transaction("groceries2"))
      .addTransaction(fourth(1), -41.15, transaction("groceries2"))
      .addTransaction(fourth(5), -41.15, transaction("groceries2"))

      .addTransaction(third(1), -20.00, transaction("cash", "1867"))
      .addTransaction(third(9), -20.00, transaction("cash", "9011"))
      .addTransaction(fourth(15), -20.00, transaction("cash2"))
      .addTransaction(fourth(10), -35.30, transaction("concert", "N1Y3454"))
      .addTransaction(fourth(17), -19.30, transaction("movies3"))
      .addTransaction(third(15), -13.70, transaction("newspaper"))
      .addTransaction(third(16), -3.70, transaction("newspaper"))
      .addTransaction(third(24), -12.50, transaction("newspaper"))
      .addTransaction(third(17), -55.65, transaction("shoes"))
      .addTransaction(fourth(5), -126.00, transaction("clothing2"))
      .addTransaction(third(27), -50.00, transaction("clothing"))
      .addTransaction(third(29), -6.50, transaction("newspaper2"))
      .addTransaction(fourth(5), -7.50, transaction("newspaper2"))
      .addTransaction(fourth(12), -8.80, transaction("newspaper2"))
      .addTransaction(fourth(12), -14.20, transaction("newspaper3"))
      .addTransaction(fourth(12), -160.20, transaction("hifi"))
      .addTransaction(third(24), -16.80, transaction("health2"))
      .addTransaction(fourth(9), -16.80, transaction("health2"))
      .addTransaction(fourth(15), 35.00, transaction("health.reimbursements"))
      .addTransaction(fourth(22), 12.50, transaction("health.reimbursements"))
      .addTransaction(fourth(5), 7.80, transaction("health.reimbursements"))
      .addTransaction(fourth(9), -16.80, transaction("health2"))
      .addTransaction(fourth(11), -45.00, transaction("health3"))
      .addTransaction(fourth(8), -35.00, transaction("health.doctor"))
      .addTransaction(fourth(22), 25.80, transaction("health.reimbursements"))
        // SAVINGS
      .addTransaction(fourth(17), -200.00, transaction("savings"))
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

  private String transaction(String keySuffix, String... args) {
    return Lang.get("demo.transactions." + keySuffix, args);
  }

  private String series(String keySuffix) {
    return Lang.get("demo.series." + keySuffix);
  }

  private String account(String keySuffix) {
    return Lang.get("demo.account." + keySuffix);
  }

  private String project(String keySuffix) {
    return Lang.get("demo.project." + keySuffix);
  }

  private String mainAccounts() {
    return Lang.get("account.summary.main");
  }
}
