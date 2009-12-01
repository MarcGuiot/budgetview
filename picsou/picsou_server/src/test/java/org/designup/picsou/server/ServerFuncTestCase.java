package org.designup.picsou.server;

import org.designup.picsou.PicsouServer;
import org.designup.picsou.functests.checkers.CategorizationChecker;
import org.designup.picsou.functests.checkers.ImportChecker;
import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.functests.checkers.ViewSelectionChecker;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.uispec4j.*;
import org.uispec4j.interception.WindowInterceptor;

import java.util.Locale;

public abstract class ServerFuncTestCase extends UISpecTestCase {
  protected Window window;
  protected PicsouServer picsouServer;
  protected static final String PICSOU_DEV_TESTFILES_SG1_QIF = "/testfiles/sg1.qif";
  protected static final String PICSOU_DEV_TESTFILES_CIC1_OFX = "/testfiles/cic1.ofx";
  private PicsouApplication picsouApplication;

  protected void setUp() throws Exception {
    super.setUp();
    Locale.setDefault(Locale.ENGLISH);
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "true");
    System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "http://localhost:8443");
    System.setProperty(PicsouApplication.LOCAL_PREVAYLER_PATH_PROPERTY, "tmp/localprevayler");
    System.setProperty(PicsouApplication.DELETE_LOCAL_PREVAYLER_PROPERTY, "true");
    System.setProperty(PicsouServer.SERVER_PREVAYLER_PATH_PROPERTY, "tmp/serverprevayler");
    System.setProperty(PicsouServer.DELETE_SERVER_PROPERTY, "true");
    System.setProperty(PicsouServer.USE_SSHL, "false");
    picsouServer = new PicsouServer();
    picsouServer.start();
    initWindow();
  }

  protected void initWindow() {
    window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        picsouApplication = new PicsouApplication();
        picsouApplication.run();
      }
    });
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    picsouServer.stop();
    picsouApplication.shutdown();
    picsouApplication = null;
    picsouServer = null;
    window.dispose();
    window = null;
    System.setProperty(PicsouApplication.DEFAULT_ADDRESS_PROPERTY, "");
  }

  public void createAndLogUser(String user, String userPassword, final String fileName) {
    TextBox textBox = window.getTextBox("name");
    textBox.setText(user);

    window.getPasswordField("password").setPassword(userPassword);
    window.getCheckBox("createAccountCheckBox").click();
    window.getPasswordField("confirmPassword").setPassword(userPassword);
    window.getButton("Enter").click();

    OperationChecker operations = new OperationChecker(window);
    ImportChecker importDialog = operations.openImportDialog();
    importDialog
      .selectFiles(fileName)
      .acceptFile()
      .checkNoErrorMessage();

    if (fileName.endsWith(".qif")) {
      importDialog.defineAccount("Société Générale", "Main account", "333");
    }

    importDialog.doImport();
  }

  protected Table getCategoryTable() {
    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectData();
    return window.getTable("category");
  }

  protected Table getTransactionTable() {
    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectData();
    return window.getTable("transaction");
  }

  protected CategorizationChecker getCategorizationView() {
    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectCategorization();
    return new CategorizationChecker(window);
  }
}
