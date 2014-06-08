package org.designup.picsou.gui.summary;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.components.layoutconfig.SplitPaneConfig;
import org.designup.picsou.gui.projects.ProjectChartView;
import org.designup.picsou.gui.series.analysis.histobuilders.range.HistoChartRange;
import org.designup.picsou.gui.series.analysis.histobuilders.range.ScrollableHistoChartRange;
import org.designup.picsou.model.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.actions.ToggleBooleanAction;
import org.globsframework.gui.splits.SplitsLoader;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.utils.BooleanFieldListener;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

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

    final JButton showProjectDetails =
      new JButton(new ToggleBooleanAction(UserPreferences.KEY,
                                       UserPreferences.SHOW_PROJECT_DETAILS,
                                       Lang.get("summaryView.hideProjectDetails"),
                                       Lang.get("summaryView.showProjectDetails"),
                                       repository));
    builder.add("toggleProjectDetails", showProjectDetails);

    final SplitsNode<JLabel> projectArrow = builder.add("projectArrow", new JLabel());

    AccountChartsPanel mainAccountsPanel = new AccountChartsPanel(AccountType.MAIN, shortRange, longRange, repository, directory, projects);
    builder.add("mainAccountsPanel", mainAccountsPanel.getPanel());

    AccountChartsPanel savingsAccountsPanel = new AccountChartsPanel(AccountType.SAVINGS, shortRange, longRange, repository, directory, projects);
    builder.add("savingsAccountsPanel", savingsAccountsPanel.getPanel());

    repository.addChangeListener(new TypeChangeSetListener(UserPreferences.TYPE, Project.TYPE) {
      public void update(GlobRepository repository) {
        updateShowProjectDetails(repository, showProjectDetails);
      }
    });

    parentBuilder.add("summaryView", builder);

    parentBuilder.add("summaryProjectSplit", SplitPaneConfig.create(directory, LayoutConfig.HOME_SUMMARY_PROJECTS));

    parentBuilder.addLoader(new SplitsLoader() {
      public void load(Component component, SplitsNode node) {
        BooleanFieldListener.installNodeStyle(UserPreferences.KEY,
                                              UserPreferences.SHOW_PROJECT_DETAILS,
                                              projectArrow, "arrowShown", "arrowHidden",
                                              repository);
        updateShowProjectDetails(repository, showProjectDetails);
      }
    });
  }

  private void updateShowProjectDetails(GlobRepository repository, final JButton showProjectDetails) {
    showProjectDetails.setEnabled(repository.contains(Project.TYPE));
  }
}
