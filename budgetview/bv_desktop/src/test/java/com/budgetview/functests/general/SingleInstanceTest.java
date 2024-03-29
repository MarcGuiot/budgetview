package com.budgetview.functests.general;

import com.budgetview.functests.checkers.*;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.functests.utils.StartUpFunctionalTestCase;
import com.budgetview.desktop.Application;
import com.budgetview.desktop.startup.components.SingleApplicationInstanceListener;
import com.budgetview.desktop.time.TimeViewPanel;
import com.budgetview.model.TransactionType;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.junit.Test;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.assertion.UISpecAssert;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.utils.ThreadLauncherTrigger;

import java.net.ServerSocket;

public class SingleInstanceTest extends StartUpFunctionalTestCase {

  protected void setUp() throws Exception {
    LoggedInFunctionalTestCase.resetWindow();
    super.setUp();
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "false");
    SingleApplicationInstanceListener.REMOTE_APPLICATION_DETECTION_TIMEOUT = 100;
    SingleApplicationInstanceListener.ACCEPT_TIMEOUT = 100;
  }

  @Test
  public void testOpenRequestsDuringLoginAndInitialFileImport() throws Exception {
    final String[] files = {
      OfxBuilder.init(this)
        .addTransaction("2000/01/01", 1.2, "mac do")
        .save(),
      OfxBuilder.init(this)
        .addTransaction("2000/01/02", 1.2, "quick")
        .save(),
      OfxBuilder.init(this)
        .addTransaction("2000/01/03", 1.2, "pizza")
        .save()
    };

    final ApplicationThread[] threads = new ApplicationThread[3];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new ApplicationThread(files[i]);
    }
    final Window slaWindow = WindowInterceptor.getModalDialog(new ThreadLauncherTrigger(threads));

    SlaValidationDialogChecker slaValidationDialogChecker = new SlaValidationDialogChecker(slaWindow);
    slaValidationDialogChecker.acceptTerms();
    final SlaValidationDialogChecker.TriggerSlaOk triggerSlaOk =
      new SlaValidationDialogChecker.TriggerSlaOk(slaValidationDialogChecker);
    Window importDialog = WindowInterceptor.getModalDialog(triggerSlaOk);

    int errorCount = 0;
    for (ApplicationThread thread : threads) {
      thread.join(5000);
      if (thread.errorReceived) {
        errorCount++;
      }
    }
    assertEquals(0, errorCount);

    ImportDialogChecker importer = new ImportDialogChecker(importDialog);

    String step2File = OfxBuilder.init(this)
      .addTransaction("2000/01/04", 1.2, "menu K")
      .save();
    Application.main(step2File);

    importer.toPreview()
      .setMainAccount()
      .importAccountAndOpenNext()
      .importAccountAndOpenNext()
      .importAccountAndComplete();

    Window mainWindow = triggerSlaOk.getMainWindow();
    getTransactionView(mainWindow).initContent()
      .add("04/01/2000", TransactionType.VIREMENT, "menu K", "", 1.20)
      .add("03/01/2000", TransactionType.VIREMENT, "pizza", "", 1.20)
      .add("02/01/2000", TransactionType.VIREMENT, "quick", "", 1.20)
      .add("01/01/2000", TransactionType.VIREMENT, "mac do", "", 1.20)
      .check();

    ImportDialogChecker importerForPathCheck = ImportDialogChecker.open(new OperationChecker(mainWindow).getImportTrigger());
    importerForPathCheck.checkDirectory(System.getProperty("user.home"))
      .close();

    mainWindow.dispose();
    for (ApplicationThread thread : threads) {
      thread.shutdown();
    }
  }

  @Test
  public void testOpenRequestsWhenTheApplicationIsRunning() throws Exception {

    final Application application = new Application();
    final Window slaWindow = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        application.run();
      }
    });

    SlaValidationDialogChecker slaValidationDialogChecker = new SlaValidationDialogChecker(slaWindow);
    slaValidationDialogChecker.acceptTerms();
    final SlaValidationDialogChecker.TriggerSlaOk triggerSlaOk =
      new SlaValidationDialogChecker.TriggerSlaOk(slaValidationDialogChecker);

    triggerSlaOk.run();

    final String initialFile = OfxBuilder.init(this)
      .addTransaction("2000/01/03", 1.2, "menu K")
      .save();
    WaitEndTriggerDecorator trigger = new WaitEndTriggerDecorator(new Trigger() {
      public void run() throws Exception {
        Application.main(initialFile);
      }
    });

    ImportDialogChecker importer = ImportDialogChecker.open(trigger);
