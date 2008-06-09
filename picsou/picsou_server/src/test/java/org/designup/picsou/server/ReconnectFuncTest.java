package org.designup.picsou.server;

import org.uispec4j.Table;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;
import org.crossbowlabs.globs.utils.TestUtils;
import org.crossbowlabs.globs.utils.Files;
import org.designup.picsou.PicsouServer;
import org.designup.picsou.functests.checkers.OperationChecker;

public class ReconnectFuncTest extends ServerFuncTestCase {

  public void test() throws Exception {
    String fileName = TestUtils.getFileName(this, ".ofx");

    Files.copyStreamTofile(ReconnectFuncTest.class.getResourceAsStream(PICSOU_DEV_TESTFILES_CIC1_OFX),
                           fileName);

    createAndLogUser("user", "_passd1", fileName);

    picsouServer.stop();
    System.setProperty(PicsouServer.DELETE_SERVER_PROPERTY, "false");
    picsouServer.start();

    OperationChecker operations = new OperationChecker(window);
    operations.importOfxFile(fileName);

    Table table = window.getTable("category");
    assertTrue(table.cellEquals(0, 2, "-50"));
  }
}