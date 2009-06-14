package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.functests.checkers.MessageFileDialogChecker;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import junit.framework.Assert;

public class BackupRestoreTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    setCurrentDate("2008/08/30");
    setInMemory(false);
    setDeleteLocalPrevayler(false);
    super.setUp();
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
    fail("Marc: Demander le mot de passe pour lire un backup si on ne reussit pas a decrypter le snaphot");
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
}
