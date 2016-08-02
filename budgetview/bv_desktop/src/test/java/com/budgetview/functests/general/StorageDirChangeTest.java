package com.budgetview.functests.general;

import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.utils.OfxBuilder;
import com.budgetview.desktop.startup.AppPaths;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.junit.Test;

import java.io.File;

public class StorageDirChangeTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    resetWindow();
    setCurrentDate("2013/04/07");
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  @Test
  public void testChangeDirectory() throws Exception {

    String defaultStoragePath = AppPaths.getCurrentStoragePath();

    OfxBuilder.init(this)
      .addTransaction("2013/01/01", -100.00, "Auchan")
      .load();

    String tmpDir = TestUtils.getDirName(this);
    File newDataPath = new File(tmpDir, "bvData");
    Files.deleteWithSubtree(newDataPath);

    operations.openPreferences()
      .setDataPath(newDataPath.getAbsolutePath())
      .validateRestart(this);

    assertEquals(newDataPath.getAbsolutePath() + File.separator + "data", AppPaths.getCurrentDataPath());

    OfxBuilder.init(this)
      .addTransaction("2013/01/03", -100.00, "FNAC")
      .load();
    transactions.initAmountContent()
      .add("03/01/2013", "FNAC", -100.00, "To categorize", -100.00, -100.00, "Account n. 00001123")
      .add("01/01/2013", "AUCHAN", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

    restartApplication();

    transactions.initAmountContent()
      .add("03/01/2013", "FNAC", -100.00, "To categorize", -100.00, -100.00, "Account n. 00001123")
      .add("01/01/2013", "AUCHAN", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

    getOperations().openPreferences()
      .revertToDefaultDataPath(defaultStoragePath)
      .validateUseTargetAndRestart(this);

    transactions.initAmountContent()
      .add("01/01/2013", "AUCHAN", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

    getOperations().openPreferences()
      .setDataPath(newDataPath.getAbsolutePath())
      .validateUseTargetAndRestart(this);

    transactions.initAmountContent()
      .add("03/01/2013", "FNAC", -100.00, "To categorize", -100.00, -100.00, "Account n. 00001123")
      .add("01/01/2013", "AUCHAN", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

    getOperations().openPreferences()
      .revertToDefaultDataPath(defaultStoragePath)
      .validateOverwriteTargetAndRestart(this);

    transactions.initAmountContent()
      .add("03/01/2013", "FNAC", -100.00, "To categorize", -100.00, -100.00, "Account n. 00001123")
      .add("01/01/2013", "AUCHAN", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();
  }
}