//    importer.checkSelectedFiles(initialFile);

    String step1File = OfxBuilder.init(this)
      .addTransaction("2000/01/01", 1.2, "mac do")
      .save();
    Application.main(step1File);

    String step2File = OfxBuilder.init(this)
      .addTransaction("2000/01/02", 1.2, "quick")
      .save();
    Application.main(step2File);

    importer.toPreview()
      .setMainAccount()
      .importAccountAndOpenNext()
      .importAccountAndComplete();

    getTransactionView(triggerSlaOk.getMainWindow()).initContent()
      .add("03/01/2000", TransactionType.VIREMENT, "menu K", "", 1.20)
      .add("02/01/2000", TransactionType.VIREMENT, "quick", "", 1.20)
      .add("01/01/2000", TransactionType.VIREMENT, "mac do", "", 1.20)
      .check();
    triggerSlaOk.getMainWindow().dispose();
    application.shutdown();
  }

  @Test
  public void testOpenRequestAndCloseAndOpenRequest() throws Exception {
    final Application application = new Application();
    final Window slaWindow = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        application.run();
      }
    });

    SlaValidationDialogChecker slaValidationDialogChecker = new SlaValidationDialogChecker(slaWindow);
    slaValidationDialogChecker.acceptTerms();
    final SlaValidationDialogChecker.TriggerSlaOk triggerSlaOk =
      new SlaValidationDialogChecker.TriggerSlaOk(slaValidationDialogChecker);

    triggerSlaOk.run();

    Window mainWindow = triggerSlaOk.getMainWindow();

    final String initialFile = OfxBuilder.init(this)
      .addTransaction("2000/01/03", 1.2, "menu K")
      .save();
    WaitEndTriggerDecorator trigger1 = new WaitEndTriggerDecorator(new Trigger() {
      public void run() throws Exception {
        Application.main(initialFile);
      }
    });
    Window importDialog = WindowInterceptor.getModalDialog(trigger1);
    trigger1.waitEnd();
    ImportDialogPreviewChecker firstImporter = new ImportDialogPreviewChecker(importDialog);
//    firstImporter.checkSelectedFiles(initialFile);
    firstImporter.skipAndComplete();
    assertFalse(importDialog.isVisible());

    WaitEndTriggerDecorator trigger2 = new WaitEndTriggerDecorator(new Trigger() {
      public void run() throws Exception {
        Application.main(initialFile);
      }
    });
    importDialog = WindowInterceptor.getModalDialog(trigger2);
    trigger2.waitEnd();
    ImportDialogPreviewChecker importer = new ImportDialogPreviewChecker(importDialog);
