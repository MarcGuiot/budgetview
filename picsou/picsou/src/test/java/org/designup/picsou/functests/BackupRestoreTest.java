package org.designup.picsou.functests;

import junit.framework.Assert;
import org.designup.picsou.functests.checkers.MessageFileDialogChecker;
import org.designup.picsou.functests.checkers.PasswordDialogChecker;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;

public class BackupRestoreTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/08/30");
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  public void testBackupAndRestore() throws Exception {
    OfxBuilder.init(this)
      .addTransaction("2008/08/26", 1000, "Company")
      .addTransaction("2008/08/10", -400.0, "Auchan")
      .load();

    views.selectCategorization();
    categorization.setNewIncome("Company", "Salaire");
    categorization.setNewEnvelope("Auchan", "Course");

    views.selectData();
    transactions
      .initContent()
      .add("26/08/2008", TransactionType.VIREMENT, "Company", "", 1000.00, "Salaire")
      .add("10/08/2008", TransactionType.PRELEVEMENT, "Auchan", "", -400.00, "Course")
      .check();

    String backupFile = operations.backup(this);

    views.selectCategorization();
    categorization.getTable().selectRows(0, 1);
    categorization.setUncategorized();

    operations.restore(backupFile);

    views.selectData();
    timeline.selectAll();
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
        .assertCurrentFileNameEquals("cashpilot-2008-08-30.backup")
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
          dialog.checkTitle("Password to read data");
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
          dialog.checkTitle("Password to read data");
          return dialog.getCancelTrigger();
        }
      })
      .run();
    views.selectData();
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
    categorization.setNewEnvelope("Auchan", "Course");

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
    categorization.setNewEnvelope("Auchan", "Course");

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
}
