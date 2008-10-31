package org.designup.picsou.functests.utils;

import org.designup.picsou.client.ServerAccess;
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

import java.util.Date;

public abstract class LoggedInFunctionalTestCase extends FunctionalTestCase {
  protected Window mainWindow;

  protected ViewSelectionChecker views;
  protected AccountViewChecker accounts;
  protected CategoryChecker categories;
  protected MonthChecker timeline;
  protected TransactionChecker transactions;
  protected TransactionDetailsChecker transactionDetails;
  protected OperationChecker operations;
  protected TitleChecker title;
  protected LicenseChecker license;
  protected MonthSummaryChecker monthSummary;
  protected BalanceSummaryChecker balanceSummary;
  protected BudgetViewChecker budgetView;
  protected CategorizationChecker categorization;
  protected SeriesViewChecker series;
  protected InfoChecker infochecker;

  protected GlobRepository repository;
  private Directory directory;
  protected ServerAccess serverAccess;

  private Date currentDate = Dates.parse("2008/08/31");
  private String isInMemory = "true";
  private String deleteLocalPrevayler = "true";

  static public String SOCIETE_GENERALE = "Société Générale";
  private boolean notRegistered = false;

  protected void setUp() throws Exception {

//    TimeService.setCurrentDate(currentDate);
//    if (mainWindow != null) {
//      reinitMainWindow();
//      return;
//    }

    super.setUp();
    TimeService.setCurrentDate(currentDate);
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, FunctionalTestCase.getUrl());
    System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "");
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, deleteLocalPrevayler);
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, isInMemory);
    System.setProperty(PicsouApplication.LOG_SOUT, "true");
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty(ConfigService.COM_PICSOU_LICENCE_URL, "");
    System.setProperty(ConfigService.COM_PICSOU_LICENCE_FTP_URL, "");
    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        if (mainWindow == null) {
          mainWindow = WindowInterceptor.run(new Trigger() {
            public void run() throws Exception {
              clearDirectory();
              directory = MainWindowLauncher.run("anonymous", "p@ssword");
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
      operations.getPreferences().changeFutureMonth(0).validate();
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
    setCurrentDate(Dates.parseMonth(monthId));
  }

  protected void setCurrentDate(Date currentDate) {
    this.currentDate = currentDate;
  }

  protected void initCheckers() {
    views = new ViewSelectionChecker(mainWindow);
    accounts = new AccountViewChecker(mainWindow);
    operations = new OperationChecker(mainWindow);
    categories = new CategoryChecker(mainWindow);
    timeline = new MonthChecker(mainWindow);
    transactions = new TransactionChecker(mainWindow);
    transactionDetails = new TransactionDetailsChecker(mainWindow);
    title = new TitleChecker(mainWindow);
    monthSummary = new MonthSummaryChecker(mainWindow);
    balanceSummary = new BalanceSummaryChecker(mainWindow);
    budgetView = new BudgetViewChecker(mainWindow);
    categorization = new CategorizationChecker(mainWindow);
    series = new SeriesViewChecker(mainWindow);
    license = new LicenseChecker(mainWindow);
    infochecker = new InfoChecker(mainWindow);
  }

  protected void tearDown() throws Exception {
//    mainWindow.getMenuBar().getMenu("Edit").getSubMenu("check").click();
    super.tearDown();
    if (mainWindow != null) {
      mainWindow.dispose();
    }
    mainWindow = null;
    views = null;
    accounts = null;
    categories = null;
    timeline = null;
    transactions = null;
    transactionDetails = null;
    operations = null;
    title = null;
    infochecker = null;
    monthSummary = null;
    balanceSummary = null;
    budgetView = null;
    categorization = null;
    series = null;
    license = null;

    repository = null;
    clearDirectory();
    directory = null;
    serverAccess = null;
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
    return DataChecker.getCategoryName(master);
  }

  public void setInMemory(String inMemory) {
    isInMemory = inMemory;
  }

  public void setDeleteLocalPrevayler(String deleteLocalPrevayler) {
    this.deleteLocalPrevayler = deleteLocalPrevayler;
  }

  public void setNotRegistered() {
    notRegistered = true;
  }
}
