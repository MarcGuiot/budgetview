package com.budgetview.desktop.projects;

import com.budgetview.desktop.View;
import com.budgetview.desktop.analysis.histobuilders.range.HistoChartRange;
import com.budgetview.desktop.analysis.histobuilders.range.ScrollableHistoChartRange;
import com.budgetview.desktop.components.charts.histo.utils.AutoRangeUpdater;
import com.budgetview.desktop.projects.components.ProjectAccountChartsPanel;
import com.budgetview.shared.model.AccountType;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class ProjectSelector extends View {

  public ProjectSelector(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  public void registerComponents(GlobsPanelBuilder parentBuilder) {

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/projects/projectSelector.splits",
                                                      repository, directory);

    final HistoChartRange initialRange = new ScrollableHistoChartRange(2, 7, false, repository);

    final ProjectChartView projects = new ProjectChartView(initialRange, repository, directory);
    projects.registerComponents(builder);

    builder.add("createProject", projects.getCreateProjectAction());

    ProjectAccountChartsPanel mainAccountsPanel = new ProjectAccountChartsPanel(AccountType.MAIN, initialRange, repository, directory);
    builder.add("mainAccountsPanel", mainAccountsPanel.getPanel());

    ProjectAccountChartsPanel savingsAccountsPanel = new ProjectAccountChartsPanel(AccountType.SAVINGS, initialRange, repository, directory);
    builder.add("savingsAccountsPanel", savingsAccountsPanel.getPanel());

    JPanel panel = builder.load();
    parentBuilder.add("projectSelector", panel);

    AutoRangeUpdater.install(panel, initialRange, repository, projects, mainAccountsPanel, savingsAccountsPanel);

  }

}
