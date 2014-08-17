package org.designup.picsou.functests.license;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;

public class PremiumEvolutionTest extends LoggedInFunctionalTestCase {

  protected void setUp() throws Exception {
    resetWindow();
    setNotRegistered();
    setInMemory(false);
    setDeleteLocalPrevayler(true);
    super.setUp();
    setDeleteLocalPrevayler(false);
  }

  protected void tearDown() throws Exception {
    resetWindow();
    super.tearDown();
  }

  public void test() throws Exception {
    operations.hideSignposts();
    license.checkPromotionShown();
    license.activateTrial();
    license.checkTrialShown();
    license.register();
    license.checkRegisteredShown();
  }
}
