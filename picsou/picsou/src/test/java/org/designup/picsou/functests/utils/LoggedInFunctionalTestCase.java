package org.designup.picsou.functests.utils;

import org.designup.picsou.functests.FunctionalTestCase;
import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.MainPanel;
import org.designup.picsou.gui.MainWindow;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.gui.startup.LoginPanel;
import org.designup.picsou.gui.time.TimeViewPanel;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.initial.DefaultSeriesFactory;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.interception.toolkit.UISpecDisplay;

import java.util.Date;

public abstract class LoggedInFunctionalTestCase extends FunctionalTestCase {
  static protected Window mainWindow;

  public ViewSelectionChecker views;
  public MainAccountViewChecker mainAccounts;
  public SavingsAccountViewChecker savingsAccounts;
  public TimeViewChecker timeline;
  public TransactionChecker transactions;
  public TransactionDetailsChecker transactionDetails;
  public TransactionCreationChecker transactionCreation;
  public OperationChecker operations;
  public TitleChecker title;
  public LicenseActivationChecker license;
  public BudgetViewChecker budgetView;
  public SavingsViewChecker savingsView;
  public NextProjectsChecker nextProjects;
  public CategorizationChecker categorization;
  public SeriesEvolutionChecker seriesEvolution;
  public VersionInfoChecker versionInfo;
  public ActionViewChecker actions;
  public NavigationViewChecker navigation;
  public NotesChecker notes;
  public BackupChecker backup;

  protected GlobRepository repository;

  private Date currentDate = Dates.parse("2008/08/31");
  private boolean isInMemory = true;
  private boolean deleteLocalPrevayler = true;
  private String localPrevaylerPath = FunctionalTestCase.getUrl();

  public static String SOCIETE_GENERALE = "Société Générale";

  private boolean notRegistered = false;
  protected boolean createDefaultSeries = false;
  protected String user = "anonymous";
  protected String password = null;
  private boolean firstLogin = true;

