package org.designup.picsou.gui.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.projects.ProjectChartView;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.model.AccountType;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

public class SummaryView extends View {

  public SummaryView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/summary/summaryView.splits",
                                                      repository, directory);

    final HistoChartRange shortRange = new ScrollableHistoChartRange(2, 7, false, repository);
    final HistoChartRange longRange = new ScrollableHistoChartRange(6, 12, false, repository);

    final ProjectChartView projects = new ProjectChartView(shortRange, repository, directory);
    projects.registerComponents(builder);

    AccountChartsPanel mainAccountsPanel = new AccountChartsPanel(AccountType.MAIN, shortRange, longRange, repository, directory, projects);
    builder.add("mainAccountsPanel", mainAccountsPanel.getPanel());

    AccountChartsPanel savingsAccountsPanel = new AccountChartsPanel(AccountType.SAVINGS, shortRange, longRange, repository, directory, projects);
    builder.add("savingsAccountsPanel", savingsAccountsPanel.getPanel());

    parentBuilder.add("summaryView", builder);

    // parentBuilder.add("summaryProjectSplit", SplitPaneConfig.create(directory, LayoutConfig.HOME_SUMMARY_PROJECTS));
  }
}
