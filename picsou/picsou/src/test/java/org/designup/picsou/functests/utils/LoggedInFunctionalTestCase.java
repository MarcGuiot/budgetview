package org.designup.picsou.functests.utils;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.checkers.components.TimeViewChecker;
import org.designup.picsou.functests.checkers.printing.PrinterChecker;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.startup.LoginPanel;
import org.designup.picsou.gui.startup.components.SingleApplicationInstanceListener;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.gui.time.TimeViewPanel;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.SignpostStatus;
import org.designup.picsou.model.initial.DefaultSeriesFactory;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Dates;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.toolkit.UISpecDisplay;

import javax.swing.*;
import java.util.Date;

public abstract class LoggedInFunctionalTestCase extends FunctionalTestCase {
  protected static Window mainWindow;

  public ScreenChecker screen;
  public ViewSelectionChecker views;
  public TimeViewChecker timeline;
  public OperationChecker operations;
  public MainAccountViewChecker mainAccounts;
  public SavingsAccountViewChecker savingsAccounts;
  public TransactionChecker transactions;
  public TransactionDetailsChecker transactionDetails;
  public TransactionCreationChecker transactionCreation;
  public LicenseActivationChecker license;
  public LicenseMessageChecker licenseMessage;
  public BudgetViewChecker budgetView;
  public SavingsViewChecker savingsView;
  public CategorizationChecker categorization;
  public SeriesAnalysisChecker seriesAnalysis;
  public NewVersionChecker newVersion;
  public DemoMessageChecker demoMessage;
  public ImportPanelChecker importPanel;
  public NotesViewChecker notes;
  public BackupChecker backup;
  public ProjectViewChecker projects;
  public SummaryViewChecker summary;
  public SignpostViewChecker signpostView;
  public FeedbackViewChecker feedbackView;
  public ReconciliationAnnotationChecker reconciliationAnnotations;
  public NotificationsChecker notifications;
  public PrinterChecker printer;

  private ApplicationChecker application;

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
    System.setProperty(PicsouApplication.LOG_TO_SOUT, "true");
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty(ConfigService.COM_APP_LICENSE_URL, "");
    System.setProperty(ConfigService.COM_APP_FTP_URL, "");
    DefaultSeriesFactory.AUTO_CREATE_DEFAULT_SERIES = createDefaultSeries;

    application = new ApplicationChecker();
    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        if (mainWindow == null) {
          if (firstLogin) {
            mainWindow = application.start();
          }
          else {
            mainWindow = application.startWithoutSLA();
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
        .validate();
      operations.openDevOptions()
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

  protected void nextMonth() {
    TimeService.setCurrentDate(Month.toDate(Month.getDay(this.currentDate), Month.next(Month.getMonthId(this.currentDate))));
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
    screen = new ScreenChecker(mainWindow);
    budgetView = new BudgetViewChecker(mainWindow);
    savingsView = new SavingsViewChecker(mainWindow);
    categorization = new CategorizationChecker(mainWindow);
    seriesAnalysis = new SeriesAnalysisChecker(mainWindow);
    license = new LicenseActivationChecker(mainWindow);
    licenseMessage = new LicenseMessageChecker(mainWindow);
    newVersion = new NewVersionChecker(mainWindow);
    demoMessage = new DemoMessageChecker(mainWindow);
    importPanel = new ImportPanelChecker(mainWindow);
    notes = new NotesViewChecker(operations, mainWindow);
    projects = new ProjectViewChecker(mainWindow);
    summary = new SummaryViewChecker(mainWindow);
    signpostView = new SignpostViewChecker(mainWindow);
    feedbackView = new FeedbackViewChecker(mainWindow);
    notifications = new NotificationsChecker(mainWindow);
    reconciliationAnnotations = new ReconciliationAnnotationChecker(mainWindow);
    printer = application.getPrinter();
  }

  protected void tearDown() throws Exception {
//    GlobRepositoryValidator.run(repository);
//    operations.checkDataIsOk();
    try {
      UISpecDisplay.instance().reset();
      if (mainWindow != null && operations != null) {
        operations.deleteUser(password);
      }
    }
    catch (Throwable e) {
//      e.printStackTrace();  // on perd l'assert si il y en a eu un.
      try {
        SwingUtilities.invokeAndWait(new Runnable() {
          public void run() {
            mainWindow.getAwtComponent().setVisible(false);
            mainWindow.dispose();
          }
        });
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
    clearCheckers();
  }

  protected void clearCheckers() {
    views = null;
    mainAccounts = null;
    savingsAccounts = null;
    timeline = null;
    transactions = null;
    transactionDetails = null;
    transactionCreation = null;
    operations = null;
    backup = null;
    screen = null;
    newVersion = null;
    demoMessage = null;
    budgetView = null;
    savingsView = null;
    categorization = null;
    seriesAnalysis = null;
    license = null;
    licenseMessage = null;
    importPanel = null;
    notes = null;
    projects = null;
    summary = null;
    signpostView = null;
    feedbackView = null;
    reconciliationAnnotations = null;
    notifications = null;
    printer = null;
    application = null;
    repository = null;
  }

  public void shutdown() {
    if (application != null) {
      application.dispose();
    }
    mainWindow = null;
    clearCheckers();
  }

  public OperationChecker getOperations() {
    return operations;
  }

  public void setInMemory(boolean inMemory) {
    isInMemory = inMemory;
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, Boolean.toString(isInMemory));
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

  public void openApplication(String user, String password) throws Exception {
    currentDate = TimeService.getToday();
    operations.backupAndLaunchApplication(user, password, currentDate);
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

  public void restartApplication() throws Exception {
    restartApplication(false);
  }

  protected void restartApplicationFromBackup() throws Exception {
    String path = operations.backup(this);
    restartApplication(true);
    operations.restore(path);
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
