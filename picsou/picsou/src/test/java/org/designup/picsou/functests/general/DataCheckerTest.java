package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.PicsouApplication;

public class DataCheckerTest extends LoggedInFunctionalTestCase {

  public void testErrorDialog() throws Exception {
    operations.throwExceptionInApplication()
      .checkMessageContains("No error was found")
      .checkDetailsContain("Exception test") // TODO: mettre le message de l'exception
      .close();
  }

  public void testExit() throws Exception {

    assertTrue(PicsouApplication.EXIT_ON_DATA_ERROR);
    PicsouApplication.EXIT_ON_DATA_ERROR = false;

    operations.throwExceptionInRepository()
      .checkMessageContains("Something just went wrong with BudgetView")
      .checkDetailsContain("Object user[id=0] already exists")
      .checkCopy()
      .close();

    Thread.sleep(50);
    PicsouApplication.EXIT_ON_DATA_ERROR = true;
  }
}