  protected void setUp() throws Exception {
    super.setUp();
    TimeService.setCurrentDate(currentDate);
    BrowsingService.setDummyBrowser(true);
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, localPrevaylerPath);
    System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "");
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, Boolean.toString(deleteLocalPrevayler));
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, Boolean.toString(isInMemory));
    System.setProperty(PicsouApplication.LOG_SOUT, "true");
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty(ConfigService.COM_APP_LICENSE_URL, "");
    System.setProperty(ConfigService.COM_APP_LICENSE_FTP_URL, "");
    DefaultSeriesFactory.AUTO_CREATE_DEFAULT_SERIES = createDefaultSeries;

    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        if (mainWindow == null) {
          if (firstLogin) {
            mainWindow = new StartupChecker().enterMain();
          }
          else {
            mainWindow = WindowInterceptor.run(new Trigger() {
              public void run() throws Exception {
                PicsouApplication.main();
              }
            });
          }
        }
        return mainWindow;
      }
    });

    if (mainWindow != null) {
      LoginChecker login = new LoginChecker(mainWindow);
      login.clickFirstAutoLogin();
    }
    else {
      mainWindow = getMainWindow();
    }
    repository = ((PicsouFrame)mainWindow.getAwtComponent()).getRepository();

    initCheckers();

    if (!notRegistered) {
      LicenseActivationChecker.enterLicense(mainWindow, "admin", "1234");
      operations.openPreferences().setFutureMonthsCount(0).validate();
    }

    selectInitialView();
  }

  protected void selectInitialView() {
    views.selectData();
  }

  protected void setCurrentMonth(String monthId) {
    setCurrentDate(monthId + "/15");
  }

  protected void setCurrentDate(String yyyyMMdd) {
    this.currentDate = Dates.parse(yyyyMMdd);
    TimeService.setCurrentDate(currentDate);
  }

  protected void initCheckers() {
    waitForApplicationToLoad();
    views = new ViewSelectionChecker(mainWindow);
    mainAccounts = new MainAccountViewChecker(mainWindow);
    savingsAccounts = new SavingsAccountViewChecker(mainWindow);
    operations = new OperationChecker(mainWindow);
    backup = new BackupChecker(operations);
    timeline = new TimeViewChecker(mainWindow);
    transactions = new TransactionChecker(mainWindow);
    transactionDetails = new TransactionDetailsChecker(mainWindow);
    transactionCreation = new TransactionCreationChecker(mainWindow);
    title = new TitleChecker(mainWindow);
    budgetView = new BudgetViewChecker(mainWindow);
    savingsView = new SavingsViewChecker(mainWindow);
    nextProjects = new NextProjectsChecker(mainWindow);
    categorization = new CategorizationChecker(mainWindow);
    seriesEvolution = new SeriesEvolutionChecker(mainWindow);
    license = new LicenseActivationChecker(mainWindow);
    versionInfo = new VersionInfoChecker(mainWindow);
    actions = new ActionViewChecker(mainWindow);
    navigation = new NavigationViewChecker(mainWindow);
    notes = new NotesChecker(mainWindow);
  }

  protected void tearDown() throws Exception {
//    GlobRepositoryValidator.run(repository);
//    operations.checkOk();
    try {
      UISpecDisplay.instance().reset();
      if (mainWindow != null && operations != null) {
        operations.deleteUser(password);
      }
    }
    catch (Throwable e) {
//      e.printStackTrace();  // on perd l'assert si il y en a eu un.
      try {
        mainWindow.dispose();
      }
      catch (Throwable e1) {
//        e1.printStackTrace();
      }
      mainWindow = null;
    }
    try {
      super.tearDown();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    views = null;
    mainAccounts = null;
    savingsAccounts = null;
    timeline = null;
    transactions = null;
    transactionDetails = null;
    transactionCreation = null;
    operations = null;
    backup = null;
    title = null;
    versionInfo = null;
    budgetView = null;
    savingsView = null;
    nextProjects = null;
    categorization = null;
    seriesEvolution = null;
    license = null;
    actions = null;
    navigation = null;
    notes = null;
    repository = null;
  }

  public OperationChecker getOperations() {
    return operations;
  }

  public void setInMemory(boolean inMemory) {
    isInMemory = inMemory;
  }

  public void setDeleteLocalPrevayler(boolean deleteLocalPrevayler) {
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, Boolean.toString(deleteLocalPrevayler));
    this.deleteLocalPrevayler = deleteLocalPrevayler;
  }

  public void setLocalPrevaylerPath(String localPrevaylerPath) {
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, localPrevaylerPath);
    this.localPrevaylerPath = localPrevaylerPath;
  }

  public void setNotRegistered() {
    notRegistered = true;
  }

  public void openApplication() throws Exception {
    currentDate = TimeService.getToday();
    operations.backupAndLaunchApplication(LoginPanel.AUTOLOG_USER, LoginPanel.AUTOLOG_USER, currentDate);
  }

  protected void restartApplication(String user, String passwd) throws Exception {
    if (mainWindow != null) {
      mainWindow.dispose();
    }
    this.firstLogin = false;
    mainWindow = null;
    this.user = user;
    this.password = passwd;
    mainWindow = getMainWindow();
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    loginChecker.logExistingUser(user, password, false);
    repository = ((PicsouFrame)mainWindow.getAwtComponent()).getRepository();
    initCheckers();
  }

  protected void restartApplication() throws Exception {
    restartApplication(false);
  }

  public void restartApplication(boolean firstLogin) throws Exception {
    operations.exit();
    mainWindow.dispose();
    mainWindow = null;
    this.firstLogin = firstLogin;
    mainWindow = getMainWindow();

    repository = ((PicsouFrame)mainWindow.getAwtComponent()).getRepository();

    initCheckers();
    if (!notRegistered) {
      LicenseActivationChecker.enterLicense(mainWindow, "admin", "1234");
    }
  }

  public static void resetWindow() {
    if (mainWindow != null) {
      mainWindow.dispose();
      mainWindow = null;
    }
  }

  protected void changeUser(String user, String password) {
    operations.deleteUser(this.password);
    this.user = user;
    this.password = password;
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    loginChecker.logNewUser(user, password);
    initCheckers();
    if (!notRegistered) {
      LicenseActivationChecker.enterLicense(mainWindow, "admin", "1234");
    }
  }

  public String getLocalPrevaylerPath() {
    return localPrevaylerPath;
  }

  protected void checkNoSignpostVisible() {
    BalloonTipTesting.checkNoBalloonTipVisible(mainWindow);
  }

  public void waitForApplicationToLoad() {
    UISpecAssert.waitUntil(mainWindow.containsSwingComponent(TimeViewPanel.class), 10000);
  }

}
