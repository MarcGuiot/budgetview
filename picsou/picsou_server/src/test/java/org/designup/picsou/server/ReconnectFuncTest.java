package org.designup.picsou.server;

import org.uispec4j.Table;
import org.uispec4j.interception.FileChooserHandler;
import org.uispec4j.interception.WindowInterceptor;
import org.crossbowlabs.globs.utils.TestUtils;
import org.crossbowlabs.globs.utils.Files;
import org.designup.picsou.PicsouServer;
import org.designup.picsou.functests.checkers.OperationChecker;

import java.io.File;

public class ReconnectFuncTest extends ServerFuncTestCase {

  public void test() throws Exception {
    String fileName = TestUtils.getFileName(this, ".qif");

    Files.copyStreamTofile(ReconnectFuncTest.class.getResourceAsStream(PICSOU_DEV_TESTFILES_SG1_QIF),
                           fileName);

    createAndLogUser("user", "_passd1", fileName);

    picsouServer.stop();
    System.setProperty(PicsouServer.DELETE_SERVER_PROPERTY, "false");
    picsouServer.start();

    OperationChecker operations = new OperationChecker(window);
    WindowInterceptor
      .init(operations.getImportTrigger())
      .process(FileChooserHandler.init().select(new String[]{fileName}))
      .processWithButtonClick("Solde inconnu")
      .run();

    Table table = window.getTable("category");
    assertTrue(table.cellEquals(0, 2, "-100"));
  }
}