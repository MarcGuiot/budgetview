package org.designup.picsou.functests.upgrade;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.globsframework.utils.Files;

public class DeferredAccountUpgradeTest extends LoggedInFunctionalTestCase {

  public void setUp() throws Exception {
    setCurrentMonth("2014/02");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(6).validate();
  }

 // data from testFirstOfxImport
  public void testUpgrade() throws Exception {
    operations.restoreWithPassword(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar141_deferred_account.budgetview"),
                                   "testDeferred");
    timeline.selectAll();
    transactions.initAmountContent()
      .add("30/11/2009", "AUCHAN", -60.00, "Courses", 830.00, 830.00, "Carte 1111")
      .add("29/11/2009", "AUCHAN", -40.00, "Courses", 890.00, 890.00, "Carte 1111")
      .add("28/11/2009", "PRELEVEMENT NOVEMBRE", -30.00, "Carte 1111", 1000.00, 1000.00, "Compte 1234")
      .add("25/11/2009", "AUCHAN", -10.00, "Courses", 1030.00, 1030.00, "Carte 1111")
      .add("29/10/2009", "AUCHAN", -20.00, "Courses", 1030.00, 1030.00, "Carte 1111")
      .add("28/10/2009", "PRELEVEMENT OCTOBRE", 0.00, "Carte 1111", 1030.00, 1030.00, "Compte 1234")
      .add("26/09/2009", "PRELEVEMENT AOUT", -50.00, "Carte 1111", 1030.00, 1030.00, "Compte 1234")
      .add("14/09/2009", "AUCHAN", -35.00, "Courses", 1080.00, 1080.00, "Carte 1111")
      .check();

    categorization.initContent()
      .add("14/09/2009", "Courses", "AUCHAN", -35.00)
      .add("29/10/2009", "Courses", "AUCHAN", -20.00)
      .add("25/11/2009", "Courses", "AUCHAN", -10.00)
      .add("29/11/2009", "Courses", "AUCHAN", -40.00)
      .add("30/11/2009", "Courses", "AUCHAN", -60.00)
      .add("26/09/2009", "Carte 1111", "PRELEVEMENT AOUT", -50.00)
      .add("28/11/2009", "Carte 1111", "PRELEVEMENT NOVEMBRE", -30.00)
      .add("28/10/2009", "Carte 1111", "PRELEVEMENT OCTOBRE", 0.00)
      .check();

    timeline.selectMonths(200911);
    budgetView.variable.checkContent("| Courses          | 30.00 | 100.00    |\n" +
                                     "| Divers           | 0.00  | To define |\n" +
                                     "| Essence          | 0.00  | To define |\n" +
                                     "| Frais bancaires  | 0.00  | To define |\n" +
                                     "| Habillement      | 0.00  | To define |\n" +
                                     "| Loisirs          | 0.00  | To define |\n" +
                                     "| Retraits liquide | 0.00  | To define |\n" +
                                     "| Santé            | 0.00  | To define |\n" +
                                     "| Soins/Beauté     | 0.00  | To define |");

    timeline.selectMonths(200911);
    budgetView.variable.checkContent("| Courses          | 30.00 | 100.00    |\n" +
                                     "| Divers           | 0.00  | To define |\n" +
                                     "| Essence          | 0.00  | To define |\n" +
                                     "| Frais bancaires  | 0.00  | To define |\n" +
                                     "| Habillement      | 0.00  | To define |\n" +
                                     "| Loisirs          | 0.00  | To define |\n" +
                                     "| Retraits liquide | 0.00  | To define |\n" +
                                     "| Santé            | 0.00  | To define |\n" +
                                     "| Soins/Beauté     | 0.00  | To define |");

    timeline.selectMonths(200912);
    budgetView.variable.checkContent("| Courses          | 100.00 | 100.00    |\n" +
                                     "| Divers           | 0.00   | To define |\n" +
                                     "| Essence          | 0.00   | To define |\n" +
                                     "| Frais bancaires  | 0.00   | To define |\n" +
                                     "| Habillement      | 0.00   | To define |\n" +
                                     "| Loisirs          | 0.00   | To define |\n" +
                                     "| Retraits liquide | 0.00   | To define |\n" +
                                     "| Santé            | 0.00   | To define |\n" +
                                     "| Soins/Beauté     | 0.00   | To define |");
  }
}
