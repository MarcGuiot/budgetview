package org.designup.picsou.functests.projects;

import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.gui.model.SeriesStat;
import org.designup.picsou.gui.model.SeriesType;
import org.designup.picsou.model.Project;
import org.designup.picsou.model.ProjectItem;
import org.designup.picsou.model.Series;
import org.designup.picsou.model.Transaction;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobPrinter;
import org.globsframework.model.utils.GlobMatchers;

import java.util.HashSet;
import java.util.Set;

import static org.globsframework.model.utils.GlobMatchers.*;

public class ProjectUpgradeTest extends LoggedInFunctionalTestCase {

  public void setUp() throws Exception {
    setCurrentMonth("2014/02");
    super.setUp();
    operations.openPreferences().setFutureMonthsCount(6).validate();
  }

  public void testCompleteCaseForJar131() throws Exception {

    operations.restore("picsou/picsou/src/test/resources/testbackups/upgrade_jar131_projects.budgetview");

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
    operations.restore("picsou/picsou/src/test/resources/testbackups/upgrade_jar131_projects_multi_root.budgetview");
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
    operations.restore("picsou/picsou/src/test/resources/testbackups/upgrade_jar96_projects.budgetview");
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
}
