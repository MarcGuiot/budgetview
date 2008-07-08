package org.designup.picsou.functests.utils;

import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.functests.FunctionalTestCase;
import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.components.PicsouFrame;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.model.LabelToCategory;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.model.FieldValue;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

public abstract class LoggedInFunctionalTestCase extends FunctionalTestCase {
  protected Window mainWindow;

  protected ViewSelectionChecker views;
  protected AccountChecker accounts;
  protected CategoryChecker categories;
  protected MonthChecker periods;
  protected TransactionChecker transactions;
  protected TransactionDetailsChecker transactionDetails;
  protected OperationChecker operations;
  protected GraphicChecker graphics;
  protected GlobRepository repository;
  protected ServerAccess serverAccess;
  protected UncategorizedMessagePanelChecker informationPanel;
  protected TitleChecker title;
  protected MonthSummaryChecker monthSummary;
  private PicsouApplication picsouApplication;

  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, FunctionalTestCase.getUrl());
    System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "");
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    System.setProperty(PicsouApplication.IS_DATA_IN_MEMORY, "true");
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
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
    views.selectData();
  }

  public void initCheckers() {
    views = new ViewSelectionChecker(mainWindow);
    accounts = new AccountChecker(mainWindow);
    operations = new OperationChecker(mainWindow);
    categories = new CategoryChecker(mainWindow);
    periods = new MonthChecker(mainWindow);
    transactions = new TransactionChecker(mainWindow);
    transactionDetails = new TransactionDetailsChecker(mainWindow);
    graphics = new GraphicChecker(mainWindow);
    informationPanel = new UncategorizedMessagePanelChecker(mainWindow);
    title = new TitleChecker(mainWindow);
    monthSummary = new MonthSummaryChecker(mainWindow);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    mainWindow.dispose();
    mainWindow = null;
    views = null;
    accounts = null;
    categories = null;
    periods = null;
    transactions = null;
    transactionDetails = null;
    operations = null;
    graphics = null;
    serverAccess = null;
    informationPanel = null;
    title = null;
    repository = null;
    monthSummary = null;
    picsouApplication.shutdown();
    picsouApplication = null;
  }

  public OperationChecker getOperations() {
    return operations;
  }

  public void learn(String label, MasterCategory category) {
    String anonyme = AllocationLearningService.anonymise(label, label, null);
    repository.create(Key.create(LabelToCategory.TYPE, repository.getIdGenerator()
      .getNextId(LabelToCategory.ID, 1)),
                      FieldValue.value(LabelToCategory.LABEL, anonyme),
                      FieldValue.value(LabelToCategory.COUNT, 1),
                      FieldValue.value(LabelToCategory.CATEGORY, category.getId()));
  }

  protected String getCategoryName(MasterCategory master) {
    return DataChecker.getCategoryName(master);
  }
}
