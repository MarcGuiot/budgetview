package com.budgetview.functests.general;

import com.budgetview.gui.startup.AppPaths;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.functests.checkers.AboutChecker;

public class AboutTest extends LoggedInFunctionalTestCase {
  public void test() throws Exception {
    AboutChecker about = operations.openAbout();
    about.checkVersion();
    about.checkConfigurationContains(AppPaths.getDefaultDataPath());
    about.checkLicensesContain("Apache");
    about.close();
  }
}
