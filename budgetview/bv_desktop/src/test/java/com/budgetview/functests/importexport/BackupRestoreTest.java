package com.budgetview.functests.importexport;

import com.budgetview.functests.checkers.LoginChecker;
import com.budgetview.functests.checkers.MessageFileDialogChecker;
import com.budgetview.functests.checkers.PasswordDialogChecker;
import com.budgetview.functests.checkers.utils.ConfirmationHandler;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.model.TransactionType;
import com.budgetview.server.model.SerializableGlobType;
import com.budgetview.server.persistence.direct.ReadOnlyAccountDataManager;
import junit.framework.Assert;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.collections.MapOfMaps;
import org.junit.Test;
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

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }

  @Test
  public void testBackupAndRestore() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();

    categorization.setNewIncome("Company", "Salaire");
    categorization.setNewVariable("Auchan", "Course");

    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course")
      .check();

    notes.setText("Some notes...");

    String backupFile = operations.backup(this);

    categorization.getTable().selectRows(0, 1);
    categorization.setUncategorized();

    operations.restore(backupFile);

    timeline.checkSelection("2008/08");

    operations.checkUndoNotAvailable();
    operations.checkRedoNotAvailable();

    notes.checkText("Some notes...");

    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course")
      .check();
    uncategorized.checkNotShown();
  }

  @Test
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
                 .assertCurrentFileNameEquals("backup-2008-08-30.budgetview")
                 .select(filePath))
      .process(ConfirmationHandler.cancel("Confirmation", "Do you want to overwrite this file?"))
      .run();

    Assert.assertTrue(Files.loadFileToString(filePath).contains("blah"));

    WindowInterceptor
      .init(operations.getBackupTrigger())
      .process(FileChooserHandler.init().select(filePath))
      .process(ConfirmationHandler.validate("Confirmation", "Do you want to overwrite this file?"))
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

  @Test
  public void testBackupAndRestoreWithOtherPassword() throws Exception {

    changeUser("user", "password");
    operations.hideSignposts();

    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();
    String fileName = operations.backup(this);

    changeUser("user1", "other passwd");
    operations.hideSignposts();

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

  @Test
  public void testBackupAndCancelRestoreWithOtherPassword() throws Exception {
    changeUser("user", "password");
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

  @Test
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

  @Test
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

    uncategorized.checkNotShown();
  }

  @Test
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

  @Test
  public void testSeriesEvolutionAfterRestore() throws Exception {
    changeUser("user", "password");
    operations.openPreferences().setFutureMonthsCount(2).validate();
    operations.hideSignposts();
    addOns.activateAnalysis();

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
    LoginChecker.init(mainWindow).logNewUser("aaaa", "aaaa");
    setCurrentDate("2009/02/03");
    restartApplication("aaaa", "aaaa");
    changeUser("testSeriesEvolutionAfterRestore", "testSeriesEvolutionAfterRestore");
    operations.hideSignposts();
    operations.restoreWithPassword(path, "password");
    views.selectAnalysis();
    analysis.table().checkRow("Salaire", "1000.00", "1000.00", "1000.00", "1000.00", "1000.00", "1000.00", "1000.00", "1000.00", "1000.00", "1000.00");
    operations.checkDataIsOk();
  }
}
