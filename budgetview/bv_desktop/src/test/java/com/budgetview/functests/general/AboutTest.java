package com.budgetview.functests.general;

import com.budgetview.functests.checkers.AboutChecker;
import com.budgetview.functests.utils.LoggedInFunctionalTestCase;
import com.budgetview.gui.startup.AppPaths;
import org.junit.Test;

public class AboutTest extends LoggedInFunctionalTestCase {
  @Test
  public void test() throws Exception {
    AboutChecker about = operations.openAbout();
    about.checkVersion();
    about.checkConfigurationContains(AppPaths.getDefaultDataPath());
    about.checkLicensesContain("Apache");
    about.close();
  }
}
