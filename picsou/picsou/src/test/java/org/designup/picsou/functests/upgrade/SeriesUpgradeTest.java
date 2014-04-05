package org.designup.picsou.functests.upgrade;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.globsframework.utils.Files;

public class SeriesUpgradeTest extends LoggedInFunctionalTestCase {

  public void testName() throws Exception {
    operations.restore(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar131_series_with_several_accounts.budgetview"));
    budgetView.recurring
      .checkContent("| Energies | 25.00 | 25.00 |\n")
      .expandGroup("Energies")
      .checkContent("| Energies       | 25.00 | 25.00 |\n" +
                    "| Account n. 456 | 20.00 | 20.00 |\n" +
                    "| Account n. 123 | 5.00  | 5.00  |\n");
  }
}
