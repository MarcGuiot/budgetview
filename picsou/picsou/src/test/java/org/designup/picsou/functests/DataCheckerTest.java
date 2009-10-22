package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class DataCheckerTest extends LoggedInFunctionalTestCase {

  public void testErrorDialog() throws Exception {
    operations.throwExceptionInApp().close();
  }

// disable par ce que le code aplicatif fait un exit...
  public void __testExit() throws Exception {
    operations.throwExceptionInRepo().checkMessageContains("Could you send the following traces").close();
  }
}
