package org.designup.picsou.functests.utils;

import org.designup.picsou.functests.FunctionalTestCase;
import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.startup.LoginPanel;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.gui.time.TimeViewPanel;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.initial.DefaultSeriesFactory;
import org.globsframework.model.GlobRepository;
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
  public CategorizationChecker categorization;
  public SeriesAnalysisChecker seriesAnalysis;
  public NewVersionChecker newVersion;
  public ImportPanelChecker importPanel;
  public NotesViewChecker notes;
  public BackupChecker backup;
  public ProjectViewChecker projects;
  public SummaryViewChecker summary;
  public SignpostViewChecker signpostView;
  public FeedbackViewChecker feedbackView;
  public ReconciliationChecker reconciliation;

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
  private boolean initialGuidesShown = false;

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
            mainWindow = new ApplicationChecker().start();
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
      operations.openPreferences()
        .setFutureMonthsCount(0)
        .setPeriodInMonth(4)
        .setMonthBack(1)
        .validate();
    }

    if (!initialGuidesShown) {
      SignpostStatus.setInitialGuidanceCompleted(repository);
    }

    selectInitialView();
  }

  public void setInitialGuidesShown(boolean initialGuidesShown) {
    this.initialGuidesShown = initialGuidesShown;
  }

  protected void disableSignposts() {
    operations.hideSignposts();
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
    savingsView = new SavingsViewChecker(mainWindow);
    categorization = new CategorizationChecker(mainWindow);
    seriesAnalysis = new SeriesAnalysisChecker(mainWindow);
    license = new LicenseActivationChecker(mainWindow);
    newVersion = new NewVersionChecker(mainWindow);
    importPanel = new ImportPanelChecker(mainWindow);
    notes = new NotesViewChecker(operations, mainWindow);
    projects = new ProjectViewChecker(mainWindow);
    summary = new SummaryViewChecker(mainWindow);
    signpostView = new SignpostViewChecker(mainWindow);
    feedbackView = new FeedbackViewChecker(mainWindow);
    reconciliation = new ReconciliationChecker(mainWindow);
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
        mainWindow.getAwtComponent().setVisible(false);
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
    newVersion = null;
    budgetView = null;
    savingsView = null;
    categorization = null;
    seriesAnalysis = null;
    license = null;
    importPanel = null;
    notes = null;
    projects = null;
    summary = null;
    signpostView = null;
    feedbackView = null;
    reconciliation = null;
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
      operations.exit();
      mainWindow.getAwtComponent().setVisible(false);
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
    mainWindow.getAwtComponent().setVisible(false);
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
      mainWindow.getAwtComponent().setVisible(false);
      mainWindow.dispose();
      try {
        Thread.sleep(100);
      }
      catch (InterruptedException e) {
      }
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
