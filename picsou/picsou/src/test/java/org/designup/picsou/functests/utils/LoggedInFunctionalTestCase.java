package org.designup.picsou.functests.utils;

import org.designup.picsou.functests.FunctionalTestCase;
import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.gui.MainWindowLauncher;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Dates;
import org.globsframework.utils.directory.Directory;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class LoggedInFunctionalTestCase extends FunctionalTestCase {
  protected Window mainWindow;

  protected ViewSelectionChecker views;
  protected MainAccountViewChecker mainAccounts;
  protected SavingsAccountViewChecker savingsAccounts;
  protected CategoryChecker categories;
  protected TimeViewChecker timeline;
  protected TransactionChecker transactions;
  protected TransactionDetailsChecker transactionDetails;
  protected OperationChecker operations;
  protected TitleChecker title;
  protected LicenseChecker license;
  protected MonthSummaryChecker monthSummary;
  protected BudgetViewChecker budgetView;
  protected CategorizationChecker categorization;
  protected SeriesViewChecker series;
  protected SeriesEvolutionChecker seriesEvolution;
  protected InfoChecker infochecker;
  protected NavigationViewChecker navigation;

  protected GlobRepository repository;
  private Directory directory;

  private Date currentDate = Dates.parse("2008/08/31");
  private boolean isInMemory = true;
  private boolean deleteLocalPrevayler = true;
  private String localPrevaylerPath = FunctionalTestCase.getUrl();

  static public String SOCIETE_GENERALE = "Société Générale";
  private boolean notRegistered = false;

  protected void setUp() throws Exception {

    super.setUp();
    TimeService.setCurrentDate(currentDate);

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, localPrevaylerPath);
    System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "");
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, Boolean.toString(deleteLocalPrevayler));
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, Boolean.toString(isInMemory));
    System.setProperty(PicsouApplication.LOG_SOUT, "true");
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty(ConfigService.COM_PICSOU_LICENSE_URL, "");
    System.setProperty(ConfigService.COM_PICSOU_LICENSE_FTP_URL, "");

    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        if (mainWindow == null) {
          mainWindow = WindowInterceptor.run(new Trigger() {
            public void run() throws Exception {
              clearDirectory();
              directory = MainWindowLauncher.run("anonymous", "password", null);
            }
          });
        }
        return mainWindow;
      }
    });

    mainWindow = getMainWindow();
    repository = ((PicsouFrame)mainWindow.getAwtComponent()).getRepository();
    initCheckers();
    if (!notRegistered) {
      LicenseChecker.enterLicense(mainWindow, "admin", "zz");
      operations.openPreferences().setFutureMonthsCount(0).validate();
    }
    selectInitialView();
  }

  private void reinitMainWindow() {
    repository = ((PicsouFrame)mainWindow.getAwtComponent()).getRepository();
    ApplicationReset.run(repository);
    initCheckers();
    license.enterLicense("admin", "zz");
    selectInitialView();
  }

  protected void selectInitialView() {
    views.selectData();
  }

  protected void setCurrentMonth(String monthId) {
    setCurrentDate(monthId + "/15");
  }

  protected void setCurrentDate(String date) {
    this.currentDate = Dates.parse(date);
  }

  protected void initCheckers() {
    views = new ViewSelectionChecker(mainWindow);
    mainAccounts = new MainAccountViewChecker(mainWindow);
    savingsAccounts = new SavingsAccountViewChecker(mainWindow);
    operations = new OperationChecker(mainWindow);
    categories = new CategoryChecker(mainWindow);
    timeline = new TimeViewChecker(mainWindow);
    transactions = new TransactionChecker(mainWindow);
    transactionDetails = new TransactionDetailsChecker(mainWindow);
    title = new TitleChecker(mainWindow);
    monthSummary = new MonthSummaryChecker(mainWindow);
    budgetView = new BudgetViewChecker(mainWindow);
    categorization = new CategorizationChecker(mainWindow);
    series = new SeriesViewChecker(mainWindow);
    seriesEvolution = new SeriesEvolutionChecker(mainWindow);
    license = new LicenseChecker(mainWindow);
    infochecker = new InfoChecker(mainWindow);
    navigation = new NavigationViewChecker(mainWindow);
  }

  protected void tearDown() throws Exception {
//    operations.checkOk();
    super.tearDown();
    if (mainWindow != null) {
      mainWindow.dispose();
    }
    mainWindow = null;
    views = null;
    mainAccounts = null;
    savingsAccounts = null;
    categories = null;
    timeline = null;
    transactions = null;
    transactionDetails = null;
    operations = null;
    title = null;
    infochecker = null;
    monthSummary = null;
    budgetView = null;
    categorization = null;
    series = null;
    seriesEvolution = null;
    license = null;
    navigation = null;

    repository = null;
    clearDirectory();
    directory = null;
  }

  private void clearDirectory() {
    if (directory != null) {
      directory.get(ColorService.class).removeAllListeners();
    }
  }

  public OperationChecker getOperations() {
    return operations;
  }

  protected String getCategoryName(MasterCategory master) {
    return GuiChecker.getCategoryName(master);
  }

  public void setInMemory(boolean inMemory) {
    isInMemory = inMemory;
  }

  public void setDeleteLocalPrevayler(boolean deleteLocalPrevayler) {
    this.deleteLocalPrevayler = deleteLocalPrevayler;
  }

  public void setLocalPrevaylerPath(String localPrevaylerPath) {
    this.localPrevaylerPath = localPrevaylerPath;
  }

  public void setNotRegistered() {
    notRegistered = true;
  }

  public void openPicsou() throws IOException, InterruptedException {
    String s = operations.backup("/tmp/");
    System.out.println("LoggedInFunctionalTestCase.openPicsou " + s);
    String javaHome = System.getProperty("java.home");
    String classPath = System.getProperty("java.class.path");
    List<String> args = new ArrayList<String>();
    args.add(javaHome + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "java");
    args.add("-Xdebug");
    args.add("-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005");
    args.add("-cp");
    args.add(classPath);
    args.add("org.designup.picsou.gui.MainWindowLauncher");
    args.add("-Dsplits.editor.enabled=false");
    args.add("-D" + PicsouApplication.APPNAME + "splits.debug.enabled=false");
    args.add("-D" + PicsouApplication.APPNAME + ".log.sout=true");
    args.add("-D" + PicsouApplication.APPNAME + ".today=" + Dates.toMonth(currentDate));
    args.add("-u");
    args.add("anonymous");
    args.add("-p");
    args.add("password");
    args.add("-s");
    args.add(s);
    Process process = Runtime.getRuntime().exec(args.toArray(new String[args.size()]));
    BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    while (true) {
      Thread.sleep(10);
      try {
        while (inputReader.ready()) {
          String line = inputReader.readLine();
          System.out.println(line);
        }
      }
      catch (IOException e) {
      }
      while (errorReader.ready()) {
        String line = errorReader.readLine();
        System.err.println(line);
      }
      try {
        process.exitValue();
        return;
      }
      catch (IllegalThreadStateException e) {
      }
    }
  }

  protected void restartApplication() {
    mainWindow.dispose();
    mainWindow = null;
    mainWindow = getMainWindow();
    initCheckers();
  }
}
