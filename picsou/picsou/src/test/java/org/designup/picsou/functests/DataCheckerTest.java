package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.PicsouApplication;

public class DataCheckerTest extends LoggedInFunctionalTestCase {

  public void testErrorDialog() throws Exception {
    operations.throwExceptionInApplication()
      .checkMessageContains("The following errors have been found")
      .checkDetailsContain("Start checking")
      .close();
  }

  public void testExit() throws Exception {

    assertTrue(PicsouApplication.EXIT_ON_DATA_ERROR);
    PicsouApplication.EXIT_ON_DATA_ERROR = false;

    operations.throwExceptionInRepository()
      .checkMessageContains("Something just went wrong with CashPilot")
      .checkDetailsContain("Object user[id=0] already exists")
      .checkCopy()
      .close();

    PicsouApplication.EXIT_ON_DATA_ERROR = true;
  }
}
