package org.designup.picsou.gui.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.projects.ProjectChartView;
import org.designup.picsou.gui.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.model.AccountType;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class ProjectSelector extends View {

  public ProjectSelector(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectSelector.splits",
                                                      repository, directory);

    final HistoChartRange shortRange = new ScrollableHistoChartRange(2, 7, false, repository);
    final HistoChartRange longRange = new ScrollableHistoChartRange(6, 12, false, repository);

    final ProjectChartView projects = new ProjectChartView(shortRange, repository, directory);
    projects.registerComponents(builder);

    builder.add("createProject", projects.getCreateProjectAction());

    AccountChartsPanel mainAccountsPanel = new AccountChartsPanel(AccountType.MAIN, shortRange, longRange, repository, directory, projects);
    builder.add("mainAccountsPanel", mainAccountsPanel.getPanel());

    AccountChartsPanel savingsAccountsPanel = new AccountChartsPanel(AccountType.SAVINGS, shortRange, longRange, repository, directory, projects);
    builder.add("savingsAccountsPanel", savingsAccountsPanel.getPanel());

    parentBuilder.add("projectSelector", builder);

    // parentBuilder.add("summaryProjectSplit", SplitPaneConfig.create(directory, LayoutConfig.HOME_SUMMARY_PROJECTS));
  }
}
