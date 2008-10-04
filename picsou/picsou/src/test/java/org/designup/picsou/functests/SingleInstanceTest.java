package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.*;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.utils.ThreadLauncherTrigger;

import java.net.ServerSocket;

public class SingleInstanceTest extends StartUpFunctionalTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    System.setProperty(SingleApplicationInstanceListener.SINGLE_INSTANCE_DISABLED, "false");
    SingleApplicationInstanceListener.REMOTE_APPLICATION_DETECTION_TIMEOUT = 100;
    SingleApplicationInstanceListener.ACCEPT_TIMEOUT = 100;
  }

  public void testOpenRequestsDuringLoginAndInitialFileImport() throws Exception {
    final String[] files = new String[3];
    files[0] = OfxBuilder.init(this)
      .addTransaction("2000/01/01", 1.2, "mac do")
      .save();
    files[1] = OfxBuilder.init(this)
      .addTransaction("2000/01/02", 1.2, "quick")
      .save();
    files[2] = OfxBuilder.init(this)
      .addTransaction("2000/01/03", 1.2, "pizza")
      .save();

    final ApplicationThread[] threads = new ApplicationThread[3];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new ApplicationThread(files[i]);
    }
    final Window window = WindowInterceptor.run(new ThreadLauncherTrigger(threads));

    int errorCount = 0;
    for (ApplicationThread thread : threads) {
      thread.join();
      if (thread.errorReceived) {
        errorCount++;
      }
    }
    assertEquals(0, errorCount);

    final LoginChecker login = new LoginChecker(window);
    Window importDialog = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        login.logNewUser("calimero", "C@limero2");
      }
    });

    ImportChecker importer = new ImportChecker(importDialog);
    importer.checkSelectedFiles(files);
    importer.acceptFile();

    String step2File = OfxBuilder.init(this)
      .addTransaction("2000/01/04", 1.2, "menu K")
      .save();
    PicsouApplication.main(step2File);

    importer.doImport();
    importer.doImport();
    importer.doImport();
    importer.doImport();

    getTransactionView(window).initContent()
      .add("04/01/2000", TransactionType.VIREMENT, "menu K", "", 1.20)
      .add("03/01/2000", TransactionType.VIREMENT, "pizza", "", 1.20)
      .add("02/01/2000", TransactionType.VIREMENT, "quick", "", 1.20)
      .add("01/01/2000", TransactionType.VIREMENT, "mac do", "", 1.20)
      .check();

    window.dispose();
    for (ApplicationThread thread : threads) {
      thread.shutdown();
    }
  }

  public void testOpenRequestsWhenTheApplicationIsRunning() throws Exception {
    final PicsouApplication picsouApplication = new PicsouApplication();
    final Window window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        picsouApplication.run();
      }
    });

    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("calimero", "C@limero2");

    final String initialFile = OfxBuilder.init(this)
      .addTransaction("2000/01/03", 1.2, "menu K")
      .save();
    WaitEndTriggerDecorator trigger = new WaitEndTriggerDecorator(new Trigger() {
      public void run() throws Exception {
        PicsouApplication.main(initialFile);
      }
    });
    Window importDialog = WindowInterceptor.getModalDialog(trigger);

    ImportChecker importer = new ImportChecker(importDialog);
    importer.checkSelectedFiles(initialFile);

    String step1File = OfxBuilder.init(this)
      .addTransaction("2000/01/01", 1.2, "mac do")
      .save();
    PicsouApplication.main(step1File);
    importer.checkSelectedFiles(initialFile, step1File);
    importer.acceptFile();
    String step2File = OfxBuilder.init(this)
      .addTransaction("2000/01/02", 1.2, "quick")
      .save();
    PicsouApplication.main(step2File);
    importer.doImport();
    importer.doImport();
    importer.doImport();

    getTransactionView(window).initContent()
      .add("03/01/2000", TransactionType.VIREMENT, "menu K", "", 1.20)
      .add("02/01/2000", TransactionType.VIREMENT, "quick", "", 1.20)
      .add("01/01/2000", TransactionType.VIREMENT, "mac do", "", 1.20)
      .check();
    window.dispose();
    picsouApplication.shutdown();
  }

  public void testOpenRequestAndCloseAndOpenRequest() throws Exception {
    final PicsouApplication picsouApplication = new PicsouApplication();
    final Window window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        picsouApplication.run();
      }
    });

    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("calimero", "C@limero2");

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
    ImportChecker firstImporter = new ImportChecker(importDialog);
    firstImporter.checkSelectedFiles(initialFile);
    firstImporter.close();
    assertFalse(importDialog.isVisible());

    WaitEndTriggerDecorator trigger2 = new WaitEndTriggerDecorator(new Trigger() {
      public void run() throws Exception {
        PicsouApplication.main(initialFile);
      }
    });
    importDialog = WindowInterceptor.getModalDialog(trigger2);
    trigger2.waitEnd();
    ImportChecker importer = new ImportChecker(importDialog);
    importer.checkSelectedFiles(initialFile);
    importer.acceptFile();
    importer.doImport();
    getTransactionView(window).initContent()
      .add("03/01/2000", TransactionType.VIREMENT, "menu K", "", 1.20)
      .check();
    window.dispose();
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
    final Window window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        picsouApplication.run();
      }
    });

    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("calimero", "C@limero2");

    final String file =
      createQifFile("file",
                    "!Type:Bank\n" +
                    "D20/04/2006\n" +
                    "T-17.65\n" +
                    "N\n" +
                    "PFAC.FRANCE 4561409\n" +
                    "MFAC.FRANCE 4561409787231717 19/04/06 STATION BP CARTE 06348905 PAIEMENT CB 1904 PARIS\n" +
                    "^");
    OperationChecker operations = new OperationChecker(window);
    Window importDialog = WindowInterceptor.getModalDialog(operations.getImportTrigger());
    ImportChecker importer = new ImportChecker(importDialog);
    BalanceEditionChecker balance = importer.selectFiles(file)
      .acceptFile()
      .selectBank(LoggedInFunctionalTestCase.SOCIETE_GENERALE)
      .enterAccountNumber("11111")
      .doImportWithBalance();
    NewApplicationThread newApplication = new NewApplicationThread(file);
    newApplication.start();
    Thread.sleep(1000);
    newApplication.checkNotOpen();
    balance.setAmount(0.).validate();
    Window newImportDialog = newApplication.getImportDialog();
    assertNotNull(newImportDialog);
    new ImportChecker(newImportDialog).close();
    picsouApplication.shutdown();
  }

  public void testImportWithCategorizationDialogOpen() throws Exception {
    final PicsouApplication picsouApplication = new PicsouApplication();
    final Window window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        picsouApplication.run();
      }
    });

    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logNewUser("calimero", "C@limero2");

    final String initialFile = OfxBuilder.init(this)
      .addTransaction("2000/01/03", 1.2, "menu K")
      .save();
    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectData();
    CategoryChecker category = new CategoryChecker(window);
    CategoryEditionChecker edition = category.openEditionDialog();
    NewApplicationThread newApplication = new NewApplicationThread(initialFile);
    newApplication.start();
    Thread.sleep(1000);
    newApplication.checkNotOpen();
    edition.cancel();
    Window dialog = newApplication.getImportDialog();
    assertNotNull(dialog);
    new ImportChecker(dialog).checkSelectedFiles(initialFile).close();
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

    public NewApplicationThread checkNotOpen() {
      synchronized (this) {
        assertNull(importDialog);
      }
      return this;
    }

    public Window getImportDialog() throws InterruptedException {
      synchronized (this) {
        if (importDialog == null) {
          wait(1000);
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
