package org.designup.picsou.functests;

import org.designup.picsou.functests.checkers.ImportChecker;
import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.functests.checkers.TransactionChecker;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.PicsouApplication;
import org.designup.picsou.gui.startup.SingleApplicationInstanceListener;
import org.designup.picsou.model.TransactionType;
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

    LoginChecker login = new LoginChecker(window);
    login.logNewUser("calimero", "C@limero2");

    ImportChecker importer = new ImportChecker(window);
    importer.checkSelectedFiles(files);
    importer.startImport();

    String step2File = OfxBuilder.init(this)
      .addTransaction("2000/01/04", 1.2, "menu K")
      .save();
    PicsouApplication.main(step2File);

    importer.doImport();
    importer.doImport();
    importer.doImport();
    importer.doImport();

    TransactionChecker transactionChecker = new TransactionChecker(window);
    transactionChecker.initContent()
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
    loginChecker.skipImport();

    final String initialFile = OfxBuilder.init(this)
      .addTransaction("2000/01/03", 1.2, "menu K")
      .save();
    Window importDialog = WindowInterceptor.getModalDialog(new Trigger() {
      public void run() throws Exception {
        PicsouApplication.main(initialFile);
      }
    });

    ImportChecker importer = new ImportChecker(importDialog);
    importer.checkSelectedFiles(initialFile);

    String step1File = OfxBuilder.init(this)
      .addTransaction("2000/01/01", 1.2, "mac do")
      .save();
    PicsouApplication.main(step1File);
    importer.checkSelectedFiles(initialFile, step1File);
    importer.startImport();
    String step2File = OfxBuilder.init(this)
      .addTransaction("2000/01/02", 1.2, "quick")
      .save();
    PicsouApplication.main(step2File);
    importer.doImport();
    importer.doImport();
    importer.doImport();

    TransactionChecker transactionChecker = new TransactionChecker(window);
    transactionChecker.initContent()
      .add("03/01/2000", TransactionType.VIREMENT, "menu K", "", 1.20)
      .add("02/01/2000", TransactionType.VIREMENT, "quick", "", 1.20)
      .add("01/01/2000", TransactionType.VIREMENT, "mac do", "", 1.20)
      .check();
    window.dispose();
    picsouApplication.shutdown();
  }

  public void testWhenFirstPortAreInUse() throws Exception {
    ServerSocket serverSocket1 = new ServerSocket(5454);
    ServerSocket serverSocket2 = new ServerSocket(3474);
    testOpenRequestsDuringLoginAndInitialFileImport();
    serverSocket1.close();
    serverSocket2.close();
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
}
