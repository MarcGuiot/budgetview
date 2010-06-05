package org.designup.picsou.functests.utils;

import org.designup.picsou.functests.FunctionalTestCase;
import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.model.initial.DefaultSeriesFactory;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.utils.Utils;
import org.uispec4j.finder.ComponentFinder;
import org.uispec4j.finder.ComponentMatchers;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.interception.toolkit.UISpecDisplay;

import java.util.Date;
import java.awt.*;

import net.java.balloontip.BalloonTip;
import junit.framework.Assert;

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
  public SeriesViewChecker series;
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
  protected String password = "password";

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

    LoginChecker login = new LoginChecker(mainWindow);
    login.logNewUser(user, password);
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

  protected void setCurrentDate(String yyyyMMdd) {
    this.currentDate = Dates.parse(yyyyMMdd);
    TimeService.setCurrentDate(currentDate);
  }

  protected void initCheckers() {
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
    series = new SeriesViewChecker(mainWindow);
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
      if (operations != null) {
        operations.deleteUser(password);
      }
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
    backup = null;
    title = null;
    versionInfo = null;
    budgetView = null;
    savingsView = null;
    nextProjects = null;
    categorization = null;
    series = null;
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
    operations.backupAndLaunchApplication("anonymous", "password", currentDate);
  }

  protected void restartApplication(String user, String passwd) throws Exception {
    if (mainWindow != null) {
      mainWindow.dispose();
    }
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

  public void restartApplication(boolean createUser) throws Exception {
    operations.exit();
    mainWindow.dispose();
    mainWindow = null;
    mainWindow = getMainWindow();
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    if (createUser) {
      loginChecker.logNewUser(user, password);
    }
    else {
      loginChecker.logExistingUser(user, password, false);
    }
    repository = ((PicsouFrame)mainWindow.getAwtComponent()).getRepository();
    initCheckers();
    if (!notRegistered) {
      LicenseActivationChecker.enterLicense(mainWindow, "admin", "zz");
    }
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
    if (mainWindow != null) {
      operations.deleteUser(this.password);
    }
    else {
      mainWindow = getMainWindow();
      initCheckers();
    }
    this.user = user;
    this.password = password;
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    loginChecker.logNewUser(user, password);
    initCheckers();
    if (!notRegistered) {
      LicenseActivationChecker.enterLicense(mainWindow, "admin", "zz");
    }
  }

  public String getLocalPrevaylerPath() {
    return localPrevaylerPath;
  }

  protected void checkNoSignpostVisible() {
    ComponentFinder finder = new ComponentFinder(mainWindow.getAwtContainer());
    final Component[] actual = finder.getComponents(ComponentMatchers.and(
      ComponentMatchers.fromClass(BalloonTip.class),
      ComponentMatchers.visible(true)
    ));
    if (actual.length > 0) {
      StringBuilder builder = new StringBuilder("Visible tips:\n");
      for (Component component : actual) {
        BalloonTip tip = (BalloonTip)component;
        builder.append(Utils.cleanupHtml(tip.getText())).append("\n");
      }
      Assert.fail(builder.toString());
    }
  }
}
