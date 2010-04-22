package org.designup.picsou.functests;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.MessageFileDialogChecker;
import org.designup.picsou.functests.checkers.PasswordDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.persistence.direct.ReadOnlyAccountDataManager;
import org.globsframework.utils.Files;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

import java.io.File;

public class BackupRestoreTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/08/30");
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  public void testRestoreIsNotPossibleAndBackupWarmThatRestoreIsNotPossibleDuringTheTrialPeriod() throws Exception {
    setDeleteLocalPrevayler(true);
    setNotRegistered();
    restartApplication(true);
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();
    operations.backup(this, true);

    operations.restoreNotAvailable();
  }

  public void testBackupAndRestore() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("Company", "Salaire");
    categorization.setNewVariable("Auchan", "Course");

    views.selectData();
    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course")
      .check();

    views.selectHome();
    notes.setText("Some notes...");

    String backupFile = operations.backup(this);

    views.selectCategorization();
    categorization.getTable().selectRows(0, 1);
    categorization.setUncategorized();

    operations.restore(backupFile);

    timeline.checkSelection("2008/08");

    operations.checkUndoNotAvailable();
    operations.checkRedoNotAvailable();

    views.selectHome();
    notes.checkText("Some notes...");

    views.selectData();
    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course")
      .check();
  }

  public void testConfirmation() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();

    final String filePath = TestUtils.getFileName(this);

    Files.dumpStringToFile(filePath, "blah");

    WindowInterceptor
      .init(operations.getBackupTrigger())
      .process(FileChooserHandler.init()
        .assertCurrentFileNameEquals("backup-2008-08-30.cashpilot")
        .select(filePath))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          assertThat(window.containsLabel("Do you want to overwrite this file?"));
          return window.getButton("No").triggerClick();
        }
      })
      .run();

    Assert.assertTrue(Files.loadFileToString(filePath).contains("blah"));

    WindowInterceptor
      .init(operations.getBackupTrigger())
      .process(FileChooserHandler.init().select(filePath))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          assertThat(window.containsLabel("Do you want to overwrite this file?"));
          return window.getButton("Yes").triggerClick();
        }
      })
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageFileDialogChecker dialog = new MessageFileDialogChecker(window);
          dialog.checkMessageContains("Backup done in file");
          dialog.checkFilePath(filePath);
          return dialog.getOkTrigger();
        }
      })
      .run();

    Assert.assertFalse(Files.loadFileToString(filePath).contains("blah"));
  }

  public void testBackupAndRestoreWithOtherPassword() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();
    String fileName = operations.backup(this);

    changeUser("user1", "other passwd");

    WindowInterceptor.init(operations.getRestoreTrigger())
      .process(FileChooserHandler.init().select(fileName))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          PasswordDialogChecker dialog = new PasswordDialogChecker(window);
          dialog.checkTitle("Secure backup");
          dialog.setPassword("password");
          return dialog.getOkTrigger();
        }
      })
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageFileDialogChecker dialog = new MessageFileDialogChecker(window);
          dialog.checkMessageContains("Restore done");
          return dialog.getOkTrigger();
        }
      })
      .run();
    views.selectData();
    timeline.selectAll();
    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00)
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -400.00)
      .check();
  }

  public void testBackupAndCancelRestoreWithOtherPassword() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();
    String fileName = operations.backup(this);

    changeUser("user1", "other passwd");
    WindowInterceptor.init(operations.getRestoreTrigger())
      .process(FileChooserHandler.init().select(fileName))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          PasswordDialogChecker dialog = new PasswordDialogChecker(window);
          dialog.checkTitle("Secure backup");
          return dialog.getCancelTrigger();
        }
      })
      .run();
    views.selectData();
    timeline.checkSelection("2008/08");
    timeline.selectAll();
    transactions
      .initContent()
      .check();
  }


  public void testRestoreWithInvalidFileDoesNotCorruptCurrentData() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("Company", "Salaire");
    categorization.setNewVariable("Auchan", "Course");

    final String filePath = TestUtils.getFileName(this);

    Files.dumpStringToFile(filePath, "blah");

    WindowInterceptor
      .init(operations.getRestoreTrigger())
      .process(FileChooserHandler.init().select(filePath))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageFileDialogChecker dialog = new MessageFileDialogChecker(window);
          dialog.checkMessageContains("Failed to restore data");
          return dialog.getOkTrigger();
        }
      })
      .run();

    views.selectData();
    timeline.checkSelection("2008/08");
    timeline.selectAll();
    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course")
      .check();
  }

  public void testSaveAndRestoreVeryDifferentSnapshot() throws Exception {
    operations.openPreferences().setFutureMonthsCount(1).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("Company", "Salaire");
    categorization.setNewVariable("Auchan", "Course");

    views.selectData();
    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course")
      .check();

    String firstBackup = operations.backup(this);

    restartApplication();

    operations.openPreferences().setFutureMonthsCount(2).validate();

    views.selectCategorization();
    categorization.selectTransaction("Company")
      .editSeries("Salaire")
      .deleteCurrentSeriesWithConfirmation();
    categorization.selectTransaction("Auchan")
      .editSeries("Course")
      .deleteCurrentSeriesWithConfirmation();
    categorization.delete("Auchan").validate();
    categorization.setNewIncome("Company", "Other Salaire");

    String secondBackup = operations.backup(this);

    operations.restore(firstBackup);
    restartApplication();

    operations.restore(secondBackup);

    views.selectData();
    timeline.selectMonth("2008/08");
    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00, "Other Salaire")
      .check();

    operations.restore(firstBackup);

    restartApplication();

    views.selectData();
    timeline.selectMonth("2008/08");
    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "AUCHAN", "", -400.00, "Course")
      .check();

    operations.restore(secondBackup);

    views.selectData();
    timeline.selectMonth("2008/08");
    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "COMPANY", "", 1000.00, "Other Salaire")
      .check();
  }

  public void testRestoreNewerVersion() throws Exception {
    final String filePath = TestUtils.getFileName(this);

    ReadOnlyAccountDataManager.writeSnapshot(new MapOfMaps<String, Integer, SerializableGlobType>(), new File(filePath), null, 99999, -1);

    WindowInterceptor
      .init(operations.getRestoreTrigger())
      .process(FileChooserHandler.init().select(filePath))
      .process(new WindowHandler() {
        public Trigger process(Window window) throws Exception {
          MessageFileDialogChecker dialog = new MessageFileDialogChecker(window);
          dialog.checkMessageContains("The backup version is newer than the application version");
          return dialog.getOkTrigger();
        }
      })
      .run();
  }

  public void testSeriesEvolutionAfterRestore() throws Exception {
    operations.openPreferences().setFutureMonthsCount(2).validate();
    OfxBuilder.init(this)
      .addTransaction("2008/05/01", 1000.00, "Salaire")
      .addTransaction("2008/05/01", -1000.00, "Loyer")
      .addTransaction("2008/06/02", 1000.00, "Salaire")
      .addTransaction("2008/06/02", -1000.00, "Loyer")
      .addTransaction("2008/07/01", 1000.00, "Salaire")
      .addTransaction("2008/07/01", -1000.00, "Loyer")
      .addTransaction("2008/08/06", 1000.00, "Salaire")
      .addTransaction("2008/08/06", -1000.00, "Loyer")
      .load();
    views.selectCategorization();
    categorization.setNewIncome("Salaire", "Salaire");
    categorization.setNewVariable("Loyer", "Loyer");
    String path = operations.backup(this);
    
    operations.deleteUser(password);
    mainWindow.dispose();
    setCurrentDate("2009/02/03");
    resetWindow();

    changeUser("testSeriesEvolutionAfterRestore", "testSeriesEvolutionAfterRestore");
    operations.restoreWithNewPassword(path, "password");

    views.selectEvolution();
    seriesEvolution.checkRow("Salaire", "1000.00", "1000.00", "1000.00", "1000.00", "1000.00", "1000.00", "1000.00", "1000.00");
    operations.checkOk();
  }

}
