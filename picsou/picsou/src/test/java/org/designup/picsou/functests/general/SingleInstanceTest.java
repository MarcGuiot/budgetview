package org.designup.picsou.functests.general;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.utils.StartUpFunctionalTestCase;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.startup.components.SingleApplicationInstanceListener;
import org.designup.picsou.gui.time.TimeViewPanel;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
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
    PicsouApplication.main(step2File);

    importer
      .setMainAccount()
      .doImport();
    importer.doImport();
    importer.doImport();
    importer.completeImport();

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

  public void testOpenRequestsWhenTheApplicationIsRunning() throws Exception {

    final PicsouApplication picsouApplication = new PicsouApplication();
    final Window slaWindow = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        picsouApplication.run();
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
        PicsouApplication.main(initialFile);
      }
    });

    ImportDialogChecker importer = ImportDialogChecker.open(trigger);
//    importer.checkSelectedFiles(initialFile);

    String step1File = OfxBuilder.init(this)
      .addTransaction("2000/01/01", 1.2, "mac do")
      .save();
    PicsouApplication.main(step1File);

    String step2File = OfxBuilder.init(this)
      .addTransaction("2000/01/02", 1.2, "quick")
      .save();
    PicsouApplication.main(step2File);
    importer
      .setMainAccount()
      .doImport();
    importer.doImport();
    importer.completeImport();

    getTransactionView(triggerSlaOk.getMainWindow()).initContent()
      .add("03/01/2000", TransactionType.VIREMENT, "menu K", "", 1.20)
      .add("02/01/2000", TransactionType.VIREMENT, "quick", "", 1.20)
      .add("01/01/2000", TransactionType.VIREMENT, "mac do", "", 1.20)
      .check();
    triggerSlaOk.getMainWindow().dispose();
    picsouApplication.shutdown();
  }

  public void testOpenRequestAndCloseAndOpenRequest() throws Exception {
    final PicsouApplication picsouApplication = new PicsouApplication();
    final Window slaWindow = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        picsouApplication.run();
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
        PicsouApplication.main(initialFile);
      }
    });
    Window importDialog = WindowInterceptor.getModalDialog(trigger1);
    trigger1.waitEnd();
    ImportDialogChecker firstImporter = new ImportDialogChecker(importDialog);
//    firstImporter.checkSelectedFiles(initialFile);
    firstImporter.skipAndComplete();
    assertFalse(importDialog.isVisible());

    WaitEndTriggerDecorator trigger2 = new WaitEndTriggerDecorator(new Trigger() {
      public void run() throws Exception {
        PicsouApplication.main(initialFile);
      }
    });
    importDialog = WindowInterceptor.getModalDialog(trigger2);
    trigger2.waitEnd();
    ImportDialogChecker importer = new ImportDialogChecker(importDialog);
//    importer.checkSelectedFiles(initialFile);
//    importer.acceptFile();
    importer
      .setMainAccount()
      .completeImport();
    getTransactionView(mainWindow).initContent()
      .add("03/01/2000", TransactionType.VIREMENT, "menu K", "", 1.20)
      .check();
    mainWindow.dispose();
    picsouApplication.shutdown();
  }

  public void testWhenFirstPortsAreInUse() throws Exception {
    ServerSocket serverSocket1 = new ServerSocket(5454);
    ServerSocket serverSocket2 = new ServerSocket(3474);
    testOpenRequestsDuringLoginAndInitialFileImport();
    serverSocket1.close();
    serverSocket2.close();
  }

  public void testImportQifWhileBalanceDialogIsOpen() throws Exception {
    final PicsouApplication picsouApplication = new PicsouApplication();
    final Window slaWindow = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        picsouApplication.run();
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
    AccountPositionEditionChecker accountPosition = importer.selectFiles(file)
      .acceptFile()
      .defineAccount(LoggedInFunctionalTestCase.SOCIETE_GENERALE, "Main account", "11111")
      .doImportWithBalance();

    NewApplicationThread newApplication = new NewApplicationThread(file);
    newApplication.start();
    Thread.sleep(1000);
    newApplication.checkNotOpen();
    accountPosition.setAmount(0.00).validate();
    importer.completeLastStep();

    Window newImportDialog = newApplication.getImportDialog();
    assertNotNull(newImportDialog);

    new ImportDialogChecker(newImportDialog)
      .skipAndComplete();
    picsouApplication.shutdown();
    newApplication.clear();
  }

  public void testImportWithSeriesEditionDialogOpen() throws Exception {
    final PicsouApplication picsouApplication = new PicsouApplication();
    final Window slaWindow = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        picsouApplication.run();
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

    new ImportDialogChecker(sameImportFileDialog)
      .skipAndComplete();
    picsouApplication.shutdown();
  }

  private String createQifFile(String discriminant, String content) {
    String fileName = TestUtils.getFileName(this, discriminant + ".qif");
    Files.dumpStringToFile(fileName, content);
    return fileName;
  }

  private static class ApplicationThread extends Thread {
    boolean errorReceived = false;
    private String[] files;
    private PicsouApplication picsouApplication;

    private ApplicationThread(String... files) {
      setDaemon(true);
      this.files = files;
    }

    public void run() {
      try {
        picsouApplication = new PicsouApplication();
        picsouApplication.run(files);
      }
      catch (Throwable e) {
        errorReceived = true;
      }
    }

    void shutdown() throws Exception {
      picsouApplication.shutdown();
      picsouApplication = null;
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
          PicsouApplication.main(file);
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
