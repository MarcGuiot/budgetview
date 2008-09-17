package org.designup.picsou.functests.utils;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.functests.FunctionalTestCase;
import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.TimeService;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Dates;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import java.util.Date;

public abstract class LoggedInFunctionalTestCase extends FunctionalTestCase {
  protected Window mainWindow;

  protected ViewSelectionChecker views;
  protected AccountChecker accounts;
  protected CategoryChecker categories;
  protected MonthChecker timeline;
  protected TransactionChecker transactions;
  protected TransactionDetailsChecker transactionDetails;
  protected OperationChecker operations;
  protected GraphicChecker graphics;
  protected GlobRepository repository;
  protected ServerAccess serverAccess;
  protected UncategorizedMessagePanelChecker informationPanel;
  protected TitleChecker title;
  protected LicenseChecker license;
  protected MonthSummaryChecker monthSummary;
  protected BudgetViewChecker budgetView;
  protected CategorizationChecker categorization;
  private PicsouApplication picsouApplication;
  private Date currentDate = Dates.parse("2008/08/31");
  private String isInMemory = "true";
  private String deleteLocalPrevayler = "true";

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
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty(ConfigService.COM_PICSOU_LICENCE_URL, "");
    System.setProperty(ConfigService.COM_PICSOU_LICENCE_FTP_URL, "");
    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        if (mainWindow == null) {
          mainWindow = WindowInterceptor.run(new Trigger() {
            public void run() throws Exception {
              picsouApplication = new PicsouApplication();
              picsouApplication.run();
            }
          });
        }
        return mainWindow;
      }
    });

    mainWindow = getMainWindow();
    LoginChecker loginChecker = new LoginChecker(mainWindow);
    loginChecker.logNewUser("anonymous", "p@ssword");
    loginChecker.skipImport();
    repository = ((PicsouFrame)mainWindow.getAwtComponent()).getRepository();
    initCheckers();
    LicenseChecker.enterLicense(mainWindow, "admin", "zz", 0);
    views.selectData();
  }

  private void reinitMainWindow() {
    repository = ((PicsouFrame)mainWindow.getAwtComponent()).getRepository();
    ApplicationReset.run(repository);
    initCheckers();
    license.enterLicense("admin", "zz", 0);
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
    accounts = new AccountChecker(mainWindow);
    operations = new OperationChecker(mainWindow);
    categories = new CategoryChecker(mainWindow);
    timeline = new MonthChecker(mainWindow);
    transactions = new TransactionChecker(mainWindow);
    transactionDetails = new TransactionDetailsChecker(mainWindow);
    graphics = new GraphicChecker(mainWindow);
    informationPanel = new UncategorizedMessagePanelChecker(mainWindow);
    title = new TitleChecker(mainWindow);
    monthSummary = new MonthSummaryChecker(mainWindow);
    budgetView = new BudgetViewChecker(mainWindow);
    categorization = new CategorizationChecker(mainWindow);
    license = new LicenseChecker(mainWindow);
  }

  protected void tearDown() throws Exception {
    mainWindow.getMenuBar().getMenu("Edit").getSubMenu("check").click();
    super.tearDown();
    mainWindow.dispose();
    mainWindow = null;
    views = null;
    accounts = null;
    categories = null;
    timeline = null;
    transactions = null;
    transactionDetails = null;
    operations = null;
    graphics = null;
    serverAccess = null;
    informationPanel = null;
    title = null;
    repository = null;
    monthSummary = null;
    budgetView = null;
    categorization = null;
    picsouApplication.shutdown();
    picsouApplication = null;
    license = null;
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
}
