package org.designup.picsou.server;

import org.designup.picsou.PicsouServer;
import org.designup.picsou.functests.checkers.LoginChecker;
import org.designup.picsou.functests.checkers.OperationChecker;
import org.designup.picsou.functests.checkers.TransactionChecker;
import org.designup.picsou.functests.checkers.ViewSelectionChecker;
import org.designup.picsou.functests.utils.OfxBuilder;
import org.designup.picsou.model.TransactionType;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.uispec4j.Table;

public abstract class ReconnectFuncTest extends ServerFuncTestCase {

  public void __test() throws Exception {
    String fileName = TestUtils.getFileName(this, ".ofx");

    Files.copyStreamTofile(ReconnectFuncTest.class.getResourceAsStream(PICSOU_DEV_TESTFILES_CIC1_OFX),
                           fileName);

    createAndLogUser("user", "_passd1", fileName);

    picsouServer.stop();
    System.setProperty(PicsouServer.DELETE_SERVER_PROPERTY, "false");
    picsouServer.start();

    OperationChecker operations = new OperationChecker(window);
    operations.importOfxFile(fileName);

    Table table = getCategoryTable();
    assertTrue(table.cellEquals(0, 2, "-50"));
  }

  public void __testRewrite() throws Exception {
    String fileName =
      OfxBuilder.init(this)
        .addTransaction("2008/07/12", -95.00, "Auchan")
        .save();

    createAndLogUser("user", "_passd1", fileName);

    ViewSelectionChecker views = new ViewSelectionChecker(window);
    views.selectData();
    TransactionChecker transactions = new TransactionChecker(window);
    transactions.initContent()
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "To categorize")
      .check();

//    picsouServer.stop();
//    System.setProperty(PicsouServer.DELETE_SERVER_PROPERTY, "false");
//    picsouServer.start();

    OperationChecker operations = new OperationChecker(window);
    OfxBuilder.init(this, operations)
      .addTransaction("2008/07/15", -10.00, "Auchan")
      .load();

    transactions.initContent()
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -10.00, "To categorize")
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "To categorize")
      .check();

    operations.exit();
    window.dispose();
    window = null;

    initWindow();
    LoginChecker loginChecker = new LoginChecker(window);
    loginChecker.logUser("user", "_passd1");

    new ViewSelectionChecker(window).selectData();
    new TransactionChecker(window).initContent()
      .add("15/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -10.00, "To categorize")
      .add("12/07/2008", TransactionType.PRELEVEMENT, "Auchan", "", -95.00, "To categorize")
      .check();
  }
}