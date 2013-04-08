package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.gui.startup.AppPaths;
import org.globsframework.utils.Files;

import java.io.File;

public class ChangeDirectoryTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    resetWindow();
    setCurrentDate("2013/04/07");
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  public void testChangeDirectory() throws Exception {

    OfxBuilder.init(this)
      .addTransaction("2013/01/01", -100.00, "Auchan")
      .load();

    String tmpDir = System.getProperty("java.io.tmpdir");
    File file = new File(tmpDir, "bvData");
    Files.deleteSubtree(file);
    operations.openPreferences()
      .setDataPath(file.getAbsolutePath())
      .validateRestart(this);

    assertEquals(file.getAbsolutePath() + File.separator + "data", AppPaths.getDataPath());

    OfxBuilder.init(this)
      .addTransaction("2013/01/03", -100.00, "Auchan")
      .load();

    transactions.initAmountContent()
      .add("03/01/2013", "AUCHAN", -100.00, "To categorize", -100.00, -100.00, "Account n. 00001123")
      .add("01/01/2013", "AUCHAN", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

    restartApplication();

    transactions.initAmountContent()
      .add("03/01/2013", "AUCHAN", -100.00, "To categorize", -100.00, -100.00, "Account n. 00001123")
      .add("01/01/2013", "AUCHAN", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

    getOperations().openPreferences()
      .clearDataPath()
      .validateRestart(this);

    transactions.initAmountContent()
      .add("01/01/2013", "AUCHAN", -100.00, "To categorize", 0.00, 0.00, "Account n. 00001123")
      .check();

  }
}
