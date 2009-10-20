package org.designup.picsou.functests.utils;

import org.designup.picsou.functests.FunctionalTestCase;
import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.model.Account;
import org.designup.picsou.model.AccountType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobList;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobRepositoryValidator;
import org.globsframework.utils.Dates;
import org.globsframework.metamodel.GlobModel;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import java.util.Date;

public abstract class LoggedInFunctionalTestCase extends FunctionalTestCase {
  static protected Window mainWindow;

  protected ViewSelectionChecker views;
  protected MainAccountViewChecker mainAccounts;
  protected SavingsAccountViewChecker savingsAccounts;
  protected TimeViewChecker timeline;
  protected TransactionChecker transactions;
  protected TransactionDetailsChecker transactionDetails;
  protected TransactionCreationChecker transactionCreation;
  protected OperationChecker operations;
  protected TitleChecker title;
  protected LicenseActivationChecker license;
  protected BudgetViewChecker budgetView;
  protected SavingsViewChecker savingsView;
  protected NextProjectsChecker nextProjects;
  protected CategorizationChecker categorization;
  protected SeriesViewChecker series;
  protected SeriesEvolutionChecker seriesEvolution;
  protected VersionInfoChecker versionInfo;
  protected NavigationViewChecker navigation;
  protected NotesChecker notes;

  protected GlobRepository repository;

  private Date currentDate = Dates.parse("2008/08/31");
  private boolean isInMemory = true;
  private boolean deleteLocalPrevayler = true;
  private String localPrevaylerPath = FunctionalTestCase.getUrl();

  static public String SOCIETE_GENERALE = "Société Générale";
  private boolean notRegistered = false;
  private String password = "password";
  private String user = "anonymous";

  protected void setUp() throws Exception {
    super.setUp();
    TimeService.setCurrentDate(currentDate);

    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, localPrevaylerPath);
    System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "");
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, Boolean.toString(deleteLocalPrevayler));
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, Boolean.toString(isInMemory));
    System.setProperty(PicsouApplication.LOG_SOUT, "true");
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty(ConfigService.COM_APP_LICENSE_URL, "");
    System.setProperty(ConfigService.COM_APP_LICENSE_FTP_URL, "");

    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        if (mainWindow == null) {
          mainWindow = WindowInterceptor.run(new Trigger() {
            public void run() throws Exception {
              PicsouApplication.main();
            }
          });
        }
        return mainWindow;
      }
    });

    mainWindow = getMainWindow();
    repository = ((PicsouFrame)mainWindow.getAwtComponent()).getRepository();
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    loginChecker.logNewUser(user, password);
    initCheckers();
    if (!notRegistered) {
      LicenseActivationChecker.enterLicense(mainWindow, "admin", "zz");
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

  protected void setCurrentDate(String date) {
    this.currentDate = Dates.parse(date);
    TimeService.setCurrentDate(currentDate);
  }

  protected void initCheckers() {
    views = new ViewSelectionChecker(mainWindow);
    mainAccounts = new MainAccountViewChecker(mainWindow);
    savingsAccounts = new SavingsAccountViewChecker(mainWindow);
    operations = new OperationChecker(mainWindow);
    timeline = new TimeViewChecker(mainWindow);
    transactions = new TransactionChecker(mainWindow);
    transactionDetails = new TransactionDetailsChecker(mainWindow);
    transactionCreation = new TransactionCreationChecker(mainWindow);
    title = new TitleChecker(mainWindow);
    budgetView = new BudgetViewChecker(mainWindow);
    savingsView = new SavingsViewChecker(mainWindow);
    nextProjects = new NextProjectsChecker(mainWindow);
    categorization = new CategorizationChecker(mainWindow);
    series = new SeriesViewChecker(mainWindow);
    seriesEvolution = new SeriesEvolutionChecker(mainWindow);
    license = new LicenseActivationChecker(mainWindow);
    versionInfo = new VersionInfoChecker(mainWindow);
    navigation = new NavigationViewChecker(mainWindow);
    notes = new NotesChecker(mainWindow);
  }

  protected void tearDown() throws Exception {

//    GlobRepositoryValidator.run(repository);

//    operations.checkOk();
    try {
      operations.deleteUser(password);
    }
    catch (Throwable e) {
      try {
        mainWindow.dispose();
      }
      catch (Throwable e1) {
        e1.printStackTrace();
      }
      mainWindow = null;
    }
    super.tearDown();
    views = null;
    mainAccounts = null;
    savingsAccounts = null;
    timeline = null;
    transactions = null;
    transactionDetails = null;
    transactionCreation = null;
    operations = null;
    title = null;
    versionInfo = null;
    budgetView = null;
    savingsView = null;
    nextProjects = null;
    categorization = null;
    series = null;
    seriesEvolution = null;
    license = null;
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
    operations.backupAndLaunchApplication("anonymous", "password", currentDate);
  }

  protected void restartApplication(String user, String passwd) {
    mainWindow.dispose();
    mainWindow = null;
    this.user = user;
    this.password = passwd;
    mainWindow = getMainWindow();
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    loginChecker.logExistingUser(user, password);
    repository = ((PicsouFrame)mainWindow.getAwtComponent()).getRepository();
    initCheckers();
  }

  protected void restartApplication() {
    operations.exit();
    mainWindow.dispose();
    mainWindow = null;
    mainWindow = getMainWindow();
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    loginChecker.logExistingUser(user, password);
    repository = ((PicsouFrame)mainWindow.getAwtComponent()).getRepository();
    initCheckers();
  }

  public static void resetWindow() {
    if (mainWindow != null) {
      mainWindow.dispose();
      mainWindow = null;
    }
  }

  public static void forceClose() {
    if (mainWindow != null) {
      mainWindow.dispose();
      mainWindow = null;
    }
  }

  protected void changeUser(String user, String password) {
    if (mainWindow != null){
      operations.deleteUser(this.password);
    }
    this.user = user;
    this.password = password;
    mainWindow = null;
    mainWindow = getMainWindow();
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    loginChecker.logNewUser(user, password);
    initCheckers();
  }
}
