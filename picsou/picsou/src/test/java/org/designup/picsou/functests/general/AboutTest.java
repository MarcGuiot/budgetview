package org.designup.picsou.functests.general;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.functests.checkers.AboutChecker;
import org.designup.picsou.gui.startup.AppPaths;

public class AboutTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {
    AboutChecker about = operations.openAbout();
    about.checkVersion();
    about.checkConfigurationContains(AppPaths.getDataPath());
    about.checkLicensesContain("Apache");
    about.close();
  }
}
