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

  private void printProjectGlobs(int projectId) {
    GlobList list = new GlobList();
    list.add(repository.get(Key.create(Project.TYPE, projectId)));
    GlobList items = repository.getAll(ProjectItem.TYPE, fieldEquals(ProjectItem.PROJECT, projectId));
    list.addAll(items);
    Set<Integer> seriesIds = new HashSet<Integer>();
    for (Glob item : items) {
      Glob series = repository.findLinkTarget(item, ProjectItem.SERIES);
      seriesIds.add(series.get(Series.ID));
      list.add(series);
      if (series.get(Series.MIRROR_SERIES) != null) {
        Glob mirror = repository.findLinkTarget(series, Series.MIRROR_SERIES);
        list.add(mirror);
        seriesIds.add(mirror.get(Series.ID));
      }
    }
    list.addAll(repository.getAll(Transaction.TYPE,
                                  GlobMatchers.and(fieldIn(Transaction.SERIES, seriesIds),
                                                   isFalse(Transaction.PLANNED))));
    list.addAll(repository.getAll(SeriesStat.TYPE,
                                  GlobMatchers.and(fieldEquals(SeriesStat.TARGET_TYPE, SeriesType.SERIES.getId()),
                                                   fieldIn(SeriesStat.TARGET, seriesIds),
                                                   fieldIn(SeriesStat.MONTH, 201402, 201403))));

    GlobPrinter.print(list);
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
    fail("tbd - version pré-été 2013, par ex 2.33");
  }
}