//    importer.checkSelectedFiles(initialFile);
//    importer.acceptFile();
    importer
      .setMainAccount()
      .importAccountAndComplete();

    getTransactionView(mainWindow).initContent()
      .add("03/01/2000", TransactionType.VIREMENT, "menu K", "", 1.20)
      .check();
    mainWindow.dispose();
    application.shutdown();
  }

  @Test
  public void testWhenFirstPortsAreInUse() throws Exception {
    ServerSocket serverSocket1 = new ServerSocket(5454);
    ServerSocket serverSocket2 = new ServerSocket(3474);
    testOpenRequestsDuringLoginAndInitialFileImport();
    serverSocket1.close();
    serverSocket2.close();
  }

  @Test
  public void testImportQifWhileBalanceDialogIsOpen() throws Exception {
    final Application application = new Application();
    final Window slaWindow = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        application.run();
      }
    });

    SlaValidationDialogChecker slaValidationDialogChecker = new SlaValidationDialogChecker(slaWindow);
    slaValidationDialogChecker.acceptTerms();
    final SlaValidationDialogChecker.TriggerSlaOk triggerSlaOk =
      new SlaValidationDialogChecker.TriggerSlaOk(slaValidationDialogChecker);

    triggerSlaOk.run();

    Window mainWindow = triggerSlaOk.getMainWindow();

    final String file =
      createQifFile("file",
                    "!Type:Bank\n" +
                    "D20/04/2006\n" +
                    "T-17.65\n" +
                    "N\n" +
                    "PFAC.FRANCE 4561409\n" +
                    "MFAC.FRANCE 4561409787231717 19/04/06 STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS\n" +
                    "^");
    OperationChecker operations = new OperationChecker(mainWindow);
    operations.hideSignposts();

    ImportDialogChecker importer = ImportDialogChecker.open(operations.getImportTrigger());

    ImportDialogPreviewChecker preview = importer.selectFiles(file)
      .importFileAndPreview();
    AccountPositionEditionChecker accountPosition = preview
      .defineAccount(LoggedInFunctionalTestCase.SOCIETE_GENERALE, "Main account", "11111")
      .importAndEditPosition();

    NewApplicationThread newApplication = new NewApplicationThread(file);
    newApplication.start();
    Thread.sleep(1000);
    newApplication.checkNotOpen();
    accountPosition.setAmount(0.00).validate();
    preview.importAccountAndComplete();

    Window newImportDialog = newApplication.getImportDialog();
    assertNotNull(newImportDialog);

    new ImportDialogPreviewChecker(newImportDialog)
      .skipAndComplete();
    application.shutdown();
    newApplication.clear();
  }

  @Test
  public void testImportWithSeriesEditionDialogOpen() throws Exception {
    final Application application = new Application();
    final Window slaWindow = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        application.run();
      }
    });

    SlaValidationDialogChecker slaValidationDialogChecker = new SlaValidationDialogChecker(slaWindow);
    slaValidationDialogChecker.acceptTerms();
    final SlaValidationDialogChecker.TriggerSlaOk triggerSlaOk =
      new SlaValidationDialogChecker.TriggerSlaOk(slaValidationDialogChecker);

    triggerSlaOk.run();

    Window mainWindow = triggerSlaOk.getMainWindow();

    UISpecAssert.waitUntil(mainWindow.containsSwingComponent(TimeViewPanel.class), 10000);

    ViewSelectionChecker views = new ViewSelectionChecker(mainWindow);
    views.selectBudget();

    BudgetViewChecker budgetView = new BudgetViewChecker(mainWindow);
    SeriesEditionDialogChecker edition = budgetView.variable.createSeries();

    final String initialFile = OfxBuilder.init(this)
      .addTransaction("2000/01/03", 1.2, "menu K")
      .save();

    NewApplicationThread newApplication = new NewApplicationThread(initialFile);
    newApplication.start();

    Thread.sleep(1000);
    newApplication.checkNotOpen();
    edition.cancel();

    Window dialog = newApplication.getImportDialog();
    assertNotNull(dialog);

    GuiChecker.pressEsc(dialog);
    assertFalse(dialog.isVisible());

    newApplication.clear();

    NewApplicationThread sameFileApplication = new NewApplicationThread(initialFile);
    sameFileApplication.start();

    Window sameImportFileDialog = sameFileApplication.getImportDialog();
    assertNotNull(sameImportFileDialog);

    new ImportDialogPreviewChecker(sameImportFileDialog)
      .skipAndComplete();
    application.shutdown();
  }

  private String createQifFile(String discriminant, String content) {
    String fileName = TestUtils.getFileName(this, discriminant + ".qif");
    Files.dumpStringToFile(fileName, content);
    return fileName;
  }

  private static class ApplicationThread extends Thread {
    boolean errorReceived = false;
    private String[] files;
    private Application application;

    private ApplicationThread(String... files) {
      setDaemon(true);
      this.files = files;
    }

    public void run() {
      try {
        application = new Application();
        application.run(files);
      }
      catch (Throwable e) {
        errorReceived = true;
      }
    }

    void shutdown() throws Exception {
      application.shutdown();
      application = null;
    }
  }

  private TransactionChecker getTransactionView(Window window) {
    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectData();
    return new TransactionChecker(window);
  }

  private static class NewApplicationThread extends Thread {
    private final String file;
    private Window importDialog;
    private WaitEndTriggerDecorator trigger;

    public NewApplicationThread(String file) {
      setDaemon(true);
      this.file = file;
    }

    public void run() {
      trigger = new WaitEndTriggerDecorator(new Trigger() {
        public void run() throws Exception {
          Application.main(file);
        }
      });
      Window importDialog = WindowInterceptor.getModalDialog(trigger);
      synchronized (this) {
        this.importDialog = importDialog;
        notify();
      }
    }

    public void clear() {
      importDialog = null;
      trigger = null;
    }

    public NewApplicationThread checkNotOpen() {
      synchronized (this) {
        assertNull(importDialog);
      }
      return this;
    }

    public Window getImportDialog() throws InterruptedException {
      synchronized (this) {
        if (importDialog == null) {
          wait(4000);
        }
      }
      trigger.waitEnd();
      return importDialog;
    }
  }

  private static class WaitEndTriggerDecorator implements Trigger {
    private Trigger trigger;
    private boolean end = false;

    private WaitEndTriggerDecorator(Trigger trigger) {
      this.trigger = trigger;
    }

    public void run() throws Exception {
      try {
        trigger.run();
      }
      finally {
        synchronized (this) {
          end = true;
          notify();
        }
      }
    }

    public void waitEnd() {
      long endDate = System.currentTimeMillis() + 3000;
      synchronized (this) {
        while (!end && System.currentTimeMillis() < endDate) {
          try {
            wait(100);
          }
          catch (InterruptedException e) {
          }
        }
      }
      if (!end) {
        fail("never end");
      }
    }
  }

}
