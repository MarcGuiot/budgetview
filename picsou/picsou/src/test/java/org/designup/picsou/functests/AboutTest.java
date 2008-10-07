package org.designup.picsou.functests;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.checkers.AboutChecker;
import org.designup.picsou.gui.PicsouApplication;

public class AboutTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {
    AboutChecker about = operations.openAbout();
    about.checkVersion();
    about.checkConfigurationContains(PicsouApplication.getDataPath());
    about.close();
  }
}
