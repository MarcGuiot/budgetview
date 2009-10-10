package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class DataCheckerTest extends LoggedInFunctionalTestCase {

  public void testErrorDialog() throws Exception {
    operations.throwExceptionInApp().close();
  }
}
