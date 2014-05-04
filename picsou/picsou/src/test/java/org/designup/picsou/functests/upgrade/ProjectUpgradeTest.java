package org.designup.picsou.functests.upgrade;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.model.*;
import org.globsframework.utils.Files;

public class ProjectUpgradeTest extends LoggedInFunctionalTestCase {

  public void setUp() throws Exception {
    setCurrentMonth("2014/02");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(6).validate();
  }

  public void testCompleteCaseForJar131() throws Exception {
    operations.restore(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar131_projects.budgetview"));

    operations.selectAllMonthsSinceJanuary();
    transactions.initAmountContent()
      .add("13/02/2014", "AIR FRANCE", -150.00, "Voyage", 3520.00, 3520.00, "Compte Perso")
      .add("12/02/2014", "LE VIEUX CAMPEUR", -70.00, "Other", 3670.00, 3670.00, "Compte Perso")
      .add("12/02/2014", "FNAC", -100.00, "Loisirs", 3740.00, 3740.00, "Compte Perso")
      .add("11/02/2014", "VIRT EPARGNE ROME", 50.00, "Provisions Rome", 2700.00, 2700.00, "Livret")
      .add("11/02/2014", "VIRT EPARGNE ROME", -50.00, "Provisions Rome", 3840.00, 3840.00, "Compte Perso")
      .add("08/02/2014", "AUCHAN", -450.00, "Courses", 3890.00, 3890.00, "Compte Perso")
      .add("05/02/2014", "WORLDCO", 2000.00, "Salaire 1", 4340.00, 4340.00, "Compte Perso")
      .add("20/01/2014", "VIRT EPARGNE ROME", 50.00, "Provisions Rome", 2650.00, 2650.00, "Livret")
      .add("20/01/2014", "VIRT EPARGNE ROME", -50.00, "Provisions Rome", 2340.00, 2340.00, "Compte Perso")
      .add("18/01/2014", "DECATHLON", -30.00, "Prepa Rome", 2420.00, 2420.00, "Compte Perso")
      .add("15/01/2014", "AIR FRANCE", -30.00, "Voyage", 2390.00, 2390.00, "Compte Perso")
      .check();

    projects.checkCurrentProjects("| Voyage Rome | Jan | 1080.00 | on |");
    projects.select("Voyage Rome");
    currentProject.checkItems("| Voyage          | Jan | 180.00  | 200.00  |\n" +
                              "| Prepa Rome      | Feb | 30.00   | 80.00   |\n" +
                              "| Hotel           | Mar | 0.00    | 300.00  |\n" +
                              "| Sorties         | Mar | 0.00    | 500.00  |\n" +
                              "| Virement Rome   | Mar | 0.00    | +500.00 |\n" +
                              "| Provisions Rome | Jan | +100.00 | +150.00 |\n" +
                              "| Other           | Feb | 70.00   | 0.00    |");
    currentProject.view(0).checkCategorizationWarningNotShown();
    currentProject.view(1).checkCategorizationWarningShown();
    currentProject.view(2).checkCategorizationWarningNotShown();
    currentProject.checkDefaultAccountLabel("Compte Perso");

    categorization.initContent()
      .add("15/01/2014", "Voyage", "AIR FRANCE", -30.00)
      .add("13/02/2014", "Voyage", "AIR FRANCE", -150.00)
      .add("08/02/2014", "Courses", "AUCHAN", -450.00)
      .add("18/01/2014", "Prepa Rome", "DECATHLON", -30.00)
      .add("12/02/2014", "Loisirs", "FNAC", -100.00)
      .add("12/02/2014", "Other", "LE VIEUX CAMPEUR", -70.00)
      .add("20/01/2014", "Provisions Rome", "VIRT EPARGNE ROME", -50.00)
      .add("20/01/2014", "Provisions Rome", "VIRT EPARGNE ROME", 50.00)
      .add("11/02/2014", "Provisions Rome", "VIRT EPARGNE ROME", -50.00)
      .add("11/02/2014", "Provisions Rome", "VIRT EPARGNE ROME", 50.00)
      .add("05/02/2014", "Salaire 1", "WORLDCO", 2000.00)
      .check();

    budgetView.extras.expandGroup("Voyage Rome");

    timeline.selectMonth(201401);
    budgetView.extras.checkContent("| Voyage Rome | 60.00 | 50.00 |\n" +
                                   "| Voyage      | 30.00 | 50.00 |\n" +
                                   "| Prepa Rome  | 30.00 | 0.00  |\n");
    budgetView.savings.checkContent("| Provisions Rome       | 50.00 | 50.00 |\n" +
                                    "| Du compte Livret      | 0.00  | 0.00  |\n" +
                                    "| Vers le compte Livret | 0.00  | 0.00  |\n");

    timeline.selectMonth(201402);
    budgetView.extras.checkContent("| Voyage Rome | 220.00 | 230.00 |\n" +
                                   "| Voyage      | 150.00 | 150.00 |\n" +
                                   "| Prepa Rome  | 0.00   | 80.00  |\n" +
                                   "| Other       | 70.00  | 0.00   |\n");
    budgetView.savings.checkContent("| Provisions Rome       | 50.00 | 50.00 |\n" +
                                    "| Du compte Livret      | 0.00  | 0.00  |\n" +
                                    "| Vers le compte Livret | 0.00  | 0.00  |\n");

    timeline.selectMonth(201403);
    budgetView.extras.checkContent("| Voyage Rome | 0.00 | 800.00 |\n" +
                                   "| Sorties     | 0.00 | 500.00 |\n" +
                                   "| Hotel       | 0.00 | 300.00 |\n");
    budgetView.savings.checkContent("| Virement Rome         | 0.00 | +500.00 |\n" +
                                    "| Provisions Rome       | 0.00 | 50.00   |\n" +
                                    "| Du compte Livret      | 0.00 | 0.00    |\n" +
                                    "| Vers le compte Livret | 0.00 | 0.00    |\n");
  }

  public void testSeveralTransactionsAssignedToRootProjectSeries() throws Exception {
    operations.restore(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar131_projects_multi_root.budgetview"));
    projects.checkCurrentProjects("| Voyage Rome | Jan | 1080.00 | on |");
    projects.select("Voyage Rome");
    currentProject.checkItems("| Voyage     | Jan | 180.00 | 200.00 |\n" +
                              "| Prepa Rome | Feb | 0.00   | 80.00  |\n" +
                              "| Hotel      | Mar | 0.00   | 300.00 |\n" +
                              "| Sorties    | Mar | 0.00   | 500.00 |\n" +
                              "| Other      | Jan | 200.00 | 0.00   |");

    categorization.initContent()
      .add("15/01/2014", "Voyage", "AIR FRANCE", -30.00)
      .add("13/02/2014", "Voyage", "AIR FRANCE", -150.00)
      .add("08/02/2014", "Courses", "AUCHAN", -450.00)
      .add("18/01/2014", "Other", "DECATHLON", -30.00)
      .add("12/02/2014", "Other", "FNAC", -100.00)
      .add("12/02/2014", "Other", "LE VIEUX CAMPEUR", -70.00)
      .add("05/02/2014", "Salaire 1", "WORLDCO", 2000.00)
      .check();

    budgetView.extras.expandGroup("Voyage Rome");

    timeline.selectMonth(201401);
    budgetView.extras.checkContent("| Voyage Rome | 60.00 | 50.00 |\n" +
                                   "| Voyage      | 30.00 | 50.00 |\n" +
                                   "| Other       | 30.00 | 0.00  |\n");

    timeline.selectMonth(201402);
    budgetView.extras.checkContent("| Voyage Rome | 320.00 | 230.00 |\n" +
                                   "| Other       | 170.00 | 0.00   |\n" +
                                   "| Voyage      | 150.00 | 150.00 |\n" +
                                   "| Prepa Rome  | 0.00   | 80.00  |\n");

    timeline.selectMonth(201403);
    budgetView.extras.checkContent("| Voyage Rome | 0.00 | 800.00 |\n" +
                                   "| Sorties     | 0.00 | 500.00 |\n" +
                                   "| Hotel       | 0.00 | 300.00 |\n");
  }

  public void testOlderVersion() throws Exception {
    operations.restore(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar96_projects.budgetview"));
    projects.checkCurrentProjects("| Voyage Rome | Jan | 500.00 | on |");
    projects.select("Voyage Rome");
    currentProject.checkItems("| Voyage 1   | Jan | 30.00  | 150.00 |\n" +
                              "| Voyage 2   | Feb | 150.00 | 150.00 |\n" +
                              "| Hotel      | Mar | 0.00   | 200.00 |\n" +
                              "| Equipement | Feb | 100.00 | 0.00   |\n" +
                              "| Other      | Feb | 100.00 | 0.00   |\n");
    currentProject.view(2).checkCategorizationWarningNotShown();
    currentProject.view(3).checkCategorizationWarningShown();

    categorization.initContent()
      .add("15/01/2014", "Voyage 1", "AIR FRANCE", -30.00)
      .add("13/02/2014", "Voyage 2", "AIR FRANCE", -150.00)
      .add("08/02/2014", "Courses", "AUCHAN", -450.00)
      .add("18/01/2014", "Equipement", "DECATHLON", -30.00)
      .add("12/02/2014", "Other", "FNAC", -100.00)
      .add("12/02/2014", "Equipement", "LE VIEUX CAMPEUR", -70.00)
      .add("05/02/2014", "Salaire", "WORLDCO", 2000.00)
      .check();

    budgetView.extras.expandGroup("Voyage Rome");

    timeline.selectMonth(201401);
    budgetView.extras.checkContent("| Voyage Rome | 60.00 | 150.00 |\n" +
                                   "| Voyage 1    | 30.00 | 150.00 |\n" +
                                   "| Equipement  | 30.00 | 0.00   |\n");

    timeline.selectMonth(201402);
    budgetView.extras.checkContent("| Voyage Rome | 320.00 | 150.00 |\n" +
                                   "| Voyage 2    | 150.00 | 150.00 |\n" +
                                   "| Other       | 100.00 | 0.00   |\n" +
                                   "| Equipement  | 70.00  | 0.00   |\n");

    timeline.selectMonth(201403);
    budgetView.extras.checkContent("| Voyage Rome | 0.00 | 200.00 |\n" +
                                   "| Hotel       | 0.00 | 200.00 |\n");
  }

  public void testProjectsWithVariousTranferItems() throws Exception {
    operations.restoreWithPassword(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar125_projets_multi_transfers.budgetview"), "pwd");
    projects.checkCurrentProjects("| Vacances | Apr | 300.00 | on |");
    projects.select("Vacances");
    currentProject.checkItems("| Courant > Epargne | Apr | +100.00 | +50.00  |\n" +
                              "| Externe > Epargne | Apr | +300.00 | +300.00 |\n" +
                              "| Epargne > Externe | Apr | +100.00 | +100.00 |\n" +
                              "| Voyage            | Apr | 0.00    | 300.00  |\n");

    transactions.initContent()
      .add("05/04/2014", TransactionType.VIREMENT, "VERS EPARGNE", "", 50.00, "Courant > Epargne")
      .add("05/04/2014", TransactionType.MANUAL, "DE EXTERNE", "", 100.00, "Externe > Epargne")
      .add("05/04/2014", TransactionType.MANUAL, "DE COURANT", "", 50.00, "Courant > Epargne")
      .add("05/04/2014", TransactionType.MANUAL, "VERS EXTERNE", "", -100.00, "Epargne > Externe")
      .add("05/04/2014", TransactionType.MANUAL, "VERS EPARGNE", "", -50.00, "Courant > Epargne")
      .add("05/04/2014", TransactionType.PRELEVEMENT, "DE COURANT", "", -50.00, "Courant > Epargne")
      .add("01/04/2014", TransactionType.PRELEVEMENT, "EPARGNE > EXTERNE", "", -100.00, "Epargne > Externe")
      .add("01/04/2014", TransactionType.VIREMENT, "EXTERNE > EPARGNE", "", 200.00, "Externe > Epargne")
      .check();
  }

  public void testProjetItemsUsingSeveralAccountsAreSplitted() throws Exception {

    operations.restore(Files.copyResourceToTmpFile(this, "/testbackups/upgrade_jar131_projets_multi_comptes.budgetview"));

    transactions.initContent()
      .add("10/04/2014", TransactionType.PRELEVEMENT, "VIRT JOINT > LIVRET1", "", -50.00, "Provisions - Compte Joint")
      .add("09/04/2014", TransactionType.PRELEVEMENT, "SNCF", "", -80.00, "Voyage - Compte Joint")
      .add("08/04/2014", TransactionType.MANUAL, "MCDO", "", -30.00, "Other - Compte Joint")
      .add("08/04/2014", TransactionType.PRELEVEMENT, "SNCF", "", -70.00, "Voyage - Compte Joint")
      .add("07/04/2014", TransactionType.PRELEVEMENT, "SUBWAY", "", -20.00, "Other - Compte Perso")
      .add("06/04/2014", TransactionType.VIREMENT, "VIRT JOINT > PERSO", "", 200.00, "Provisions - Compte Perso")
      .add("06/04/2014", TransactionType.VIREMENT, "VIRT LIVRET1", "", 50.00, "Provisions - Compte Joint")
      .add("06/04/2014", TransactionType.PRELEVEMENT, "VIRT PERSO > LIVRET1", "", -200.00, "Provisions - Compte Perso")
      .add("05/04/2014", TransactionType.PRELEVEMENT, "AIR FRANCE", "", -300.00, "Voyage - Compte Perso")
      .check();

    projects.select("Vacances");
    currentProject.checkDefaultAccountLabel("Compte Joint");
    currentProject.checkItems("| Voyage - Compte Joint     | Apr | 150.00  | 250.00  |\n" +
                              "| Voyage - Compte Perso     | Apr | 300.00  | 250.00  |\n" +
                              "| Provisions - Compte Joint | Apr | +50.00  | +150.00 |\n" +
                              "| Provisions - Compte Perso | Apr | +200.00 | +150.00 |\n" +
                              "| Other - Compte Joint      | Apr | 30.00   | 0.00    |\n" +
                              "| Other - Compte Perso      | Apr | 20.00   | 0.00    |");
    currentProject.backToList();
    projects.checkCurrentProjects("| Vacances | Apr | 500.00 | on |");

    views.selectBudget();
    budgetView.savings
      .checkTotalAmounts(250.00, 250.00)
      .checkContent("| Provisions - Compte Perso | 200.00 | 125.00 |\n" +
                    "| Provisions - Compte Joint | 50.00  | 125.00 |\n" +
                    "| Du compte Livret 1        | 0.00   | 0.00   |\n" +
                    "| Du compte Livret 2        | 0.00   | 0.00   |\n" +
                    "| Vers le compte Livret 1   | 0.00   | 0.00   |\n" +
                    "| Vers le compte Livret 2   | 0.00   | 0.00   |\n");
  }
}
