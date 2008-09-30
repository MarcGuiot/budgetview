package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class HelpTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {
    operations.openHelp().checkTitle("Index");
  }
}