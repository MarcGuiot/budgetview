package org.designup.picsou.functests.utils;

import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.http.EncrypterToTransportServerAccess;
import org.designup.picsou.client.local.LocalClientTransport;
import org.designup.picsou.functests.ServerFunctionalTestCase;
import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.gui.MainWindowLauncher;
import org.designup.picsou.model.LabelToCategory;
import org.designup.picsou.model.MasterCategory;
import org.globsframework.model.FieldValue;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.uispec4j.Trigger;
import org.uispec4j.UISpecAdapter;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

import javax.swing.*;

public abstract class LoggedInFunctionalTestCase extends ServerFunctionalTestCase {
  protected Window mainWindow;

  protected AccountChecker accounts;
  protected CategoryChecker categories;
  protected MonthChecker periods;
  protected TransactionChecker transactions;
  protected OperationChecker operations;
  protected ImportChecker imports;
  protected GraphicChecker graphics;
  protected GlobRepository repository;
  protected ServerAccess serverAccess;
  protected InformationPanelChecker informationPanel;
  protected TitleChecker title;

  protected void setUp() throws Exception {
    super.setUp();
    setAdapter(new UISpecAdapter() {
      public Window getMainWindow() {
        if (mainWindow == null) {
          mainWindow = WindowInterceptor.run(new Trigger() {
            public void run() throws Exception {
              serverAccess = new EncrypterToTransportServerAccess(new LocalClientTransport(directory));
              repository = MainWindowLauncher.run(serverAccess, new String[0]);
            }
          });
        }
        return mainWindow;
      }
    });

    mainWindow = getMainWindow();
    initCheckers();
  }

  public void initCheckers() {
    accounts = new AccountChecker(mainWindow);
    operations = new OperationChecker(mainWindow);
    categories = new CategoryChecker(mainWindow);
    periods = new MonthChecker(mainWindow);
    transactions = new TransactionChecker(mainWindow);
    graphics = new GraphicChecker(mainWindow);
    informationPanel = new InformationPanelChecker(mainWindow);
    title = new TitleChecker(mainWindow);
    imports = new ImportChecker(repository);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    ((JFrame)mainWindow.getAwtComponent()).dispose();
    mainWindow = null;
    accounts = null;
    categories = null;
    periods = null;
    transactions = null;
    operations = null;
    imports = null;
    graphics = null;
    repository = null;
    serverAccess = null;
    informationPanel = null;
    title = null;
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
